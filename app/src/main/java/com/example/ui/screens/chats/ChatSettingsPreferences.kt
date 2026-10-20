package com.example.ui.screens.chats

import android.content.Context
import android.content.SharedPreferences
import com.example.data.FirebaseChatRepository
import com.example.data.LocalChatDatabase
import com.example.model.Conversation
import com.example.model.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChatSettingsState(
    val enterToSend: Boolean = false,
    val quickReaction: Boolean = true,
    val quickReactionEmoji: String = "👍",
    val voiceMessageConfirmation: Boolean = false,
    val googleBackupEnabled: Boolean = false,
    val lastBackupTimestamp: Long = 0L,
    val lastBackupSizeFormatted: String = "0 KB",
    val backupAccount: String = "help@opendesh.com",
    val backupFrequency: String = "Daily",
    val backupOverCellular: Boolean = false,
    val includeVideos: Boolean = false,
    val mediaVisibility: Boolean = true,
    val chatWallpaper: String = "Default",
    val fontSize: String = "Medium",
    val isBackingUp: Boolean = false,
    val isRestoring: Boolean = false
)

class ChatSettingsPreferences(private val context: Context) {
    private val appContext = context.applicationContext
    private val prefs: SharedPreferences =
        appContext.getSharedPreferences("olinam_chat_settings_prefs", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(loadState())
    val state: StateFlow<ChatSettingsState> = _state.asStateFlow()

    private fun loadState(): ChatSettingsState {
        return ChatSettingsState(
            enterToSend = prefs.getBoolean("enter_to_send", false),
            quickReaction = prefs.getBoolean("quick_reaction", true),
            quickReactionEmoji = prefs.getString("quick_reaction_emoji", "👍") ?: "👍",
            voiceMessageConfirmation = prefs.getBoolean("voice_msg_confirmation", false),
            googleBackupEnabled = prefs.getBoolean("google_backup_enabled", false),
            lastBackupTimestamp = prefs.getLong("last_backup_timestamp", 0L),
            lastBackupSizeFormatted = prefs.getString("last_backup_size", "0 KB") ?: "0 KB",
            backupAccount = prefs.getString("backup_account", "help@opendesh.com") ?: "help@opendesh.com",
            backupFrequency = prefs.getString("backup_frequency", "Daily") ?: "Daily",
            backupOverCellular = prefs.getBoolean("backup_over_cellular", false),
            includeVideos = prefs.getBoolean("include_videos", false),
            mediaVisibility = prefs.getBoolean("media_visibility", true),
            chatWallpaper = prefs.getString("chat_wallpaper", "Default") ?: "Default",
            fontSize = prefs.getString("font_size", "Medium") ?: "Medium"
        )
    }

    fun setEnterToSend(enabled: Boolean) {
        prefs.edit().putBoolean("enter_to_send", enabled).apply()
        _state.value = _state.value.copy(enterToSend = enabled)
    }

    fun setQuickReaction(enabled: Boolean) {
        prefs.edit().putBoolean("quick_reaction", enabled).apply()
        _state.value = _state.value.copy(quickReaction = enabled)
    }

    fun setQuickReactionEmoji(emoji: String) {
        prefs.edit().putString("quick_reaction_emoji", emoji).apply()
        _state.value = _state.value.copy(quickReactionEmoji = emoji)
    }

    fun setVoiceMessageConfirmation(enabled: Boolean) {
        prefs.edit().putBoolean("voice_msg_confirmation", enabled).apply()
        _state.value = _state.value.copy(voiceMessageConfirmation = enabled)
    }

    fun setGoogleBackupEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("google_backup_enabled", enabled).apply()
        _state.value = _state.value.copy(googleBackupEnabled = enabled)
    }

    fun setBackupFrequency(freq: String) {
        prefs.edit().putString("backup_frequency", freq).apply()
        _state.value = _state.value.copy(backupFrequency = freq)
    }

    fun setBackupAccount(account: String) {
        prefs.edit().putString("backup_account", account).apply()
        _state.value = _state.value.copy(backupAccount = account)
    }

    fun setBackupOverCellular(enabled: Boolean) {
        prefs.edit().putBoolean("backup_over_cellular", enabled).apply()
        _state.value = _state.value.copy(backupOverCellular = enabled)
    }

    fun setIncludeVideos(enabled: Boolean) {
        prefs.edit().putBoolean("include_videos", enabled).apply()
        _state.value = _state.value.copy(includeVideos = enabled)
    }

    fun setMediaVisibility(enabled: Boolean) {
        prefs.edit().putBoolean("media_visibility", enabled).apply()
        _state.value = _state.value.copy(mediaVisibility = enabled)
    }

    fun setChatWallpaper(wallpaper: String) {
        prefs.edit().putString("chat_wallpaper", wallpaper).apply()
        _state.value = _state.value.copy(chatWallpaper = wallpaper)
    }

    fun setFontSize(size: String) {
        prefs.edit().putString("font_size", size).apply()
        _state.value = _state.value.copy(fontSize = size)
    }

    suspend fun performBackup(repository: FirebaseChatRepository): Boolean = withContext(Dispatchers.IO) {
        _state.value = _state.value.copy(isBackingUp = true)
        try {
            val localDb = LocalChatDatabase.getInstance(appContext)
            val conversations = localDb.getAllConversations().ifEmpty { repository.conversations.value }

            val backupRoot = JSONObject()
            backupRoot.put("version", 1)
            backupRoot.put("app", "Olinam")
            backupRoot.put("timestamp", System.currentTimeMillis())

            val convArray = JSONArray()
            for (conv in conversations) {
                val cObj = JSONObject().apply {
                    put("id", conv.id)
                    put("title", conv.title)
                    put("phoneNumber", conv.phoneNumber ?: "")
                    put("isGroup", conv.isGroup)
                    put("lastMessageText", conv.lastMessageText)
                    put("lastMessageTimestamp", conv.lastMessageTimestamp)
                    put("unreadCount", conv.unreadCount)
                    put("isVerified", conv.isVerified)
                    put("isSmsContact", conv.isSmsContact)
                    put("disappearingDuration", conv.disappearingMessagesDuration)
                }

                // Messages for this conversation
                val messages = localDb.getMessages(conv.id)
                val msgArray = JSONArray()
                for (m in messages) {
                    val mObj = JSONObject().apply {
                        put("id", m.id)
                        put("conversationId", m.conversationId)
                        put("senderId", m.senderId)
                        put("senderName", m.senderName)
                        put("text", m.text)
                        put("timestamp", m.timestamp)
                        put("isSms", m.isSms)
                        put("status", m.status.name)
                        put("mediaUrl", m.mediaUrl ?: "")
                        put("reaction", m.reaction ?: "")
                    }
                    msgArray.put(mObj)
                }
                cObj.put("messages", msgArray)
                convArray.put(cObj)
            }
            backupRoot.put("conversations", convArray)

            // Save to secure app storage backup file
            val backupDir = File(appContext.filesDir, "olinam_backups")
            if (!backupDir.exists()) backupDir.mkdirs()
            val backupFile = File(backupDir, "olinam_chat_backup.json")
            backupFile.writeText(backupRoot.toString(2))

            val sizeBytes = backupFile.length()
            val sizeFormatted = formatFileSize(sizeBytes)
            val now = System.currentTimeMillis()

            prefs.edit()
                .putLong("last_backup_timestamp", now)
                .putString("last_backup_size", sizeFormatted)
                .apply()

            _state.value = _state.value.copy(
                lastBackupTimestamp = now,
                lastBackupSizeFormatted = sizeFormatted,
                isBackingUp = false
            )
            true
        } catch (e: Exception) {
            _state.value = _state.value.copy(isBackingUp = false)
            false
        }
    }

    suspend fun performRestore(repository: FirebaseChatRepository): Boolean = withContext(Dispatchers.IO) {
        _state.value = _state.value.copy(isRestoring = true)
        try {
            val backupFile = File(File(appContext.filesDir, "olinam_backups"), "olinam_chat_backup.json")
            if (!backupFile.exists()) {
                _state.value = _state.value.copy(isRestoring = false)
                return@withContext false
            }

            val jsonContent = backupFile.readText()
            val backupRoot = JSONObject(jsonContent)
            val convArray = backupRoot.optJSONArray("conversations") ?: JSONArray()

            val localDb = LocalChatDatabase.getInstance(appContext)
            val restoredConvs = mutableListOf<Conversation>()

            for (i in 0 until convArray.length()) {
                val cObj = convArray.getJSONObject(i)
                val conv = Conversation(
                    id = cObj.getString("id"),
                    title = cObj.getString("title"),
                    phoneNumber = cObj.optString("phoneNumber", "").ifBlank { null },
                    isGroup = cObj.optBoolean("isGroup", false),
                    lastMessageText = cObj.optString("lastMessageText", ""),
                    lastMessageTimestamp = cObj.optLong("lastMessageTimestamp", System.currentTimeMillis()),
                    unreadCount = cObj.optInt("unreadCount", 0),
                    isVerified = cObj.optBoolean("isVerified", false),
                    isSmsContact = cObj.optBoolean("isSmsContact", false),
                    disappearingMessagesDuration = cObj.optString("disappearingDuration", "Off")
                )
                restoredConvs.add(conv)
                localDb.saveConversation(conv)

                val msgArray = cObj.optJSONArray("messages") ?: JSONArray()
                val restoredMsgs = mutableListOf<Message>()
                for (j in 0 until msgArray.length()) {
                    val mObj = msgArray.getJSONObject(j)
                    val status = runCatching {
                        com.example.model.MessageStatus.valueOf(mObj.optString("status", "READ"))
                    }.getOrDefault(com.example.model.MessageStatus.READ)

                    val msg = Message(
                        id = mObj.getString("id"),
                        conversationId = mObj.getString("conversationId"),
                        senderId = mObj.getString("senderId"),
                        senderName = mObj.optString("senderName", ""),
                        text = mObj.optString("text", ""),
                        timestamp = mObj.optLong("timestamp", System.currentTimeMillis()),
                        isSms = mObj.optBoolean("isSms", false),
                        status = status,
                        mediaUrl = mObj.optString("mediaUrl", "").ifBlank { null },
                        reaction = mObj.optString("reaction", "").ifBlank { null }
                    )
                    restoredMsgs.add(msg)
                }
                if (restoredMsgs.isNotEmpty()) {
                    localDb.saveMessages(conv.id, restoredMsgs)
                }
            }

            if (restoredConvs.isNotEmpty()) {
                repository.initLocalDb(appContext)
            }

            _state.value = _state.value.copy(isRestoring = false)
            true
        } catch (e: Exception) {
            _state.value = _state.value.copy(isRestoring = false)
            false
        }
    }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> String.format(Locale.US, "%.1f MB", bytes.toDouble() / (1024 * 1024))
        }
    }

    fun formatBackupDate(timestamp: Long): String {
        if (timestamp <= 0L) return "Never"
        val sdf = SimpleDateFormat("d MMM yyyy, h:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun getBackupFile(): File? {
        val f = File(File(appContext.filesDir, "olinam_backups"), "olinam_chat_backup.json")
        return if (f.exists()) f else null
    }
}
