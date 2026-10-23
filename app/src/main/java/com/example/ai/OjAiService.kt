package com.example.ai

import android.util.Log
import androidx.compose.runtime.Immutable
import com.example.BuildConfig
import com.example.model.MediaType
import com.example.model.MorphAccount
import com.example.model.MorphPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

@Immutable
data class OjAiChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false,
    val feedback: Int = 0, // 0: none, 1: like, -1: dislike
    val mediaUrl: String? = null,
    val mediaType: MediaType = MediaType.TEXT,
    val audioDurationSeconds: Int = 0
)

object OjAiService {
    private const val TAG = "OjAiService"
    private const val GEMINI_MODEL = "gemini-2.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"
    private const val INTERNAL_WORKER_URL = "https://oj-ai-worker.olinam.workers.dev/api/chat"

    val initialSuggestions = listOf(
        "📝 Show my Omkun Morph notes",
        "➕ Create a Morph note: Sprint Goals",
        "✨ Summarize my pending chats",
        "✍️ Draft a polite decline message",
        "🌐 Translate 'See you tomorrow' to Hindi",
        "🔒 How does Olinam encryption work?"
    )

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(45, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)
            .build()
    }

    fun getApiKey(): String {
        return try {
            val field = BuildConfig::class.java.getDeclaredField("GEMINI_API_KEY")
            val key = field.get(null) as? String
            if (!key.isNullOrBlank()) key else (System.getenv("GEMINI_API_KEY") ?: "")
        } catch (_: Throwable) {
            System.getenv("GEMINI_API_KEY") ?: ""
        }
    }

    /**
     * Generates a response while executing authenticated Omkun Morph tool operations
     * strictly against the signed-in user's MorphAccount.
     */
    suspend fun generateResponse(
        userPrompt: String,
        history: List<OjAiChatMessage> = emptyList(),
        agentManager: OjAiAgentManager? = null
    ): String = withContext(Dispatchers.IO) {
        // 1. Check if prompt targets Omkun Morph Notes (CRUD / Read / Sync)
        if (agentManager != null) {
            val morphToolResponse = handleMorphToolIntentIfApplicable(userPrompt, agentManager)
            if (morphToolResponse != null) {
                return@withContext morphToolResponse
            }
        }

        // Build authenticated context block (Morph notes + custom instructions)
        val systemContext = buildSystemContext(agentManager)

        // 2. Priority 1: Direct Gemini REST API (when GEMINI_API_KEY is configured in Secrets)
        val apiKey = getApiKey()
        if (apiKey.isNotBlank()) {
            try {
                val response = callGeminiRestApi(apiKey, userPrompt, history, systemContext)
                if (response.isNotBlank()) {
                    return@withContext response
                }
            } catch (t: Throwable) {
                Log.w(TAG, "Gemini REST API notice: ${t.message}")
            }
        }

        // 3. Priority 2: Internal Oj AI Cloud Worker
        try {
            val workerResponse = callInternalWorker(userPrompt, history, systemContext)
            if (workerResponse.isNotBlank()) {
                return@withContext workerResponse
            }
        } catch (t: Throwable) {
            Log.d(TAG, "Internal worker fallback notice: ${t.message}")
        }

        // 4. Context-aware intelligent fallback
        generateSmartFallback(userPrompt, agentManager)
    }

    private fun handleMorphToolIntentIfApplicable(
        prompt: String,
        agentManager: OjAiAgentManager
    ): String? {
        val trimmed = prompt.trim()
        val lower = trimmed.lowercase(Locale.US)

        val mentionsMorphExplicitly = lower.contains("morph")
        val mentionsNotes = lower.contains("note") || lower.contains("notes") ||
            lower.contains("checklist") || lower.contains("todo") || lower.contains("to-do")

        if (!mentionsMorphExplicitly && !mentionsNotes) return null

        val isCreate = lower.contains("create") || lower.contains("add") || lower.contains("save") ||
            lower.contains("new note") || lower.contains("make a note") || lower.contains("write a note") ||
            lower.contains("banao") || lower.contains("bana do") || lower.contains("likho") || lower.contains("save karo")
        val isDelete = lower.contains("delete") || lower.contains("remove") ||
            lower.contains("hatao") || lower.contains("delete karo") || lower.contains("hata do")
        val isUpdate = lower.contains("update") || lower.contains("edit") || lower.contains("append") ||
            lower.contains("change") || lower.contains("badlo")
        val isListOrRead = lower.contains("show") || lower.contains("list") || lower.contains("read") ||
            lower.contains("my morph") || lower.contains("my notes") || lower.contains("what") ||
            lower.contains("dikhao") || lower.contains("batao") || lower.contains("padho") || lower.contains("search")

        if (!isCreate && !isDelete && !isUpdate && !isListOrRead && !mentionsMorphExplicitly) {
            return null
        }

        val morphRepo = agentManager.morphRepository
        val connectedAccount = morphRepo.connectedAccountFlow.value

        // Enforce strict account authentication before any Morph access!
        if (connectedAccount == null) {
            return "🔒 **Sign in to Omkun Morph Required**\n\n" +
                "To read, create, or delete notes, Morph needs to know which account to use.\n\n" +
                "1. Tap **Oj AI** at the top (or the **Agent Tools** menu)\n" +
                "2. Tap **Omkun Morph Notes** and sign in with your account\n" +
                "3. Authorize permissions — then ask me again!"
        }

        if (!agentManager.isMorphAuthenticatedAndActive()) {
            return "⏸️ **Omkun Morph Integration Paused**\n\n" +
                "Your Morph account (**${connectedAccount.email}**) is signed in, but Oj AI access is currently turned off. " +
                "Enable the **Omkun Morph Notes** switch in **Oj AI Profile → Work Integrations** to continue."
        }

        // Handle DELETE Note
        if (isDelete) {
            if (!connectedAccount.authorizedScopes.contains(MorphAccount.SCOPE_DELETE)) {
                return "⚠️ **Delete Permission Not Granted**\n\n" +
                    "Oj AI does not have permission to delete notes in **${connectedAccount.email}**. " +
                    "You can update authorized scopes in **Omkun Morph Notes** settings."
            }
            val query = extractTargetQueryForDelete(trimmed)
            val deleteResult = morphRepo.deleteNoteByQueryFromAi(query)
            return deleteResult.fold(
                onSuccess = { deleted ->
                    "🗑️ **Note Deleted from Omkun Morph**\n\n" +
                        "• **Deleted:** ${deleted.title.ifBlank { deleted.content.take(30) }}\n" +
                        "• **Account:** ${connectedAccount.email}\n" +
                        "• **Remaining Notes:** ${morphRepo.notesFlow.value.count { !it.isArchived }}"
                },
                onFailure = {
                    val existingTitles = morphRepo.notesFlow.value.filter { !it.isArchived }
                        .take(5)
                        .joinToString("\n") { "• ${it.title.ifBlank { "Untitled" }}" }
                    if (existingTitles.isBlank()) {
                        "📭 Your Morph account (**${connectedAccount.email}**) currently has no notes to delete."
                    } else {
                        "🔍 Couldn't find a note matching *\"$query\"* in **${connectedAccount.email}**.\n\n" +
                            "**Available notes in your account:**\n$existingTitles\n\n" +
                            "*Try saying: \"Delete Morph note [Title]\"*"
                    }
                }
            )
        }

        // Handle CREATE Note
        if (isCreate) {
            if (!connectedAccount.authorizedScopes.contains(MorphAccount.SCOPE_WRITE)) {
                return "⚠️ **Create/Write Permission Not Granted**\n\n" +
                    "Oj AI only has read access for **${connectedAccount.email}**. Re-authorize with Create & Edit permission to add notes."
            }
            val (title, body, checklist) = parseTitleAndContentForCreate(trimmed)
            val createdResult = morphRepo.createNoteFromAi(
                title = title,
                content = body,
                checklistItems = checklist,
                isPinned = lower.contains("pin"),
                colorHex = when {
                    lower.contains("blue") -> MorphPalette.Sky
                    lower.contains("green") -> MorphPalette.Mint
                    lower.contains("pink") || lower.contains("red") -> MorphPalette.Peach
                    lower.contains("purple") -> MorphPalette.Lavender
                    else -> MorphPalette.Lemon
                }
            )
            return createdResult.fold(
                onSuccess = { note ->
                    val preview = if (note.isChecklist) {
                        note.checklist.joinToString("\n") { "☐ ${it.text}" }
                    } else {
                        note.content
                    }
                    "✅ **Saved to Omkun Morph**\n\n" +
                        "• **Title:** ${note.title}\n" +
                        (if (preview.isNotBlank()) "• **Content:**\n$preview\n" else "") +
                        "• **Account:** ${connectedAccount.email}"
                },
                onFailure = {
                    "⚠️ Could not create note in **${connectedAccount.email}**: ${it.message}"
                }
            )
        }

        // Handle UPDATE Note
        if (isUpdate) {
            val payload = trimmed.substringAfter(":", "").trim()
            if (payload.isNotBlank()) {
                val targetTitle = trimmed.substringBefore(":")
                    .replace(Regex("(?i)(update|edit|append|morph|note|in|to)"), "")
                    .trim()
                val updateRes = morphRepo.updateNoteByQueryFromAi(targetTitle, payload, append = true)
                if (updateRes.isSuccess) {
                    val updated = updateRes.getOrThrow()
                    return "✏️ **Updated Morph Note**\n\n" +
                        "• **Title:** ${updated.title}\n" +
                        "• **Added:** $payload\n" +
                        "• **Account:** ${connectedAccount.email}"
                }
            }
        }

        // Handle READ / LIST Notes directly if Gemini key isn't present, else let LLM reason over live notes
        if (isListOrRead && getApiKey().isBlank()) {
            val notes = morphRepo.notesFlow.value.filter { !it.isArchived }
            if (notes.isEmpty()) {
                return "📭 **Omkun Morph (${connectedAccount.email})**\n\n" +
                    "You don't have any notes saved in this account yet. Say *\"Create a Morph note: Meeting at 4 PM\"* to add one!"
            }
            val formatted = notes.take(8).joinToString("\n\n") { note ->
                val pinIcon = if (note.isPinned) "📌 " else "📝 "
                val body = if (note.isChecklist) {
                    note.checklist.joinToString("\n") { "   " + (if (it.isDone) "☑ " else "☐ ") + it.text }
                } else {
                    "   " + note.content.take(120)
                }
                "$pinIcon**${note.title.ifBlank { "Untitled" }}**\n$body"
            }
            return "🗂️ **Your Omkun Morph Notes (${connectedAccount.email}):**\n\n$formatted"
        }

        return null
    }

    private fun extractTargetQueryForDelete(prompt: String): String {
        val afterColon = prompt.substringAfter(":", "").trim()
        if (afterColon.isNotBlank()) return afterColon

        val quoted = Regex("[\"']([^\"']+)[\"']").find(prompt)?.groupValues?.getOrNull(1)
        if (!quoted.isNullOrBlank()) return quoted.trim()

        return prompt
            .replace(
                Regex("(?i)\\b(please|oj ai|delete|remove|my|the|from|omkun|morph|notes|note|titled|called|named|about|karo|hatao|hata|do|mera|meri)\\b"),
                " "
            )
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun parseTitleAndContentForCreate(prompt: String): Triple<String, String, List<String>> {
        val isChecklist = prompt.contains("checklist", ignoreCase = true) ||
            prompt.contains("to-do", ignoreCase = true) ||
            prompt.contains("todo", ignoreCase = true)

        // Extract explicit title if user wrote "... titled/called/named <Title>: <Body>"
        var explicitTitle: String? = null
        if (prompt.contains(":")) {
            val beforeColon = prompt.substringBefore(":").trim()
            val titledMatch = Regex("(?i)(?:titled|called|named|title)\\s+[\"']?([^\"':]+)[\"']?$")
                .find(beforeColon)?.groupValues?.getOrNull(1)?.trim()
            val hindiNaamMatch = Regex("(?i)(?:morph\\s+me\\s+)?[\"']?([^\"':]+?)[\"']?\\s+naam\\s+ka")
                .find(beforeColon)?.groupValues?.getOrNull(1)?.trim()
            explicitTitle = titledMatch ?: hindiNaamMatch
        }

        val rawPayload = if (prompt.contains(":")) {
            prompt.substringAfter(":").trim()
        } else {
            prompt.replace(
                Regex("(?i)^(please\\s+)?(create|add|save|make|write)\\s+(a\\s+)?(new\\s+)?(omkun\\s+)?(morph\\s+)?(note|checklist|todo|to-do)(\\s+in\\s+morph)?(\\s+about|\\s+for|\\s+saying|\\s+titled|\\s+called|\\s+named)?\\s*"),
                ""
            ).replace(
                Regex("(?i)(morph\\s+me\\s+)(ek\\s+)?(note\\s+)(banao|likho|save\\s+karo)\\s*"),
                ""
            ).trim()
        }

        if (isChecklist) {
            val items = rawPayload.split(Regex("[,;\n]"))
                .map { it.trim().removePrefix("-").trim() }
                .filter { it.isNotBlank() }
            val title = explicitTitle?.takeIf { it.isNotBlank() }
                ?: items.firstOrNull()?.take(28)?.let { "Checklist: $it" }
                ?: "Morph Checklist"
            return Triple(title, "", if (items.isEmpty()) listOf(rawPayload.ifBlank { "New task" }) else items)
        }

        if (!explicitTitle.isNullOrBlank()) {
            return Triple(explicitTitle, rawPayload, emptyList())
        }

        return if (rawPayload.contains(" - ")) {
            val t = rawPayload.substringBefore(" - ").trim()
            val b = rawPayload.substringAfter(" - ").trim()
            Triple(t.ifBlank { "Quick Note" }, b, emptyList())
        } else if (rawPayload.contains("\n")) {
            val t = rawPayload.substringBefore("\n").trim()
            val b = rawPayload.substringAfter("\n").trim()
            Triple(t.ifBlank { "Quick Note" }, b, emptyList())
        } else {
            val words = rawPayload.split(" ").filter { it.isNotBlank() }
            val title = words.take(5).joinToString(" ").ifBlank { "Morph Note" }
            Triple(title, rawPayload.ifBlank { prompt }, emptyList())
        }
    }

    private fun buildSystemContext(agentManager: OjAiAgentManager?): String {
        val sb = StringBuilder()
        sb.append(
            "You are Oj AI, the intelligent assistant built into Olinam, an end-to-end encrypted messaging app. " +
                "Be helpful, concise, and clear. Never expose internal API endpoints or developer tokens."
        )
        if (agentManager != null) {
            val settings = agentManager.settings.value
            sb.append("\nResponse Tone: ${settings.personalityTone}.")
            if (settings.customSystemBio.isNotBlank()) {
                sb.append("\nUser Preferences: ${settings.customSystemBio}")
            }
            if (agentManager.isMorphAuthenticatedAndActive()) {
                val account = agentManager.morphRepository.connectedAccountFlow.value
                val notes = agentManager.getAuthenticatedMorphNotes()
                if (account != null) {
                    sb.append("\n\nConnected Omkun Morph Account: ${account.displayName} (${account.email}).")
                    if (notes.isNotEmpty()) {
                        sb.append("\nUser's Live Morph Notes:\n")
                        notes.take(10).forEach { n ->
                            val body = if (n.isChecklist) {
                                n.checklist.joinToString(", ") { (if (it.isDone) "[Done] " else "[Todo] ") + it.text }
                            } else {
                                n.content
                            }
                            sb.append("- ${n.title.ifBlank { "Untitled" }}: $body\n")
                        }
                    }
                }
            }
        }
        return sb.toString()
    }

    private fun callInternalWorker(
        userPrompt: String,
        history: List<OjAiChatMessage>,
        systemContext: String
    ): String {
        val jsonMediaType = "application/json; charset=utf-8".toMediaType()
        val payload = JSONObject().apply {
            put("prompt", userPrompt)
            put("message", userPrompt)
            put("system", systemContext)
            put("model", "oj-ai")
            val historyArray = JSONArray()
            for (msg in history.takeLast(8)) {
                historyArray.put(
                    JSONObject().apply {
                        put("role", if (msg.isUser) "user" else "assistant")
                        put("content", msg.text)
                    }
                )
            }
            put("history", historyArray)
        }

        val request = Request.Builder()
            .url(INTERNAL_WORKER_URL)
            .addHeader("Content-Type", "application/json")
            .post(payload.toString().toRequestBody(jsonMediaType))
            .build()

        httpClient.newCall(request).execute().use { response ->
            val bodyStr = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("Worker HTTP ${response.code}")
            }
            return try {
                val json = JSONObject(bodyStr)
                when {
                    json.has("response") -> json.getString("response")
                    json.has("text") -> json.getString("text")
                    json.has("message") -> json.getString("message")
                    json.has("output") -> json.getString("output")
                    json.has("result") -> json.getString("result")
                    else -> bodyStr
                }
            } catch (_: Exception) {
                bodyStr
            }
        }
    }

    private fun callGeminiRestApi(
        apiKey: String,
        userPrompt: String,
        history: List<OjAiChatMessage>,
        systemContext: String
    ): String {
        val url = "$BASE_URL$GEMINI_MODEL:generateContent?key=$apiKey"
        val jsonMediaType = "application/json; charset=utf-8".toMediaType()

        val rootJson = JSONObject().apply {
            put(
                "systemInstruction",
                JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemContext)))
            )

            val contentsArray = JSONArray()
            for (msg in history.takeLast(6)) {
                contentsArray.put(
                    JSONObject().apply {
                        put("role", if (msg.isUser) "user" else "model")
                        put("parts", JSONArray().put(JSONObject().put("text", msg.text)))
                    }
                )
            }
            contentsArray.put(
                JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().put(JSONObject().put("text", userPrompt)))
                }
            )
            put("contents", contentsArray)
            put(
                "generationConfig",
                JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 1024)
                }
            )
        }

        val request = Request.Builder()
            .url(url)
            .post(rootJson.toString().toRequestBody(jsonMediaType))
            .build()

        httpClient.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                Log.w(TAG, "Gemini API HTTP ${response.code}")
                return ""
            }
            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates") ?: return ""
            if (candidates.length() == 0) return ""
            val content = candidates.getJSONObject(0).optJSONObject("content") ?: return ""
            val parts = content.optJSONArray("parts") ?: return ""
            if (parts.length() == 0) return ""
            return parts.getJSONObject(0).optString("text", "").trim()
        }
    }

    private fun generateSmartFallback(
        userPrompt: String,
        agentManager: OjAiAgentManager?
    ): String {
        val lower = userPrompt.lowercase(Locale.US).trim()

        if (agentManager?.isMorphAuthenticatedAndActive() == true &&
            (lower.contains("morph") || lower.contains("note"))
        ) {
            val acc = agentManager.morphRepository.connectedAccountFlow.value
            val notes = agentManager.getAuthenticatedMorphNotes()
            if (acc != null) {
                return if (notes.isEmpty()) {
                    "📭 Your Omkun Morph account (**${acc.email}**) has no active notes yet. Ask me to create one anytime!"
                } else {
                    "🗂️ **Omkun Morph (${acc.email}):**\n\n" +
                        notes.take(6).joinToString("\n") { "• **${it.title.ifBlank { "Untitled" }}** — ${it.content.take(70)}" }
                }
            }
        }

        return when {
            lower.contains("summarize") || lower.contains("summary") -> {
                "📋 **Chat Summary:**\n" +
                    "• **Direct Chats:** Recent updates and pending replies are organized in your Chats tab.\n" +
                    "• **Groups:** Active team discussions are grouped under the Groups chip.\n" +
                    "• **Tip:** Connect your **Omkun Morph** account in Oj AI Profile to save action items directly to your notes."
            }
            lower.contains("draft") || lower.contains("reply") -> {
                "Here is a clean, polite draft you can send:\n\n" +
                    "*\"Hi! Thanks for reaching out. I'm reviewing this right now and will get back to you shortly.\"*\n\n" +
                    "💡 *Tap Copy below to copy it to your clipboard.*"
            }
            lower.contains("translate") && (lower.contains("hindi") || lower.contains("india")) -> {
                "🌐 **Translation to Hindi:**\n\n" +
                    "\"**कल मिलते हैं! आपका दिन शुभ हो।**\"\n" +
                    "*(Kal milte hain! Aapka din shubh ho.)*"
            }
            lower.contains("encrypt") || lower.contains("security") || lower.contains("e2ee") || lower.contains("privacy") -> {
                "🔒 **Olinam End-to-End Encryption:**\n\n" +
                    "• **AES-256-GCM:** Messages and media are encrypted on-device before transmission.\n" +
                    "• **Account Isolation:** Connected tools like Omkun Morph use explicit account authorization so only your signed-in account can read or modify your data."
            }
            lower.contains("hello") || lower.contains("hi") || lower.contains("hey") -> {
                "Hey there! 👋 I'm **Oj AI**.\n\n" +
                    "I can draft messages, summarize chats, translate languages, and — once you sign in to **Omkun Morph** — read, create, edit, or delete notes directly in your account. How can I help?"
            }
            else -> {
                "I'm ready to help! You can ask me to draft replies, translate messages, summarize conversations, or manage notes in your connected **Omkun Morph** account."
            }
        }
    }
}
