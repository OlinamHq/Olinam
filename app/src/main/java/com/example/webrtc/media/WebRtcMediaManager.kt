package com.example.webrtc.media

import android.content.Context
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.EglBase
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnectionFactory
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer
import org.webrtc.VideoSource
import org.webrtc.VideoTrack

class WebRtcMediaManager(
    private val context: Context,
    private val factory: PeerConnectionFactory,
    private val eglBase: EglBase
) {
    var localAudioTrack: AudioTrack? = null
        private set
    var localVideoTrack: VideoTrack? = null
        private set

    private var audioSource: AudioSource? = null
    private var videoSource: VideoSource? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null
    private var videoCapturer: VideoCapturer? = null
    private var cameraCapturerHelper: WebRtcCameraCapturer? = null

    var isMicMuted: Boolean = false
        private set
    var isVideoDisabled: Boolean = false
        private set

    fun initAudioTrack(): AudioTrack {
        val audioConstraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("googEchoCancellation", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googAutoGainControl", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googHighpassFilter", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googNoiseSuppression", "true"))
        }

        audioSource = factory.createAudioSource(audioConstraints)
        val track = factory.createAudioTrack("olinam_audio_track_local", audioSource)
        track.setEnabled(true)
        localAudioTrack = track
        return track
    }

    fun initVideoTrack(): VideoTrack? {
        cameraCapturerHelper = WebRtcCameraCapturer(context)
        val capturer = cameraCapturerHelper?.createCapturer() ?: return null
        videoCapturer = capturer

        surfaceTextureHelper = SurfaceTextureHelper.create("WebRtcCaptureThread", eglBase.eglBaseContext)
        videoSource = factory.createVideoSource(capturer.isScreencast)

        capturer.initialize(surfaceTextureHelper, context, videoSource?.capturerObserver)
        // 720p at 30 fps
        capturer.startCapture(1280, 720, 30)

        val track = factory.createVideoTrack("olinam_video_track_local", videoSource)
        track.setEnabled(true)
        localVideoTrack = track
        return track
    }

    fun toggleMicrophone(isMuted: Boolean) {
        isMicMuted = isMuted
        localAudioTrack?.setEnabled(!isMuted)
    }

    fun toggleVideo(isEnabled: Boolean) {
        isVideoDisabled = !isEnabled
        localVideoTrack?.setEnabled(isEnabled)
    }

    fun switchCamera(onComplete: (Boolean) -> Unit = {}) {
        cameraCapturerHelper?.switchCamera(onComplete)
    }

    fun isFrontCamera(): Boolean {
        return cameraCapturerHelper?.isFrontCamera ?: true
    }

    fun dispose() {
        try {
            videoCapturer?.stopCapture()
            videoCapturer?.dispose()
            videoCapturer = null

            surfaceTextureHelper?.dispose()
            surfaceTextureHelper = null

            videoSource?.dispose()
            videoSource = null

            localVideoTrack?.dispose()
            localVideoTrack = null

            audioSource?.dispose()
            audioSource = null

            localAudioTrack?.dispose()
            localAudioTrack = null
        } catch (_: Exception) {}
    }
}
