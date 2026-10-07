package com.example.data

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Telephony
import com.example.model.Conversation
import com.example.model.Message
import com.example.model.MessageStatus
import java.util.Locale

data class SmsConversationResult(
    val personalConversations: List<Conversation>,
    val spamConversations: List<Conversation>
)

object SmsHelper {

    private val SPAM_KEYWORDS = listOf(
        "otp", "verification code", "one time password", "pre-approved", "loan",
        "congratulations", "claim now", "win cash", "rummy", "lottery", "jackpot",
        "flat 50%", "cashback", "credited with inr", "debited by inr", "promo code",
        "exclusive offer", "recharge now", "limited time offer", "unsubscribe",
        "kyc suspended", "call now to claim", "free spins", "bonus", "discount",
        "hurry", "urgent action required", "winner", "prize", "credit card", "personal loan",
        "50% off", "flat 40%", "casino", "poker", "teen patti", "betting", "win 10000"
    )

    fun isSpamSenderOrBody(address: String, body: String): Boolean {
        val cleanAddr = address.trim()
        val lowerBody = body.lowercase(Locale.ROOT)

        // 1. Alphanumeric sender header typical of bulk business/promotional SMS in India and globally
        // e.g. "AX-HDFCBK", "VK-SBIINB", "AD-JIOFBR", "VM-AIRTEL", "DM-AMAZON", "JM-RUMMY"
        val hasLetters = cleanAddr.any { it.isLetter() }
        if (hasLetters) {
            return true
        }

        // 2. Shortcode sender (e.g. 56161, 57575)
        val digitsOnly = cleanAddr.filter { it.isDigit() }
        if (digitsOnly.length in 3..6 && !cleanAddr.startsWith("+")) {
            return true
        }

        // 3. Keyword matching for promo/spam
        for (kw in SPAM_KEYWORDS) {
            if (lowerBody.contains(kw)) {
                return true
            }
        }

        return false
    }

    fun getDeviceSmsConversations(context: Context): SmsConversationResult {
        val personalList = mutableListOf<Conversation>()
        val spamList = mutableListOf<Conversation>()
        val seenAddresses = mutableMapOf<String, Conversation>()

        try {
            val uri = Uri.parse("content://sms")
            val cursor = context.contentResolver.query(
                uri,
                arrayOf("_id", "address", "body", "date", "type", "thread_id", "read"),
                null,
                null,
                "date DESC"
            )

            cursor?.use {
                val addrIdx = it.getColumnIndex("address")
                val bodyIdx = it.getColumnIndex("body")
                val dateIdx = it.getColumnIndex("date")
                val readIdx = it.getColumnIndex("read")
                val threadIdx = it.getColumnIndex("thread_id")

                while (it.moveToNext()) {
                    val address = if (addrIdx >= 0) it.getString(addrIdx) ?: "" else ""
                    val body = if (bodyIdx >= 0) it.getString(bodyIdx) ?: "" else ""
                    val date = if (dateIdx >= 0) it.getLong(dateIdx) else System.currentTimeMillis()
                    val threadId = if (threadIdx >= 0) it.getString(threadIdx) ?: address else address
                    val isRead = if (readIdx >= 0) it.getInt(readIdx) == 1 else true

                    if (address.isBlank()) continue

                    val cleanAddr = address.trim()
                    if (seenAddresses.containsKey(cleanAddr)) continue

                    val resolvedContactName = resolveContactName(context, cleanAddr)
                    val isKnownContact = resolvedContactName != null && resolvedContactName.isNotBlank() && resolvedContactName != cleanAddr

                    val contactName = resolvedContactName ?: cleanAddr

                    val conv = Conversation(
                        id = "sms_thread_$threadId",
                        title = contactName,
                        isGroup = false,
                        lastMessageText = body,
                        lastMessageTimestamp = date,
                        unreadCount = if (isRead) 0 else 1,
                        isE2EE = false,
                        iconType = "USER",
                        isSmsContact = true,
                        phoneNumber = cleanAddr,
                        isSpam = false,
                        labelIds = listOf("sms")
                    )

                    seenAddresses[cleanAddr] = conv
                    personalList.add(conv)
                }
            }
        } catch (_: Exception) {
            // Permission not granted or query failed
        }

        return SmsConversationResult(
            personalConversations = personalList,
            spamConversations = spamList
        )
    }

    fun getMessagesForAddress(context: Context, address: String): List<Message> {
        val messages = mutableListOf<Message>()
        try {
            val uri = Uri.parse("content://sms")
            val cursor = context.contentResolver.query(
                uri,
                arrayOf("_id", "address", "body", "date", "type"),
                "address = ? OR address LIKE ?",
                arrayOf(address, "%${address.takeLast(10)}%"),
                "date ASC"
            )

            cursor?.use {
                val idIdx = it.getColumnIndex("_id")
                val bodyIdx = it.getColumnIndex("body")
                val dateIdx = it.getColumnIndex("date")
                val typeIdx = it.getColumnIndex("type")

                while (it.moveToNext()) {
                    val id = if (idIdx >= 0) it.getString(idIdx) else java.util.UUID.randomUUID().toString()
                    val body = if (bodyIdx >= 0) it.getString(bodyIdx) ?: "" else ""
                    val date = if (dateIdx >= 0) it.getLong(dateIdx) else System.currentTimeMillis()
                    val type = if (typeIdx >= 0) it.getInt(typeIdx) else Telephony.Sms.MESSAGE_TYPE_INBOX

                    val isMe = type == Telephony.Sms.MESSAGE_TYPE_SENT

                    messages.add(
                        Message(
                            id = "sms_msg_$id",
                            conversationId = "sms_$address",
                            senderId = if (isMe) "me" else address,
                            senderName = if (isMe) "You" else address,
                            text = body,
                            timestamp = date,
                            isEncrypted = false,
                            status = MessageStatus.READ
                        )
                    )
                }
            }
        } catch (_: Exception) {}
        return messages
    }

    private fun resolveContactName(context: Context, phoneNumber: String): String? {
        try {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber)
            )
            val cursor = context.contentResolver.query(
                uri,
                arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME),
                null,
                null,
                null
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    val idx = it.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                    if (idx >= 0) {
                        return it.getString(idx)
                    }
                }
            }
        } catch (_: Exception) {}
        return null
    }
}
