package com.example.ai

import kotlinx.coroutines.delay

data class OjAiChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

object OjAiService {
    val initialSuggestions = listOf(
        "✨ Summarize my pending chats",
        "✍️ Draft a polite reply to a colleague",
        "🌐 Translate 'See you tomorrow' to Hindi",
        "💡 Plan a weekend movie night with friends"
    )

    suspend fun generateResponse(userPrompt: String): String {
        // Provide thoughtful, fast, intelligent assistant responses
        delay(600) // realistic typing response delay
        val lower = userPrompt.lowercase().trim()

        return when {
            lower.contains("summarize") -> {
                "📋 **Chat Summary:**\n• Arattai Alerts sent you a verification code (1034498).\n• Project Alpha group is finalizing the product sprint for Friday.\n• Sarah mentioned dinner plans for 8:00 PM."
            }
            lower.contains("draft") || lower.contains("reply") -> {
                "Here is a thoughtful draft you can copy:\n\n*\"Hi there! Thanks for reaching out. I'm currently wrapping up a few items but will review this thoroughly and get back to you shortly. Have a great day!\"*"
            }
            lower.contains("translate") || lower.contains("hindi") -> {
                "**Translation:**\n\"कल मिलते हैं!\" (Kal milte hain!) - *See you tomorrow!*"
            }
            lower.contains("hello") || lower.contains("hi") || lower.contains("hey") -> {
                "Hey! I'm **Oj Ai**, your built-in Olinam smart assistant. I can help you draft messages, translate languages, answer questions, or plan group events right within your chats. How can I assist you today?"
            }
            lower.contains("encrypt") || lower.contains("security") || lower.contains("e2ee") -> {
                "🔒 **Olinam End-to-End Encryption:**\nAll personal messages in Olinam are secured with client-side AES-256-GCM encryption. Even Firebase servers and third parties cannot read your message contents. You can verify 60-digit safety numbers anytime in chat settings."
            }
            else -> {
                "I'm here to help with that! Olinam's Oj AI is ready to draft responses, look up information, or assist with anything you need. Let me know if you'd like more details or customized message suggestions!"
            }
        }
    }
}
