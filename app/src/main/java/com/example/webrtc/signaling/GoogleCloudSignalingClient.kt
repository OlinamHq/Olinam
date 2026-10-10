package com.example.webrtc.signaling

import android.util.Log
import com.example.webrtc.model.WebRtcCallType
import com.example.webrtc.model.WebRtcIceCandidate
import com.example.webrtc.model.WebRtcSessionDescription
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Enterprise Google Cloud / Firebase Firestore Signaling Client for WebRtc.
 * Completely replaces AWS API Gateway WebSocket signaling with native Google Cloud
 * Firestore real-time collections and snapshot listeners.
 */
class GoogleCloudSignalingClient {

    private val tag = "GCloudSignaling"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var activeCallId: String? = null
    private var currentUserId: String? = null
    private var signalingListener: ListenerRegistration? = null

    private val _incomingMessages = MutableSharedFlow<GoogleSignalingMessage>(extraBufferCapacity = 64)
    val incomingMessages: SharedFlow<GoogleSignalingMessage> = _incomingMessages.asSharedFlow()

    private val _connectionState = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)
    val connectionState: SharedFlow<Boolean> = _connectionState.asSharedFlow()

    fun connect(userId: String) {
        currentUserId = userId
        scope.launch {
            _connectionState.emit(true)
        }
        Log.d(tag, "Google Cloud signaling client ready for user: $userId")
    }

    fun startListeningForCall(callId: String, currentUserId: String) {
        activeCallId = callId
        this.currentUserId = currentUserId
        signalingListener?.remove()

        val db = try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(tag, "Firestore instance not available yet: ${e.message}")
            return
        }

        try {
            signalingListener = db.collection("webrtc_signaling")
                .document(callId)
                .collection("messages")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(tag, "Signaling snapshot listener error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && !snapshot.isEmpty) {
                        for (docChange in snapshot.documentChanges) {
                            if (docChange.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                                val doc = docChange.document
                                val senderId = doc.getString("senderId") ?: ""
                                // Only process messages targeted for current user or sent by peer
                                if (senderId != currentUserId) {
                                    val msg = parseSignalingDoc(doc)
                                    if (msg != null) {
                                        scope.launch {
                                            _incomingMessages.emit(msg)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            Log.d(tag, "Listening to Google Cloud signaling messages for call: $callId")
        } catch (e: Exception) {
            Log.e(tag, "Failed to start signaling listener: ${e.message}")
        }
    }

    fun sendOffer(
        callId: String,
        senderId: String,
        recipientId: String,
        callType: WebRtcCallType,
        sdp: WebRtcSessionDescription
    ) {
        startListeningForCall(callId, senderId)
        val payload = hashMapOf<String, Any>(
            "type" to "OFFER",
            "callId" to callId,
            "senderId" to senderId,
            "recipientId" to recipientId,
            "callType" to callType.name,
            "sdpType" to sdp.type.name,
            "sdpDescription" to sdp.description,
            "timestamp" to System.currentTimeMillis()
        )
        dispatchMessage(callId, payload)
    }

    fun sendAnswer(
        callId: String,
        senderId: String,
        recipientId: String,
        sdp: WebRtcSessionDescription
    ) {
        startListeningForCall(callId, senderId)
        val payload = hashMapOf<String, Any>(
            "type" to "ANSWER",
            "callId" to callId,
            "senderId" to senderId,
            "recipientId" to recipientId,
            "sdpType" to sdp.type.name,
            "sdpDescription" to sdp.description,
            "timestamp" to System.currentTimeMillis()
        )
        dispatchMessage(callId, payload)
    }

    fun sendIceCandidate(
        callId: String,
        senderId: String,
        recipientId: String,
        candidate: WebRtcIceCandidate
    ) {
        val payload = hashMapOf<String, Any>(
            "type" to "ICE_CANDIDATE",
            "callId" to callId,
            "senderId" to senderId,
            "recipientId" to recipientId,
            "candidateSdpMid" to candidate.sdpMid,
            "candidateSdpMLineIndex" to candidate.sdpMLineIndex,
            "candidateSdp" to candidate.sdp,
            "candidateServerUrl" to (candidate.serverUrl ?: ""),
            "timestamp" to System.currentTimeMillis()
        )
        dispatchMessage(callId, payload)
    }

    fun sendEndCall(callId: String, senderId: String, recipientId: String) {
        val payload = hashMapOf<String, Any>(
            "type" to "END_CALL",
            "callId" to callId,
            "senderId" to senderId,
            "recipientId" to recipientId,
            "timestamp" to System.currentTimeMillis()
        )
        dispatchMessage(callId, payload)
    }

    private fun dispatchMessage(callId: String, payload: Map<String, Any>) {
        scope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("webrtc_signaling")
                    .document(callId)
                    .collection("messages")
                    .add(payload)
                    .addOnSuccessListener {
                        Log.d(tag, "Google Cloud signaling message sent: ${payload["type"]}")
                    }
                    .addOnFailureListener { e ->
                        Log.w(tag, "Failed to send signaling message: ${e.message}")
                    }
            } catch (e: Exception) {
                Log.w(tag, "Error in dispatchMessage: ${e.message}")
            }
        }
    }

    private fun parseSignalingDoc(doc: com.google.firebase.firestore.DocumentSnapshot): GoogleSignalingMessage? {
        return try {
            val typeStr = doc.getString("type") ?: return null
            val type = GoogleSignalingMessageType.valueOf(typeStr)
            val callId = doc.getString("callId") ?: ""
            val senderId = doc.getString("senderId") ?: ""
            val recipientId = doc.getString("recipientId") ?: ""
            val callTypeStr = doc.getString("callType") ?: "AUDIO"
            val callType = if (callTypeStr == "VIDEO") WebRtcCallType.VIDEO else WebRtcCallType.AUDIO
            val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()

            val sdpDescription = doc.getString("sdpDescription")
            val sdp = if (!sdpDescription.isNullOrEmpty()) {
                val sdpTypeStr = doc.getString("sdpType") ?: "OFFER"
                val sdpType = try {
                    com.example.webrtc.model.SdpType.valueOf(sdpTypeStr)
                } catch (_: Exception) {
                    com.example.webrtc.model.SdpType.OFFER
                }
                WebRtcSessionDescription(type = sdpType, description = sdpDescription)
            } else null

            val candidateSdp = doc.getString("candidateSdp")
            val candidate = if (!candidateSdp.isNullOrEmpty()) {
                WebRtcIceCandidate(
                    sdpMid = doc.getString("candidateSdpMid") ?: "",
                    sdpMLineIndex = doc.getLong("candidateSdpMLineIndex")?.toInt() ?: 0,
                    sdp = candidateSdp,
                    serverUrl = doc.getString("candidateServerUrl") ?: ""
                )
            } else null

            GoogleSignalingMessage(
                type = type,
                callId = callId,
                senderId = senderId,
                recipientId = recipientId,
                callType = callType,
                sdp = sdp,
                candidate = candidate,
                timestamp = timestamp
            )
        } catch (e: Exception) {
            Log.w(tag, "Error parsing signaling doc: ${e.message}")
            null
        }
    }

    fun disconnect() {
        signalingListener?.remove()
        signalingListener = null
        activeCallId = null
        scope.launch {
            _connectionState.emit(false)
        }
        Log.d(tag, "Google Cloud signaling client disconnected")
    }
}
