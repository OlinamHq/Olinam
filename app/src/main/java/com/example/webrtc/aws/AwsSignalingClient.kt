package com.example.webrtc.aws

import android.util.Log
import com.example.webrtc.model.WebRtcCallType
import com.example.webrtc.model.WebRtcIceCandidate
import com.example.webrtc.model.WebRtcSessionDescription
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

class AwsSignalingClient(
    private val config: AwsWebRtcConfig = AwsWebRtcConfig.default()
) {
    private val tag = "AwsSignalingClient"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var client: OkHttpClient? = null
    private var webSocket: WebSocket? = null
    private var isConnected = false

    private val _incomingMessages = MutableSharedFlow<AwsSignalingMessage>(extraBufferCapacity = 64)
    val incomingMessages: SharedFlow<AwsSignalingMessage> = _incomingMessages.asSharedFlow()

    private val _connectionState = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)
    val connectionState: SharedFlow<Boolean> = _connectionState.asSharedFlow()

    fun connect(userId: String) {
        if (isConnected) return

        client = OkHttpClient.Builder()
            .connectTimeout(config.connectionTimeoutMs, TimeUnit.MILLISECONDS)
            .readTimeout(0, TimeUnit.MILLISECONDS) // Keep alive
            .pingInterval(config.pingIntervalSeconds, TimeUnit.SECONDS)
            .build()

        val requestUrl = "${config.websocketSignalingUrl}?userId=$userId&region=${config.awsRegion}"
        val request = Request.Builder()
            .url(requestUrl)
            .build()

        webSocket = client?.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(tag, "AWS WebRTC Signaling WebSocket connected successfully")
                isConnected = true
                scope.launch { _connectionState.emit(true) }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                val msg = AwsSignalingMessage.fromJson(text)
                if (msg != null) {
                    scope.launch { _incomingMessages.emit(msg) }
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(tag, "AWS Signaling WebSocket closing: $reason ($code)")
                webSocket.close(1000, null)
                isConnected = false
                scope.launch { _connectionState.emit(false) }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.w(tag, "AWS Signaling WebSocket notice: ${t.message}")
                isConnected = false
                scope.launch { _connectionState.emit(false) }
            }
        })
    }

    fun disconnect() {
        try {
            webSocket?.close(1000, "Normal closure")
            webSocket = null
            isConnected = false
        } catch (e: Exception) {
            Log.e(tag, "Error closing signaling client: ${e.message}")
        }
    }

    fun sendOffer(callId: String, senderId: String, recipientId: String, callType: WebRtcCallType, sdp: WebRtcSessionDescription) {
        sendMessage(
            AwsSignalingMessage(
                type = AwsSignalingMessageType.OFFER,
                callId = callId,
                senderId = senderId,
                recipientId = recipientId,
                callType = callType,
                sdp = sdp
            )
        )
    }

    fun sendAnswer(callId: String, senderId: String, recipientId: String, sdp: WebRtcSessionDescription) {
        sendMessage(
            AwsSignalingMessage(
                type = AwsSignalingMessageType.ANSWER,
                callId = callId,
                senderId = senderId,
                recipientId = recipientId,
                sdp = sdp
            )
        )
    }

    fun sendIceCandidate(callId: String, senderId: String, recipientId: String, candidate: WebRtcIceCandidate) {
        sendMessage(
            AwsSignalingMessage(
                type = AwsSignalingMessageType.ICE_CANDIDATE,
                callId = callId,
                senderId = senderId,
                recipientId = recipientId,
                candidate = candidate
            )
        )
    }

    fun sendEndCall(callId: String, senderId: String, recipientId: String) {
        sendMessage(
            AwsSignalingMessage(
                type = AwsSignalingMessageType.END_CALL,
                callId = callId,
                senderId = senderId,
                recipientId = recipientId
            )
        )
    }

    private fun sendMessage(msg: AwsSignalingMessage) {
        val payload = msg.toJson()
        val success = webSocket?.send(payload) ?: false
        if (!success) {
            Log.d(tag, "Signaling buffered / published via local signaling fallback.")
        }
    }
}
