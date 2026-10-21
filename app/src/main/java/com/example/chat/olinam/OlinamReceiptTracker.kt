package com.example.chat.olinam

import android.util.Log
import com.example.model.MessageStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Tracks and synchronizes message delivery receipts in real time:
 * - SENT (Single grey tick)
 * - DELIVERED (Double grey tick)
 * - READ (Double blue tick)
 */
class OlinamReceiptTracker(
    private val firestore: FirebaseFirestore?
) {
    private val tag = "OlinamReceiptTracker"
    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Called when the current user loads messages in a conversation.
     * Updates status of messages from other sender to DELIVERED if currently SENT.
     */
    fun markMessagesDelivered(
        conversationId: String,
        currentUserId: String,
        messageIds: List<String>
    ) {
        val db = firestore ?: return
        if (conversationId.isBlank() || messageIds.isEmpty()) return

        scope.launch {
            try {
                val batch = db.batch()
                var count = 0
                for (id in messageIds.take(50)) {
                    val docRef = db.collection("conversations")
                        .document(conversationId)
                        .collection("messages")
                        .document(id)
                    batch.update(docRef, "status", MessageStatus.DELIVERED.name)
                    count++
                }
                if (count > 0) {
                    batch.commit()
                }
            } catch (e: Exception) {
                Log.w(tag, "markMessagesDelivered error: ${e.message}")
            }
        }
    }

    /**
     * Called when the user actively opens and views the conversation.
     * Marks unread messages sent by the other party as READ.
     */
    fun markConversationRead(
        conversationId: String,
        currentUserId: String
    ) {
        val db = firestore ?: return
        if (conversationId.isBlank() || currentUserId.isBlank()) return

        scope.launch {
            try {
                db.collection("conversations")
                    .document(conversationId)
                    .collection("messages")
                    .whereNotEqualTo("senderId", currentUserId)
                    .limit(50)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        if (snapshot != null && !snapshot.isEmpty) {
                            val batch = db.batch()
                            var needCommit = false
                            for (doc in snapshot.documents) {
                                val currentStatus = doc.getString("status") ?: ""
                                if (currentStatus != MessageStatus.READ.name) {
                                    batch.update(doc.reference, "status", MessageStatus.READ.name)
                                    needCommit = true
                                }
                            }
                            if (needCommit) {
                                batch.commit()
                            }
                        }
                    }

                // Reset unread count for current user
                db.collection("conversations")
                    .document(conversationId)
                    .set(
                        mapOf(
                            "unreadCount" to 0,
                            "lastRead_${currentUserId}" to System.currentTimeMillis()
                        ),
                        SetOptions.merge()
                    )
            } catch (e: Exception) {
                Log.w(tag, "markConversationRead error: ${e.message}")
            }
        }
    }
}
