package com.example.webrtc.signaling

import com.example.webrtc.model.WebRtcIceServerConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Service responsible for resolving ICE (STUN & TURN) servers on Google Cloud infrastructure.
 * Uses high-availability Google Public STUN servers and open STUN/TURN relays.
 */
class GoogleCloudIceServerService {

    suspend fun getIceServers(): List<WebRtcIceServerConfig> = withContext(Dispatchers.IO) {
        val servers = mutableListOf<WebRtcIceServerConfig>()

        // High-performance Google STUN servers with global Anycast routing
        servers.add(WebRtcIceServerConfig(uri = "stun:stun.l.google.com:19302"))
        servers.add(WebRtcIceServerConfig(uri = "stun:stun1.l.google.com:19302"))
        servers.add(WebRtcIceServerConfig(uri = "stun:stun2.l.google.com:19302"))
        servers.add(WebRtcIceServerConfig(uri = "stun:stun3.l.google.com:19302"))
        servers.add(WebRtcIceServerConfig(uri = "stun:stun4.l.google.com:19302"))
        servers.add(WebRtcIceServerConfig(uri = "stun:stun.relay.metered.ca:80"))

        servers
    }
}
