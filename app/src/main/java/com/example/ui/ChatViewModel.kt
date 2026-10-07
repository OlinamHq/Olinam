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

    // Filtered conversations based on custom label and search query
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

    // Messages for the active conversation
    val activeConversationMessages: StateFlow<List<Message>> = combine(
        _activeConversationId,
        repository.messages
    ) { convId, messagesMap ->
        if (convId == null) emptyList() else messagesMap[convId] ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectLabel(labelId: String) {
        _selectedLabelId.value = labelId
    }

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

    fun openConversation(conversationId: String, context: Context? = null) {
        _activeConversationId.value = conversationId
        repository.markConversationAsRead(conversationId)
        repository.listenToMessages(conversationId)

        val conv = repository.conversations.value.find { it.id == conversationId }
        if (conv?.isSmsContact == true && conv.phoneNumber != null && context != null) {
            viewModelScope.launch(Dispatchers.IO) {
                val smsMessages = SmsHelper.getMessagesForAddress(context, conv.phoneNumber)
                if (smsMessages.isNotEmpty()) {
                    repository.setLocalMessages(conversationId, smsMessages)
                }
            }
        }
    }

    fun closeConversation() {
        _activeConversationId.value = null
    }

    fun sendMessage(conversationId: String, text: String) {
        if (text.isBlank()) return
        repository.sendMessage(conversationId, text)
    }

    fun createNewChat(name: String, isGroup: Boolean, initialMessage: String = ""): String {
        val convId = repository.createNewConversation(name, isGroup, initialMessage)
        openConversation(convId)
        return convId
    }

    fun getOrCreateConversationForContact(name: String, phoneNumber: String, isSms: Boolean = false): String {
        val convId = repository.getOrCreateConversationForContact(name, phoneNumber, isSms)
        return convId
    }

    fun addStory(caption: String) {
        repository.addStory(caption)
    }

    fun initiateCall(contactName: String, type: CallType) {
        repository.addCallLog(contactName, type, CallDirection.OUTGOING)
    }

    fun updateProfile(name: String, status: String, phone: String) {
        repository.updateProfile(name, status, phone)
    }

    fun sendOjAiPrompt(prompt: String) {
        if (prompt.isBlank()) return
        val userMsg = OjAiChatMessage(text = prompt, isUser = true)
        _ojAiMessages.value = _ojAiMessages.value + userMsg
        _isOjAiTyping.value = true

        viewModelScope.launch {
            val responseText = OjAiService.generateResponse(prompt)
            _isOjAiTyping.value = false
            val aiMsg = OjAiChatMessage(text = responseText, isUser = false)
            _ojAiMessages.value = _ojAiMessages.value + aiMsg
        }
    }

    fun getSafetyNumber(conversationId: String): String {
        return EncryptionManager.generateSafetyNumber(currentUser.value.id, conversationId, conversationId)
    }

    val isLoggedIn: StateFlow<Boolean> = combine(currentUser, MutableStateFlow(Unit)) { user, _ ->
        user.isLoggedIn
    }.stateIn(viewModelScope, SharingStarted.Eagerly, repository.currentUser.value.isLoggedIn)

    fun loginWithPhone(name: String, phoneNumber: String, context: Context? = null) {
        repository.loginUser(name = name, phoneNumber = phoneNumber)
        persistUserSession(name, phoneNumber, "", context)
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
        repository.logoutUser()
        if (context != null) {
            context.getSharedPreferences("olinam_user_prefs", Context.MODE_PRIVATE)
                .edit().clear().apply()
        }
    }

    fun restoreSavedSession(context: Context) {
        val prefs = context.getSharedPreferences("olinam_user_prefs", Context.MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)
        if (isLoggedIn) {
            val name = prefs.getString("user_name", "") ?: ""
            val phone = prefs.getString("user_phone", "") ?: ""
            val email = prefs.getString("user_email", "") ?: ""
            if (name.isNotBlank() || phone.isNotBlank() || email.isNotBlank()) {
                repository.loginUser(name = name, phoneNumber = phone, email = email)
            }
        }
    }

    private fun persistUserSession(name: String, phone: String, email: String, context: Context?) {
        if (context != null) {
            val prefs = context.getSharedPreferences("olinam_user_prefs", Context.MODE_PRIVATE)
            prefs.edit()
                .putBoolean("is_logged_in", true)
                .putString("user_name", name)
                .putString("user_phone", phone)
                .putString("user_email", email)
                .apply()
        }
    }
}
