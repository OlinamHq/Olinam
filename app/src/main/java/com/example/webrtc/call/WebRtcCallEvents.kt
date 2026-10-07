package com.example.webrtc.call

import com.example.webrtc.model.WebRtcCallState
import com.example.webrtc.model.WebRtcCallType
import com.example.webrtc.model.WebRtcParticipant
import org.webrtc.VideoTrack

sealed interface WebRtcCallEvent {
    data class StateChanged(val state: WebRtcCallState) : WebRtcCallEvent
    data class IncomingCall(
        val callId: String,
        val caller: WebRtcParticipant,
        val callType: WebRtcCallType
    ) : WebRtcCallEvent
    data class RemoteVideoTrackReady(val videoTrack: VideoTrack) : WebRtcCallEvent
    data class LocalVideoTrackReady(val videoTrack: VideoTrack) : WebRtcCallEvent
    data class DurationTick(val durationSeconds: Long) : WebRtcCallEvent
    data class Error(val message: String) : WebRtcCallEvent
    data object CallEnded : WebRtcCallEvent
}
