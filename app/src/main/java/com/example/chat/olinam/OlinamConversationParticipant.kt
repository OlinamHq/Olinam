package com.example.chat.olinam

import androidx.compose.runtime.Immutable

@Immutable
data class OlinamParticipant(
    val userId: String = "",
    val phoneNumber: String = "",
    val displayName: String = "",
    val avatarUrl: String? = null,
    val publicKey: String? = null,
    val isOnline: Boolean = false,
    val lastSeen: Long? = null
) {
    fun toFirestoreMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "userId" to userId,
            "phoneNumber" to phoneNumber,
            "displayName" to displayName
        )
        avatarUrl?.let { map["avatarUrl"] = it }
        publicKey?.let { map["publicKey"] = it }
        return map
    }

    companion object {
        fun fromFirestoreMap(data: Map<String, Any?>): OlinamParticipant {
            return OlinamParticipant(
                userId = data["userId"] as? String ?: "",
                phoneNumber = data["phoneNumber"] as? String ?: "",
                displayName = data["displayName"] as? String ?: "",
                avatarUrl = data["avatarUrl"] as? String,
                publicKey = data["publicKey"] as? String
            )
        }
    }
}
