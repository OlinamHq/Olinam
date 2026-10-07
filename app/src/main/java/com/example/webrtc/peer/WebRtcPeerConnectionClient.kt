package com.example.webrtc.peer

import android.util.Log
import com.example.webrtc.model.WebRtcIceCandidate
import com.example.webrtc.model.WebRtcIceServerConfig
import com.example.webrtc.model.WebRtcSessionDescription
import org.webrtc.AudioTrack
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.SessionDescription
import org.webrtc.VideoTrack

class WebRtcPeerConnectionClient(
    private val factory: PeerConnectionFactory,
    private val onIceCandidateGenerated: (WebRtcIceCandidate) -> Unit,
    private val onConnectionStateChanged: (PeerConnection.PeerConnectionState) -> Unit,
    private val onRemoteVideoTrackReceived: (VideoTrack) -> Unit,
    private val onRemoteAudioTrackReceived: (AudioTrack) -> Unit
) {
    private val tag = "WebRtcPeerClient"
    var peerConnection: PeerConnection? = null
        private set

    fun initialize(iceServers: List<WebRtcIceServerConfig>) {
        val nativeIceServers = iceServers.map { it.toPeerConnectionIceServer() }

        val rtcConfig = PeerConnection.RTCConfiguration(nativeIceServers).apply {
            tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.ENABLED
            bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
            rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
            keyType = PeerConnection.KeyType.ECDSA
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        }

        val observer = object : WebRtcPeerConnectionObserver(
            onIceCandidateAction = { candidate ->
                onIceCandidateGenerated(WebRtcIceCandidate.fromOrgWebRtc(candidate))
            },
            onConnectionChangeAction = { state ->
                Log.d(tag, "PeerConnectionState changed: $state")
                onConnectionStateChanged(state)
            },
            onAddTrackAction = { receiver, _ ->
                val track = receiver.track()
                if (track is VideoTrack) {
                    Log.d(tag, "Remote VideoTrack received")
                    onRemoteVideoTrackReceived(track)
                } else if (track is AudioTrack) {
                    Log.d(tag, "Remote AudioTrack received")
                    onRemoteAudioTrackReceived(track)
                }
            }
        ) {}

        peerConnection = factory.createPeerConnection(rtcConfig, observer)
    }

    fun addLocalTracks(audioTrack: AudioTrack?, videoTrack: VideoTrack?) {
        val streamIds = listOf("olinam_media_stream")
        audioTrack?.let {
            peerConnection?.addTrack(it, streamIds)
        }
        videoTrack?.let {
            peerConnection?.addTrack(it, streamIds)
        }
    }

    fun createOffer(
        isVideo: Boolean,
        onSuccess: (WebRtcSessionDescription) -> Unit,
        onError: (String) -> Unit
    ) {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", if (isVideo) "true" else "false"))
        }

        peerConnection?.createOffer(
            object : WebRtcSdpObserver(
                onCreateSuccessAction = { desc ->
                    peerConnection?.setLocalDescription(object : WebRtcSdpObserver() {}, desc)
                    onSuccess(WebRtcSessionDescription.fromOrgWebRtc(desc))
                },
                onCreateFailureAction = { error ->
                    onError(error)
                }
            ) {},
            constraints
        )
    }

    fun createAnswer(
        isVideo: Boolean,
        onSuccess: (WebRtcSessionDescription) -> Unit,
        onError: (String) -> Unit
    ) {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", if (isVideo) "true" else "false"))
        }

        peerConnection?.createAnswer(
            object : WebRtcSdpObserver(
                onCreateSuccessAction = { desc ->
                    peerConnection?.setLocalDescription(object : WebRtcSdpObserver() {}, desc)
                    onSuccess(WebRtcSessionDescription.fromOrgWebRtc(desc))
                },
                onCreateFailureAction = { error ->
                    onError(error)
                }
            ) {},
            constraints
        )
    }

    fun setRemoteDescription(
        sdp: WebRtcSessionDescription,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val orgDesc = sdp.toOrgWebRtc()
        peerConnection?.setRemoteDescription(
            object : WebRtcSdpObserver(
                onSetSuccessAction = onSuccess,
                onSetFailureAction = onError
            ) {},
            orgDesc
        )
    }

    fun addRemoteIceCandidate(candidate: WebRtcIceCandidate) {
        peerConnection?.addIceCandidate(candidate.toOrgWebRtc())
    }

    fun close() {
        try {
            peerConnection?.dispose()
            peerConnection = null
        } catch (_: Exception) {}
    }
}
