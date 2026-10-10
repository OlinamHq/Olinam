# Google Cloud & Firebase Architecture for Olinam

This document details the production Google Cloud architecture implemented in the Olinam Android application.

## 1. System Architecture Overview

```
                          +------------------------------------------+
                          |             Olinam Android App           |
                          +------------------------------------------+
                                    /           |            \
                                   /            |             \
                                  /             |              \
             +-----------------------+   +-------------------+  +-------------------------+
             | Google Cloud Firebase |   | Google Cloud E2EE |  | Google Cloud STUN /     |
             | Authentication (OAuth)|   | Firestore Realtime|  | WebRTC Signaling        |
             | Phone / Google Sign-In|   | Database Engine   |  | PeerConnection Mesh     |
             +-----------------------+   +-------------------+  +-------------------------+
                                                |
                                 +------------------------------+
                                 | Cloudflare Edge Global CDN   |
                                 | Encrypted Media Storage (R2) |
                                 +------------------------------+
```

## 2. Infrastructure Components

1. **Google Cloud Firebase Authentication & Firestore Realtime DB**:
   - Provisioned under Google Cloud Project: `olinam-90d42`.
   - Real-time snapshot listeners for instant message synchronization (`conversations`, `messages`, `users`).
   - Client-side AES-256-GCM End-to-End Encryption before writing to Google Cloud Firestore.

2. **Google Cloud WebRTC Calling Engine**:
   - `GoogleCloudSignalingClient.kt`: Native Firestore-backed signaling channel (`webrtc_signaling/{callId}/messages`) for SDP Offer, SDP Answer, ICE Candidates, and call lifecycle.
   - `GoogleCloudIceServerService.kt`: Global Anycast Google STUN network (`stun.l.google.com:19302`, `stun1.l.google.com:19302`, `stun2.l.google.com:19302`, `stun3.l.google.com:19302`, `stun4.l.google.com:19302`).
   - Hardware-accelerated WebRTC video & audio processing.

3. **Cloudflare Edge R2 Storage**:
   - Presigned URL uploads (`https://olinam-r2-media.olinamhq.workers.dev/`).
   - Public CDN delivery (`https://olinam-r2-media.olinamhq.workers.dev/file/`).
