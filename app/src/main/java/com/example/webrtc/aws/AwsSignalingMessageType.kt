package com.example.webrtc.aws

enum class AwsSignalingMessageType {
    JOIN,
    LEAVE,
    INVITE,
    RINGING,
    ACCEPT,
    DECLINE,
    OFFER,
    ANSWER,
    ICE_CANDIDATE,
    END_CALL,
    HEARTBEAT
}
