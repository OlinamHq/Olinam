package com.example.webrtc.call

import android.content.Context
import android.util.Log
import com.example.webrtc.media.AudioDevice
import com.example.webrtc.media.WebRtcAudioSwitch
import com.example.webrtc.media.WebRtcMediaManager
import com.example.webrtc.model.WebRtcCallState
import com.example.webrtc.model.WebRtcCallType
import com.example.webrtc.model.WebRtcParticipant
import com.example.webrtc.peer.WebRtcPeerConnectionClient
import com.example.webrtc.peer.WebRtcPeerConnectionFactory
import com.example.webrtc.signaling.GoogleCloudIceServerService
import com.example.webrtc.signaling.GoogleCloudSignalingClient
import com.example.webrtc.signaling.GoogleSignalingMessage
import com.example.webrtc.signaling.GoogleSignalingMessageType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.webrtc.EglBase
import org.webrtc.PeerConnection
import org.webrtc.VideoTrack
import java.util.UUID

class WebRtcCallManager(
    private val context: Context
) {
    private val tag = "WebRtcCallManager"
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // WebRTC Core
    private val rtcFactory = WebRtcPeerConnectionFactory(context)
    val eglBaseContext: EglBase.Context get() = rtcFactory.eglBase.eglBaseContext

    private val mediaManager = WebRtcMediaManager(context, rtcFactory.factory, rtcFactory.eglBase)
    private val audioSwitch = WebRtcAudioSwitch(context)
    private val ringtoneManager = WebRtcRingtoneManager(context)
    private val iceService = GoogleCloudIceServerService()
    private val googleSignalingClient = GoogleCloudSignalingClient()

    private var peerClient: WebRtcPeerConnectionClient? = null

    // Call States
    private val _callState = MutableStateFlow(WebRtcCallState.IDLE)
    val callState: StateFlow<WebRtcCallState> = _callState.asStateFlow()

    private val _activeParticipant = MutableStateFlow<WebRtcParticipant?>(null)
    val activeParticipant: StateFlow<WebRtcParticipant?> = _activeParticipant.asStateFlow()

    private val _callType = MutableStateFlow(WebRtcCallType.AUDIO)
    val callType: StateFlow<WebRtcCallType> = _callType.asStateFlow()

    private val _remoteVideoTrack = MutableStateFlow<VideoTrack?>(null)
    val remoteVideoTrack: StateFlow<VideoTrack?> = _remoteVideoTrack.asStateFlow()

    private val _localVideoTrack = MutableStateFlow<VideoTrack?>(null)
    val localVideoTrack: StateFlow<VideoTrack?> = _localVideoTrack.asStateFlow()

    private val _durationSeconds = MutableStateFlow(0L)
    val durationSeconds: StateFlow<Long> = _durationSeconds.asStateFlow()

    private val _isSpeakerOn = MutableStateFlow(false)
    val isSpeakerOn: StateFlow<Boolean> = _isSpeakerOn.asStateFlow()

    private val _isMicMuted = MutableStateFlow(false)
    val isMicMuted: StateFlow<Boolean> = _isMicMuted.asStateFlow()

    private val _isVideoDisabled = MutableStateFlow(false)
    val isVideoDisabled: StateFlow<Boolean> = _isVideoDisabled.asStateFlow()

    private val _isFrontCamera = MutableStateFlow(true)
    val isFrontCamera: StateFlow<Boolean> = _isFrontCamera.asStateFlow()

    private var activeCallId: String = ""
    private var currentUserId: String = ""
    private var durationJob: Job? = null

    init {
        // Observe Google Cloud Signaling messages
        scope.launch {
            googleSignalingClient.incomingMessages.collect { msg ->
                handleIncomingSignalingMessage(msg)
            }
        }
    }

    fun startOutgoingCall(
        senderId: String,
        targetParticipant: WebRtcParticipant,
        type: WebRtcCallType
    ) {
        currentUserId = senderId
        activeCallId = "call_${UUID.randomUUID().toString().take(8)}"
        _activeParticipant.value = targetParticipant
        _callType.value = type
        _callState.value = WebRtcCallState.OUTGOING_RINGING
        _durationSeconds.value = 0L

        ringtoneManager.startOutgoingRingback()
        audioSwitch.start(isSpeakerDefault = type == WebRtcCallType.VIDEO)
        _isSpeakerOn.value = type == WebRtcCallType.VIDEO

        // Connect Google Cloud signaling
        googleSignalingClient.connect(senderId)
        googleSignalingClient.startListeningForCall(activeCallId, senderId)

        scope.launch {
            setupPeerConnectionAndTracks(type)

            // Create and send SDP Offer over Google Cloud
            peerClient?.createOffer(
                isVideo = type == WebRtcCallType.VIDEO,
                onSuccess = { sdp ->
                    Log.d(tag, "Local SDP Offer created successfully")
                    googleSignalingClient.sendOffer(
                        callId = activeCallId,
                        senderId = senderId,
                        recipientId = targetParticipant.userId,
                        callType = type,
                        sdp = sdp
                    )
                },
                onError = { err ->
                    Log.e(tag, "Error creating SDP Offer: $err")
                }
            )
        }
    }

    fun receiveIncomingCall(
        callId: String,
        caller: WebRtcParticipant,
        type: WebRtcCallType
    ) {
        activeCallId = callId
        _activeParticipant.value = caller
        _callType.value = type
        _callState.value = WebRtcCallState.INCOMING_RINGING
        _durationSeconds.value = 0L

        ringtoneManager.startIncomingRinging()
        googleSignalingClient.startListeningForCall(callId, currentUserId)
    }

    fun answerIncomingCall() {
        ringtoneManager.stopAll()
        val type = _callType.value
        _callState.value = WebRtcCallState.CONNECTING

        audioSwitch.start(isSpeakerDefault = type == WebRtcCallType.VIDEO)
        _isSpeakerOn.value = type == WebRtcCallType.VIDEO

        scope.launch {
            setupPeerConnectionAndTracks(type)

            peerClient?.createAnswer(
                isVideo = type == WebRtcCallType.VIDEO,
                onSuccess = { sdp ->
                    val participant = _activeParticipant.value
                    if (participant != null) {
                        googleSignalingClient.sendAnswer(
                            callId = activeCallId,
                            senderId = currentUserId,
                            recipientId = participant.userId,
                            sdp = sdp
                        )
                    }
                },
                onError = { err ->
                    Log.e(tag, "Error creating SDP Answer: $err")
                }
            )
        }
    }

    fun declineIncomingCall() {
        ringtoneManager.stopAll()
        val participant = _activeParticipant.value
        if (participant != null) {
            googleSignalingClient.sendEndCall(activeCallId, currentUserId, participant.userId)
        }
        teardownCall()
    }

    fun endCall() {
        ringtoneManager.playDisconnectTone()
        val participant = _activeParticipant.value
        if (participant != null) {
            googleSignalingClient.sendEndCall(activeCallId, currentUserId, participant.userId)
        }
        teardownCall()
    }

    private suspend fun setupPeerConnectionAndTracks(type: WebRtcCallType) {
        val iceServers = iceService.getIceServers()

        peerClient = WebRtcPeerConnectionClient(
            factory = rtcFactory.factory,
            onIceCandidateGenerated = { candidate ->
                val participant = _activeParticipant.value
                if (participant != null) {
                    googleSignalingClient.sendIceCandidate(
                        callId = activeCallId,
                        senderId = currentUserId,
                        recipientId = participant.userId,
                        candidate = candidate
                    )
                }
            },
            onConnectionStateChanged = { state ->
                scope.launch {
                    when (state) {
                        PeerConnection.PeerConnectionState.CONNECTED -> {
                            ringtoneManager.stopAll()
                            _callState.value = WebRtcCallState.CONNECTED
                            startDurationCounter()
                        }
                        PeerConnection.PeerConnectionState.CONNECTING -> {
                            _callState.value = WebRtcCallState.CONNECTING
                        }
                        PeerConnection.PeerConnectionState.DISCONNECTED,
                        PeerConnection.PeerConnectionState.FAILED -> {
                            endCall()
                        }
                        PeerConnection.PeerConnectionState.CLOSED -> {
                            teardownCall()
                        }
                        else -> {}
                    }
                }
            },
            onRemoteVideoTrackReceived = { track ->
                scope.launch {
                    _remoteVideoTrack.value = track
                }
            },
            onRemoteAudioTrackReceived = {
                // Audio is automatically played via WebRTC JavaAudioDeviceModule
            }
        )

        peerClient?.initialize(iceServers)

        // Local media
        val audioTrack = mediaManager.initAudioTrack()
        val videoTrack = if (type == WebRtcCallType.VIDEO) {
            val vTrack = mediaManager.initVideoTrack()
            _localVideoTrack.value = vTrack
            vTrack
        } else null

        peerClient?.addLocalTracks(audioTrack, videoTrack)
    }

    private fun handleIncomingSignalingMessage(msg: GoogleSignalingMessage) {
        when (msg.type) {
            GoogleSignalingMessageType.OFFER -> {
                msg.sdp?.let { sdp ->
                    val caller = WebRtcParticipant(
                        userId = msg.senderId,
                        displayName = "Olinam Contact",
                        phoneNumber = msg.senderId
                    )
                    receiveIncomingCall(msg.callId, caller, msg.callType)
                    peerClient?.setRemoteDescription(sdp)
                }
            }
            GoogleSignalingMessageType.ANSWER -> {
                msg.sdp?.let { sdp ->
                    peerClient?.setRemoteDescription(sdp, onSuccess = {
                        _callState.value = WebRtcCallState.CONNECTED
                        ringtoneManager.stopAll()
                        startDurationCounter()
                    })
                }
            }
            GoogleSignalingMessageType.ICE_CANDIDATE -> {
                msg.candidate?.let { cand ->
                    peerClient?.addRemoteIceCandidate(cand)
                }
            }
            GoogleSignalingMessageType.END_CALL,
            GoogleSignalingMessageType.DECLINE -> {
                ringtoneManager.playDisconnectTone()
                teardownCall()
            }
            else -> {}
        }
    }

    private fun startDurationCounter() {
        durationJob?.cancel()
        durationJob = scope.launch {
            while (_callState.value == WebRtcCallState.CONNECTED) {
                delay(1000)
                _durationSeconds.value = _durationSeconds.value + 1
            }
        }
    }

    fun toggleMute() {
        val newMute = !_isMicMuted.value
        _isMicMuted.value = newMute
        mediaManager.toggleMicrophone(newMute)
    }

    fun toggleSpeaker() {
        val isNowSpeaker = audioSwitch.toggleSpeaker()
        _isSpeakerOn.value = isNowSpeaker
    }

    fun toggleVideo() {
        val newVideo = _isVideoDisabled.value
        _isVideoDisabled.value = !newVideo
        mediaManager.toggleVideo(newVideo)
    }

    fun switchCamera() {
        mediaManager.switchCamera { isFront ->
            _isFrontCamera.value = isFront
        }
    }

    private fun teardownCall() {
        durationJob?.cancel()
        durationJob = null
        ringtoneManager.stopAll()
        audioSwitch.stop()

        peerClient?.close()
        peerClient = null

        mediaManager.dispose()
        _localVideoTrack.value = null
        _remoteVideoTrack.value = null

        _callState.value = WebRtcCallState.IDLE
        _activeParticipant.value = null
        _durationSeconds.value = 0L
        _isMicMuted.value = false
        _isSpeakerOn.value = false
        _isVideoDisabled.value = false

        googleSignalingClient.disconnect()
    }
}
