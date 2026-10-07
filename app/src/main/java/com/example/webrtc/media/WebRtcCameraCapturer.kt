package com.example.webrtc.media

import android.content.Context
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.VideoCapturer

class WebRtcCameraCapturer(private val context: Context) {
    private var videoCapturer: CameraVideoCapturer? = null
    var isFrontCamera = true
        private set

    fun createCapturer(): VideoCapturer? {
        val enumerator = Camera2Enumerator(context)
        val deviceNames = enumerator.deviceNames

        // Try to find front-facing camera first
        for (deviceName in deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                videoCapturer = enumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    isFrontCamera = true
                    return videoCapturer
                }
            }
        }

        // Fallback to back-facing camera
        for (deviceName in deviceNames) {
            if (enumerator.isBackFacing(deviceName)) {
                videoCapturer = enumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    isFrontCamera = false
                    return videoCapturer
                }
            }
        }

        return null
    }

    fun switchCamera(onComplete: (Boolean) -> Unit = {}) {
        videoCapturer?.switchCamera(object : CameraVideoCapturer.CameraSwitchHandler {
            override fun onCameraSwitchDone(isFront: Boolean) {
                isFrontCamera = isFront
                onComplete(isFront)
            }

            override fun onCameraSwitchError(errorDescription: String?) {
                onComplete(isFrontCamera)
            }
        })
    }

    fun dispose() {
        try {
            videoCapturer?.stopCapture()
            videoCapturer?.dispose()
            videoCapturer = null
        } catch (_: Exception) {}
    }
}
