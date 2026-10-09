package com.example.webrtc.aws

/**
 * Configuration structure for AWS WebRTC & WebSocket messaging infrastructure.
 * Configured with live deployed AWS API Gateway WebSocket endpoint.
 */
data class AwsWebRtcConfig(
    val websocketSignalingUrl: String = "wss://kvtrq0bydd.execute-api.ap-southeast-2.amazonaws.com/production",
    val awsRegion: String = "ap-southeast-2",
    val channelArn: String = "",
    val channelName: String = "olinam-calls-channel",
    val clientId: String = "",
    val turnServerEndpoint: String = "turn.olinam.aws",
    val turnUsername: String = "",
    val turnCredential: String = "",
    val isAwsSignalingEnabled: Boolean = true,
    val connectionTimeoutMs: Long = 10000L,
    val pingIntervalSeconds: Long = 20L
) {
    companion object {
        fun default(): AwsWebRtcConfig {
            return AwsWebRtcConfig(
                websocketSignalingUrl = "wss://kvtrq0bydd.execute-api.ap-southeast-2.amazonaws.com/production",
                awsRegion = "ap-southeast-2",
                channelName = "olinam-main-channel",
                isAwsSignalingEnabled = true
            )
        }
    }
}
