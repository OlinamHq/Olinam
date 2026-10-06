package com.example.data

import android.util.Log
import com.example.crypto.EncryptionManager
import com.example.model.CallDirection
import com.example.model.CallLog
import com.example.model.CallType
import com.example.model.ChatLabel
import com.example.model.Conversation
import com.example.model.EncryptedPayload
import com.example.model.MediaType
import com.example.model.Message
import com.example.model.MessageStatus
import com.example.model.Story
import com.example.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class FirebaseChatRepository {

    private val tag = "FirebaseChatRepo"
    private val scope = CoroutineScope(Dispatchers.IO)

    private var firestore: FirebaseFirestore? = null
    private var auth: FirebaseAuth? = null

    // In-memory / cache states
    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _messages = MutableStateFlow<Map<String, List<Message>>>(emptyMap())
    val messages: StateFlow<Map<String, List<Message>>> = _messages.asStateFlow()

    private val _stories = MutableStateFlow<List<Story>>(emptyList())
    val stories: StateFlow<List<Story>> = _stories.asStateFlow()

    private val _callLogs = MutableStateFlow<List<CallLog>>(emptyList())
    val callLogs: StateFlow<List<CallLog>> = _callLogs.asStateFlow()

    private val defaultLabels = listOf(
        ChatLabel(id = "all", name = "Chats", colorHex = "#0160E3"),
        ChatLabel(id = "direct", name = "Direct", colorHex = "#0160E3"),
        ChatLabel(id = "groups", name = "Groups", colorHex = "#0160E3"),
        ChatLabel(id = "spam", name = "Spam", colorHex = "#EF4444")
    )
    private val _labels = MutableStateFlow<List<ChatLabel>>(defaultLabels)
    val labels: StateFlow<List<ChatLabel>> = _labels.asStateFlow()

    private val _currentUser = MutableStateFlow(
        UserProfile(
            id = "current_user_olinam",
            name = "Om Kun",
            phoneNumber = "+91 98765 43210",
            statusMessage = "Using Olinam with End-to-End Encryption 🔒"
        )
    )
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    private val _firebaseConnected = MutableStateFlow(false)
    val firebaseConnected: StateFlow<Boolean> = _firebaseConnected.asStateFlow()

    private val messageListeners = mutableMapOf<String, ListenerRegistration>()
    private var conversationsListener: ListenerRegistration? = null

    init {
        initFirebaseSafely()
        bootstrapInitialData()
    }

    private fun initFirebaseSafely() {
        try {
            auth = FirebaseAuth.getInstance()
            firestore = FirebaseFirestore.getInstance()
            _firebaseConnected.value = true
            Log.d(tag, "Firebase initialized successfully with project olinam-90d42")

            val currentAuthUser = auth?.currentUser
            if (currentAuthUser != null) {
                _currentUser.value = _currentUser.value.copy(
                    id = currentAuthUser.uid,
                    name = currentAuthUser.displayName ?: "Olinam User",
                    phoneNumber = currentAuthUser.phoneNumber ?: _currentUser.value.phoneNumber
                )
            }
            listenToConversations()
        } catch (e: Exception) {
            Log.w(tag, "Firebase initialization notice: ${e.message}. Using high-speed reactive local engine.")
            _firebaseConnected.value = false
        }
    }

    private fun bootstrapInitialData() {
        // Zero dummy data for clean enterprise production
        _conversations.value = emptyList()
        _messages.value = emptyMap()

        // Clean user stories
        _stories.value = listOf(
            Story(
                id = "story_me",
                userId = _currentUser.value.id,
                userName = "My status",
                caption = "Tap to add status update",
                timestamp = System.currentTimeMillis(),
                viewed = false
            )
        )

        // Empty call history ready for real calls
        _callLogs.value = emptyList()
    }

    fun createLabel(name: String, colorHex: String, chatIds: List<String>): String {
        val newId = "label_${UUID.randomUUID().toString().take(6)}"
        val newLabel = ChatLabel(
            id = newId,
            name = name,
            colorHex = colorHex,
            chatIds = chatIds
        )
        _labels.value = _labels.value + newLabel
        if (chatIds.isNotEmpty()) {
            _conversations.value = _conversations.value.map { conv ->
                if (chatIds.contains(conv.id) && !conv.labelIds.contains(newId)) {
                    conv.copy(labelIds = conv.labelIds + newId)
                } else conv
            }
        }
        return newId
    }

    private fun createEncryptedMessage(
        id: String,
        convId: String,
        senderId: String,
        senderName: String,
        text: String,
        timestamp: Long
    ): Message {
        val encryptedPayload = EncryptionManager.encrypt(text, convId)
        return Message(
            id = id,
            conversationId = convId,
            senderId = senderId,
            senderName = senderName,
            text = text,
            encryptedPayload = encryptedPayload,
            timestamp = timestamp,
            isEncrypted = true,
            status = MessageStatus.READ
        )
    }

    private fun listenToConversations() {
        val db = firestore ?: return
        try {
            conversationsListener = db.collection("conversations")
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(tag, "Firestore conversations listen failed: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && !snapshot.isEmpty) {
                        val remoteList = snapshot.documents.mapNotNull { doc ->
                            try {
                                Conversation(
                                    id = doc.id,
                                    title = doc.getString("title") ?: "Chat",
                                    isGroup = doc.getBoolean("isGroup") ?: false,
                                    lastMessageText = doc.getString("lastMessageText") ?: "",
                                    lastMessageTimestamp = doc.getLong("lastMessageTimestamp") ?: System.currentTimeMillis(),
                                    unreadCount = doc.getLong("unreadCount")?.toInt() ?: 0,
                                    isVerified = doc.getBoolean("isVerified") ?: false,
                                    isSystemAlert = doc.getBoolean("isSystemAlert") ?: false,
                                    iconType = doc.getString("iconType") ?: "USER"
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        _conversations.value = remoteList
                    }
                }
        } catch (e: Exception) {
            Log.w(tag, "Error setting up firestore listener: ${e.message}")
        }
    }

    fun listenToMessages(conversationId: String) {
        val db = firestore ?: return
        if (messageListeners.containsKey(conversationId)) return

        try {
            val listener = db.collection("conversations")
                .document(conversationId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(tag, "Messages listen failed: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && !snapshot.isEmpty) {
                        val remoteMessages = snapshot.documents.mapNotNull { doc ->
                            try {
                                val ciphertext = doc.getString("ciphertext") ?: ""
                                val iv = doc.getString("iv") ?: ""
                                val salt = doc.getString("salt") ?: ""
                                val algorithm = doc.getString("algorithm") ?: "AES-256-GCM"
                                val payload = EncryptedPayload(ciphertext, iv, salt, algorithm)
                                val decryptedText = if (ciphertext.isNotEmpty()) {
                                    EncryptionManager.decrypt(payload, conversationId)
                                } else {
                                    doc.getString("text") ?: ""
                                }

                                Message(
                                    id = doc.id,
                                    conversationId = conversationId,
                                    senderId = doc.getString("senderId") ?: "",
                                    senderName = doc.getString("senderName") ?: "User",
                                    text = decryptedText,
                                    encryptedPayload = payload,
                                    timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                    isEncrypted = doc.getBoolean("isEncrypted") ?: true,
                                    status = MessageStatus.valueOf(doc.getString("status") ?: "SENT")
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }

                        val currentMap = _messages.value.toMutableMap()
                        currentMap[conversationId] = remoteMessages
                        _messages.value = currentMap
                    }
                }
            messageListeners[conversationId] = listener
        } catch (e: Exception) {
            Log.w(tag, "Failed to listen to messages: ${e.message}")
        }
    }

    fun sendMessage(conversationId: String, text: String, mediaUrl: String? = null, mediaType: MediaType = MediaType.TEXT) {
        val currentUser = _currentUser.value
        val messageId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()

        // 1. Perform client-side End-to-End Encryption
        val encryptedPayload = EncryptionManager.encrypt(text, conversationId)

        val newMessage = Message(
            id = messageId,
            conversationId = conversationId,
            senderId = currentUser.id,
            senderName = currentUser.name,
            text = text, // decrypted view for client
            encryptedPayload = encryptedPayload,
            timestamp = timestamp,
            isEncrypted = true,
            status = MessageStatus.SENT,
            mediaUrl = mediaUrl,
            mediaType = mediaType
        )

        // 2. Immediately update local state for zero latency UI
        val currentMap = _messages.value.toMutableMap()
        val list = (currentMap[conversationId] ?: emptyList()).toMutableList()
        list.add(newMessage)
        currentMap[conversationId] = list
        _messages.value = currentMap

        // Update conversation last message
        val updatedConversations = _conversations.value.map { conv ->
            if (conv.id == conversationId) {
                conv.copy(
                    lastMessageText = text,
                    lastMessageTimestamp = timestamp,
                    unreadCount = 0
                )
            } else conv
        }
        _conversations.value = updatedConversations

        // 3. Persist encrypted payload to Firebase Firestore
        val db = firestore
        if (db != null) {
            scope.launch {
                try {
                    val firestoreDoc = hashMapOf(
                        "senderId" to currentUser.id,
                        "senderName" to currentUser.name,
                        "ciphertext" to encryptedPayload.ciphertext,
                        "iv" to encryptedPayload.iv,
                        "salt" to encryptedPayload.salt,
                        "algorithm" to encryptedPayload.algorithm,
                        "timestamp" to timestamp,
                        "isEncrypted" to true,
                        "status" to "SENT"
                    )

                    db.collection("conversations")
                        .document(conversationId)
                        .collection("messages")
                        .document(messageId)
                        .set(firestoreDoc)

                    db.collection("conversations")
                        .document(conversationId)
                        .update(
                            mapOf(
                                "lastMessageText" to "[Encrypted]",
                                "lastMessageTimestamp" to timestamp
                            )
                        )
                } catch (e: Exception) {
                    Log.w(tag, "Firestore write note: ${e.message}")
                }
            }
        }
    }

    fun createNewConversation(title: String, isGroup: Boolean, initialMessage: String = ""): String {
        val newId = "conv_${UUID.randomUUID().toString().take(8)}"
        val conv = Conversation(
            id = newId,
            title = title,
            isGroup = isGroup,
            lastMessageText = initialMessage.ifEmpty { if (isGroup) "Group created" else "New chat started" },
            lastMessageTimestamp = System.currentTimeMillis(),
            unreadCount = 0,
            iconType = if (isGroup) "GROUP" else "USER"
        )

        val updated = _conversations.value.toMutableList()
        // Put after system alerts or at top
        updated.add(2.coerceAtMost(updated.size), conv)
        _conversations.value = updated

        if (initialMessage.isNotEmpty()) {
            sendMessage(newId, initialMessage)
        }

        val db = firestore
        if (db != null) {
            scope.launch {
                try {
                    db.collection("conversations").document(newId).set(
                        mapOf(
                            "title" to title,
                            "isGroup" to isGroup,
                            "lastMessageText" to conv.lastMessageText,
                            "lastMessageTimestamp" to conv.lastMessageTimestamp,
                            "unreadCount" to 0,
                            "iconType" to conv.iconType
                        )
                    )
                } catch (e: Exception) {
                    Log.w(tag, "Firestore create conversation note: ${e.message}")
                }
            }
        }
        return newId
    }

    fun getOrCreateConversationForContact(name: String, phoneNumber: String, isSms: Boolean = false): String {
        val existing = _conversations.value.find {
            it.title.equals(name, ignoreCase = true) ||
                    (it.phoneNumber != null && it.phoneNumber == phoneNumber) ||
                    it.title.contains(phoneNumber)
        }
        if (existing != null) {
            return existing.id
        }

        val newId = "conv_${UUID.randomUUID().toString().take(8)}"
        val conv = Conversation(
            id = newId,
            title = name,
            isGroup = false,
            lastMessageText = if (isSms) "SMS Chat (Powered by Olinam)" else "Chat started",
            lastMessageTimestamp = System.currentTimeMillis(),
            unreadCount = 0,
            iconType = "USER",
            isSmsContact = isSms,
            phoneNumber = phoneNumber
        )

        val updated = _conversations.value.toMutableList()
        updated.add(0, conv)
        _conversations.value = updated

        val db = firestore
        if (db != null) {
            scope.launch {
                try {
                    db.collection("conversations").document(newId).set(
                        mapOf(
                            "title" to name,
                            "phoneNumber" to phoneNumber,
                            "isGroup" to false,
                            "isSmsContact" to isSms,
                            "lastMessageText" to conv.lastMessageText,
                            "lastMessageTimestamp" to conv.lastMessageTimestamp,
                            "unreadCount" to 0,
                            "iconType" to "USER"
                        )
                    )
                } catch (e: Exception) {
                    Log.w(tag, "Firestore create contact conversation note: ${e.message}")
                }
            }
        }
        return newId
    }

    fun addStory(caption: String) {
        val newStory = Story(
            id = UUID.randomUUID().toString(),
            userId = _currentUser.value.id,
            userName = "My status",
            caption = caption,
            timestamp = System.currentTimeMillis(),
            viewed = false
        )
        val list = _stories.value.toMutableList()
        list.add(0, newStory)
        _stories.value = list
    }

    fun addCallLog(contactName: String, type: CallType, direction: CallDirection) {
        val newCall = CallLog(
            id = UUID.randomUUID().toString(),
            contactName = contactName,
            callType = type,
            direction = direction,
            timestamp = System.currentTimeMillis(),
            duration = "00:32"
        )
        val list = _callLogs.value.toMutableList()
        list.add(0, newCall)
        _callLogs.value = list
    }

    fun updateProfile(name: String, status: String, phone: String) {
        _currentUser.value = _currentUser.value.copy(
            name = name,
            statusMessage = status,
            phoneNumber = phone
        )
    }

    fun markConversationAsRead(conversationId: String) {
        _conversations.value = _conversations.value.map {
            if (it.id == conversationId) it.copy(unreadCount = 0) else it
        }
    }

    fun mergeSmsConversations(personal: List<Conversation>, spam: List<Conversation>) {
        val existing = _conversations.value.toMutableList()
        val allSms = personal + spam

        for (smsConv in allSms) {
            val idx = existing.indexOfFirst {
                it.id == smsConv.id || (it.phoneNumber != null && it.phoneNumber == smsConv.phoneNumber)
            }
            if (idx >= 0) {
                existing[idx] = existing[idx].copy(
                    lastMessageText = smsConv.lastMessageText,
                    lastMessageTimestamp = smsConv.lastMessageTimestamp,
                    isSpam = smsConv.isSpam
                )
            } else {
                existing.add(smsConv)
            }
        }
        existing.sortByDescending { it.lastMessageTimestamp }
        _conversations.value = existing
    }

    fun setLocalMessages(conversationId: String, msgList: List<Message>) {
        val current = _messages.value.toMutableMap()
        val existing = current[conversationId] ?: emptyList()
        val combined = (msgList + existing).distinctBy { it.id }.sortedBy { it.timestamp }
        current[conversationId] = combined
        _messages.value = current
    }
}
