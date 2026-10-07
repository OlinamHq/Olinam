package com.example.webrtc.model

enum class SdpType {
    OFFER,
    PRANSWER,
    ANSWER,
    ROLLBACK
}

data class WebRtcSessionDescription(
    val type: SdpType,
    val description: String
) {
    fun toOrgWebRtc(): org.webrtc.SessionDescription {
        val orgType = when (type) {
            SdpType.OFFER -> org.webrtc.SessionDescription.Type.OFFER
            SdpType.PRANSWER -> org.webrtc.SessionDescription.Type.PRANSWER
            SdpType.ANSWER -> org.webrtc.SessionDescription.Type.ANSWER
            SdpType.ROLLBACK -> org.webrtc.SessionDescription.Type.ROLLBACK
        }
        return org.webrtc.SessionDescription(orgType, description)
    }

    companion object {
        fun fromOrgWebRtc(sdp: org.webrtc.SessionDescription): WebRtcSessionDescription {
            val mappedType = when (sdp.type) {
                org.webrtc.SessionDescription.Type.OFFER -> SdpType.OFFER
                org.webrtc.SessionDescription.Type.PRANSWER -> SdpType.PRANSWER
                org.webrtc.SessionDescription.Type.ANSWER -> SdpType.ANSWER
                org.webrtc.SessionDescription.Type.ROLLBACK -> SdpType.ROLLBACK
                else -> SdpType.OFFER
            }
            return WebRtcSessionDescription(mappedType, sdp.description)
        }
    }
}
