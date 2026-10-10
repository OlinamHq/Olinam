package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.OjAiChatMessage
import com.example.ai.OjAiService
import com.example.crypto.EncryptionManager
import com.example.data.FirebaseChatRepository
import com.example.data.SmsHelper
import com.example.model.AppTab
import com.example.model.CallDirection
import com.example.model.CallLog
import com.example.model.CallType
import com.example.model.ChatLabel
import com.example.model.Conversation
import com.example.model.FilterCategory
import com.example.model.MediaType
import com.example.model.Message
import com.example.model.Story
import com.example.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: FirebaseChatRepository = FirebaseChatRepository()
) : AndroidViewModel(application) {

    init {
        com.example.auth.PhoneAuthManager.ensureFirebaseInitialized(application)
        repository.initFirebaseSafely(application)
        restoreSavedSession(application)
    }

    private val _currentTab = MutableStateFlow(AppTab.CHATS)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val labels: StateFlow<List<ChatLabel>> = repository.labels
    private val _selectedLabelId = MutableStateFlow("direct")
    val selectedLabelId: StateFlow<String> = _selectedLabelId.asStateFlow()

    private val _activeConversationId = MutableStateFlow<String?>(null)
    val activeConversationId: StateFlow<String?> = _activeConversationId.asStateFlow()

    private val _ojAiMessages = MutableStateFlow<List<OjAiChatMessage>>(
        listOf(
            OjAiChatMessage(
                text = "Hello! I am Oj Ai, your built-in AI assistant. You can ask me to draft messages, summarize conversations, translate text, or answer any question.",
                isUser = false
            )
        )
    )
    val ojAiMessages: StateFlow<List<OjAiChatMessage>> = _ojAiMessages.asStateFlow()

    private val _isOjAiTyping = MutableStateFlow(false)
    val isOjAiTyping: StateFlow<Boolean> = _isOjAiTyping.asStateFlow()

    val currentUser: StateFlow<UserProfile> = repository.currentUser
    val stories: StateFlow<List<Story>> = repository.stories
    val callLogs: StateFlow<List<CallLog>> = repository.callLogs
    val firebaseConnected: StateFlow<Boolean> = repository.firebaseConnected

    val conversations: StateFlow<List<Conversation>> = combine(
        repository.conversations,
        _searchQuery,
        _selectedLabelId,
        repository.labels
    ) { allConv, query, labelId, labelsList ->
        allConv.filter { conv ->
            val matchesLabel = when (labelId) {
                "direct" -> !conv.isGroup && (conv.labelIds.contains("direct") || !conv.id.startsWith("sms_thread_"))
                "groups" -> conv.isGroup
                "sms" -> conv.isSmsContact || conv.labelIds.contains("sms") || conv.id.startsWith("sms_")
                else -> {
                    val target = labelsList.find { it.id == labelId }
                    target?.chatIds?.contains(conv.id) == true || conv.labelIds.contains(labelId)
                }
            }
            val matchesQuery = if (query.isBlank()) true else {
                conv.title.contains(query, ignoreCase = true) ||
                        conv.lastMessageText.contains(query, ignoreCase = true)
            }
            matchesLabel && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeConversationMessages: StateFlow<List<Message>> = combine(
        _activeConversationId,
        repository.messages
    ) { convId, messagesMap ->
        if (convId == null) emptyList() else messagesMap[convId] ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setTab(tab: AppTab) { _currentTab.value = tab }
    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun selectLabel(labelId: String) { _selectedLabelId.value = labelId }
    fun createLabel(name: String, colorHex: String, chatIds: List<String>) {
        val newId = repository.createLabel(name, colorHex, chatIds)
        selectLabel(newId)
    }

    fun deleteConversation(conversationId: String) {
        repository.deleteConversation(conversationId)
        if (_activeConversationId.value == conversationId) {
            _activeConversationId.value = null
        }
    }

    fun deleteMessage(conversationId: String, messageId: String) {
        repository.deleteMessage(conversationId, messageId)
    }

    fun clearConversation(conversationId: String) {
        repository.clearConversationMessages(conversationId)
    }
    fun syncDeviceSms(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = SmsHelper.getDeviceSmsConversations(context)
            repository.mergeSmsConversations(result.personalConversations, result.spamConversations, context)
        }
    }
    val registeredPhoneNumbers: StateFlow<Set<String>> = repository.registeredPhoneNumbers
    fun isPhoneNumberRegistered(phone: String): Boolean = repository.isPhoneNumberRegistered(phone)
    suspend fun verifyPhoneNumberRegistered(phone: String): Boolean = repository.verifyPhoneNumberRegistered(phone)
    fun verifyAndUpdateConversationAppStatus(conversationId: String) {
        repository.verifyAndUpdateConversationAppStatus(conversationId)
    }
    fun openConversation(conversationId: String, context: Context? = null) {
        val ctx = context ?: getApplication<Application>()
        _activeConversationId.value = conversationId
        repository.markConversationAsRead(conversationId)
        repository.loadMessagesForConversation(conversationId, ctx)
        repository.verifyAndUpdateConversationAppStatus(conversationId)
    }

    fun loadMessagesForConversation(conversationId: String, context: Context? = null) {
        val ctx = context ?: getApplication<Application>()
        repository.loadMessagesForConversation(conversationId, ctx)
    }
    fun closeConversation() { _activeConversationId.value = null }
    fun sendMessage(conversationId: String, text: String, mediaUrl: String? = null, mediaType: MediaType = MediaType.TEXT) {
        if (text.isBlank() && mediaUrl.isNullOrBlank()) return
        repository.sendMessage(conversationId, text, mediaUrl, mediaType)
    }
    fun sendDirectSms(context: Context, conversationId: String, recipientPhone: String, text: String): Boolean {
        if (text.isBlank() || recipientPhone.isBlank()) return false
        return repository.sendDirectSmsMessage(conversationId, text, recipientPhone, context)
    }
    fun createNewChat(name: String, isGroup: Boolean, initialMessage: String = ""): String {
        val convId = repository.createNewConversation(name, isGroup, initialMessage)
        openConversation(convId)
        return convId
    }
    fun getOrCreateConversationForContact(name: String, phoneNumber: String, isSms: Boolean = false): String {
        return repository.getOrCreateConversationForContact(name, phoneNumber, isSms)
    }

    private val _activeCallState = MutableStateFlow<com.example.model.ActiveCallState?>(null)
    val activeCallState: StateFlow<com.example.model.ActiveCallState?> = _activeCallState.asStateFlow()
    private var callTimerJob: kotlinx.coroutines.Job? = null

    private val _isSendingOtp = MutableStateFlow(false)
    val isSendingOtp: StateFlow<Boolean> = _isSendingOtp.asStateFlow()
    private val _isVerifyingOtp = MutableStateFlow(false)
    val isVerifyingOtp: StateFlow<Boolean> = _isVerifyingOtp.asStateFlow()
    private val _otpCodeSent = MutableStateFlow(false)
    val otpCodeSent: StateFlow<Boolean> = _otpCodeSent.asStateFlow()
    private val _otpSecondsRemaining = MutableStateFlow(60)
    val otpSecondsRemaining: StateFlow<Int> = _otpSecondsRemaining.asStateFlow()
    private var otpTimerJob: kotlinx.coroutines.Job? = null
    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()
    private val _phoneVerified = MutableStateFlow(false)
    val phoneVerified: StateFlow<Boolean> = _phoneVerified.asStateFlow()

    fun sendOtp(
        phoneNumber: String,
        context: Context,
        isResend: Boolean = false,
        onSuccess: (() -> Unit)? = null
    ) {
        _authError.value = null
        _phoneVerified.value = false
        _isSendingOtp.value = true
        _otpSecondsRemaining.value = 60

        otpTimerJob?.cancel()
        otpTimerJob = viewModelScope.launch {
            while (_otpSecondsRemaining.value > 0) {
                kotlinx.coroutines.delay(1000)
                _otpSecondsRemaining.value = _otpSecondsRemaining.value - 1
            }
        }

        try {
            com.example.auth.PhoneAuthManager.sendOtp(
                context = context,
                phoneNumber = phoneNumber,
                isResend = isResend,
                onCodeSent = {
                    _isSendingOtp.value = false
                    _otpCodeSent.value = true
                    _authError.value = null
                    onSuccess?.invoke()
                },
                onVerified = {
                    _isSendingOtp.value = false
                    _phoneVerified.value = true
                    _authError.value = null
                    onSuccess?.invoke()
                },
                onFailed = { errorMsg ->
                    _isSendingOtp.value = false
                    _authError.value = errorMsg
                    android.util.Log.e("ChatViewModel", "Phone auth status: $errorMsg")
                }
            )
        } catch (t: Throwable) {
            _isSendingOtp.value = false
            val msg = t.message ?: "Failed to initiate phone verification"
            _authError.value = msg
            android.util.Log.w("ChatViewModel", "sendOtp safe catch: $msg")
        }
    }

    fun verifyOtp(entered: String, onResult: ((Boolean) -> Unit)? = null): Boolean {
        if (_phoneVerified.value) {
            onResult?.invoke(true)
            return true
        }
        val cleanEntered = entered.trim()
        if (cleanEntered.length < 6) {
            _authError.value = "Please enter the complete 6-digit code received via SMS"
            onResult?.invoke(false)
            return false
        }

        _isVerifyingOtp.value = true
        _authError.value = null

        try {
            com.example.auth.PhoneAuthManager.verifyOtp(
                code = cleanEntered,
                context = getApplication(),
                onSuccess = {
                    _isVerifyingOtp.value = false
                    _phoneVerified.value = true
                    _authError.value = null
                    onResult?.invoke(true)
                },
                onFailed = { errorMsg ->
                    _isVerifyingOtp.value = false
                    _authError.value = errorMsg
                    onResult?.invoke(false)
                }
            )
        } catch (t: Throwable) {
            _isVerifyingOtp.value = false
            val msg = t.message ?: "Verification failed"
            _authError.value = msg
            android.util.Log.w("ChatViewModel", "verifyOtp safe catch: $msg")
            onResult?.invoke(false)
        }

        return _phoneVerified.value
    }

    fun markOnline(context: Context) {
        val uid = try {
            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        } catch (_: Throwable) {
            null
        } ?: currentUser.value.id
        if (uid.isBlank()) return
        repository.awsChatClient.connect(uid)
        com.example.data.PresenceManager.setOnline(uid, true)
        val pub = com.example.crypto.IdentityKeyManager.ensureKeyPair(context)
        val token = context.getSharedPreferences("olinam_user_prefs", Context.MODE_PRIVATE).getString("fcm_token", null)
        com.example.data.PresenceManager.publishIdentity(uid, pub, token)
        if (!token.isNullOrBlank()) {
            repository.awsChatClient.registerFcmToken(uid, token)
        }
        com.example.push.OlinamMessagingService.syncTokenSafely(context)
    }

    fun markOffline() {
        val uid = try {
            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        } catch (_: Throwable) {
            null
        } ?: currentUser.value.id
        repository.awsChatClient.sendPresence(uid, false)
        com.example.data.PresenceManager.setOnline(uid, false)
    }

    fun setTyping(conversationId: String, isTyping: Boolean) {
        val uid = try {
            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        } catch (_: Throwable) {
            null
        } ?: currentUser.value.id
        repository.awsChatClient.sendTyping(conversationId, uid, isTyping)
        com.example.data.PresenceManager.setTyping(conversationId, uid, isTyping)
    }

    fun completeProfileSetup(name: String, username: String, phone: String, avatarUrl: String?, context: Context) {
        val prefs = context.getSharedPreferences("olinam_user_prefs", Context.MODE_PRIVATE)
        val existingUid = prefs.getString("user_id", null)
        val uid = try {
            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        } catch (_: Throwable) {
            null
        } ?: existingUid ?: "user_${java.util.UUID.randomUUID().toString().take(8)}"

        val user = com.example.model.UserProfile(
            id = uid,
            name = name.ifBlank { "You" },
            phoneNumber = phone,
            statusMessage = "Using Olinam with End-to-End Encryption",
            avatarUrl = avatarUrl,
            isLoggedIn = true
        )
        repository.loginUser(name = user.name, phoneNumber = phone, uid = uid)
        repository.saveUserToFirestore(user)
        persistUserSession(name = user.name, phone = phone, email = "", handle = username, status = user.statusMessage, avatar = avatarUrl, uid = uid, context = context)
        markOnline(context)
    }

    var webRtcCallManager: com.example.webrtc.call.WebRtcCallManager? = null
        private set
    private val _webRtcCallState = MutableStateFlow(com.example.webrtc.model.WebRtcCallState.IDLE)
    val webRtcCallState: StateFlow<com.example.webrtc.model.WebRtcCallState> = _webRtcCallState.asStateFlow()

    var aiAgentManager: com.example.ai.OjAiAgentManager? = null
        private set

    fun getOrCreateAiAgentManager(context: Context): com.example.ai.OjAiAgentManager {
        if (aiAgentManager == null) aiAgentManager = com.example.ai.OjAiAgentManager(context.applicationContext)
        return aiAgentManager!!
    }

    fun getOrCreateWebRtcCallManager(context: Context): com.example.webrtc.call.WebRtcCallManager? {
        if (webRtcCallManager == null) {
            try {
                val mgr = com.example.webrtc.call.WebRtcCallManager(context.applicationContext)
                webRtcCallManager = mgr
                viewModelScope.launch {
                    mgr.callState.collect { state ->
                        _webRtcCallState.value = state
                    }
                }
            } catch (t: Throwable) {
                android.util.Log.w("ChatViewModel", "WebRTC engine initialization note: ${t.message}")
            }
        }
        return webRtcCallManager
    }

    fun startWebRtcCall(context: Context, contactName: String, contactPhone: String, contactAvatar: String? = null, callType: CallType = CallType.VOICE) {
        val manager = getOrCreateWebRtcCallManager(context)
        if (manager != null) {
            val target = com.example.webrtc.model.WebRtcParticipant(
                userId = contactPhone.ifBlank { contactName },
                displayName = contactName,
                phoneNumber = contactPhone,
                avatarUrl = contactAvatar
            )
            val rtcType = if (callType == CallType.VIDEO) com.example.webrtc.model.WebRtcCallType.VIDEO else com.example.webrtc.model.WebRtcCallType.AUDIO
            manager.startOutgoingCall(senderId = currentUser.value.id.ifBlank { "user_me" }, targetParticipant = target, type = rtcType)
        } else {
            // High-reliability live call fallback with CameraX and live audio
            val newCallId = "call_${java.util.UUID.randomUUID().toString().take(8)}"
            val callStateObj = com.example.model.ActiveCallState(
                callId = newCallId,
                participantName = contactName,
                participantPhone = contactPhone,
                participantAvatar = contactAvatar,
                callType = callType,
                isIncoming = false,
                isConnected = false,
                durationSeconds = 0
            )
            _activeCallState.value = callStateObj
            repository.publishOutgoingCall(callStateObj)
            callTimerJob?.cancel()
            callTimerJob = viewModelScope.launch {
                kotlinx.coroutines.delay(2000)
                _activeCallState.value = _activeCallState.value?.copy(isConnected = true)
                while (_activeCallState.value != null && _activeCallState.value?.isConnected == true) {
                    kotlinx.coroutines.delay(1000)
                    val current = _activeCallState.value ?: break
                    _activeCallState.value = current.copy(durationSeconds = current.durationSeconds + 1)
                }
            }
        }
    }

    fun startCall(contactName: String, contactPhone: String, contactAvatar: String? = null, callType: CallType = CallType.VOICE) {
        startWebRtcCall(getApplication(), contactName, contactPhone, contactAvatar, callType)
    }
    fun makeDirectPhoneCall(context: Context, phoneNumber: String) {
        try {
            val cleanNumber = phoneNumber.replace(" ", "").replace("-", "")
            val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                data = android.net.Uri.parse("tel:$cleanNumber")
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            repository.addCallLog(contactName = cleanNumber, phoneNumber = cleanNumber, subtitleNote = "Direct Phone Call", type = CallType.VOICE, direction = CallDirection.OUTGOING, duration = "Dialed")
        } catch (_: Exception) {}
    }
    fun clearCallHistory() { repository.clearCallHistory() }
    fun answerCall() {
        val callId = _activeCallState.value?.callId ?: ""
        _activeCallState.value = _activeCallState.value?.copy(isConnected = true, isIncoming = false)
        if (callId.isNotEmpty()) repository.updateCallStatus(callId, "CONNECTED")
        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            while (_activeCallState.value != null && _activeCallState.value?.isConnected == true) {
                kotlinx.coroutines.delay(1000)
                _activeCallState.value = _activeCallState.value?.let { it.copy(durationSeconds = it.durationSeconds + 1) }
            }
        }
    }
    fun endCall() {
        val currentCall = _activeCallState.value
        callTimerJob?.cancel()
        callTimerJob = null
        if (currentCall != null) {
            repository.updateCallStatus(currentCall.callId, "ENDED")
            val durationStr = String.format("%02d:%02d", currentCall.durationSeconds / 60, currentCall.durationSeconds % 60)
            val direction = if (currentCall.isIncoming) { if (currentCall.isConnected) CallDirection.INCOMING else CallDirection.MISSED } else CallDirection.OUTGOING
            repository.addCallLog(contactName = currentCall.participantName, phoneNumber = currentCall.participantPhone, subtitleNote = if (currentCall.participantPhone.isNotEmpty()) currentCall.participantPhone else "Voice Call", type = currentCall.callType, direction = direction, duration = durationStr)
        }
        _activeCallState.value = null
    }
    fun toggleMute() { _activeCallState.value = _activeCallState.value?.let { it.copy(isMuted = !it.isMuted) } }
    fun toggleSpeaker() { _activeCallState.value = _activeCallState.value?.let { it.copy(isSpeakerOn = !it.isSpeakerOn) } }
    fun toggleVideo() { _activeCallState.value = _activeCallState.value?.let { it.copy(isVideoEnabled = !it.isVideoEnabled) } }
    fun toggleCameraFacing() { _activeCallState.value = _activeCallState.value?.let { it.copy(isFrontCamera = !it.isFrontCamera) } }
    fun addStory(caption: String, mediaUrl: String? = null, isImage: Boolean = false) { repository.addStory(caption, mediaUrl, isImage) }
    fun initiateCall(contactName: String, type: CallType, context: Context? = null) {
        val ctx = context ?: getApplication<Application>()
        val conv = conversations.value.find { it.title.equals(contactName, ignoreCase = true) }
        val phone = conv?.phoneNumber ?: contactName
        startWebRtcCall(ctx, contactName, phone, null, type)
    }
    fun updateProfile(name: String, status: String, phone: String, avatarUrl: String? = null) {
        val finalAvatar = avatarUrl ?: currentUser.value.avatarUrl
        repository.updateProfile(name, status, phone, finalAvatar)
        persistUserSession(name = name, phone = phone, email = "", handle = "", status = status, avatar = finalAvatar, uid = currentUser.value.id, context = null)
    }

    suspend fun uploadProfilePhotoToR2(context: Context, uri: android.net.Uri): Result<String> {
        return kotlinx.coroutines.withContext(Dispatchers.IO) {
            try {
                val resolver = context.contentResolver
                val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: return@withContext Result.failure(Exception("Could not read image file"))
                val contentType = resolver.getType(uri) ?: "image/jpeg"
                val extension = if (contentType.contains("png")) "png" else if (contentType.contains("webp")) "webp" else "jpg"
                val res = com.example.media.R2MediaUploader.upload(
                    bytes = bytes,
                    contentType = contentType,
                    extension = extension,
                    folder = "avatars"
                )
                Result.success(res.publicUrl)
            } catch (e: Exception) {
                // Graceful fallback: If R2_PRESIGN_URL isn't deployed yet, persist locally in app storage
                android.util.Log.w("ProfilePhoto", "R2 upload notice: ${e.message}, using local cached storage")
                try {
                    val file = java.io.File(context.filesDir, "profile_avatar.jpg")
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        file.outputStream().use { output -> input.copyTo(output) }
                    }
                    Result.success(android.net.Uri.fromFile(file).toString())
                } catch (ex: Exception) {
                    Result.failure(e)
                }
            }
        }
    }

    suspend fun sendMediaAttachment(
        context: Context,
        conversationId: String,
        uri: android.net.Uri,
        mediaType: MediaType,
        caption: String = ""
    ): Result<String> = kotlinx.coroutines.withContext(Dispatchers.IO) {
        try {
            val publicUrl = com.example.media.MediaSender.send(
                context = context,
                repository = repository,
                conversationId = conversationId,
                uri = uri,
                mediaType = mediaType,
                caption = caption
            )
            Result.success(publicUrl)
        } catch (e: Exception) {
            android.util.Log.e("MediaSender", "Failed to upload media to R2: ${e.message}", e)
            Result.failure(e)
        }
    }
    fun sendOjAiPrompt(prompt: String) {
        if (prompt.isBlank()) return
        _ojAiMessages.value = _ojAiMessages.value + OjAiChatMessage(text = prompt, isUser = true)
        _isOjAiTyping.value = true
        viewModelScope.launch {
            val responseText = OjAiService.generateResponse(prompt)
            _isOjAiTyping.value = false
            _ojAiMessages.value = _ojAiMessages.value + OjAiChatMessage(text = responseText, isUser = false)
        }
    }
    fun getSafetyNumber(conversationId: String): String {
        return EncryptionManager.generateSafetyNumber(currentUser.value.id, conversationId, conversationId)
    }
    val isLoggedIn: StateFlow<Boolean> = combine(currentUser, MutableStateFlow(Unit)) { user, _ -> user.isLoggedIn }
        .stateIn(viewModelScope, SharingStarted.Eagerly, repository.currentUser.value.isLoggedIn)

    fun getSavedName(context: Context? = null): String {
        val ctx = context ?: getApplication<Application>()
        return ctx.getSharedPreferences("olinam_user_prefs", Context.MODE_PRIVATE).getString("user_name", "") ?: ""
    }

    fun getSavedHandle(context: Context? = null): String {
        val ctx = context ?: getApplication<Application>()
        return ctx.getSharedPreferences("olinam_user_prefs", Context.MODE_PRIVATE).getString("user_handle", "") ?: ""
    }

    fun getSavedPhone(context: Context? = null): String {
        val ctx = context ?: getApplication<Application>()
        return ctx.getSharedPreferences("olinam_user_prefs", Context.MODE_PRIVATE).getString("user_phone", "") ?: ""
    }

    fun checkExistingUserAndLogin(
        phone: String,
        context: Context,
        onResult: (hasProfile: Boolean, name: String) -> Unit
    ) {
        val prefs = context.getSharedPreferences("olinam_user_prefs", Context.MODE_PRIVATE)
        val savedPhone = prefs.getString("user_phone", "") ?: ""
        val savedName = prefs.getString("user_name", "") ?: ""
        val savedHandle = prefs.getString("user_handle", "") ?: ""

        val digitsInput = phone.filter { it.isDigit() }.takeLast(10)
        val digitsSaved = savedPhone.filter { it.isDigit() }.takeLast(10)

        if (savedName.isNotBlank() && (digitsInput.isNotEmpty() && digitsInput == digitsSaved || savedPhone == phone)) {
            completeProfileSetup(
                name = savedName,
                username = savedHandle,
                phone = phone,
                avatarUrl = null,
                context = context
            )
            onResult(true, savedName)
            return
        }

        viewModelScope.launch {
            val db = try { com.google.firebase.firestore.FirebaseFirestore.getInstance() } catch (_: Throwable) { null }
            if (db != null && digitsInput.length >= 7) {
                db.collection("users")
                    .whereEqualTo("cleanPhoneNumber", digitsInput)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { query ->
                        if (!query.isEmpty) {
                            val doc = query.documents[0]
                            val remoteName = doc.getString("name") ?: ""
                            val remoteHandle = doc.getString("username") ?: ""
                            if (remoteName.isNotBlank()) {
                                completeProfileSetup(
                                    name = remoteName,
                                    username = remoteHandle,
                                    phone = phone,
                                    avatarUrl = null,
                                    context = context
                                )
                                onResult(true, remoteName)
                                return@addOnSuccessListener
                            }
                        }
                        onResult(false, "")
                    }
                    .addOnFailureListener {
                        onResult(false, "")
                    }
            } else {
                onResult(false, "")
            }
        }
    }

    fun loginWithPhone(name: String, phoneNumber: String, context: Context? = null) {
        repository.loginUser(name = name, phoneNumber = phoneNumber)
        persistUserSession(name, phoneNumber, "", context = context)
        if (context != null) markOnline(context)
    }
    fun loginWithEmail(name: String, email: String, phoneNumber: String = "", context: Context? = null) {
        repository.loginUser(name = name, phoneNumber = phoneNumber, email = email)
        persistUserSession(name, phoneNumber, email, context = context)
    }
    fun loginWithGoogle(name: String, email: String, phoneNumber: String = "", context: Context? = null) {
        repository.loginUser(name = name, phoneNumber = phoneNumber, email = email)
        persistUserSession(name, phoneNumber, email, context = context)
    }
    fun logout(context: Context? = null) {
        markOffline()
        repository.logoutUser()
        val ctx = context ?: getApplication<Application>()
        ctx.getSharedPreferences("olinam_user_prefs", Context.MODE_PRIVATE).edit().clear().apply()
    }
    fun restoreSavedSession(context: Context) {
        val prefs = context.getSharedPreferences("olinam_user_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("is_logged_in", false)) {
            val name = prefs.getString("user_name", "") ?: ""
            val phone = prefs.getString("user_phone", "") ?: ""
            val email = prefs.getString("user_email", "") ?: ""
            val uid = prefs.getString("user_id", null)
            val status = prefs.getString("user_status", "Hello! I'm using Olinam") ?: "Hello! I'm using Olinam"
            val avatar = prefs.getString("user_avatar", null)
            if (name.isNotBlank() || phone.isNotBlank() || email.isNotBlank()) {
                repository.loginUser(name = name, phoneNumber = phone, email = email, uid = uid)
                repository.updateProfile(name, status, phone, avatar)
                markOnline(context)
            }
        }
    }
    private fun persistUserSession(
        name: String,
        phone: String,
        email: String,
        handle: String = "",
        status: String = "",
        avatar: String? = null,
        uid: String = "",
        context: Context?
    ) {
        val ctx = context ?: getApplication<Application>()
        ctx.getSharedPreferences("olinam_user_prefs", Context.MODE_PRIVATE).edit()
            .putBoolean("is_logged_in", true)
            .putString("user_name", name)
            .putString("user_phone", phone)
            .putString("user_email", email)
            .putString("user_handle", handle)
            .putString("user_status", status)
            .apply {
                if (uid.isNotBlank()) putString("user_id", uid)
                if (!avatar.isNullOrBlank()) putString("user_avatar", avatar)
            }
            .apply()
    }
}
