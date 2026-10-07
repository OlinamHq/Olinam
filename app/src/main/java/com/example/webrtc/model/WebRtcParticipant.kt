package com.example.webrtc.model

data class WebRtcParticipant(
    val userId: String,
    val displayName: String,
    val phoneNumber: String = "",
    val avatarUrl: String? = null,
    val isAudioMuted: Boolean = false,
    val isVideoMuted: Boolean = false,
    val isFrontCamera: Boolean = true
)
