package com.example.webrtc.model

import org.webrtc.PeerConnection

data class WebRtcIceServerConfig(
    val uri: String,
    val username: String? = null,
    val password: String? = null,
    val tlsCertPolicy: PeerConnection.TlsCertPolicy = PeerConnection.TlsCertPolicy.TLS_CERT_POLICY_SECURE
) {
    fun toPeerConnectionIceServer(): PeerConnection.IceServer {
        val builder = PeerConnection.IceServer.builder(uri)
        if (!username.isNullOrEmpty()) {
            builder.setUsername(username)
        }
        if (!password.isNullOrEmpty()) {
            builder.setPassword(password)
        }
        builder.setTlsCertPolicy(tlsCertPolicy)
        return builder.createIceServer()
    }

    companion object {
        fun defaultServers(): List<WebRtcIceServerConfig> {
            return listOf(
                WebRtcIceServerConfig(uri = "stun:stun.l.google.com:19302"),
                WebRtcIceServerConfig(uri = "stun:stun1.l.google.com:19302"),
                WebRtcIceServerConfig(uri = "stun:stun2.l.google.com:19302"),
                WebRtcIceServerConfig(uri = "stun:stun.relay.metered.ca:80")
            )
        }

        fun awsTurnServers(
            turnEndpoint: String,
            turnUsername: String,
            turnCredential: String
        ): List<WebRtcIceServerConfig> {
            return listOf(
                WebRtcIceServerConfig(uri = "stun:$turnEndpoint:3478"),
                WebRtcIceServerConfig(
                    uri = "turn:$turnEndpoint:3478?transport=udp",
                    username = turnUsername,
                    password = turnCredential
                ),
                WebRtcIceServerConfig(
                    uri = "turn:$turnEndpoint:3478?transport=tcp",
                    username = turnUsername,
                    password = turnCredential
                ),
                WebRtcIceServerConfig(
                    uri = "turns:$turnEndpoint:5349?transport=tcp",
                    username = turnUsername,
                    password = turnCredential
                )
            )
        }
    }
}
