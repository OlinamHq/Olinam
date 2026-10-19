package com.example.data

import android.content.Context
import com.example.model.MorphChecklistItem
import com.example.model.MorphNote
import com.example.model.MorphPalette
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

/**
 * Repository for Omkun Morph notes.
 * Provides offline local persistence via SharedPreferences + prepared REST API architecture
 * so the user can easily plug in their real Omkun Morph backend API URL and Token later.
 */
class MorphRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("omkun_morph_prefs", Context.MODE_PRIVATE)
    private val _notesFlow = MutableStateFlow<List<MorphNote>>(emptyList())
    val notesFlow: StateFlow<List<MorphNote>> = _notesFlow.asStateFlow()

    // Configurable Omkun Morph API endpoints
    var apiBaseUrl: String
        get() = prefs.getString("morph_api_base_url", "https://api.morph.omkun.com/v1") ?: "https://api.morph.omkun.com/v1"
        set(value) = prefs.edit().putString("morph_api_base_url", value).apply()

    var apiAuthToken: String
        get() = prefs.getString("morph_api_token", "") ?: ""
        set(value) = prefs.edit().putString("morph_api_token", value).apply()

    init {
        loadNotes()
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

    fun saveNote(note: MorphNote) {
        val current = _notesFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == note.id }
        val updatedNote = note.copy(updatedAt = System.currentTimeMillis())
        if (index >= 0) {
            current[index] = updatedNote
        } else {
            current.add(0, updatedNote)
        }
        _notesFlow.value = current
        saveToDisk(current)
    }

    fun deleteNote(noteId: String) {
        val current = _notesFlow.value.filter { it.id != noteId }
        _notesFlow.value = current
        saveToDisk(current)
    }

    fun togglePin(noteId: String) {
        val current = _notesFlow.value.map {
            if (it.id == noteId) it.copy(isPinned = !it.isPinned, updatedAt = System.currentTimeMillis())
            else it
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
                note.copy(checklist = updatedChecklist, updatedAt = System.currentTimeMillis())
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
                content = "Your private, lightweight notes & thoughts space right inside Olinam, powered by Omkun Morph.\n\nKeep notes color-coded, pinned, and organized just like Google Keep.",
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
                    MorphChecklistItem(text = "Create a custom checklist note", isDone = false),
                    MorphChecklistItem(text = "Pin important reminders to the top", isDone = false)
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

        @Volatile
        private var instance: MorphRepository? = null

        fun getInstance(context: Context): MorphRepository {
            return instance ?: synchronized(this) {
                instance ?: MorphRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
