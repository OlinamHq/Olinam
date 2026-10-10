package com.example.data

import android.content.Context
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
        ChatLabel(id = "direct", name = "Chats", colorHex = "#0160E3"),
        ChatLabel(id = "groups", name = "Groups", colorHex = "#0160E3"),
        ChatLabel(id = "sms", name = "SMS", colorHex = "#00A884")
    )
    private val _labels = MutableStateFlow<List<ChatLabel>>(defaultLabels)
    val labels: StateFlow<List<ChatLabel>> = _labels.asStateFlow()

    private var localDb: LocalChatDatabase? = null

    fun initLocalDb(context: Context) {
        if (localDb == null) {
            val db = LocalChatDatabase.getInstance(context)
            localDb = db
            val cached = db.getAllConversations()
            if (cached.isNotEmpty()) {
                _conversations.value = cached
            }
        }
    }

    private val _currentUser = MutableStateFlow(
        UserProfile(
            id = "user_me",
            name = "",
            phoneNumber = "",
            email = "",
            isLoggedIn = false,
            statusMessage = "Using Olinam with End-to-End Encryption 🔒"
        )
    )
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    private val _firebaseConnected = MutableStateFlow(false)
    val firebaseConnected: StateFlow<Boolean> = _firebaseConnected.asStateFlow()

    private val _registeredPhoneNumbers = MutableStateFlow<Set<String>>(emptySet())
    val registeredPhoneNumbers: StateFlow<Set<String>> = _registeredPhoneNumbers.asStateFlow()

    private var usersListener: ListenerRegistration? = null
    private val messageListeners = mutableMapOf<String, ListenerRegistration>()
    private var conversationsListener: ListenerRegistration? = null

    init {
        initFirebaseSafely()
        bootstrapInitialData()
    }

    fun initFirebaseSafely(context: Context? = null) {
        try {
            if (context != null) {
                initLocalDb(context)
                com.example.auth.PhoneAuthManager.ensureFirebaseInitialized(context)
            }
            auth = FirebaseAuth.getInstance()
            firestore = FirebaseFirestore.getInstance()
            _firebaseConnected.value = true
            Log.d(tag, "Firebase initialized successfully with project olinam-90d42")

            val currentAuthUser = auth?.currentUser
            if (currentAuthUser != null) {
                _currentUser.value = _currentUser.value.copy(
                    id = currentAuthUser.uid,
                    name = currentAuthUser.displayName ?: "Olinam User",
                    email = currentAuthUser.email ?: "",
                    phoneNumber = currentAuthUser.phoneNumber ?: "",
                    isLoggedIn = true
                )
            }
            listenToConversations()
            listenToRegisteredUsers()
        } catch (e: Exception) {
            Log.w(tag, "Firebase initialization notice: ${e.message}. Using high-speed reactive local engine.")
            _firebaseConnected.value = false
        }
    }

    private fun listenToRegisteredUsers() {
        val db = firestore ?: return
        try {
            usersListener?.remove()
            usersListener = db.collection("users").addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val set = mutableSetOf<String>()
                for (doc in snapshot.documents) {
                    val phone = doc.getString("phoneNumber") ?: ""
                    val cleanPhone = doc.getString("cleanPhoneNumber") ?: ""
                    if (phone.isNotBlank()) {
                        set.add(phone.trim())
                        val digits = phone.filter { it.isDigit() }
                        if (digits.isNotBlank()) {
                            set.add(digits)
                            set.add(digits.takeLast(10))
                        }
                    }
                    if (cleanPhone.isNotBlank()) {
                        set.add(cleanPhone.trim())
                        set.add(cleanPhone.takeLast(10))
                    }
                }
                _registeredPhoneNumbers.value = set
                // Automatically re-evaluate existing conversations app status
                updateAllConversationsAppStatus()
            }
        } catch (e: Exception) {
            Log.w(tag, "Users listen note: ${e.message}")
        }
    }

    fun isPhoneNumberRegistered(phoneNumber: String): Boolean {
        if (phoneNumber.isBlank()) return false
        val cleanNumber = phoneNumber.trim()
        val digits = cleanNumber.filter { it.isDigit() }
        val last10 = digits.takeLast(10)
        val registered = _registeredPhoneNumbers.value
        return registered.contains(cleanNumber) ||
                registered.contains(digits) ||
                (last10.length >= 7 && registered.contains(last10))
    }

    suspend fun verifyPhoneNumberRegistered(phoneNumber: String): Boolean {
        if (isPhoneNumberRegistered(phoneNumber)) return true
        val db = firestore ?: return false
        return kotlinx.coroutines.suspendCancellableCoroutine { cont ->
            val cleanNumber = phoneNumber.trim()
            val digits = cleanNumber.filter { it.isDigit() }
            val last10 = digits.takeLast(10)

            db.collection("users")
                .whereEqualTo("phoneNumber", cleanNumber)
                .limit(1)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val set = _registeredPhoneNumbers.value.toMutableSet()
                        set.add(cleanNumber)
                        if (digits.isNotEmpty()) {
                            set.add(digits)
                            set.add(last10)
                        }
                        _registeredPhoneNumbers.value = set
                        if (cont.isActive) cont.resume(true) {}
                    } else if (last10.length >= 7) {
                        db.collection("users")
                            .whereEqualTo("cleanPhoneNumber", last10)
                            .limit(1)
                            .get()
                            .addOnSuccessListener { subQuery ->
                                val found = !subQuery.isEmpty
                                if (found) {
                                    val set = _registeredPhoneNumbers.value.toMutableSet()
                                    set.add(cleanNumber)
                                    set.add(last10)
                                    _registeredPhoneNumbers.value = set
                                }
                                if (cont.isActive) cont.resume(found) {}
                            }
                            .addOnFailureListener {
                                if (cont.isActive) cont.resume(false) {}
                            }
                    } else {
                        if (cont.isActive) cont.resume(false) {}
                    }
                }
                .addOnFailureListener {
                    if (cont.isActive) cont.resume(false) {}
                }
        }
    }

    private fun updateAllConversationsAppStatus() {
        val current = _conversations.value
        val updated = current.map { conv ->
            val phone = conv.phoneNumber ?: conv.title.filter { it.isDigit() || it == '+' }
            if (phone.isNotBlank() && !conv.isGroup) {
                val hasApp = isPhoneNumberRegistered(phone)
                conv.copy(
                    isSmsContact = !hasApp,
                    isE2EE = hasApp,
                    onlineStatus = if (hasApp) "End-to-End Encrypted 🔒" else "Cellular SMS (No Olinam app installed)"
                )
            } else {
                conv
            }
        }
        if (updated != current) {
            _conversations.value = updated
            localDb?.saveConversations(updated)
        }
    }

    fun verifyAndUpdateConversationAppStatus(conversationId: String) {
        val conv = _conversations.value.find { it.id == conversationId } ?: return
        val phone = conv.phoneNumber ?: conv.title.filter { it.isDigit() || it == '+' }
        if (phone.isBlank() || conv.isGroup) return

        scope.launch {
            val hasApp = verifyPhoneNumberRegistered(phone)
            val updated = _conversations.value.map { item ->
                if (item.id == conversationId) {
                    item.copy(
                        isSmsContact = !hasApp,
                        isE2EE = hasApp,
                        onlineStatus = if (hasApp) "End-to-End Encrypted 🔒" else "Cellular SMS (No Olinam app installed)"
                    )
                } else item
            }
            _conversations.value = updated
        }
    }

    private fun bootstrapInitialData() {
        // Zero dummy data: load local SQLite persistence for instant startup
        _callLogs.value = emptyList()
        val cached = localDb?.getAllConversations() ?: emptyList()
        _conversations.value = cached
        _messages.value = emptyMap()
        _stories.value = emptyList()
    }

    fun loginUser(name: String, phoneNumber: String, email: String = "", uid: String? = null) {
        val currentId = _currentUser.value.id
        val finalUid = uid?.ifBlank { null }
            ?: (if (currentId.isNotBlank() && currentId != "user_me") currentId else "user_${UUID.randomUUID().toString().take(8)}")
        _currentUser.value = _currentUser.value.copy(
            id = finalUid,
            name = name.ifBlank { "You" },
            phoneNumber = phoneNumber,
            email = email,
            isLoggedIn = true
        )
    }

    fun logoutUser() {
        try {
            auth?.signOut()
        } catch (_: Exception) {}
        _currentUser.value = UserProfile(
            id = "user_me",
            name = "",
            phoneNumber = "",
            email = "",
            isLoggedIn = false
        )
        _conversations.value = emptyList()
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

    private var lastRemoteConversations: List<Conversation> = emptyList()
    private var lastSmsConversations: List<Conversation> = emptyList()

    private fun updateCombinedConversations() {
        val dbList = localDb?.getAllConversations() ?: emptyList()
        val existing = (dbList + lastRemoteConversations).distinctBy { it.id }.toMutableList()
        for (smsConv in lastSmsConversations) {
            if (localDb?.isItemDeleted(smsConv.id) == true) continue
            val idx = existing.indexOfFirst {
                it.id == smsConv.id || (it.phoneNumber != null && it.phoneNumber == smsConv.phoneNumber)
            }
            if (idx >= 0) {
                existing[idx] = existing[idx].copy(
                    lastMessageText = smsConv.lastMessageText,
                    lastMessageTimestamp = smsConv.lastMessageTimestamp,
                    isSpam = false,
                    labelIds = if (existing[idx].labelIds.contains("sms")) existing[idx].labelIds else existing[idx].labelIds + "sms"
                )
            } else {
                existing.add(smsConv)
            }
        }
        existing.sortByDescending { it.lastMessageTimestamp }
        _conversations.value = existing
        localDb?.saveConversations(existing)
    }

    private fun listenToConversations() {
        val db = firestore ?: return
        try {
            conversationsListener = db.collection("conversations")
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
                .limit(100)
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
                        lastRemoteConversations = remoteList
                        updateCombinedConversations()
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
                .limitToLast(100)
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
                val newLabels = if (!conv.isGroup && !conv.labelIds.contains("direct")) conv.labelIds + "direct" else conv.labelIds
                conv.copy(
                    lastMessageText = text,
                    lastMessageTimestamp = timestamp,
                    unreadCount = 0,
                    labelIds = newLabels
                )
            } else conv
        }
        _conversations.value = updatedConversations
        localDb?.saveMessage(newMessage)
        localDb?.saveConversations(updatedConversations)

        // 2. Persist encrypted payload to Google Cloud Firebase Firestore
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
        updated.add(0, conv)
        _conversations.value = updated
        localDb?.saveConversation(conv)

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
            // Ensure this conversation appears in Chats tab by adding "direct" label
            if (!existing.labelIds.contains("direct")) {
                val updatedLabels = existing.labelIds + "direct"
                val updatedConv = existing.copy(labelIds = updatedLabels)
                val list = _conversations.value.toMutableList()
                val idx = list.indexOfFirst { it.id == existing.id }
                if (idx >= 0) {
                    list[idx] = updatedConv
                    _conversations.value = list
                    localDb?.saveConversation(updatedConv)
                }
            }
            // Verify if user registered since last seen
            verifyAndUpdateConversationAppStatus(existing.id)
            return existing.id
        }

        val hasApp = isPhoneNumberRegistered(phoneNumber)
        val finalIsSms = isSms && !hasApp

        val newId = "conv_${UUID.randomUUID().toString().take(8)}"
        val conv = Conversation(
            id = newId,
            title = name,
            isGroup = false,
            lastMessageText = if (finalIsSms) "SMS Chat (Standard SIM Text)" else "End-to-End Encrypted Chat started",
            lastMessageTimestamp = System.currentTimeMillis(),
            unreadCount = 0,
            iconType = "USER",
            isSmsContact = finalIsSms,
            isE2EE = !finalIsSms,
            onlineStatus = if (!finalIsSms) "End-to-End Encrypted 🔒" else "Cellular SMS (No Olinam app installed)",
            phoneNumber = phoneNumber,
            labelIds = if (finalIsSms) listOf("direct", "sms") else listOf("direct")
        )

        val updated = _conversations.value.toMutableList()
        updated.add(0, conv)
        _conversations.value = updated
        localDb?.saveConversation(conv)

        // Verify asynchronously in case phone was just registered
        if (phoneNumber.isNotBlank()) {
            verifyAndUpdateConversationAppStatus(newId)
        }

        val db = firestore
        if (db != null) {
            scope.launch {
                try {
                    db.collection("conversations").document(newId).set(
                        mapOf(
                            "title" to name,
                            "phoneNumber" to phoneNumber,
                            "isGroup" to false,
                            "isSmsContact" to finalIsSms,
                            "isE2EE" to !finalIsSms,
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

    fun sendDirectSmsMessage(
        conversationId: String,
        text: String,
        destinationPhone: String,
        context: android.content.Context
    ): Boolean {
        if (text.isBlank() || destinationPhone.isBlank()) return false
        val success = SmsHelper.sendDirectSms(context, destinationPhone, text)

        val messageId = "sms_sent_${UUID.randomUUID().toString().take(8)}"
        val timestamp = System.currentTimeMillis()
        val currentUser = _currentUser.value

        val newMessage = Message(
            id = messageId,
            conversationId = conversationId,
            senderId = currentUser.id,
            senderName = currentUser.name.ifBlank { "You" },
            text = text,
            isEncrypted = false,
            isSms = true,
            status = if (success) MessageStatus.SENT else MessageStatus.SENDING,
            timestamp = timestamp
        )

        // 1. Instantly append to active messages
        val currentMap = _messages.value.toMutableMap()
        val list = (currentMap[conversationId] ?: emptyList()).toMutableList()
        list.add(newMessage)
        currentMap[conversationId] = list
        _messages.value = currentMap

        // 2. Update conversation snippet
        val updatedConversations = _conversations.value.map { conv ->
            if (conv.id == conversationId) {
                val newLabels = if (!conv.isGroup && !conv.labelIds.contains("direct")) conv.labelIds + "direct" else conv.labelIds
                conv.copy(
                    lastMessageText = text,
                    lastMessageTimestamp = timestamp,
                    unreadCount = 0,
                    labelIds = newLabels
                )
            } else conv
        }
        _conversations.value = updatedConversations
        localDb?.saveMessage(newMessage)
        localDb?.saveConversations(updatedConversations)
        return success
    }

    fun addStory(caption: String, mediaUrl: String? = null, isImage: Boolean = false) {
        val newStory = Story(
            id = UUID.randomUUID().toString(),
            userId = _currentUser.value.id,
            userName = _currentUser.value.name.ifBlank { "My status" },
            caption = caption,
            timestamp = System.currentTimeMillis(),
            mediaUrl = mediaUrl,
            isImageStory = isImage,
            viewed = false
        )
        val list = _stories.value.toMutableList()
        list.add(0, newStory)
        _stories.value = list

        val db = firestore
        if (db != null) {
            scope.launch {
                try {
                    db.collection("stories").document(newStory.id).set(
                        mapOf(
                            "userName" to newStory.userName,
                            "caption" to caption,
                            "timestamp" to newStory.timestamp,
                            "mediaUrl" to (mediaUrl ?: ""),
                            "isImageStory" to isImage
                        )
                    )
                } catch (e: Exception) {
                    Log.w(tag, "Firestore story note: ${e.message}")
                }
            }
        }
    }

    fun addCallLog(
        contactName: String,
        phoneNumber: String = "",
        subtitleNote: String? = null,
        type: CallType = CallType.VOICE,
        direction: CallDirection = CallDirection.OUTGOING,
        duration: String = "00:32"
    ) {
        val newCall = CallLog(
            id = UUID.randomUUID().toString(),
            contactName = contactName,
            phoneNumber = phoneNumber,
            subtitleNote = subtitleNote,
            callType = type,
            direction = direction,
            timestamp = System.currentTimeMillis(),
            duration = duration
        )
        val list = _callLogs.value.toMutableList()
        list.add(0, newCall)
        _callLogs.value = list

        val db = firestore
        if (db != null) {
            scope.launch {
                try {
                    db.collection("calls").document(newCall.id).set(
                        mapOf(
                            "contactName" to contactName,
                            "phoneNumber" to phoneNumber,
                            "callType" to type.name,
                            "direction" to direction.name,
                            "timestamp" to newCall.timestamp,
                            "duration" to duration
                        )
                    )
                } catch (e: Exception) {
                    Log.w(tag, "Firestore call log note: ${e.message}")
                }
            }
        }
    }

    fun clearCallHistory() {
        _callLogs.value = emptyList()
    }

    fun saveUserToFirestore(profile: UserProfile) {
        val db = firestore
        val clean = profile.phoneNumber.filter { it.isDigit() }.takeLast(10)
        if (db != null) {
            scope.launch {
                try {
                    db.collection("users").document(profile.id).set(
                        mapOf(
                            "id" to profile.id,
                            "name" to profile.name,
                            "phoneNumber" to profile.phoneNumber,
                            "cleanPhoneNumber" to clean,
                            "email" to profile.email,
                            "statusMessage" to profile.statusMessage,
                            "avatarUrl" to (profile.avatarUrl ?: ""),
                            "lastSeen" to System.currentTimeMillis()
                        )
                    )
                } catch (e: Exception) {
                    Log.w(tag, "Firestore save user: ${e.message}")
                }
            }
        }
    }

    fun updateProfile(name: String, status: String, phone: String, avatarUrl: String? = null) {
        val updated = _currentUser.value.copy(
            name = name,
            statusMessage = status,
            phoneNumber = phone,
            avatarUrl = avatarUrl ?: _currentUser.value.avatarUrl
        )
        _currentUser.value = updated
        val db = firestore
        if (db != null && updated.id.isNotBlank()) {
            scope.launch {
                try {
                    db.collection("users").document(updated.id).set(
                        mapOf(
                            "name" to updated.name,
                            "phoneNumber" to updated.phoneNumber,
                            "statusMessage" to updated.statusMessage,
                            "avatarUrl" to (updated.avatarUrl ?: "")
                        ),
                        com.google.firebase.firestore.SetOptions.merge()
                    )
                } catch (e: Exception) {
                    Log.w(tag, "Firestore update profile note: ${e.message}")
                }
            }
        }
    }

    private var incomingCallsListener: ListenerRegistration? = null

    fun listenToIncomingCalls(userPhone: String, onIncomingCall: (com.example.model.ActiveCallState) -> Unit) {
        val db = firestore ?: return
        if (userPhone.isBlank()) return

        incomingCallsListener?.remove()
        try {
            incomingCallsListener = db.collection("active_calls")
                .whereEqualTo("calleePhone", userPhone.trim())
                .whereEqualTo("status", "RINGING")
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    for (doc in snapshot.documents) {
                        val callId = doc.id
                        val callerName = doc.getString("callerName") ?: "Incoming Call"
                        val callerPhone = doc.getString("callerPhone") ?: ""
                        val callTypeStr = doc.getString("callType") ?: "VOICE"
                        val callType = if (callTypeStr == "VIDEO") CallType.VIDEO else CallType.VOICE

                        onIncomingCall(
                            com.example.model.ActiveCallState(
                                callId = callId,
                                participantName = callerName,
                                participantPhone = callerPhone,
                                callType = callType,
                                isIncoming = true,
                                isConnected = false
                            )
                        )
                        break
                    }
                }
        } catch (e: Exception) {
            Log.w(tag, "Listen incoming calls note: ${e.message}")
        }
    }

    fun publishOutgoingCall(callState: com.example.model.ActiveCallState) {
        val db = firestore ?: return
        scope.launch {
            try {
                db.collection("active_calls").document(callState.callId).set(
                    mapOf(
                        "callerId" to _currentUser.value.id,
                        "callerName" to _currentUser.value.name.ifBlank { "Olinam User" },
                        "callerPhone" to _currentUser.value.phoneNumber,
                        "calleePhone" to callState.participantPhone,
                        "calleeName" to callState.participantName,
                        "callType" to callState.callType.name,
                        "status" to "RINGING",
                        "timestamp" to System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                Log.w(tag, "Publish outgoing call note: ${e.message}")
            }
        }
    }

    fun updateCallStatus(callId: String, status: String) {
        val db = firestore ?: return
        scope.launch {
            try {
                db.collection("active_calls").document(callId).update("status", status)
            } catch (e: Exception) {
                Log.w(tag, "Update call status note: ${e.message}")
            }
        }
    }

    fun markConversationAsRead(conversationId: String) {
        _conversations.value = _conversations.value.map {
            if (it.id == conversationId) it.copy(unreadCount = 0) else it
        }
    }

    fun mergeSmsConversations(personal: List<Conversation>, spam: List<Conversation>, context: android.content.Context? = null) {
        if (context != null) initLocalDb(context)
        // Include all text messages into SMS label (no spam folder segregation)
        val allSms = (personal + spam)
            .filter { localDb?.isItemDeleted(it.id) != true }
            .map {
                it.copy(
                    isSpam = false,
                    labelIds = if (it.labelIds.contains("sms")) it.labelIds else it.labelIds + "sms"
                )
            }
        lastSmsConversations = allSms
        updateCombinedConversations()

        if (context != null) {
            scope.launch {
                for (conv in allSms.take(15)) {
                    val phone = conv.phoneNumber ?: conv.title
                    if (phone.isNotBlank()) {
                        val messages = SmsHelper.getMessagesForAddress(context, phone)
                        if (messages.isNotEmpty()) {
                            setLocalMessages(conv.id, messages)
                        }
                    }
                }
            }
        }
    }

    fun loadMessagesForConversation(conversationId: String, context: android.content.Context) {
        initLocalDb(context)
        // 1. Immediately load cached messages from local SQLite database (0ms latency)
        val localMsgs = localDb?.getMessages(conversationId) ?: emptyList()
        if (localMsgs.isNotEmpty()) {
            val current = _messages.value.toMutableMap()
            current[conversationId] = localMsgs
            _messages.value = current
        }

        val conv = _conversations.value.find { it.id == conversationId }
        val isSms = conv?.isSmsContact == true || conversationId.startsWith("sms_")
        val targetPhone = conv?.phoneNumber?.ifBlank { null }
            ?: conv?.title?.filter { it.isDigit() || it == '+' }?.ifBlank { null }
            ?: conversationId.removePrefix("sms_thread_").removePrefix("sms_")

        if (isSms && targetPhone.isNotBlank()) {
            scope.launch {
                val smsMessages = SmsHelper.getMessagesForAddress(context, targetPhone)
                if (smsMessages.isNotEmpty()) {
                    setLocalMessages(conversationId, smsMessages)
                }
            }
        } else {
            listenToMessages(conversationId)
        }
    }

    fun setLocalMessages(conversationId: String, msgList: List<Message>) {
        val current = _messages.value.toMutableMap()
        val existing = current[conversationId] ?: emptyList()
        val combined = (msgList + existing).distinctBy { it.id }.sortedBy { it.timestamp }
        current[conversationId] = combined
        _messages.value = current
        localDb?.saveMessages(conversationId, combined)
    }

    fun deleteConversation(conversationId: String) {
        localDb?.deleteConversation(conversationId)
        val updated = _conversations.value.filter { it.id != conversationId }
        _conversations.value = updated
        val msgs = _messages.value.toMutableMap()
        msgs.remove(conversationId)
        _messages.value = msgs

        val db = firestore
        if (db != null) {
            scope.launch {
                try {
                    db.collection("conversations").document(conversationId).delete()
                } catch (e: Exception) {
                    Log.w(tag, "Firestore delete conv note: ${e.message}")
                }
            }
        }
    }

    fun deleteMessage(conversationId: String, messageId: String) {
        localDb?.deleteMessage(conversationId, messageId)
        val msgs = _messages.value.toMutableMap()
        val list = (msgs[conversationId] ?: emptyList()).filter { it.id != messageId }
        msgs[conversationId] = list
        _messages.value = msgs

        val latestMsg = list.lastOrNull()
        val updatedConversations = _conversations.value.map { conv ->
            if (conv.id == conversationId) {
                conv.copy(
                    lastMessageText = latestMsg?.text ?: "No messages",
                    lastMessageTimestamp = latestMsg?.timestamp ?: conv.lastMessageTimestamp
                )
            } else conv
        }
        _conversations.value = updatedConversations
        localDb?.saveConversations(updatedConversations)

        val db = firestore
        if (db != null) {
            scope.launch {
                try {
                    db.collection("conversations")
                        .document(conversationId)
                        .collection("messages")
                        .document(messageId)
                        .delete()
                } catch (e: Exception) {
                    Log.w(tag, "Firestore delete message note: ${e.message}")
                }
            }
        }
    }

    fun clearConversationMessages(conversationId: String) {
        localDb?.clearMessages(conversationId)
        val msgs = _messages.value.toMutableMap()
        msgs[conversationId] = emptyList()
        _messages.value = msgs

        val updatedConversations = _conversations.value.map { conv ->
            if (conv.id == conversationId) {
                conv.copy(lastMessageText = "Chat cleared")
            } else conv
        }
        _conversations.value = updatedConversations
        localDb?.saveConversations(updatedConversations)
    }
}
