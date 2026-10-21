package com.example.chat.olinam

import java.security.MessageDigest

/**
 * Generates deterministic, collision-free 1-on-1 conversation IDs.
 * Both User A and User B will always generate the EXACT SAME conversation ID
 * regardless of who initiated the chat first.
 */
object OlinamConversationIdGenerator {

    /**
     * Generates a deterministic direct message conversation ID based on two phone numbers.
     * Guaranteed: generateDirectId(phoneA, phoneB) == generateDirectId(phoneB, phoneA)
     */
    fun generateDirectId(phone1: String, phone2: String): String {
        val norm1 = PhoneNormalizer.toE164(phone1).ifBlank { PhoneNormalizer.extractDigits(phone1) }
        val norm2 = PhoneNormalizer.toE164(phone2).ifBlank { PhoneNormalizer.extractDigits(phone2) }

        val sorted = listOf(norm1, norm2).sorted()
        val combined = "olinam_direct:${sorted[0]}:${sorted[1]}"

        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(combined.toByteArray(Charsets.UTF_8))
        val hex = digest.take(10).joinToString("") { "%02x".format(it) }

        return "conv_olinam_$hex"
    }

    /**
     * Generates a unique group conversation ID.
     */
    fun generateGroupId(): String {
        val uuid = java.util.UUID.randomUUID().toString().replace("-", "").take(12)
        return "group_olinam_$uuid"
    }
}
