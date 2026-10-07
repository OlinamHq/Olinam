package com.example.webrtc.peer

import android.content.Context
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.PeerConnectionFactory
import org.webrtc.audio.AudioDeviceModule
import org.webrtc.audio.JavaAudioDeviceModule

class WebRtcPeerConnectionFactory(
    private val context: Context,
    val eglBase: EglBase = EglBase.create()
) {
    private var audioDeviceModule: JavaAudioDeviceModule? = null

    val factory: PeerConnectionFactory by lazy {
        initFactory()
    }

    private fun initFactory(): PeerConnectionFactory {
        val initOptions = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .setFieldTrials("WebRTC-H264HighProfile/Enabled/")
            .createInitializationOptions()
        PeerConnectionFactory.initialize(initOptions)

        // Only enable hardware AEC and Noise Suppression if supported by the underlying device hardware
        val isAecSupported = try {
            JavaAudioDeviceModule.isBuiltInAcousticEchoCancelerSupported()
        } catch (_: Exception) {
            false
        }
        val isNsSupported = try {
            JavaAudioDeviceModule.isBuiltInNoiseSuppressorSupported()
        } catch (_: Exception) {
            false
        }

        val adm = JavaAudioDeviceModule.builder(context)
            .setUseHardwareAcousticEchoCanceler(isAecSupported)
            .setUseHardwareNoiseSuppressor(isNsSupported)
            .createAudioDeviceModule()
        audioDeviceModule = adm

        val videoEncoderFactory = DefaultVideoEncoderFactory(
            eglBase.eglBaseContext,
            true, // enableIntelVp8Encoder
            true  // enableH264HighProfile
        )

        val videoDecoderFactory = DefaultVideoDecoderFactory(eglBase.eglBaseContext)

        val options = PeerConnectionFactory.Options()

        return PeerConnectionFactory.builder()
            .setOptions(options)
            .setAudioDeviceModule(adm)
            .setVideoEncoderFactory(videoEncoderFactory)
            .setVideoDecoderFactory(videoDecoderFactory)
            .createPeerConnectionFactory()
    }

    fun release() {
        try {
            factory.dispose()
            audioDeviceModule?.release()
            eglBase.release()
        } catch (_: Exception) {}
    }
}
