package com.example.webrtc.peer

import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

open class WebRtcSdpObserver(
    private val onSetSuccessAction: () -> Unit = {},
    private val onCreateSuccessAction: (SessionDescription) -> Unit = {},
    private val onCreateFailureAction: (String) -> Unit = {},
    private val onSetFailureAction: (String) -> Unit = {}
) : SdpObserver {
    override fun onCreateSuccess(desc: SessionDescription) {
        onCreateSuccessAction(desc)
    }

    override fun onSetSuccess() {
        onSetSuccessAction()
    }

    override fun onCreateFailure(error: String?) {
        onCreateFailureAction(error ?: "Unknown SDP creation failure")
    }

    override fun onSetFailure(error: String?) {
        onSetFailureAction(error ?: "Unknown SDP set failure")
    }
}
