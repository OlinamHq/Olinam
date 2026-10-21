package com.example.chat.olinam

import android.content.Context
import android.util.Log
import com.example.model.Conversation
import com.example.model.MediaType
import com.example.model.Message
import com.example.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Master Coordinator for Olinam to Olinam real-time chats.
 * Provides unified API for:
 * 1. Contact discovery & phone number verification
 * 2. Deterministic 1-on-1 Conversation ID resolution
 * 3. End-to-End Encrypted message dispatch
 * 4. Read & delivery receipt synchronization
 */
class OlinamChatCoordinator(
    private val context: Context,
    private val firestore: FirebaseFirestore?
) {
    private val tag = "OlinamCoordinator"
    private val scope = CoroutineScope(Dispatchers.IO)

    val directorySync = OlinamDirectorySync(firestore)
    val receiptTracker = OlinamReceiptTracker(firestore)
    val messageDispatcher = OlinamMessageDispatcher(context, firestore, directorySync)

    val registeredPhoneNumbers: StateFlow<Set<String>> = directorySync.registeredNumbers

    /**
     * Checks if a contact phone number is registered on Olinam.
     */
    fun isContactOnOlinam(phoneNumber: String?): Boolean {
        return directorySync.isRegistered(phoneNumber)
    }

    /**
     * Resolves or generates the canonical, deterministic 1-on-1 conversation ID.
     */
    fun resolveDirectConversationId(myPhone: String, contactPhone: String): String {
        return OlinamConversationIdGenerator.generateDirectId(myPhone, contactPhone)
    }

    /**
     * Prepares or retrieves a production-grade Olinam conversation object.
     * Both User A and User B will generate the IDENTICAL conversation ID!
     */
    fun getOrCreateDirectConversation(
        currentUser: UserProfile,
        recipientName: String,
        recipientPhone: String,
        existingConversations: List<Conversation>
    ): Conversation {
        val myPhone = currentUser.phoneNumber.ifBlank { "+91 98765 43210" }
        val deterministicId = resolveDirectConversationId(myPhone, recipientPhone)

        // 1. Check if already loaded in current state by deterministic ID
        val existing = existingConversations.find {
            it.id == deterministicId ||
                    (it.phoneNumber != null && PhoneNormalizer.areMatching(it.phoneNumber, recipientPhone)) ||
                    PhoneNormalizer.areMatching(it.title, recipientPhone)
        }

        val isOnOlinam = directorySync.isRegistered(recipientPhone)
        val e164 = PhoneNormalizer.toE164(recipientPhone)
        val myE164 = PhoneNormalizer.toE164(myPhone)

        if (existing != null) {
            val updated = existing.copy(
                isSmsContact = !isOnOlinam,
                isE2EE = isOnOlinam,
                onlineStatus = if (isOnOlinam) "End-to-End Encrypted 🔒" else "Cellular SMS (Non-app contact)",
                phoneNumber = e164.ifBlank { recipientPhone }
            )
            return updated
        }

        // 2. Create new deterministic Conversation
        val newConv = Conversation(
            id = deterministicId,
            title = recipientName.ifBlank { PhoneNormalizer.formatDisplay(recipientPhone) },
            isGroup = false,
            participantIds = listOf(currentUser.id),
            lastMessageText = if (isOnOlinam) "End-to-End Encrypted Chat started" else "SMS Chat (Standard SIM Text)",
            lastMessageTimestamp = System.currentTimeMillis(),
            unreadCount = 0,
            iconType = "USER",
            isSmsContact = !isOnOlinam,
            isE2EE = isOnOlinam,
            onlineStatus = if (isOnOlinam) "End-to-End Encrypted 🔒" else "Cellular SMS (Non-app contact)",
            phoneNumber = e164.ifBlank { recipientPhone },
            labelIds = if (isOnOlinam) listOf("direct") else listOf("sms")
        )

        // Sync conversation to Firestore
        val db = firestore
        if (db != null && isOnOlinam) {
            scope.launch {
                try {
                    val participantPhones = listOf(myE164, e164).filter { it.isNotBlank() }.distinct()
                    val data = hashMapOf<String, Any>(
                        "id" to deterministicId,
                        "title" to newConv.title,
                        "phoneNumber" to (newConv.phoneNumber ?: ""),
                        "participantPhones" to participantPhones,
                        "isGroup" to false,
                        "isSmsContact" to false,
                        "isE2EE" to true,
                        "lastMessageText" to newConv.lastMessageText,
                        "lastMessageTimestamp" to newConv.lastMessageTimestamp,
                        "unreadCount" to 0,
                        "iconType" to "USER"
                    )
                    db.collection("conversations")
                        .document(deterministicId)
                        .set(data, SetOptions.merge())
                } catch (e: Exception) {
                    Log.w(tag, "Firestore save direct conversation error: ${e.message}")
                }
            }
        }

        return newConv
    }

    /**
     * Dispatches an Olinam message with end-to-end encryption.
     */
    fun sendDirectMessage(
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
        return messageDispatcher.dispatchMessage(
            conversationId = conversationId,
            currentUser = currentUser,
            recipientPhone = recipientPhone,
            text = text,
            mediaUrl = mediaUrl,
            mediaType = mediaType,
            replyToMessageId = replyToMessageId,
            replyToSenderName = replyToSenderName,
            replyToText = replyToText,
            onLocalMessageCreated = onLocalMessageCreated
        )
    }

    fun markConversationRead(conversationId: String, currentUserId: String) {
        receiptTracker.markConversationRead(conversationId, currentUserId)
    }

    fun markMessagesDelivered(conversationId: String, currentUserId: String, messageIds: List<String>) {
        receiptTracker.markMessagesDelivered(conversationId, currentUserId, messageIds)
    }
}
