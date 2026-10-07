package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
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

class ChatViewModel(
    private val repository: FirebaseChatRepository = FirebaseChatRepository()
) : ViewModel() {

    private val _currentTab = MutableStateFlow(AppTab.CHATS)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val labels: StateFlow<List<ChatLabel>> = repository.labels
    private val _selectedLabelId = MutableStateFlow("all")
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
                "all" -> true
                "direct" -> !conv.isSmsContact && !conv.isGroup
                "groups" -> conv.isGroup
                "sms" -> conv.isSmsContact
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
    fun syncDeviceSms(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = SmsHelper.getDeviceSmsConversations(context)
            repository.mergeSmsConversations(result.personalConversations, result.spamConversations)
        }
    }
    val registeredPhoneNumbers: StateFlow<Set<String>> = repository.registeredPhoneNumbers
    fun isPhoneNumberRegistered(phone: String): Boolean = repository.isPhoneNumberRegistered(phone)
    suspend fun verifyPhoneNumberRegistered(phone: String): Boolean = repository.verifyPhoneNumberRegistered(phone)
    fun verifyAndUpdateConversationAppStatus(conversationId: String) {
        repository.verifyAndUpdateConversationAppStatus(conversationId)
    }
    fun openConversation(conversationId: String, context: Context? = null) {
        _activeConversationId.value = conversationId
        repository.markConversationAsRead(conversationId)
        repository.listenToMessages(conversationId)
        repository.verifyAndUpdateConversationAppStatus(conversationId)
        val conv = repository.conversations.value.find { it.id == conversationId }
        if (conv?.isSmsContact == true && conv.phoneNumber != null && context != null) {
            viewModelScope.launch(Dispatchers.IO) {
                val smsMessages = SmsHelper.getMessagesForAddress(context, conv.phoneNumber)
                if (smsMessages.isNotEmpty()) repository.setLocalMessages(conversationId, smsMessages)
            }
        }
    }
    fun closeConversation() { _activeConversationId.value = null }
    fun sendMessage(conversationId: String, text: String) {
        if (text.isBlank()) return
        repository.sendMessage(conversationId, text)
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

    private val _generatedOtp = MutableStateFlow<String?>(null)
    val generatedOtp: StateFlow<String?> = _generatedOtp.asStateFlow()
    private val _otpSecondsRemaining = MutableStateFlow(60)
    val otpSecondsRemaining: StateFlow<Int> = _otpSecondsRemaining.asStateFlow()
    private var otpTimerJob: kotlinx.coroutines.Job? = null
    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()
    private val _phoneVerified = MutableStateFlow(false)
    val phoneVerified: StateFlow<Boolean> = _phoneVerified.asStateFlow()

    fun sendOtp(phoneNumber: String, context: Context) {
        _authError.value = null
        _phoneVerified.value = false
        _otpSecondsRemaining.value = 60
        otpTimerJob?.cancel()
        otpTimerJob = viewModelScope.launch {
            while (_otpSecondsRemaining.value > 0) {
                kotlinx.coroutines.delay(1000)
                _otpSecondsRemaining.value = _otpSecondsRemaining.value - 1
            }
        }
        com.example.auth.PhoneAuthManager.sendOtp(
            context = context,
            phoneNumber = phoneNumber,
            onCodeSent = { _generatedOtp.value = "sent" },
            onVerified = { _phoneVerified.value = true; _generatedOtp.value = "auto" },
            onFailed = { _authError.value = it }
        )
    }

    fun verifyOtp(entered: String): Boolean {
        if (_phoneVerified.value) return true
        var ok = false
        val latch = java.util.concurrent.CountDownLatch(1)
        com.example.auth.PhoneAuthManager.verifyOtp(
            code = entered,
            onSuccess = { ok = true; latch.countDown() },
            onFailed = { _authError.value = it; latch.countDown() }
        )
        latch.await(8, java.util.concurrent.TimeUnit.SECONDS)
        return ok
    }

    fun markOnline(context: Context) {
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: currentUser.value.id
        if (uid.isBlank()) return
        com.example.data.PresenceManager.setOnline(uid, true)
        val pub = com.example.crypto.IdentityKeyManager.ensureKeyPair(context)
        val token = context.getSharedPreferences("olinam_user_prefs", Context.MODE_PRIVATE).getString("fcm_token", null)
        com.example.data.PresenceManager.publishIdentity(uid, pub, token)
    }

    fun markOffline() {
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: currentUser.value.id
        com.example.data.PresenceManager.setOnline(uid, false)
    }

    fun setTyping(conversationId: String, isTyping: Boolean) {
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: currentUser.value.id
        com.example.data.PresenceManager.setTyping(conversationId, uid, isTyping)
    }

    fun completeProfileSetup(name: String, username: String, phone: String, avatarUrl: String?, context: Context) {
        val user = com.example.model.UserProfile(
            id = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "user_${java.util.UUID.randomUUID().toString().take(8)}",
            name = name.ifBlank { "You" },
            phoneNumber = phone,
            statusMessage = "Using Olinam with End-to-End Encryption",
            avatarUrl = avatarUrl,
            isLoggedIn = true
        )
        repository.loginUser(name = user.name, phoneNumber = phone)
        repository.saveUserToFirestore(user)
        persistUserSession(user.name, phone, "", context)
        markOnline(context)
    }

    var webRtcCallManager: com.example.webrtc.call.WebRtcCallManager? = null
        private set
    var aiAgentManager: com.example.ai.OjAiAgentManager? = null
        private set

    fun getOrCreateAiAgentManager(context: Context): com.example.ai.OjAiAgentManager {
        if (aiAgentManager == null) aiAgentManager = com.example.ai.OjAiAgentManager(context.applicationContext)
        return aiAgentManager!!
    }
    fun getOrCreateWebRtcCallManager(context: Context): com.example.webrtc.call.WebRtcCallManager {
        if (webRtcCallManager == null) webRtcCallManager = com.example.webrtc.call.WebRtcCallManager(context.applicationContext)
        return webRtcCallManager!!
    }
    fun startWebRtcCall(context: Context, contactName: String, contactPhone: String, contactAvatar: String? = null, callType: CallType = CallType.VOICE) {
        val manager = getOrCreateWebRtcCallManager(context)
        val target = com.example.webrtc.model.WebRtcParticipant(userId = contactPhone.ifBlank { contactName }, displayName = contactName, phoneNumber = contactPhone, avatarUrl = contactAvatar)
        val rtcType = if (callType == CallType.VIDEO) com.example.webrtc.model.WebRtcCallType.VIDEO else com.example.webrtc.model.WebRtcCallType.AUDIO
        manager.startOutgoingCall(senderId = currentUser.value.id.ifBlank { "user_me" }, targetParticipant = target, type = rtcType)
    }
    fun startCall(contactName: String, contactPhone: String, contactAvatar: String? = null, callType: CallType = CallType.VOICE) {
        val callId = "call_${java.util.UUID.randomUUID().toString().take(8)}"
        val callObj = com.example.model.ActiveCallState(callId = callId, participantName = contactName, participantPhone = contactPhone, participantAvatar = contactAvatar, callType = callType, isIncoming = false, isConnected = false, durationSeconds = 0, isMuted = false, isSpeakerOn = callType == CallType.VIDEO, isVideoEnabled = callType == CallType.VIDEO, isFrontCamera = true)
        _activeCallState.value = callObj
        repository.publishOutgoingCall(callObj)
        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            kotlinx.coroutines.delay(1500)
            _activeCallState.value = _activeCallState.value?.copy(isConnected = true)
            while (_activeCallState.value != null && _activeCallState.value?.isConnected == true) {
                kotlinx.coroutines.delay(1000)
                _activeCallState.value = _activeCallState.value?.let { it.copy(durationSeconds = it.durationSeconds + 1) }
            }
        }
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
    fun initiateCall(contactName: String, type: CallType) {
        val conv = conversations.value.find { it.title.equals(contactName, ignoreCase = true) }
        startCall(contactName, conv?.phoneNumber ?: contactName, null, type)
    }
    fun updateProfile(name: String, status: String, phone: String) { repository.updateProfile(name, status, phone) }
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
    fun loginWithPhone(name: String, phoneNumber: String, context: Context? = null) {
        repository.loginUser(name = name, phoneNumber = phoneNumber)
        persistUserSession(name, phoneNumber, "", context)
        if (context != null) markOnline(context)
    }
    fun loginWithEmail(name: String, email: String, phoneNumber: String = "", context: Context? = null) {
        repository.loginUser(name = name, phoneNumber = phoneNumber, email = email)
        persistUserSession(name, phoneNumber, email, context)
    }
    fun loginWithGoogle(name: String, email: String, phoneNumber: String = "", context: Context? = null) {
        repository.loginUser(name = name, phoneNumber = phoneNumber, email = email)
        persistUserSession(name, phoneNumber, email, context)
    }
    fun logout(context: Context? = null) {
        markOffline()
        repository.logoutUser()
        if (context != null) context.getSharedPreferences("olinam_user_prefs", Context.MODE_PRIVATE).edit().clear().apply()
    }
    fun restoreSavedSession(context: Context) {
        val prefs = context.getSharedPreferences("olinam_user_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("is_logged_in", false)) {
            val name = prefs.getString("user_name", "") ?: ""
            val phone = prefs.getString("user_phone", "") ?: ""
            val email = prefs.getString("user_email", "") ?: ""
            if (name.isNotBlank() || phone.isNotBlank() || email.isNotBlank()) {
                repository.loginUser(name = name, phoneNumber = phone, email = email)
                markOnline(context)
            }
        }
    }
    private fun persistUserSession(name: String, phone: String, email: String, context: Context?) {
        if (context != null) {
            context.getSharedPreferences("olinam_user_prefs", Context.MODE_PRIVATE).edit()
                .putBoolean("is_logged_in", true)
                .putString("user_name", name)
                .putString("user_phone", phone)
                .putString("user_email", email)
                .apply()
        }
    }
}
