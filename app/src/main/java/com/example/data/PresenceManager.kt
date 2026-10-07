package com.example.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

/**
 * Real presence: online, last seen, and typing indicator in Firestore.
 */
object PresenceManager {
    private fun db(): FirebaseFirestore? = try {
        FirebaseFirestore.getInstance()
    } catch (_: Exception) {
        null
    }

    fun setOnline(userId: String, online: Boolean) {
        if (userId.isBlank()) return
        val database = db() ?: return
        database.collection("users").document(userId).set(
            mapOf(
                "isOnline" to online,
                "lastSeen" to System.currentTimeMillis()
            ),
            SetOptions.merge()
        )
    }

    fun setTyping(conversationId: String, userId: String, isTyping: Boolean) {
        if (conversationId.isBlank() || userId.isBlank()) return
        val database = db() ?: return
        database.collection("conversations").document(conversationId)
            .collection("typing").document(userId)
            .set(
                mapOf(
                    "isTyping" to isTyping,
                    "updatedAt" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            )
    }

    fun publishIdentity(userId: String, publicKey: String, fcmToken: String? = null) {
        if (userId.isBlank()) return
        val database = db() ?: return
        val data = mutableMapOf<String, Any>("publicKey" to publicKey)
        if (!fcmToken.isNullOrBlank()) data["fcmToken"] = fcmToken
        database.collection("users").document(userId).set(data, SetOptions.merge())
    }
}
