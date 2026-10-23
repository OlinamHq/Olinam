package com.example.ui.components.audio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import java.util.Locale

/**
 * WhatsApp-style voice message recording bar.
 * Replaces the input capsule with real recording controls:
 * - Red blinking recording dot
 * - Live timer ticker (0:01, 0:02...)
 * - Dynamic live audio waveform visualizer
 * - Red Trash icon to discard recording
 * - Circular Send button to save & send real audio note
 */
@Composable
fun VoiceMessageRecordingBar(
    recordingManager: AudioRecordingManager,
    modifier: Modifier = Modifier,
    onSend: (file: File, durationSec: Int) -> Unit,
    onCancel: () -> Unit
) {
    val durationSec by recordingManager.durationSeconds.collectAsState()
    val amplitude by recordingManager.amplitude.collectAsState()

    // Blinking red recording indicator
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_mic")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val minutes = durationSec / 60
    val seconds = durationSec % 60
    val formattedDuration = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Red Delete/Trash button
        IconButton(
            onClick = {
                recordingManager.cancelRecording()
                onCancel()
            },
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color(0xFFFEE2E2))
                .testTag("cancel_voice_record")
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Cancel recording",
                tint = Color(0xFFDC2626),
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // WhatsApp recording status capsule
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .shadow(1.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Blinking red recording dot
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .alpha(pulseAlpha)
                        .background(Color(0xFFE11D48))
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Live timer
                Text(
                    text = formattedDuration,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E293B)
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Dynamic live waveform bars
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(26.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val barCount = 18
                    repeat(barCount) { idx ->
                        // Wave heights reacting to live amplitude
                        val factor = ((idx * 5) % 11 + 3) / 14f
                        val liveHeight = ((amplitude * factor * 22f).coerceIn(4f, 24f)).dp
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(liveHeight)
                                .clip(RoundedCornerShape(1.5.dp))
                                .background(Color(0xFF0160E3).copy(alpha = 0.85f))
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Blue Send button to commit & send audio
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(2.dp, CircleShape)
                .clip(CircleShape)
                .background(Color(0xFF0160E3))
                .clickable {
                    val result = recordingManager.stopRecording()
                    if (result != null) {
                        onSend(result.first, result.second)
                    } else {
                        onCancel()
                    }
                }
                .testTag("send_voice_note_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send Voice Message",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
