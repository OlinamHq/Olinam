package com.example.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.model.Conversation
import com.example.model.Message
import com.example.model.MessageStatus
import java.util.Collections

/**
 * High-performance, zero-latency local SQLite database for Olinam.
 * Persists all chats, SMS messages, and deleted states locally so app opens instantly (0ms)
 * without waiting for background ContentResolver or cloud queries.
 */
class LocalChatDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "olinam_local_chat.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_CONVERSATIONS = "conversations"
        private const val TABLE_MESSAGES = "messages"
        private const val TABLE_DELETED_ITEMS = "deleted_items"

        @Volatile
        private var instance: LocalChatDatabase? = null

        fun getInstance(context: Context): LocalChatDatabase {
            return instance ?: synchronized(this) {
                instance ?: LocalChatDatabase(context.applicationContext).also { instance = it }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_CONVERSATIONS (
                id TEXT PRIMARY KEY,
                title TEXT,
                phoneNumber TEXT,
                isGroup INTEGER,
                lastMessageText TEXT,
                lastMessageTimestamp INTEGER,
                unreadCount INTEGER,
                isVerified INTEGER,
                isSystemAlert INTEGER,
                isSmsContact INTEGER,
                isE2EE INTEGER,
                isSpam INTEGER,
                onlineStatus TEXT,
                iconType TEXT,
                labelIds TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_MESSAGES (
                id TEXT PRIMARY KEY,
                conversationId TEXT,
                senderId TEXT,
                senderName TEXT,
                text TEXT,
                timestamp INTEGER,
                isEncrypted INTEGER,
                status TEXT,
                mediaUrl TEXT,
                mediaType TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_DELETED_ITEMS (
                itemId TEXT PRIMARY KEY,
                itemType TEXT,
                deletedAt INTEGER
            )
            """.trimIndent()
        )

        db.execSQL("CREATE INDEX IF NOT EXISTS idx_msg_conv ON $TABLE_MESSAGES (conversationId, timestamp)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Migration logic if schema changes
    }

    // --- CONVERSATIONS ---

    fun saveConversation(conv: Conversation) {
        if (isItemDeleted(conv.id)) return
        val db = writableDatabase
        val cv = ContentValues().apply {
            put("id", conv.id)
            put("title", conv.title)
            put("phoneNumber", conv.phoneNumber ?: "")
            put("isGroup", if (conv.isGroup) 1 else 0)
            put("lastMessageText", conv.lastMessageText)
            put("lastMessageTimestamp", conv.lastMessageTimestamp)
            put("unreadCount", conv.unreadCount)
            put("isVerified", if (conv.isVerified) 1 else 0)
            put("isSystemAlert", if (conv.isSystemAlert) 1 else 0)
            put("isSmsContact", if (conv.isSmsContact) 1 else 0)
            put("isE2EE", if (conv.isE2EE) 1 else 0)
            put("isSpam", if (conv.isSpam) 1 else 0)
            put("onlineStatus", conv.onlineStatus ?: "")
            put("iconType", conv.iconType)
            put("labelIds", conv.labelIds.joinToString(","))
        }
        db.insertWithOnConflict(TABLE_CONVERSATIONS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun saveConversations(list: List<Conversation>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            for (conv in list) {
                if (isItemDeleted(conv.id)) continue
                val cv = ContentValues().apply {
                    put("id", conv.id)
                    put("title", conv.title)
                    put("phoneNumber", conv.phoneNumber ?: "")
                    put("isGroup", if (conv.isGroup) 1 else 0)
                    put("lastMessageText", conv.lastMessageText)
                    put("lastMessageTimestamp", conv.lastMessageTimestamp)
                    put("unreadCount", conv.unreadCount)
                    put("isVerified", if (conv.isVerified) 1 else 0)
                    put("isSystemAlert", if (conv.isSystemAlert) 1 else 0)
                    put("isSmsContact", if (conv.isSmsContact) 1 else 0)
                    put("isE2EE", if (conv.isE2EE) 1 else 0)
                    put("isSpam", if (conv.isSpam) 1 else 0)
                    put("onlineStatus", conv.onlineStatus ?: "")
                    put("iconType", conv.iconType)
                    put("labelIds", conv.labelIds.joinToString(","))
                }
                db.insertWithOnConflict(TABLE_CONVERSATIONS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getAllConversations(): List<Conversation> {
        val result = mutableListOf<Conversation>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_CONVERSATIONS ORDER BY lastMessageTimestamp DESC",
            null
        )
        cursor.use {
            val idIdx = it.getColumnIndex("id")
            val titleIdx = it.getColumnIndex("title")
            val phoneIdx = it.getColumnIndex("phoneNumber")
            val isGroupIdx = it.getColumnIndex("isGroup")
            val lastMsgIdx = it.getColumnIndex("lastMessageText")
            val lastTimeIdx = it.getColumnIndex("lastMessageTimestamp")
            val unreadIdx = it.getColumnIndex("unreadCount")
            val verifiedIdx = it.getColumnIndex("isVerified")
            val alertIdx = it.getColumnIndex("isSystemAlert")
            val smsIdx = it.getColumnIndex("isSmsContact")
            val e2eeIdx = it.getColumnIndex("isE2EE")
            val spamIdx = it.getColumnIndex("isSpam")
            val statusIdx = it.getColumnIndex("onlineStatus")
            val iconIdx = it.getColumnIndex("iconType")
            val labelsIdx = it.getColumnIndex("labelIds")

            while (it.moveToNext()) {
                val id = it.getString(idIdx)
                if (isItemDeleted(id)) continue

                val labelIdsStr = if (labelsIdx >= 0) it.getString(labelsIdx) ?: "" else ""
                val labelIds = if (labelIdsStr.isNotBlank()) labelIdsStr.split(",") else emptyList()

                result.add(
                    Conversation(
                        id = id,
                        title = if (titleIdx >= 0) it.getString(titleIdx) ?: "Chat" else "Chat",
                        phoneNumber = if (phoneIdx >= 0) it.getString(phoneIdx).ifBlank { null } else null,
                        isGroup = if (isGroupIdx >= 0) it.getInt(isGroupIdx) == 1 else false,
                        lastMessageText = if (lastMsgIdx >= 0) it.getString(lastMsgIdx) ?: "" else "",
                        lastMessageTimestamp = if (lastTimeIdx >= 0) it.getLong(lastTimeIdx) else System.currentTimeMillis(),
                        unreadCount = if (unreadIdx >= 0) it.getInt(unreadIdx) else 0,
                        isVerified = if (verifiedIdx >= 0) it.getInt(verifiedIdx) == 1 else false,
                        isSystemAlert = if (alertIdx >= 0) it.getInt(alertIdx) == 1 else false,
                        isSmsContact = if (smsIdx >= 0) it.getInt(smsIdx) == 1 else false,
                        isE2EE = if (e2eeIdx >= 0) it.getInt(e2eeIdx) == 1 else true,
                        isSpam = if (spamIdx >= 0) it.getInt(spamIdx) == 1 else false,
                        onlineStatus = if (statusIdx >= 0) it.getString(statusIdx).ifBlank { null } else null,
                        iconType = if (iconIdx >= 0) it.getString(iconIdx) ?: "USER" else "USER",
                        labelIds = labelIds
                    )
                )
            }
        }
        return result
    }

    fun deleteConversation(convId: String) {
        val db = writableDatabase
        markItemDeleted(convId, "conversation")
        db.delete(TABLE_CONVERSATIONS, "id = ?", arrayOf(convId))
        db.delete(TABLE_MESSAGES, "conversationId = ?", arrayOf(convId))
    }

    // --- MESSAGES ---

    fun saveMessage(msg: Message) {
        if (isItemDeleted(msg.id)) return
        val db = writableDatabase
        val cv = ContentValues().apply {
            put("id", msg.id)
            put("conversationId", msg.conversationId)
            put("senderId", msg.senderId)
            put("senderName", msg.senderName)
            put("text", msg.text)
            put("timestamp", msg.timestamp)
            put("isEncrypted", if (msg.isEncrypted) 1 else 0)
            put("status", msg.status.name)
            put("mediaUrl", msg.mediaUrl ?: "")
            put("mediaType", msg.mediaType?.name ?: "")
        }
        db.insertWithOnConflict(TABLE_MESSAGES, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun saveMessages(conversationId: String, messages: List<Message>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            for (msg in messages) {
                if (isItemDeleted(msg.id)) continue
                val cv = ContentValues().apply {
                    put("id", msg.id)
                    put("conversationId", conversationId)
                    put("senderId", msg.senderId)
                    put("senderName", msg.senderName)
                    put("text", msg.text)
                    put("timestamp", msg.timestamp)
                    put("isEncrypted", if (msg.isEncrypted) 1 else 0)
                    put("status", msg.status.name)
                    put("mediaUrl", msg.mediaUrl ?: "")
                    put("mediaType", msg.mediaType?.name ?: "")
                }
                db.insertWithOnConflict(TABLE_MESSAGES, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getMessages(conversationId: String): List<Message> {
        val result = mutableListOf<Message>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_MESSAGES WHERE conversationId = ? ORDER BY timestamp ASC",
            arrayOf(conversationId)
        )
        cursor.use {
            val idIdx = it.getColumnIndex("id")
            val sIdIdx = it.getColumnIndex("senderId")
            val sNameIdx = it.getColumnIndex("senderName")
            val textIdx = it.getColumnIndex("text")
            val timeIdx = it.getColumnIndex("timestamp")
            val encIdx = it.getColumnIndex("isEncrypted")
            val statusIdx = it.getColumnIndex("status")
            val mediaUrlIdx = it.getColumnIndex("mediaUrl")
            val mediaTypeIdx = it.getColumnIndex("mediaType")

            while (it.moveToNext()) {
                val id = it.getString(idIdx)
                if (isItemDeleted(id)) continue

                val statusStr = if (statusIdx >= 0) it.getString(statusIdx) else "READ"
                val status = try {
                    MessageStatus.valueOf(statusStr)
                } catch (_: Exception) {
                    MessageStatus.READ
                }

                val mediaUrl = if (mediaUrlIdx >= 0) it.getString(mediaUrlIdx).ifBlank { null } else null
                val mediaTypeStr = if (mediaTypeIdx >= 0) it.getString(mediaTypeIdx).ifBlank { null } else null
                val mediaType = mediaTypeStr?.let { str ->
                    try { com.example.model.MediaType.valueOf(str) } catch (_: Exception) { null }
                } ?: com.example.model.MediaType.TEXT

                result.add(
                    Message(
                        id = id,
                        conversationId = conversationId,
                        senderId = if (sIdIdx >= 0) it.getString(sIdIdx) else "",
                        senderName = if (sNameIdx >= 0) it.getString(sNameIdx) else "",
                        text = if (textIdx >= 0) it.getString(textIdx) ?: "" else "",
                        timestamp = if (timeIdx >= 0) it.getLong(timeIdx) else System.currentTimeMillis(),
                        isEncrypted = if (encIdx >= 0) it.getInt(encIdx) == 1 else false,
                        status = status,
                        mediaUrl = mediaUrl,
                        mediaType = mediaType
                    )
                )
            }
        }
        return result
    }

    fun deleteMessage(convId: String, messageId: String): Message? {
        val db = writableDatabase
        markItemDeleted(messageId, "message")
        db.delete(TABLE_MESSAGES, "id = ?", arrayOf(messageId))

        // Get remaining latest message
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_MESSAGES WHERE conversationId = ? ORDER BY timestamp DESC LIMIT 1",
            arrayOf(convId)
        )
        cursor.use {
            if (it.moveToFirst()) {
                val textIdx = it.getColumnIndex("text")
                val timeIdx = it.getColumnIndex("timestamp")
                val lastText = if (textIdx >= 0) it.getString(textIdx) ?: "" else ""
                val lastTime = if (timeIdx >= 0) it.getLong(timeIdx) else System.currentTimeMillis()

                val cv = ContentValues().apply {
                    put("lastMessageText", lastText)
                    put("lastMessageTimestamp", lastTime)
                }
                db.update(TABLE_CONVERSATIONS, cv, "id = ?", arrayOf(convId))
            } else {
                val cv = ContentValues().apply {
                    put("lastMessageText", "No messages")
                }
                db.update(TABLE_CONVERSATIONS, cv, "id = ?", arrayOf(convId))
            }
        }
        return null
    }

    fun clearMessages(convId: String) {
        val db = writableDatabase
        db.delete(TABLE_MESSAGES, "conversationId = ?", arrayOf(convId))
        val cv = ContentValues().apply {
            put("lastMessageText", "Chat cleared")
        }
        db.update(TABLE_CONVERSATIONS, cv, "id = ?", arrayOf(convId))
    }

    // --- DELETED ITEMS TRACKING ---

    fun markItemDeleted(itemId: String, type: String) {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put("itemId", itemId)
            put("itemType", type)
            put("deletedAt", System.currentTimeMillis())
        }
        db.insertWithOnConflict(TABLE_DELETED_ITEMS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun isItemDeleted(itemId: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT 1 FROM $TABLE_DELETED_ITEMS WHERE itemId = ? LIMIT 1",
            arrayOf(itemId)
        )
        cursor.use {
            return it.moveToFirst()
        }
    }
}
