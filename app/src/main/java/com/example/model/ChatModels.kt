package com.example.model

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ
}

enum class MediaType {
    TEXT,
    IMAGE,
    AUDIO
}

data class EncryptedPayload(
    val ciphertext: String = "",
    val iv: String = "",
    val salt: String = "",
    val algorithm: String = "AES-256-GCM"
)

data class Message(
    val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val encryptedPayload: EncryptedPayload? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isEncrypted: Boolean = true,
    val status: MessageStatus = MessageStatus.SENT,
    val mediaUrl: String? = null,
    val mediaType: MediaType = MediaType.TEXT
)

data class ChatLabel(
    val id: String = "",
    val name: String = "",
    val colorHex: String = "#0160E3",
    val chatIds: List<String> = emptyList()
)

data class Conversation(
    val id: String = "",
    val title: String = "",
    val isGroup: Boolean = false,
    val participantIds: List<String> = emptyList(),
    val lastMessageText: String = "",
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val avatarUrl: String? = null,
    val isVerified: Boolean = false,
    val isSystemAlert: Boolean = false,
    val isPinned: Boolean = false,
    val isE2EE: Boolean = true,
    val onlineStatus: String? = null,
    val iconType: String = "DEFAULT", // "ALERT", "INVITE", "USER", "GROUP"
    val labelIds: List<String> = emptyList(),
    val isSmsContact: Boolean = false,
    val phoneNumber: String? = null,
    val isSpam: Boolean = false
)

enum class CallType {
    VOICE,
    VIDEO
}

enum class CallDirection {
    INCOMING,
    OUTGOING,
    MISSED
}

data class CallLog(
    val id: String = "",
    val contactName: String = "",
    val contactAvatar: String? = null,
    val callType: CallType = CallType.VOICE,
    val direction: CallDirection = CallDirection.OUTGOING,
    val timestamp: Long = System.currentTimeMillis(),
    val duration: String = "00:00"
)

data class Story(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userAvatar: String? = null,
    val caption: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val mediaUrl: String? = null,
    val viewed: Boolean = false
)

enum class FilterCategory {
    CHATS,
    DIRECT,
    GROUPS
}

enum class AppTab {
    CHATS,
    STORIES,
    OJ_AI,
    CALLS
}

data class UserProfile(
    val id: String = "me",
    val name: String = "You",
    val phoneNumber: String = "",
    val email: String = "",
    val statusMessage: String = "Hey there! I am using Olinam.",
    val avatarUrl: String? = null,
    val isLoggedIn: Boolean = false,
    val safetyNumber: String = "45109 82341 09124 78652 14320 89125 67019 32145 90124 55102 78321 00412"
)
