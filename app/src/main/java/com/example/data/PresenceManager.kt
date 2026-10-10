package com.example.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

    fun observeUserPresence(
        userId: String,
        onPresenceChanged: (isOnline: Boolean, lastSeen: Long?) -> Unit
    ): ListenerRegistration? {
        if (userId.isBlank()) return null
        val database = db() ?: return null
        return try {
            database.collection("users").document(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null || !snapshot.exists()) {
                        onPresenceChanged(false, null)
                        return@addSnapshotListener
                    }
                    val isOnline = snapshot.getBoolean("isOnline") ?: false
                    val lastSeen = snapshot.getLong("lastSeen")
                    onPresenceChanged(isOnline, lastSeen)
                }
        } catch (_: Exception) {
            null
        }
    }

    fun observeTyping(
        conversationId: String,
        currentUserId: String,
        onTypingChanged: (isTyping: Boolean) -> Unit
    ): ListenerRegistration? {
        if (conversationId.isBlank()) return null
        val database = db() ?: return null
        return try {
            database.collection("conversations").document(conversationId)
                .collection("typing")
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) {
                        onTypingChanged(false)
                        return@addSnapshotListener
                    }
                    val now = System.currentTimeMillis()
                    // Check if any other user in this conversation is typing within last 12 seconds
                    val anyTyping = snapshot.documents.any { doc ->
                        doc.id != currentUserId &&
                                doc.getBoolean("isTyping") == true &&
                                (now - (doc.getLong("updatedAt") ?: 0L)) < 12_000L
                    }
                    onTypingChanged(anyTyping)
                }
        } catch (_: Exception) {
            null
        }
    }

    fun formatLastSeen(lastSeen: Long?): String {
        if (lastSeen == null || lastSeen <= 0L) return "last seen recently"
        val now = System.currentTimeMillis()
        val diff = now - lastSeen
        if (diff < 60_000L) return "last seen just now"

        val lastSeenCal = Calendar.getInstance().apply { timeInMillis = lastSeen }
        val nowCal = Calendar.getInstance().apply { timeInMillis = now }

        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val formattedTime = timeFormat.format(Date(lastSeen))

        return when {
            lastSeenCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
                    lastSeenCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR) -> {
                "last seen today at $formattedTime"
            }
            lastSeenCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
                    lastSeenCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR) - 1 -> {
                "last seen yesterday at $formattedTime"
            }
            else -> {
                val dateFormat = SimpleDateFormat("d MMM 'at' h:mm a", Locale.getDefault())
                "last seen ${dateFormat.format(Date(lastSeen))}"
            }
        }
    }
}
