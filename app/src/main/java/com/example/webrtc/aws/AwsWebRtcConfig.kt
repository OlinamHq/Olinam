package com.example.webrtc.aws

/**
 * Configuration structure for AWS WebRTC infrastructure.
 *
 * Developers can configure their AWS Console resources:
 * 1. AWS API Gateway WebSocket endpoint OR AWS Kinesis Video Streams WebRTC Signaling Channel.
 * 2. AWS Region (e.g. us-east-1, ap-south-1).
 * 3. AWS TURN / STUN server endpoint (coturn on EC2 / ECS or Kinesis Video Streams TURN).
 */
data class AwsWebRtcConfig(
    val websocketSignalingUrl: String = "wss://api.olinam.aws/signaling", // Placeholder configured in AWS Console
    val awsRegion: String = "ap-south-1",
    val channelArn: String = "",
    val channelName: String = "olinam-calls-channel",
    val clientId: String = "",
    val turnServerEndpoint: String = "turn.olinam.aws",
    val turnUsername: String = "",
    val turnCredential: String = "",
    val isAwsSignalingEnabled: Boolean = false, // Set to true once AWS console endpoint is deployed
    val connectionTimeoutMs: Long = 10000L,
    val pingIntervalSeconds: Long = 20L
) {
    companion object {
        fun default(): AwsWebRtcConfig {
            return AwsWebRtcConfig(
                websocketSignalingUrl = "wss://echo.websocket.org", // Testable fallback
                awsRegion = "ap-south-1",
                channelName = "olinam-main-channel",
                isAwsSignalingEnabled = false
            )
        }
    }
}
