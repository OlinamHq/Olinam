package com.example.webrtc.signaling

import com.example.webrtc.model.WebRtcCallType
import com.example.webrtc.model.WebRtcIceCandidate
import com.example.webrtc.model.WebRtcSessionDescription

enum class GoogleSignalingMessageType {
    OFFER,
    ANSWER,
    ICE_CANDIDATE,
    JOIN,
    LEAVE,
    DECLINE,
    END_CALL,
    HEARTBEAT
}

data class GoogleSignalingMessage(
    val type: GoogleSignalingMessageType,
    val callId: String,
    val senderId: String,
    val recipientId: String,
    val callType: WebRtcCallType = WebRtcCallType.AUDIO,
    val sdp: WebRtcSessionDescription? = null,
    val candidate: WebRtcIceCandidate? = null,
    val timestamp: Long = System.currentTimeMillis()
)
