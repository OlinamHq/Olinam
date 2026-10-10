package com.example.webrtc.aws

import com.example.webrtc.model.WebRtcIceServerConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Service responsible for resolving ICE (STUN & TURN) servers.
 * Connects to AWS Kinesis Video Streams GetIceServerConfig API or provides
 * configured AWS coturn server configurations.
 */
class AwsTurnCredentialService(
    private val config: AwsWebRtcConfig = AwsWebRtcConfig.default()
) {
    suspend fun getIceServers(): List<WebRtcIceServerConfig> = withContext(Dispatchers.IO) {
        val servers = mutableListOf<WebRtcIceServerConfig>()

        // 1. Always include standard public STUN servers for direct NAT traversal
        servers.addAll(WebRtcIceServerConfig.defaultServers())

        // 2. If AWS TURN/STUN server configured, append direct AWS ICE servers
        if (config.turnServerEndpoint.isNotBlank()) {
            servers.add(WebRtcIceServerConfig(uri = "stun:${config.turnServerEndpoint}:3478"))
            if (config.turnUsername.isNotBlank()) {
                servers.addAll(
                    WebRtcIceServerConfig.awsTurnServers(
                        turnEndpoint = config.turnServerEndpoint,
                        turnUsername = config.turnUsername,
                        turnCredential = config.turnCredential
                    )
                )
            }
        }

        servers
    }
}
