package com.example.webrtc.aws

/**
 * Configuration structure for AWS WebRTC & WebSocket messaging infrastructure.
 * Configured with live deployed AWS API Gateway WebSocket endpoint.
 */
data class AwsWebRtcConfig(
    val websocketSignalingUrl: String = "wss://egjgcb3sb1.execute-api.ap-south-1.amazonaws.com/production",
    val awsRegion: String = "ap-south-1",
    val channelArn: String = "",
    val channelName: String = "olinam-calls-channel",
    val clientId: String = "",
    val turnServerEndpoint: String = "13.127.255.11",
    val turnUsername: String = "olinamuser",
    val turnCredential: String = "olinampassword123",
    val isAwsSignalingEnabled: Boolean = true,
    val connectionTimeoutMs: Long = 10000L,
    val pingIntervalSeconds: Long = 20L
) {
    companion object {
        fun default(): AwsWebRtcConfig {
            return AwsWebRtcConfig(
                websocketSignalingUrl = "wss://egjgcb3sb1.execute-api.ap-south-1.amazonaws.com/production",
                awsRegion = "ap-south-1",
                channelName = "olinam-main-channel",
                turnServerEndpoint = "13.127.255.11",
                turnUsername = "olinamuser",
                turnCredential = "olinampassword123",
                isAwsSignalingEnabled = true
            )
        }
    }
}
