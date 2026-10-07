# AWS WebRTC Audio & Video Calling Architecture Guide for Olinam

This document details the production AWS WebRTC architecture implemented in the Olinam Android application, and the exact steps to configure your AWS Console backend.

---

## 1. System Architecture Overview

```
                      +-----------------------------+
                      |       AWS API Gateway       |
                      |   (WebSocket Signaling API) |
                      +--------------+--------------+
                                     |
              +----------------------+----------------------+
              |                                             |
              v                                             v
     +-----------------+                           +-----------------+
     | AWS Lambda      |                           | AWS DynamoDB    |
     | (Signaling      |                           | (Connection &   |
     |  Controller)    |                           |  Session Store) |
     +-----------------+                           +-----------------+
              |
              |
+-------------+-------------------------------------------------------+
|                                                                     |
|  Direct P2P Encrypted Audio / Video Stream (WebRTC Unified Plan)    |
|                                                                     |
v                                                                     v
[User A (Olinam App)] <=====================================> [User B (Olinam App)]
         ^                                                              ^
         |                    +---------------------+                   |
         +------------------- |  AWS EC2 / ECS      | ------------------+
                              |  (coturn TURN/STUN) |
                              +---------------------+
```

---

## 2. Android App Structure Created

The codebase now has a modular WebRTC subsystem in `com.example.webrtc.*`:

1. **`com.example.webrtc.model`**:
   - `WebRtcCallType.kt`: Audio and Video call types.
   - `WebRtcCallState.kt`: Call states (`IDLE`, `OUTGOING_RINGING`, `INCOMING_RINGING`, `CONNECTING`, `CONNECTED`, `TERMINATED`).
   - `WebRtcSessionDescription.kt`: SDP Offer and Answer models with unified plan mapping.
   - `WebRtcIceCandidate.kt`: ICE candidate data for NAT traversal.
   - `WebRtcIceServerConfig.kt`: STUN and TURN server configuration (Google STUN + AWS coturn / Kinesis Video Streams).
   - `WebRtcParticipant.kt`: Remote peer details (userId, name, phone, audio/video status).

2. **`com.example.webrtc.aws`**:
   - `AwsWebRtcConfig.kt`: Holds your AWS WebSocket URL (`wss://...`), Region (`ap-south-1`, `us-east-1`), and TURN server credentials.
   - `AwsSignalingMessageType.kt`: Enum of signaling action types (`OFFER`, `ANSWER`, `ICE_CANDIDATE`, `JOIN`, `LEAVE`, `END_CALL`).
   - `AwsSignalingMessage.kt`: Robust JSON serialization/deserialization for signaling payloads.
   - `AwsSignalingClient.kt`: Full OkHttp WebSocket client with auto-reconnection, ping-pong heartbeat, and Kotlin Flow streams.
   - `AwsTurnCredentialService.kt`: Resolves STUN and AWS TURN relay configurations.

3. **`com.example.webrtc.peer`**:
   - `WebRtcPeerConnectionFactory.kt`: Hardware-accelerated H.264/VP8 video encoders and decoders (`DefaultVideoEncoderFactory`, `DefaultVideoDecoderFactory`), EglBase context, and `JavaAudioDeviceModule`.
   - `WebRtcPeerConnectionClient.kt`: Manages `PeerConnection`, tracks, SDP creation, remote description, and candidate exchange.
   - `WebRtcSdpObserver.kt`: Kotlin callback wrapper for SDP operations.
   - `WebRtcPeerConnectionObserver.kt`: Kotlin callback wrapper for ICE and track events.

4. **`com.example.webrtc.media`**:
   - `WebRtcCameraCapturer.kt`: Camera2 enumerator capturer with front and back camera switching.
   - `WebRtcMediaManager.kt`: Local audio/video sources, surface texture helper, 720p 30fps capture, microphone mute, and camera toggle.
   - `WebRtcAudioSwitch.kt`: Real audio hardware routing (Earpiece, Speakerphone, Bluetooth) and proximity sensor lock to turn off screen near the ear.
   - `WebRtcVideoView.kt`: Jetpack Compose wrapper for WebRTC `SurfaceViewRenderer`.

5. **`com.example.webrtc.call`**:
   - `WebRtcCallManager.kt`: The central orchestrator combining media, signaling, peer connections, and call states.
   - `WebRtcRingtoneManager.kt`: Manages phone ringback tone, incoming ringtone, vibration, and disconnect tone.
   - `WebRtcCallEvents.kt`: Sealed event hierarchy for reactive UI state.

6. **`com.example.webrtc.ui`**:
   - `WebRtcActiveCallScreen.kt`: Full-screen WhatsApp-level call UI with remote video, local PIP, caller info, duration, and controls.
   - `WebRtcCallControlsRow.kt`: In-call control buttons (speaker, video, flip camera, mute, end call).
   - `WebRtcIncomingCallBanner.kt`: Incoming call prompt (Answer / Decline).

---

## 3. Step-by-Step AWS Console Setup

### Step A: Deploy AWS API Gateway WebSocket API
1. Open the **AWS Management Console** -> Go to **API Gateway**.
2. Click **Create API** -> Choose **WebSocket API**.
3. Set **API Name** to `olinam-webrtc-signaling`.
4. Set **Route Selection Expression** to `$request.body.action`.
5. Add the three primary routes:
   - `$connect`
   - `$disconnect`
   - `$default` (or custom routes: `sendmessage`, `offer`, `answer`, `candidate`).
6. Deploy the API to a stage named `prod`.
7. Copy the **WebSocket URL** (e.g. `wss://xxxxxx.execute-api.ap-south-1.amazonaws.com/prod`).

### Step B: Create DynamoDB Connection Table
1. Open **DynamoDB Console** -> Click **Create Table**.
2. Set Table Name to `OlinamWebRtcConnections`.
3. Set Partition Key to `connectionId` (String).
4. Add a Global Secondary Index (GSI) on `userId` (String).

### Step C: Deploy AWS Lambda Signaling Functions
Create a Lambda function (Node.js or Python) connected to the API Gateway WebSocket routes:

```javascript
// Sample Node.js AWS Lambda for $default / message routing
const { ApiGatewayManagementApiClient, PostToConnectionCommand } = require("@aws-sdk/client-apigatewaymanagementapi");
const { DynamoDBClient, ScanCommand } = require("@aws-sdk/client-dynamodb");

exports.handler = async (event) => {
    const body = JSON.parse(event.body);
    const domainName = event.requestContext.domainName;
    const stage = event.requestContext.stage;
    const endpoint = `https://${domainName}/${stage}`;
    
    const apiGwClient = new ApiGatewayManagementApiClient({ endpoint });
    
    // Route message to recipient
    const recipientId = body.recipientId;
    const recipientConnectionId = await getConnectionIdForUser(recipientId);
    
    if (recipientConnectionId) {
        await apiGwClient.send(new PostToConnectionCommand({
            ConnectionId: recipientConnectionId,
            Data: Buffer.from(JSON.stringify(body))
        }));
    }
    
    return { statusCode: 200, body: 'Message delivered' };
};
```

### Step D: Deploy coturn TURN Server on AWS EC2
1. Launch an **Ubuntu EC2 instance** (e.g., `t3.small` or `t4g.small`).
2. Open Security Group ports:
   - Port `3478` (UDP & TCP)
   - Port `5349` (TLS)
   - Ports `49152-65535` (UDP for relay media streams).
3. Install coturn:
   ```bash
   sudo apt-get update
   sudo apt-get install -y coturn
   ```
4. Configure `/etc/turnserver.conf`:
   ```
   listening-port=3478
   tls-listening-port=5349
   realm=olinam.aws
   fingerprint
   lt-cred-mech
   user=olinamuser:OlinamSecretPass123
   ```
5. Start service:
   ```bash
   sudo systemctl enable coturn
   sudo systemctl restart coturn
   ```

### Step E: Configure Endpoint in the Android App
Open `app/src/main/java/com/example/webrtc/aws/AwsWebRtcConfig.kt` and paste your deployed AWS details:
```kotlin
val config = AwsWebRtcConfig(
    websocketSignalingUrl = "wss://your-api-id.execute-api.ap-south-1.amazonaws.com/prod",
    awsRegion = "ap-south-1",
    turnServerEndpoint = "your-ec2-turn-ip-or-domain",
    turnUsername = "olinamuser",
    turnCredential = "OlinamSecretPass123",
    isAwsSignalingEnabled = true
)
```

---

## 4. Testing WebRTC Calling

- The app uses `WebRtcCallManager` which routes audio and video using hardware-accelerated WebRTC pipelines.
- Works over both cellular and Wi-Fi networks using STUN for NAT discovery and TURN for symmetric NAT fallback.
