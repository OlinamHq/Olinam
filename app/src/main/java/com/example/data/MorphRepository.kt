package com.example.data

import android.content.Context
import android.util.Log
import com.example.model.MorphChecklistItem
import com.example.model.MorphNote
import com.example.model.MorphPalette
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Repository for Omkun Morph notes.
 * Automatically connects and authenticates with Omkun Morph Cloud REST API:
 * Base URL: https://ais-dev-kps2metajswcv3lmn3d4iq-193817906370.asia-southeast1.run.app/api/v1
 * API Key: omk_live_test_7f8a9b2c
 * Supports:
 * - GET /notes (Load all notes)
 * - POST /notes (Create note with {title, note, color, pinned})
 * - PATCH /notes/{id} (Update note with {title, note})
 * - DELETE /notes/{id} (Delete note)
 * Also provides offline SharedPreferences disk caching for instant responsiveness.
 */
class MorphRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("omkun_morph_prefs", Context.MODE_PRIVATE)
    private val _notesFlow = MutableStateFlow<List<MorphNote>>(emptyList())
    val notesFlow: StateFlow<List<MorphNote>> = _notesFlow.asStateFlow()

    private val _syncStatus = MutableStateFlow<String>("Connected • Cloud Active")
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    // Configurable Omkun Morph API endpoints with pre-authenticated defaults
    var apiBaseUrl: String
        get() = prefs.getString("morph_api_base_url", DEFAULT_MORPH_API_BASE_URL) ?: DEFAULT_MORPH_API_BASE_URL
        set(value) = prefs.edit().putString("morph_api_base_url", value.trim()).apply()

    var apiAuthToken: String
        get() = prefs.getString("morph_api_token", DEFAULT_MORPH_API_KEY) ?: DEFAULT_MORPH_API_KEY
        set(value) = prefs.edit().putString("morph_api_token", value.trim()).apply()

    var isConnected: Boolean
        get() = prefs.getBoolean("morph_is_connected", true)
        set(value) = prefs.edit().putBoolean("morph_is_connected", value).apply()

    var lastSyncTime: Long
        get() = prefs.getLong("morph_last_sync_time", 0L)
        set(value) = prefs.edit().putLong("morph_last_sync_time", value).apply()

    init {
        loadNotes()
        // Auto-authenticate and connect on repository initialization
        if (isConnected && apiAuthToken.isNotBlank()) {
            syncWithServer()
        }
    }

    /**
     * Authenticates and connects automatically with Morph Cloud API.
     * No manual API entry required on the Morph page.
     */
    fun authenticateAndConnect(
        apiKey: String = DEFAULT_MORPH_API_KEY,
        baseUrl: String = DEFAULT_MORPH_API_BASE_URL
    ) {
        this.apiBaseUrl = baseUrl
        this.apiAuthToken = apiKey
        this.isConnected = true
        _syncStatus.value = "Authenticating..."
        syncWithServer { success, msg ->
            if (success) {
                _syncStatus.value = "Connected • Cloud Synced"
            } else {
                _syncStatus.value = "Connected (Cached)"
            }
        }
    }

    /**
     * 1. SAARE NOTES LOAD KARNA (GET /notes)
     */
    fun syncWithServer(onComplete: ((Boolean, String) -> Unit)? = null) {
        if (!isConnected || apiAuthToken.isBlank()) {
            onComplete?.invoke(false, "Not connected")
            return
        }

        coroutineScope.launch {
            try {
                _syncStatus.value = "Syncing..."
                val url = "${apiBaseUrl.trimEnd('/')}/notes"
                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer $apiAuthToken")
                    .addHeader("Content-Type", "application/json")
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val bodyString = response.body?.string().orEmpty()
                        if (bodyString.isNotBlank()) {
                            val parsedNotes = parseNotesFromJson(bodyString)
                            if (parsedNotes.isNotEmpty()) {
                                val currentList = _notesFlow.value.toMutableList()
                                val remoteMap = parsedNotes.associateBy { it.id }
                                val merged = mutableListOf<MorphNote>()
                                merged.addAll(parsedNotes)

                                // Keep any existing local seed / custom notes that don't collide
                                currentList.forEach { localNote ->
                                    if (!remoteMap.containsKey(localNote.id)) {
                                        merged.add(localNote)
                                    }
                                }

                                _notesFlow.value = merged
                                saveToDisk(merged)
                            }
                        }
                        lastSyncTime = System.currentTimeMillis()
                        _syncStatus.value = "Connected • Cloud Synced"
                        onComplete?.invoke(true, "Notes synced from Morph Cloud")
                    } else {
                        Log.w("MorphRepository", "GET /notes returned ${response.code}")
                        _syncStatus.value = "Connected (Cached)"
                        onComplete?.invoke(false, "HTTP ${response.code}")
                    }
                }
            } catch (e: Exception) {
                Log.w("MorphRepository", "syncWithServer error: ${e.message}")
                _syncStatus.value = "Connected (Cached)"
                onComplete?.invoke(false, e.message ?: "Network error")
            }
        }
    }

    private fun parseNotesFromJson(jsonString: String): List<MorphNote> {
        val list = mutableListOf<MorphNote>()
        try {
            val root = JSONObject(jsonString)
            val dataArray = if (root.has("data")) {
                root.optJSONArray("data")
            } else null

            val array = dataArray ?: JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                val id = item.optString("id").ifBlank {
                    item.optString("_id").ifBlank { UUID.randomUUID().toString() }
                }
                val title = item.optString("title", "")
                val noteContent = item.optString("note").ifBlank {
                    item.optString("content", "")
                }
                val colorStr = item.optString("color", "yellow")
                val isPinned = item.optBoolean("pinned", item.optBoolean("isPinned", false))
                val colorHex = mapApiColorToHex(colorStr)
                val updatedAt = item.optLong("updatedAt", System.currentTimeMillis())

                list.add(
                    MorphNote(
                        id = id,
                        title = title,
                        content = noteContent,
                        colorHex = colorHex,
                        isPinned = isPinned,
                        updatedAt = updatedAt
                    )
                )
            }
        } catch (e: Exception) {
            Log.d("MorphRepository", "parseNotesFromJson non-fatal: ${e.message}")
        }
        return list
    }

    private fun loadNotes() {
        val savedJson = prefs.getString(KEY_NOTES_DATA, null)
        if (savedJson.isNullOrBlank()) {
            val initial = getSeedNotes()
            _notesFlow.value = initial
            saveToDisk(initial)
        } else {
            try {
                val list = mutableListOf<MorphNote>()
                val array = JSONArray(savedJson)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val checklistArray = obj.optJSONArray("checklist")
                    val checklistItems = mutableListOf<MorphChecklistItem>()
                    if (checklistArray != null) {
                        for (j in 0 until checklistArray.length()) {
                            val cObj = checklistArray.getJSONObject(j)
                            checklistItems.add(
                                MorphChecklistItem(
                                    id = cObj.optString("id"),
                                    text = cObj.optString("text"),
                                    isDone = cObj.optBoolean("isDone")
                                )
                            )
                        }
                    }

                    val labelsArray = obj.optJSONArray("labels")
                    val labels = mutableListOf<String>()
                    if (labelsArray != null) {
                        for (k in 0 until labelsArray.length()) {
                            labels.add(labelsArray.getString(k))
                        }
                    }

                    list.add(
                        MorphNote(
                            id = obj.optString("id"),
                            title = obj.optString("title"),
                            content = obj.optString("content"),
                            checklist = checklistItems,
                            isChecklist = obj.optBoolean("isChecklist"),
                            colorHex = obj.optString("colorHex", MorphPalette.Default),
                            isPinned = obj.optBoolean("isPinned"),
                            labels = labels,
                            updatedAt = obj.optLong("updatedAt", System.currentTimeMillis()),
                            isArchived = obj.optBoolean("isArchived")
                        )
                    )
                }
                _notesFlow.value = list
            } catch (_: Exception) {
                val fallback = getSeedNotes()
                _notesFlow.value = fallback
                saveToDisk(fallback)
            }
        }
    }

    private fun saveToDisk(notes: List<MorphNote>) {
        try {
            val array = JSONArray()
            for (note in notes) {
                val obj = JSONObject().apply {
                    put("id", note.id)
                    put("title", note.title)
                    put("content", note.content)
                    put("isChecklist", note.isChecklist)
                    put("colorHex", note.colorHex)
                    put("isPinned", note.isPinned)
                    put("updatedAt", note.updatedAt)
                    put("isArchived", note.isArchived)

                    val checkArr = JSONArray()
                    for (item in note.checklist) {
                        checkArr.put(
                            JSONObject().apply {
                                put("id", item.id)
                                put("text", item.text)
                                put("isDone", item.isDone)
                            }
                        )
                    }
                    put("checklist", checkArr)

                    val labelArr = JSONArray()
                    for (lbl in note.labels) {
                        labelArr.put(lbl)
                    }
                    put("labels", labelArr)
                }
                array.put(obj)
            }
            prefs.edit().putString(KEY_NOTES_DATA, array.toString()).apply()
        } catch (_: Exception) {}
    }

    /**
     * 2. NAYA NOTE CREATE KARNA (POST /notes)
     * 3. NOTE EDIT / UPDATE KARNA (PATCH /notes/{id})
     */
    fun saveNote(note: MorphNote) {
        val current = _notesFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == note.id }
        val updatedNote = note.copy(updatedAt = System.currentTimeMillis())
        val isNew = (index < 0)

        if (!isNew) {
            current[index] = updatedNote
        } else {
            current.add(0, updatedNote)
        }
        _notesFlow.value = current
        saveToDisk(current)

        // Remote cloud sync with Morph API
        if (isConnected && apiAuthToken.isNotBlank()) {
            coroutineScope.launch {
                try {
                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    if (isNew) {
                        // POST /notes
                        val json = JSONObject().apply {
                            put("title", updatedNote.title)
                            put("note", if (updatedNote.isChecklist) {
                                updatedNote.checklist.joinToString("\n") { (if (it.isDone) "☑ " else "☐ ") + it.text }
                            } else updatedNote.content)
                            put("color", mapHexToApiColor(updatedNote.colorHex))
                            put("pinned", updatedNote.isPinned)
                        }
                        val request = Request.Builder()
                            .url("${apiBaseUrl.trimEnd('/')}/notes")
                            .addHeader("Authorization", "Bearer $apiAuthToken")
                            .addHeader("Content-Type", "application/json")
                            .post(json.toString().toRequestBody(mediaType))
                            .build()

                        client.newCall(request).execute().use { response ->
                            if (response.isSuccessful) {
                                val bodyStr = response.body?.string().orEmpty()
                                try {
                                    val resObj = JSONObject(bodyStr)
                                    val dataObj = resObj.optJSONObject("data")
                                    val serverId = dataObj?.optString("id")?.ifBlank {
                                        dataObj.optString("_id")
                                    }
                                    if (!serverId.isNullOrBlank() && serverId != updatedNote.id) {
                                        val updatedList = _notesFlow.value.map {
                                            if (it.id == updatedNote.id) it.copy(id = serverId) else it
                                        }
                                        _notesFlow.value = updatedList
                                        saveToDisk(updatedList)
                                    }
                                } catch (_: Exception) {}
                            }
                        }
                    } else {
                        // PATCH /notes/{id}
                        val json = JSONObject().apply {
                            put("title", updatedNote.title)
                            put("note", if (updatedNote.isChecklist) {
                                updatedNote.checklist.joinToString("\n") { (if (it.isDone) "☑ " else "☐ ") + it.text }
                            } else updatedNote.content)
                            put("color", mapHexToApiColor(updatedNote.colorHex))
                            put("pinned", updatedNote.isPinned)
                        }
                        val request = Request.Builder()
                            .url("${apiBaseUrl.trimEnd('/')}/notes/${updatedNote.id}")
                            .addHeader("Authorization", "Bearer $apiAuthToken")
                            .addHeader("Content-Type", "application/json")
                            .patch(json.toString().toRequestBody(mediaType))
                            .build()

                        client.newCall(request).execute().close()
                    }
                } catch (e: Exception) {
                    Log.d("MorphRepository", "Remote saveNote notice: ${e.message}")
                }
            }
        }
    }

    /**
     * 4. NOTE DELETE KARNA (DELETE /notes/{id})
     */
    fun deleteNote(noteId: String) {
        val current = _notesFlow.value.filter { it.id != noteId }
        _notesFlow.value = current
        saveToDisk(current)

        if (isConnected && apiAuthToken.isNotBlank()) {
            coroutineScope.launch {
                try {
                    val request = Request.Builder()
                        .url("${apiBaseUrl.trimEnd('/')}/notes/$noteId")
                        .addHeader("Authorization", "Bearer $apiAuthToken")
                        .addHeader("Content-Type", "application/json")
                        .delete()
                        .build()

                    client.newCall(request).execute().close()
                } catch (e: Exception) {
                    Log.d("MorphRepository", "Remote deleteNote notice: ${e.message}")
                }
            }
        }
    }

    fun togglePin(noteId: String) {
        val current = _notesFlow.value.map {
            if (it.id == noteId) {
                val updated = it.copy(isPinned = !it.isPinned, updatedAt = System.currentTimeMillis())
                saveNote(updated)
                updated
            } else it
        }
        _notesFlow.value = current
        saveToDisk(current)
    }

    fun toggleChecklistItem(noteId: String, itemIndex: Int) {
        val current = _notesFlow.value.map { note ->
            if (note.id == noteId && itemIndex in note.checklist.indices) {
                val updatedChecklist = note.checklist.mapIndexed { i, item ->
                    if (i == itemIndex) item.copy(isDone = !item.isDone) else item
                }
                val updated = note.copy(checklist = updatedChecklist, updatedAt = System.currentTimeMillis())
                saveNote(updated)
                updated
            } else {
                note
            }
        }
        _notesFlow.value = current
        saveToDisk(current)
    }

    private fun getSeedNotes(): List<MorphNote> {
        val now = System.currentTimeMillis()
        return listOf(
            MorphNote(
                id = "seed_1",
                title = "Welcome to Omkun Morph",
                content = "Your private, lightweight notes & thoughts space right inside Olinam, connected to Morph Cloud API.\n\nKeep notes color-coded, pinned, and organized just like Google Keep.",
                colorHex = MorphPalette.Lemon,
                isPinned = true,
                labels = listOf("Omkun", "Intro"),
                updatedAt = now
            ),
            MorphNote(
                id = "seed_2",
                title = "Quick To-Do",
                content = "",
                isChecklist = true,
                checklist = listOf(
                    MorphChecklistItem(text = "Try Omkun Morph notes", isDone = true),
                    MorphChecklistItem(text = "Toggle on Morph Note inside Oj AI", isDone = true),
                    MorphChecklistItem(text = "Auto-sync notes without manual API setup", isDone = true)
                ),
                colorHex = MorphPalette.Mint,
                isPinned = true,
                labels = listOf("Tasks"),
                updatedAt = now - 60000
            ),
            MorphNote(
                id = "seed_3",
                title = "Ideas & Thoughts",
                content = "Notion-level architecture with Google Keep-level speed. Jot down quick reminders, drafts, and meeting notes seamlessly.",
                colorHex = MorphPalette.Sky,
                isPinned = false,
                labels = listOf("Ideas"),
                updatedAt = now - 120000
            )
        )
    }

    companion object {
        private const val KEY_NOTES_DATA = "morph_notes_data_v1"

        const val DEFAULT_MORPH_API_BASE_URL = "https://ais-dev-kps2metajswcv3lmn3d4iq-193817906370.asia-southeast1.run.app/api/v1"
        const val DEFAULT_MORPH_API_KEY = "omk_live_test_7f8a9b2c"

        fun mapHexToApiColor(hex: String): String {
            return when (hex.uppercase()) {
                MorphPalette.Lemon.uppercase() -> "yellow"
                MorphPalette.Sky.uppercase() -> "blue"
                MorphPalette.Peach.uppercase() -> "red"
                MorphPalette.Mint.uppercase() -> "green"
                MorphPalette.Orange.uppercase() -> "orange"
                MorphPalette.Lavender.uppercase() -> "purple"
                MorphPalette.Teal.uppercase() -> "teal"
                else -> "yellow"
            }
        }

        fun mapApiColorToHex(color: String?): String {
            return when (color?.lowercase()?.trim()) {
                "yellow" -> MorphPalette.Lemon
                "blue" -> MorphPalette.Sky
                "red" -> MorphPalette.Peach
                "green" -> MorphPalette.Mint
                "orange" -> MorphPalette.Orange
                "purple" -> MorphPalette.Lavender
                "teal" -> MorphPalette.Teal
                null -> MorphPalette.Default
                else -> {
                    if (color.startsWith("#")) color else MorphPalette.Default
                }
            }
        }

        @Volatile
        private var instance: MorphRepository? = null

        fun getInstance(context: Context): MorphRepository {
            return instance ?: synchronized(this) {
                instance ?: MorphRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
