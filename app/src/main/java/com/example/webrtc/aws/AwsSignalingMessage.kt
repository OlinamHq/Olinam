package com.example.webrtc.aws

import com.example.webrtc.model.WebRtcCallType
import com.example.webrtc.model.WebRtcIceCandidate
import com.example.webrtc.model.WebRtcSessionDescription
import org.json.JSONObject

data class AwsSignalingMessage(
    val type: AwsSignalingMessageType,
    val callId: String,
    val senderId: String,
    val recipientId: String,
    val callType: WebRtcCallType = WebRtcCallType.AUDIO,
    val sdp: WebRtcSessionDescription? = null,
    val candidate: WebRtcIceCandidate? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val payload: String? = null
) {
    fun toJson(): String {
        val json = JSONObject()
        json.put("type", type.name)
        json.put("callId", callId)
        json.put("senderId", senderId)
        json.put("recipientId", recipientId)
        json.put("callType", callType.name)
        json.put("timestamp", timestamp)

        if (sdp != null) {
            val sdpJson = JSONObject()
            sdpJson.put("type", sdp.type.name)
            sdpJson.put("description", sdp.description)
            json.put("sdp", sdpJson)
        }

        if (candidate != null) {
            val candJson = JSONObject()
            candJson.put("sdpMid", candidate.sdpMid)
            candJson.put("sdpMLineIndex", candidate.sdpMLineIndex)
            candJson.put("sdp", candidate.sdp)
            candJson.put("serverUrl", candidate.serverUrl)
            json.put("candidate", candJson)
        }

        if (payload != null) {
            json.put("payload", payload)
        }

        return json.toString()
    }

    companion object {
        fun fromJson(jsonStr: String): AwsSignalingMessage? {
            return try {
                val json = JSONObject(jsonStr)
                val type = AwsSignalingMessageType.valueOf(json.optString("type", "HEARTBEAT"))
                val callId = json.optString("callId", "")
                val senderId = json.optString("senderId", "")
                val recipientId = json.optString("recipientId", "")
                val callTypeStr = json.optString("callType", "AUDIO")
                val callType = if (callTypeStr == "VIDEO") WebRtcCallType.VIDEO else WebRtcCallType.AUDIO
                val timestamp = json.optLong("timestamp", System.currentTimeMillis())
                val payload = if (json.has("payload")) json.optString("payload") else null

                var sdp: WebRtcSessionDescription? = null
                if (json.has("sdp")) {
                    val sdpObj = json.getJSONObject("sdp")
                    val sdpTypeStr = sdpObj.optString("type", "OFFER")
                    val sdpType = com.example.webrtc.model.SdpType.valueOf(sdpTypeStr)
                    val desc = sdpObj.optString("description", "")
                    sdp = WebRtcSessionDescription(sdpType, desc)
                }

                var candidate: WebRtcIceCandidate? = null
                if (json.has("candidate")) {
                    val candObj = json.getJSONObject("candidate")
                    candidate = WebRtcIceCandidate(
                        sdpMid = candObj.optString("sdpMid", ""),
                        sdpMLineIndex = candObj.optInt("sdpMLineIndex", 0),
                        sdp = candObj.optString("sdp", ""),
                        serverUrl = candObj.optString("serverUrl", "")
                    )
                }

                AwsSignalingMessage(
                    type = type,
                    callId = callId,
                    senderId = senderId,
                    recipientId = recipientId,
                    callType = callType,
                    sdp = sdp,
                    candidate = candidate,
                    timestamp = timestamp,
                    payload = payload
                )
            } catch (_: Exception) {
                null
            }
        }
    }
}
