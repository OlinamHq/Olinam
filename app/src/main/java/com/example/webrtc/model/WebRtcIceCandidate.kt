package com.example.webrtc.model

data class WebRtcIceCandidate(
    val sdpMid: String,
    val sdpMLineIndex: Int,
    val sdp: String,
    val serverUrl: String = ""
) {
    fun toOrgWebRtc(): org.webrtc.IceCandidate {
        return org.webrtc.IceCandidate(sdpMid, sdpMLineIndex, sdp)
    }

    companion object {
        fun fromOrgWebRtc(candidate: org.webrtc.IceCandidate): WebRtcIceCandidate {
            return WebRtcIceCandidate(
                sdpMid = candidate.sdpMid ?: "",
                sdpMLineIndex = candidate.sdpMLineIndex,
                sdp = candidate.sdp ?: "",
                serverUrl = candidate.serverUrl ?: ""
            )
        }
    }
}
