package com.example.chat.olinam

import android.content.Context
import android.util.Log
import com.example.crypto.IdentityKeyManager
import com.example.model.MediaType
import com.example.model.Message
import com.example.model.MessageStatus
import com.example.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Enterprise message dispatcher for Olinam-to-Olinam communication.
 * Handles client-side encryption, Firestore atomic writes, and recipient indexing.
 */
class OlinamMessageDispatcher(
    private val context: Context,
    private val firestore: FirebaseFirestore?,
    private val directorySync: OlinamDirectorySync
) {
    private val tag = "OlinamDispatcher"
    private val scope = CoroutineScope(Dispatchers.IO)

    fun dispatchMessage(
        conversationId: String,
        currentUser: UserProfile,
        recipientPhone: String,
        text: String,
        mediaUrl: String? = null,
        mediaType: MediaType = MediaType.TEXT,
        replyToMessageId: String? = null,
        replyToSenderName: String? = null,
        replyToText: String? = null,
        onLocalMessageCreated: (Message) -> Unit
    ): Message {
        val messageId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()

        // 1. Derive optional ECDH shared secret if remote public key is registered
        val remotePubKey = directorySync.getPublicKeyForPhone(recipientPhone)
        val ecdhSecret = if (!remotePubKey.isNullOrBlank()) {
            IdentityKeyManager.sharedSecret(context, remotePubKey)
        } else null

        // 2. Encrypt text with AES-256-GCM
        val encryptedPayload = OlinamE2EEManager.encrypt(
            plaintext = text,
            conversationId = conversationId,
            ecdhSecret = ecdhSecret
        )

        val localMessage = Message(
            id = messageId,
            conversationId = conversationId,
            senderId = currentUser.id,
            senderName = currentUser.name.ifBlank { "You" },
            text = text,
            encryptedPayload = encryptedPayload,
            timestamp = timestamp,
            isEncrypted = true,
            status = MessageStatus.SENT,
            mediaUrl = mediaUrl,
            mediaType = mediaType,
            replyToMessageId = replyToMessageId,
            replyToSenderName = replyToSenderName,
            replyToText = replyToText
        )

        // Zero latency callback for UI
        onLocalMessageCreated(localMessage)

        // 3. Write to Firestore in background
        val db = firestore
        if (db != null) {
            scope.launch {
                try {
                    val myPhoneE164 = PhoneNormalizer.toE164(currentUser.phoneNumber)
                    val recipientE164 = PhoneNormalizer.toE164(recipientPhone)
                    val participantPhones = listOf(myPhoneE164, recipientE164).filter { it.isNotBlank() }.distinct()
                    val participantIds = listOf(currentUser.id).distinct()

                    val messageDoc = hashMapOf<String, Any>(
                        "id" to messageId,
                        "conversationId" to conversationId,
                        "senderId" to currentUser.id,
                        "senderName" to currentUser.name,
                        "ciphertext" to encryptedPayload.ciphertext,
                        "iv" to encryptedPayload.iv,
                        "salt" to encryptedPayload.salt,
                        "algorithm" to encryptedPayload.algorithm,
                        "timestamp" to timestamp,
                        "isEncrypted" to true,
                        "status" to MessageStatus.SENT.name,
                        "mediaType" to mediaType.name
                    )

                    mediaUrl?.let { messageDoc["mediaUrl"] = it }
                    replyToMessageId?.let {
                        messageDoc["replyToMessageId"] = it
                        messageDoc["replyToSenderName"] = replyToSenderName ?: ""
                        messageDoc["replyToText"] = replyToText ?: ""
                    }

                    // Write message
                    db.collection("conversations")
                        .document(conversationId)
                        .collection("messages")
                        .document(messageId)
                        .set(messageDoc)

                    // Update parent conversation document metadata with participant indexing
                    val conversationUpdates = hashMapOf<String, Any>(
                        "lastMessageText" to "[Encrypted]",
                        "lastMessageTimestamp" to timestamp,
                        "participantPhones" to participantPhones,
                        "isE2EE" to true,
                        "isSmsContact" to false
                    )

                    db.collection("conversations")
                        .document(conversationId)
                        .set(conversationUpdates, SetOptions.merge())

                } catch (e: Exception) {
                    Log.w(tag, "Failed to persist encrypted message to Firestore: ${e.message}")
                }
            }
        }

        return localMessage
    }
}
