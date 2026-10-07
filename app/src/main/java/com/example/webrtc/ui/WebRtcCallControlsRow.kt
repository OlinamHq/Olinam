package com.example.webrtc.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun WebRtcCallControlsRow(
    isVideoCall: Boolean,
    isSpeakerOn: Boolean,
    isMicMuted: Boolean,
    isVideoDisabled: Boolean,
    onToggleSpeaker: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleVideo: () -> Unit,
    onSwitchCamera: () -> Unit,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFF1E293B).copy(alpha = 0.92f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Speakerphone
            IconButton(
                onClick = onToggleSpeaker,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(if (isSpeakerOn) Color.White else Color(0xFF334155))
                    .testTag("webrtc_speaker_button")
            ) {
                Icon(
                    imageVector = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                    contentDescription = "Speaker",
                    tint = if (isSpeakerOn) Color(0xFF0F172A) else Color.White
                )
            }

            // 2. Video Toggle (if Video Call)
            if (isVideoCall) {
                IconButton(
                    onClick = onToggleVideo,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(if (!isVideoDisabled) Color.White else Color(0xFF334155))
                        .testTag("webrtc_video_toggle_button")
                ) {
                    Icon(
                        imageVector = if (!isVideoDisabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                        contentDescription = "Video",
                        tint = if (!isVideoDisabled) Color(0xFF0F172A) else Color.White
                    )
                }

                // 3. Switch Camera
                IconButton(
                    onClick = onSwitchCamera,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF334155))
                        .testTag("webrtc_switch_camera_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Flip camera",
                        tint = Color.White
                    )
                }
            }

            // 4. Mute Microphone
            IconButton(
                onClick = onToggleMute,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(if (isMicMuted) Color(0xFFEF4444) else Color(0xFF334155))
                    .testTag("webrtc_mute_button")
            ) {
                Icon(
                    imageVector = if (isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Mute",
                    tint = Color.White
                )
            }

            // 5. End Call Button (Large Red Circle)
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444))
                    .clickable { onEndCall() }
                    .testTag("webrtc_end_call_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "End Call",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
