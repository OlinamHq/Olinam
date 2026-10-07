package com.example.webrtc.peer

import org.webrtc.CandidatePairChangeEvent
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.RtpReceiver
import org.webrtc.RtpTransceiver

open class WebRtcPeerConnectionObserver(
    private val onIceCandidateAction: (IceCandidate) -> Unit = {},
    private val onIceConnectionChangeAction: (PeerConnection.IceConnectionState) -> Unit = {},
    private val onConnectionChangeAction: (PeerConnection.PeerConnectionState) -> Unit = {},
    private val onAddTrackAction: (RtpReceiver, Array<out MediaStream>) -> Unit = { _, _ -> },
    private val onRemoveTrackAction: (RtpReceiver) -> Unit = {}
) : PeerConnection.Observer {

    override fun onSignalingChange(newState: PeerConnection.SignalingState?) {}

    override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState?) {
        if (newState != null) {
            onIceConnectionChangeAction(newState)
        }
    }

    override fun onStandardizedIceConnectionChange(newState: PeerConnection.IceConnectionState?) {}

    override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
        if (newState != null) {
            onConnectionChangeAction(newState)
        }
    }

    override fun onIceConnectionReceivingChange(receiving: Boolean) {}

    override fun onIceGatheringChange(newState: PeerConnection.IceGatheringState?) {}

    override fun onIceCandidate(candidate: IceCandidate?) {
        if (candidate != null) {
            onIceCandidateAction(candidate)
        }
    }

    override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {}

    override fun onAddStream(stream: MediaStream?) {}

    override fun onRemoveStream(stream: MediaStream?) {}

    override fun onDataChannel(dataChannel: DataChannel?) {}

    override fun onRenegotiationNeeded() {}

    override fun onAddTrack(receiver: RtpReceiver?, mediaStreams: Array<out MediaStream>?) {
        if (receiver != null && mediaStreams != null) {
            onAddTrackAction(receiver, mediaStreams)
        }
    }

    override fun onTrack(transceiver: RtpTransceiver?) {}

    override fun onSelectedCandidatePairChanged(event: CandidatePairChangeEvent?) {}
}
