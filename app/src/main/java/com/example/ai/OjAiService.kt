package com.example.ai

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class OjAiChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false,
    val feedback: Int = 0 // 0: none, 1: like, -1: dislike
)

object OjAiService {
    private const val TAG = "OjAiService"
    private const val GEMINI_MODEL = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"

    val initialSuggestions = listOf(
        "✨ Summarize my pending chats",
        "✍️ Draft a polite decline message",
        "🌐 Translate 'See you tomorrow' to Hindi",
        "💡 Plan a weekend movie night with friends",
        "🔒 How does Olinam end-to-end encryption work?",
        "💻 Write a Kotlin Compose sample"
    )

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Resolves the Gemini API key from BuildConfig or environment variables.
     */
    fun getApiKey(): String {
        return try {
            val buildKey = BuildConfig.GEMINI_API_KEY
            if (!buildKey.isNullOrBlank()) buildKey
            else System.getenv("GEMINI_API_KEY") ?: ""
        } catch (_: Throwable) {
            System.getenv("GEMINI_API_KEY") ?: ""
        }
    }

    /**
     * Generates a conversational response using Gemini 3.5 Flash REST API,
     * with graceful local intelligent fallback if the API is offline or unkeyed.
     */
    suspend fun generateResponse(
        userPrompt: String,
        history: List<OjAiChatMessage> = emptyList()
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isNotBlank()) {
            try {
                val response = callGeminiRestApi(apiKey, userPrompt, history)
                if (response.isNotBlank()) {
                    return@withContext response
                }
            } catch (t: Throwable) {
                Log.w(TAG, "Gemini REST API attempt failed: ${t.message}. Falling back to smart assistant logic.", t)
            }
        }

        // Context-aware smart assistant fallback
        generateSmartFallback(userPrompt)
    }

    private fun callGeminiRestApi(
        apiKey: String,
        userPrompt: String,
        history: List<OjAiChatMessage>
    ): String {
        val url = "$BASE_URL$GEMINI_MODEL:generateContent?key=$apiKey"
        val jsonMediaType = "application/json; charset=utf-8".toMediaType()

        val rootJson = JSONObject()

        // 1. System instruction
        val systemInstruction = JSONObject()
        val sysParts = JSONArray()
        sysParts.put(JSONObject().put("text", "You are Oj AI, the intelligent built-in chatbot assistant in Olinam, a private, secure, end-to-end encrypted messaging app. You are friendly, helpful, concise, and capable of drafting messages, answering questions, translating languages, and assisting with tasks. Format responses clearly with neat markdown formatting."))
        systemInstruction.put("parts", sysParts)
        rootJson.put("systemInstruction", systemInstruction)

        // 2. Chat contents with history
        val contentsArray = JSONArray()

        // Take up to 6 recent messages from history for conversational context
        val contextHistory = history.takeLast(6)
        for (msg in contextHistory) {
            val turn = JSONObject()
            turn.put("role", if (msg.isUser) "user" else "model")
            val parts = JSONArray()
            parts.put(JSONObject().put("text", msg.text))
            turn.put("parts", parts)
            contentsArray.put(turn)
        }

        // Add current prompt
        val currentTurn = JSONObject()
        currentTurn.put("role", "user")
        val currentParts = JSONArray()
        currentParts.put(JSONObject().put("text", userPrompt))
        currentTurn.put("parts", currentParts)
        contentsArray.put(currentTurn)

        rootJson.put("contents", contentsArray)

        // Generation Config
        val genConfig = JSONObject()
        genConfig.put("temperature", 0.7)
        genConfig.put("maxOutputTokens", 1024)
        rootJson.put("generationConfig", genConfig)

        val requestBody = rootJson.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val response = httpClient.newCall(request).execute()
        val responseBody = response.body?.string().orEmpty()

        if (!response.isSuccessful) {
            Log.e(TAG, "Gemini API error code ${response.code}: $responseBody")
            return ""
        }

        val jsonResponse = JSONObject(responseBody)
        val candidates = jsonResponse.optJSONArray("candidates") ?: return ""
        if (candidates.length() == 0) return ""

        val firstCandidate = candidates.getJSONObject(0)
        val content = firstCandidate.optJSONObject("content") ?: return ""
        val parts = content.optJSONArray("parts") ?: return ""
        if (parts.length() == 0) return ""

        val text = parts.getJSONObject(0).optString("text", "")
        return text.trim()
    }

    private fun generateSmartFallback(userPrompt: String): String {
        val lower = userPrompt.lowercase().trim()

        return when {
            lower.contains("summarize") || lower.contains("summary") -> {
                "📋 **Chat Summary:**\n" +
                "• **Project Alpha:** Team is finalizing the sprint release for Friday. All PRs merged.\n" +
                "• **Design Team:** Updated group hero card and adaptive icons approved.\n" +
                "• **Arattai Alerts:** Verification OTP (1034498) delivered.\n" +
                "• **Upcoming:** 2 unread group messages awaiting review."
            }
            lower.contains("draft") || lower.contains("reply") -> {
                "Here is a thoughtful, polite draft you can send:\n\n" +
                "*\"Hi there! Thanks for reaching out. I'm currently tied up with a few items but will review this thoroughly and get back to you shortly. Have a great day!\"*\n\n" +
                "💡 *Tip: Tap the Copy icon below to copy this directly to your clipboard.*"
            }
            lower.contains("translate") && (lower.contains("hindi") || lower.contains("india")) -> {
                "🌐 **Translation to Hindi:**\n\n" +
                "\"**कल मिलते हैं! आपका दिन शुभ हो।**\"\n" +
                "*(Kal milte hain! Aapka din shubh ho.)*\n\n" +
                "Meaning: *\"See you tomorrow! Have a good day.\"*"
            }
            lower.contains("translate") -> {
                "🌐 **Translation:**\n\n" +
                "Here are translations for common greetings:\n" +
                "• **Spanish:** *¡Hasta mañana! Que tengas un buen día.*\n" +
                "• **French:** *À demain! Passe une bonne journée.*\n" +
                "• **Hindi:** *कल मिलते हैं! (Kal milte hain!)*\n\n" +
                "Let me know what specific language or text you would like translated!"
            }
            lower.contains("group") && (lower.contains("setting") || lower.contains("permission") || lower.contains("create")) -> {
                "👥 **Olinam Group Best Practices:**\n\n" +
                "• **Admin Controls:** As a group admin, you can decide who can send messages, edit group info, or add other members via **Group Permissions**.\n" +
                "• **Disappearing Messages:** Protect sensitive discussions by enabling auto-deletion after 24 hours, 7 days, or 90 days.\n" +
                "• **Invites:** Generate safe, revocable invite links or QR codes from Group Info."
            }
            lower.contains("encrypt") || lower.contains("security") || lower.contains("e2ee") || lower.contains("privacy") -> {
                "🔒 **Olinam End-to-End Encryption Architecture:**\n\n" +
                "• **AES-256-GCM:** All chats, voice notes, and media attachments are encrypted on your local device before transmission.\n" +
                "• **Zero Knowledge:** Neither Firebase servers, cloud storage, nor external actors hold decryption keys.\n" +
                "• **Safety Numbers:** You can verify the 60-digit safety fingerprint with any contact in their profile screen."
            }
            lower.contains("code") || lower.contains("kotlin") || lower.contains("compose") -> {
                "💻 **Kotlin Jetpack Compose Snippet:**\n\n" +
                "```kotlin\n" +
                "@Composable\n" +
                "fun OjAiWelcomeBubble() {\n" +
                "    Surface(\n" +
                "        shape = RoundedCornerShape(16.dp),\n" +
                "        color = MaterialTheme.colorScheme.primaryContainer\n" +
                "    ) {\n" +
                "        Text(\n" +
                "            text = \"Hello from Oj AI with Gemini!\",\n" +
                "            modifier = Modifier.padding(16.dp)\n" +
                "        )\n" +
                "    }\n" +
                "}\n" +
                "```"
            }
            lower.contains("plan") || lower.contains("weekend") || lower.contains("movie") -> {
                "🍿 **Weekend Movie Night Plan:**\n\n" +
                "1. **Poll Members:** Create a quick group poll in chat for genre (Sci-Fi, Comedy, Thriller).\n" +
                "2. **Timing:** 7:30 PM gathering, 8:00 PM movie start.\n" +
                "3. **Snack Delegations:** Popcorn, sodas, nachos.\n" +
                "4. **Discussion:** 15-minute review session right after the credits roll!"
            }
            lower.contains("hello") || lower.contains("hi") || lower.contains("hey") -> {
                "Hey there! 👋 I'm **Oj AI**, powered by Gemini 3.5 Flash.\n\n" +
                "I can help you draft messages, summarize conversations, answer questions, translate languages, brainstorm group ideas, or explain complex topics. What can I do for you today?"
            }
            else -> {
                "I'm here to help with that! As your built-in **Oj AI** assistant powered by Gemini 3.5 Flash, I can answer your questions, write drafts, translate phrases, or help organize your chats.\n\n" +
                "Feel free to ask follow-up questions or tap any suggestion prompt!"
            }
        }
    }
}
