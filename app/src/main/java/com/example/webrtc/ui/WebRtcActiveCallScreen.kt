package com.example.webrtc.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OlinamOnlineGreen
import com.example.ui.theme.OlinamPrimary
import com.example.webrtc.call.WebRtcCallManager
import com.example.webrtc.media.WebRtcVideoView
import com.example.webrtc.model.WebRtcCallState
import com.example.webrtc.model.WebRtcCallType

@Composable
fun WebRtcActiveCallScreen(
    callManager: WebRtcCallManager,
    modifier: Modifier = Modifier
) {
    val callState by callManager.callState.collectAsState()
    val participant by callManager.activeParticipant.collectAsState()
    val callType by callManager.callType.collectAsState()
    val remoteTrack by callManager.remoteVideoTrack.collectAsState()
    val localTrack by callManager.localVideoTrack.collectAsState()
    val durationSeconds by callManager.durationSeconds.collectAsState()
    val isSpeakerOn by callManager.isSpeakerOn.collectAsState()
    val isMicMuted by callManager.isMicMuted.collectAsState()
    val isVideoDisabled by callManager.isVideoDisabled.collectAsState()
    val isFrontCamera by callManager.isFrontCamera.collectAsState()

    if (callState == WebRtcCallState.IDLE) return

    BackHandler {
        callManager.endCall()
    }

    val isVideo = callType == WebRtcCallType.VIDEO
    val isIncomingRinging = callState == WebRtcCallState.INCOMING_RINGING

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .testTag("webrtc_active_call_screen")
    ) {
        // 1. Remote Video Track View (Full Screen)
        if (isVideo && remoteTrack != null && !isVideoDisabled) {
            WebRtcVideoView(
                videoTrack = remoteTrack,
                eglBaseContext = callManager.eglBaseContext,
                isMirror = false,
                modifier = Modifier.fillMaxSize()
            )
        } else if (isVideo && localTrack != null && !isVideoDisabled) {
            // While waiting for peer, show local video full screen
            WebRtcVideoView(
                videoTrack = localTrack,
                eglBaseContext = callManager.eglBaseContext,
                isMirror = isFrontCamera,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 2. Dark overlay for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.65f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        // 3. Local PIP Video Preview (When remote video is active)
        if (isVideo && remoteTrack != null && localTrack != null && !isVideoDisabled) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 80.dp, end = 16.dp)
                    .size(width = 110.dp, height = 160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.5.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
            ) {
                WebRtcVideoView(
                    videoTrack = localTrack,
                    eglBaseContext = callManager.eglBaseContext,
                    isMirror = isFrontCamera,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // 4. Header: Caller Info & Status
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // End-to-End Encrypted & Google Cloud WebRTC Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Encrypted",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "Google Cloud WebRTC • End-to-End Encrypted",
                    fontSize = 11.5.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Participant Name
            Text(
                text = participant?.displayName ?: "Olinam Contact",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Phone Number
            if (!participant?.phoneNumber.isNullOrBlank()) {
                Text(
                    text = participant?.phoneNumber ?: "",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.75f)
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Call State / Live Duration
            val statusText = when (callState) {
                WebRtcCallState.OUTGOING_RINGING -> "Ringing..."
                WebRtcCallState.INCOMING_RINGING -> "Incoming ${if (isVideo) "video" else "audio"} call..."
                WebRtcCallState.CONNECTING -> "Connecting WebRTC..."
                WebRtcCallState.CONNECTED -> {
                    val minutes = durationSeconds / 60
                    val seconds = durationSeconds % 60
                    String.format("%02d:%02d", minutes, seconds)
                }
                WebRtcCallState.RECONNECTING -> "Reconnecting..."
                WebRtcCallState.DISCONNECTED -> "Call disconnected"
                WebRtcCallState.FAILED -> "Call failed"
                WebRtcCallState.TERMINATED -> "Call ended"
                else -> ""
            }

            Text(
                text = statusText,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (callState == WebRtcCallState.CONNECTED) OlinamOnlineGreen else Color.White.copy(alpha = 0.9f)
            )

            // Audio Call Big Avatar Display
            if (!isVideo || isVideoDisabled) {
                Spacer(modifier = Modifier.height(55.dp))
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(OlinamPrimary.copy(alpha = 0.25f))
                        .border(3.dp, OlinamPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (participant?.displayName ?: "O").take(1).uppercase(),
                        fontSize = 58.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // 5. Bottom Controls
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 20.dp, start = 16.dp, end = 16.dp)
        ) {
            if (isIncomingRinging) {
                WebRtcIncomingCallBanner(
                    isVideo = isVideo,
                    onAnswer = { callManager.answerIncomingCall() },
                    onDecline = { callManager.declineIncomingCall() }
                )
            } else {
                WebRtcCallControlsRow(
                    isVideoCall = isVideo,
                    isSpeakerOn = isSpeakerOn,
                    isMicMuted = isMicMuted,
                    isVideoDisabled = isVideoDisabled,
                    onToggleSpeaker = { callManager.toggleSpeaker() },
                    onToggleMute = { callManager.toggleMute() },
                    onToggleVideo = { callManager.toggleVideo() },
                    onSwitchCamera = { callManager.switchCamera() },
                    onEndCall = { callManager.endCall() }
                )
            }
        }
    }
}
