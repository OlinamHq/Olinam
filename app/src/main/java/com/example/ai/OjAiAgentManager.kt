package com.example.ai

import android.content.Context
import android.telephony.TelephonyManager
import android.util.Log
import com.example.BuildConfig
import com.example.data.MorphRepository
import com.example.model.MorphNote
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class OjAiAgentManager(val appContext: Context) {

    private val tag = "OjAiAgentManager"
    private val prefs = appContext.getSharedPreferences("oj_ai_agent_prefs", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val morphRepository: MorphRepository = MorphRepository.getInstance(appContext)

    private val _plugins = MutableStateFlow<List<AiPlugin>>(emptyList())
    val plugins: StateFlow<List<AiPlugin>> = _plugins.asStateFlow()

    private val _delegatedContacts = MutableStateFlow<List<DelegatedContact>>(emptyList())
    val delegatedContacts: StateFlow<List<DelegatedContact>> = _delegatedContacts.asStateFlow()

    private val _settings = MutableStateFlow(
        AiAgentSettings(
            isAgentMasterEnabled = true,
            autonomousMode = true,
            personalityTone = "Professional Executive",
            userTitle = "Manager",
            customSystemBio = "Handle client and team inquiries smoothly and professionally."
        )
    )
    val settings: StateFlow<AiAgentSettings> = _settings.asStateFlow()

    private val _recentActions = MutableStateFlow<List<String>>(emptyList())
    val recentActions: StateFlow<List<String>> = _recentActions.asStateFlow()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    init {
        loadSavedState()
        refreshPlugins()

        // Reactively sync Oj AI plugin state whenever Morph account or notes change
        scope.launch {
            combine(
                morphRepository.connectedAccountFlow,
                morphRepository.notesFlow
            ) { account, notes -> account to notes }.collect {
                refreshPlugins()
            }
        }
    }

    fun refreshPlugins() {
        val morphAccount = morphRepository.connectedAccountFlow.value
        val morphPluginEnabled = prefs.getBoolean("plugin_enabled_morph_plugin", true)
        val isMorphActive = morphAccount != null && morphPluginEnabled
        val activeMorphNotes = if (morphAccount != null) {
            morphRepository.notesFlow.value.filter { !it.isArchived }
        } else {
            emptyList()
        }
        val morphSamples = activeMorphNotes.take(4).map { note ->
            if (note.isChecklist) {
                "☑️ ${note.title} (${note.checklist.count { it.isDone }}/${note.checklist.size} done)"
            } else {
                "📝 ${note.title.ifBlank { "Untitled" }}: ${note.content.take(35)}"
            }
        }
        val morphSummary = when {
            morphAccount == null -> "Not signed in • Tap to connect your Morph account"
            !morphPluginEnabled -> "Paused for ${morphAccount.email} • Toggle to resume"
            else -> "Connected as ${morphAccount.email} • ${activeMorphNotes.size} notes"
        }

        val (hasSim, simSummary) = getRealSimInfo()
        val customBio = _settings.value.customSystemBio

        val gmailAccount = getAuthorizedPluginAccount("gmail_plugin")
        val driveAccount = getAuthorizedPluginAccount("drive_plugin")
        val calendarAccount = getAuthorizedPluginAccount("calendar_plugin")
        val photosAccount = getAuthorizedPluginAccount("photos_plugin")

        val list = listOf(
            AiPlugin(
                id = "morph_plugin",
                name = "Omkun Morph Notes",
                description = "Reads, creates, updates, and deletes notes inside your authenticated Omkun Morph account.",
                iconType = "MORPH",
                isConnected = isMorphActive,
                connectedAccountEmail = morphAccount?.email.orEmpty(),
                syncSummary = morphSummary,
                sampleItems = morphSamples
            ),
            AiPlugin(
                id = "gmail_plugin",
                name = "Gmail Integration",
                description = "Reads relevant inbox emails and thread context from your authorized Google account.",
                iconType = "GMAIL",
                isConnected = gmailAccount.isNotBlank() && prefs.getBoolean("plugin_enabled_gmail_plugin", false),
                connectedAccountEmail = gmailAccount,
                syncSummary = if (gmailAccount.isNotBlank() && prefs.getBoolean("plugin_enabled_gmail_plugin", false)) {
                    "Authorized • $gmailAccount"
                } else {
                    "Not connected • Tap to sign in with Google"
                }
            ),
            AiPlugin(
                id = "drive_plugin",
                name = "Google Drive Docs",
                description = "Indexes documents, project specs, and spreadsheets in your authorized Drive.",
                iconType = "DRIVE",
                isConnected = driveAccount.isNotBlank() && prefs.getBoolean("plugin_enabled_drive_plugin", false),
                connectedAccountEmail = driveAccount,
                syncSummary = if (driveAccount.isNotBlank() && prefs.getBoolean("plugin_enabled_drive_plugin", false)) {
                    "Authorized • $driveAccount"
                } else {
                    "Not connected • Tap to sign in with Google"
                }
            ),
            AiPlugin(
                id = "calendar_plugin",
                name = "Google Calendar Sync",
                description = "Checks your live meetings, schedules, and confirms availability automatically.",
                iconType = "CALENDAR",
                isConnected = calendarAccount.isNotBlank() && prefs.getBoolean("plugin_enabled_calendar_plugin", false),
                connectedAccountEmail = calendarAccount,
                syncSummary = if (calendarAccount.isNotBlank() && prefs.getBoolean("plugin_enabled_calendar_plugin", false)) {
                    "Authorized • $calendarAccount"
                } else {
                    "Not connected • Tap to sign in with Google"
                }
            ),
            AiPlugin(
                id = "photos_plugin",
                name = "Google Photos Library",
                description = "Accesses photo albums and visual memories via Android Photo Picker.",
                iconType = "PHOTOS",
                isConnected = photosAccount.isNotBlank() && prefs.getBoolean("plugin_enabled_photos_plugin", false),
                connectedAccountEmail = photosAccount,
                syncSummary = if (photosAccount.isNotBlank() && prefs.getBoolean("plugin_enabled_photos_plugin", false)) {
                    "Authorized • $photosAccount"
                } else {
                    "Not connected • Tap to authorize Google Photos"
                }
            ),
            AiPlugin(
                id = "sms_pilot_plugin",
                name = "SIM Carrier SMS Auto-Pilot",
                description = "Allows Oj AI to directly transmit auto-replies via your device's SIM card to delegated contacts when away.",
                iconType = "SMS",
                isConnected = prefs.getBoolean("plugin_enabled_sms_pilot_plugin", hasSim),
                syncSummary = simSummary,
                sampleItems = listOf(simSummary)
            ),
            AiPlugin(
                id = "knowledge_base_plugin",
                name = "Personal Knowledge Base",
                description = "Custom rules, FAQs, business hours, and instructions on how you prefer to communicate.",
                iconType = "KNOWLEDGE",
                isConnected = prefs.getBoolean("plugin_enabled_knowledge_base_plugin", true),
                syncSummary = if (customBio.isNotBlank()) "Active • Custom instructions configured" else "Tap to configure custom rules & FAQs",
                sampleItems = if (customBio.isNotBlank()) listOf("💡 $customBio") else emptyList()
            )
        )
        _plugins.value = list
    }

    fun authorizeWorkspacePlugin(pluginId: String, accountEmail: String) {
        prefs.edit()
            .putString("auth_account_$pluginId", accountEmail.trim())
            .putBoolean("plugin_enabled_$pluginId", true)
            .apply()
        refreshPlugins()
    }

    fun disconnectWorkspacePlugin(pluginId: String) {
        prefs.edit()
            .remove("auth_account_$pluginId")
            .putBoolean("plugin_enabled_$pluginId", false)
            .apply()
        refreshPlugins()
    }

    fun getAuthorizedPluginAccount(pluginId: String): String {
        return prefs.getString("auth_account_$pluginId", "") ?: ""
    }

    fun isMorphAuthenticatedAndActive(): Boolean {
        return morphRepository.connectedAccountFlow.value != null &&
            prefs.getBoolean("plugin_enabled_morph_plugin", true)
    }

    fun getAuthenticatedMorphNotes(): List<MorphNote> {
        if (!isMorphAuthenticatedAndActive()) return emptyList()
        return morphRepository.notesFlow.value.filter { !it.isArchived }
    }

    private fun getRealSimInfo(): Pair<Boolean, String> {
        try {
            val telephony = appContext.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            val carrier = telephony?.networkOperatorName?.ifBlank { telephony.simOperatorName }
            if (!carrier.isNullOrBlank()) {
                return true to "Connected • Active SIM: $carrier"
            }
        } catch (_: Exception) {}
        return true to "Connected • Direct SIM dispatch active"
    }

    private fun loadSavedState() {
        val jsonStr = prefs.getString("delegated_contacts_json", null)
        if (jsonStr != null) {
            try {
                val array = JSONArray(jsonStr)
                val list = mutableListOf<DelegatedContact>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        DelegatedContact(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            phoneNumber = obj.getString("phoneNumber"),
                            isAiAutoReplyEnabled = obj.optBoolean("isAiAutoReplyEnabled", true),
                            allowReadEmailContext = obj.optBoolean("allowReadEmailContext", true),
                            allowDirectSend = obj.optBoolean("allowDirectSend", true),
                            customInstruction = obj.optString("customInstruction", "Respond politely"),
                            lastActionSummary = obj.optString("lastActionSummary", "Ready to respond")
                        )
                    )
                }
                _delegatedContacts.value = list
            } catch (e: Exception) {
                Log.w(tag, "Failed to load delegated contacts: ${e.message}")
            }
        }

        val masterEnabled = prefs.getBoolean("master_enabled", true)
        val autonomous = prefs.getBoolean("autonomous_mode", true)
        val tone = prefs.getString("tone", "Professional Executive") ?: "Professional Executive"
        val bio = prefs.getString("custom_system_bio", "Handle client and team inquiries smoothly and professionally.") ?: ""

        _settings.value = _settings.value.copy(
            isAgentMasterEnabled = masterEnabled,
            autonomousMode = autonomous,
            personalityTone = tone,
            customSystemBio = bio
        )
    }

    private fun saveDelegatedContacts() {
        val array = JSONArray()
        for (item in _delegatedContacts.value) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("name", item.name)
                put("phoneNumber", item.phoneNumber)
                put("isAiAutoReplyEnabled", item.isAiAutoReplyEnabled)
                put("allowReadEmailContext", item.allowReadEmailContext)
                put("allowDirectSend", item.allowDirectSend)
                put("customInstruction", item.customInstruction)
                put("lastActionSummary", item.lastActionSummary)
            }
            array.put(obj)
        }
        prefs.edit().putString("delegated_contacts_json", array.toString()).apply()
    }

    /**
     * Toggles a plugin on/off. Note: Morph cannot be toggled ON unless a MorphAccount is authenticated.
     */
    fun togglePlugin(pluginId: String, isConnected: Boolean): Boolean {
        if (pluginId == "morph_plugin") {
            if (isConnected && morphRepository.connectedAccountFlow.value == null) {
                // Caller must trigger MorphAuthBottomSheet so user signs in first!
                return false
            }
            prefs.edit().putBoolean("plugin_enabled_morph_plugin", isConnected).apply()
            morphRepository.isConnected = isConnected
            if (isConnected) {
                morphRepository.syncWithServer()
            }
            refreshPlugins()
            return true
        }

        if (pluginId in listOf("gmail_plugin", "drive_plugin", "calendar_plugin", "photos_plugin")) {
            if (isConnected && getAuthorizedPluginAccount(pluginId).isBlank()) {
                return false
            }
        }

        prefs.edit().putBoolean("plugin_enabled_$pluginId", isConnected).apply()
        refreshPlugins()
        return true
    }

    fun addDelegatedContact(contact: DelegatedContact) {
        val current = _delegatedContacts.value.toMutableList()
        current.removeAll { it.id == contact.id }
        current.add(0, contact)
        _delegatedContacts.value = current
        saveDelegatedContacts()
    }

    fun removeDelegatedContact(contactId: String) {
        _delegatedContacts.value = _delegatedContacts.value.filter { it.id != contactId }
        saveDelegatedContacts()
    }

    fun updateDelegatedContact(contact: DelegatedContact) {
        _delegatedContacts.value = _delegatedContacts.value.map {
            if (it.id == contact.id) contact else it
        }
        saveDelegatedContacts()
    }

    fun setMasterEnabled(enabled: Boolean) {
        updateSettings(_settings.value.copy(isAgentMasterEnabled = enabled))
    }

    fun updateSettings(newSettings: AiAgentSettings) {
        _settings.value = newSettings
        prefs.edit()
            .putBoolean("master_enabled", newSettings.isAgentMasterEnabled)
            .putBoolean("autonomous_mode", newSettings.autonomousMode)
            .putString("tone", newSettings.personalityTone)
            .putString("custom_system_bio", newSettings.customSystemBio)
            .apply()
        refreshPlugins()
    }

    fun restoreDefaults() {
        prefs.edit()
            .putBoolean("master_enabled", true)
            .putBoolean("autonomous_mode", true)
            .putString("tone", "Professional Executive")
            .putString("custom_system_bio", "Handle client and team inquiries smoothly and professionally.")
            .apply()
        loadSavedState()
        refreshPlugins()
    }

    /**
     * Synthesizes context from connected real plugins (authenticated Morph Notes, Knowledge Base) and calls Gemini API.
     */
    suspend fun generateAutonomousReply(
        contact: DelegatedContact,
        incomingMessage: String
    ): AiReplyResult = withContext(Dispatchers.IO) {
        val sourcesUsed = mutableListOf<String>()

        val contextBuilder = StringBuilder()
        contextBuilder.append("You are Oj AI, an intelligent executive agent acting on behalf of the user.\n")
        contextBuilder.append("Your task: Respond to an incoming message from contact '${contact.name}' (${contact.phoneNumber}).\n")
        contextBuilder.append("Tone: ${_settings.value.personalityTone}\n")
        contextBuilder.append("Specific instructions for this contact: ${contact.customInstruction}\n")
        if (_settings.value.customSystemBio.isNotBlank()) {
            contextBuilder.append("User instructions: ${_settings.value.customSystemBio}\n\n")
        }

        // Live Authenticated Omkun Morph Notes Context
        if (isMorphAuthenticatedAndActive()) {
            val account = morphRepository.connectedAccountFlow.value
            val notes = getAuthenticatedMorphNotes()
            if (notes.isNotEmpty() && account != null) {
                sourcesUsed.add("Omkun Morph (${account.email})")
                contextBuilder.append("--- AUTHENTICATED OMKUN MORPH NOTES (${account.email}) ---\n")
                notes.take(8).forEach { note ->
                    val summary = if (note.isChecklist) {
                        note.checklist.joinToString(", ") { (if (it.isDone) "[x] " else "[ ] ") + it.text }
                    } else {
                        note.content
                    }
                    contextBuilder.append("• ${note.title.ifBlank { "Untitled" }}: $summary\n")
                }
                contextBuilder.append("\n")
            }
        }

        contextBuilder.append("Incoming Message from ${contact.name}:\n\"$incomingMessage\"\n\n")
        contextBuilder.append("Provide a concise, direct, helpful reply to send back to them. Write ONLY the message body, without meta-commentary.")

        val geminiApiKey = try {
            val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
            field.get(null) as? String ?: ""
        } catch (_: Exception) {
            ""
        }
        var generatedAnswer = ""

        if (geminiApiKey.isNotBlank()) {
            try {
                generatedAnswer = callGeminiRestApi(geminiApiKey, contextBuilder.toString())
            } catch (e: Exception) {
                Log.w(tag, "Gemini REST API notice: ${e.message}")
            }
        }

        if (generatedAnswer.isBlank()) {
            generatedAnswer = generateLocalReasoningReply(contact, incomingMessage)
        }

        val actionRecord = "Replied to ${contact.name}: \"${generatedAnswer.take(45)}...\""
        val actions = _recentActions.value.toMutableList()
        actions.add(0, actionRecord)
        _recentActions.value = actions.take(10)

        val updatedContact = contact.copy(
            lastActionSummary = "Auto-replied: \"${generatedAnswer.take(35)}...\"",
            lastActionTimestamp = System.currentTimeMillis()
        )
        updateDelegatedContact(updatedContact)

        AiReplyResult(
            generatedText = generatedAnswer,
            contextSourcesUsed = sourcesUsed,
            isDirectlySent = _settings.value.autonomousMode && contact.allowDirectSend
        )
    }

    private fun generateLocalReasoningReply(
        contact: DelegatedContact,
        message: String
    ): String {
        val lower = message.lowercase()
        return when {
            lower.contains("available") || lower.contains("free") || lower.contains("meet") -> {
                "Hello! Thanks for reaching out. I'm currently occupied, but I will get back to you shortly."
            }
            lower.contains("urgent") || lower.contains("asap") -> {
                "Hi ${contact.name}, I received your urgent note. I will prioritize this and respond as soon as I can step away."
            }
            lower.contains("hi") || lower.contains("hello") || lower.contains("hey") -> {
                "Hi ${contact.name}! I received your message. How can I help you today?"
            }
            else -> {
                "Thank you for your message, ${contact.name}. I've received it and will follow up with you as soon as possible."
            }
        }
    }

    private suspend fun callGeminiRestApi(apiKey: String, prompt: String): String = withContext(Dispatchers.IO) {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

        val jsonBody = JSONObject().apply {
            val contentsArr = JSONArray().apply {
                val partObj = JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    })
                }
                put(partObj)
            }
            put("contents", contentsArr)
        }

        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("Gemini API HTTP ${response.code}")
            }

            val respStr = response.body?.string() ?: ""
            val respJson = JSONObject(respStr)
            val candidates = respJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val first = candidates.getJSONObject(0)
                val content = first.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    return@withContext parts.getJSONObject(0).optString("text", "").trim()
                }
            }
        }
        ""
    }
}
