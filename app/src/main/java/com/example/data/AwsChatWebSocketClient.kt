package com.example.data

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AwsIncomingChatMessage(
    val conversationId: String,
    val messageId: String,
    val senderId: String,
    val senderName: String,
    val ciphertext: String,
    val iv: String,
    val salt: String,
    val timestamp: Long
)

data class AwsTypingEvent(
    val conversationId: String,
    val userId: String,
    val isTyping: Boolean
)

/**
 * Production-ready AWS WebSocket Client for Real-Time Text Messaging,
 * live typing indicators, and presence heartbeats.
 * Operates over AWS API Gateway WebSocket endpoint.
 */
class AwsChatWebSocketClient(
    val endpointUrl: String = "wss://egjgcb3sb1.execute-api.ap-south-1.amazonaws.com/production"
) {
    private val tag = "AwsChatWebSocketClient"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var client: OkHttpClient? = null
    private var webSocket: WebSocket? = null
    private var currentUserId: String? = null

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _incomingMessages = MutableSharedFlow<AwsIncomingChatMessage>(extraBufferCapacity = 64)
    val incomingMessages: SharedFlow<AwsIncomingChatMessage> = _incomingMessages.asSharedFlow()

    private val _incomingTypingEvents = MutableSharedFlow<AwsTypingEvent>(extraBufferCapacity = 64)
    val incomingTypingEvents: SharedFlow<AwsTypingEvent> = _incomingTypingEvents.asSharedFlow()

    fun connect(userId: String) {
        if (_isConnected.value && currentUserId == userId) return
        currentUserId = userId

        try {
            client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.MILLISECONDS) // Keep alive
                .pingInterval(20, TimeUnit.SECONDS)    // Keep-alive ping for AWS API Gateway
                .build()

            val cleanUrl = endpointUrl.trimEnd('/')
            val request = Request.Builder()
                .url("$cleanUrl?userId=$userId")
                .build()

            webSocket = client?.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    Log.d(tag, "Connected to AWS WebSocket Messaging Gateway successfully!")
                    _isConnected.value = true
                    sendPresence(userId, true)
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    handleIncomingPayload(text)
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    Log.d(tag, "AWS WebSocket closing: $reason ($code)")
                    webSocket.close(1000, null)
                    _isConnected.value = false
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    Log.w(tag, "AWS WebSocket connection notice: ${t.message}")
                    _isConnected.value = false
                }
            })
        } catch (e: Exception) {
            Log.w(tag, "Failed to initialize AWS WebSocket: ${e.message}")
            _isConnected.value = false
        }
    }

    fun disconnect() {
        try {
            currentUserId?.let { sendPresence(it, false) }
            webSocket?.close(1000, "User logout")
            webSocket = null
            _isConnected.value = false
        } catch (e: Exception) {
            Log.e(tag, "Error closing AWS WebSocket: ${e.message}")
        }
    }

    fun sendChatMessage(
        conversationId: String,
        messageId: String,
        senderId: String,
        senderName: String,
        ciphertext: String,
        iv: String,
        salt: String,
        timestamp: Long
    ): Boolean {
        return try {
            val payload = JSONObject().apply {
                put("action", "sendmessage")
                put("type", "CHAT_MESSAGE")
                put("conversationId", conversationId)
                put("messageId", messageId)
                put("senderId", senderId)
                put("senderName", senderName)
                put("ciphertext", ciphertext)
                put("iv", iv)
                put("salt", salt)
                put("timestamp", timestamp)
            }
            val sent = webSocket?.send(payload.toString()) ?: false
            if (sent) {
                Log.d(tag, "Dispatched message $messageId over AWS WebSocket with zero latency")
            }
            sent
        } catch (e: Exception) {
            Log.w(tag, "Error sending over AWS WebSocket: ${e.message}")
            false
        }
    }

    fun sendTyping(conversationId: String, userId: String, isTyping: Boolean) {
        try {
            val payload = JSONObject().apply {
                put("action", "typing")
                put("type", "TYPING_STATUS")
                put("conversationId", conversationId)
                put("userId", userId)
                put("isTyping", isTyping)
            }
            webSocket?.send(payload.toString())
        } catch (_: Exception) {}
    }

    fun sendPresence(userId: String, isOnline: Boolean) {
        try {
            val payload = JSONObject().apply {
                put("action", "presence")
                put("type", "PRESENCE")
                put("userId", userId)
                put("isOnline", isOnline)
                put("timestamp", System.currentTimeMillis())
            }
            webSocket?.send(payload.toString())
        } catch (_: Exception) {}
    }

    fun registerFcmToken(userId: String, fcmToken: String) {
        try {
            val payload = JSONObject().apply {
                put("action", "register_token")
                put("type", "REGISTER_FCM_TOKEN")
                put("userId", userId)
                put("fcmToken", fcmToken)
                put("timestamp", System.currentTimeMillis())
            }
            webSocket?.send(payload.toString())
        } catch (_: Exception) {}
    }

    private fun handleIncomingPayload(rawJson: String) {
        try {
            val json = JSONObject(rawJson)
            val type = json.optString("type")

            when (type) {
                "CHAT_MESSAGE" -> {
                    val msg = AwsIncomingChatMessage(
                        conversationId = json.optString("conversationId"),
                        messageId = json.optString("messageId"),
                        senderId = json.optString("senderId"),
                        senderName = json.optString("senderName"),
                        ciphertext = json.optString("ciphertext"),
                        iv = json.optString("iv"),
                        salt = json.optString("salt"),
                        timestamp = json.optLong("timestamp", System.currentTimeMillis())
                    )
                    scope.launch { _incomingMessages.emit(msg) }
                }
                "TYPING_STATUS" -> {
                    val event = AwsTypingEvent(
                        conversationId = json.optString("conversationId"),
                        userId = json.optString("userId"),
                        isTyping = json.optBoolean("isTyping")
                    )
                    scope.launch { _incomingTypingEvents.emit(event) }
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "Failed to parse incoming AWS WebSocket message: ${e.message}")
        }
    }
}
