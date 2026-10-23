package com.example.ui.components.audio

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

/**
 * WhatsApp-style Voice Note Audio Bubble with interactive waveform playback,
 * speed toggle (1x / 1.5x / 2x), live progress, and avatar badge.
 */
@Composable
fun VoiceNoteBubble(
    audioId: String,
    audioUri: String,
    durationSec: Int = 0,
    isMe: Boolean = true,
    userAvatarUrl: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val currentPlayingId by AudioPlaybackManager.currentPlayingId.collectAsState()
    val isPlayingOverall by AudioPlaybackManager.isPlaying.collectAsState()
    val progress by AudioPlaybackManager.playbackProgress.collectAsState()
    val currentSec by AudioPlaybackManager.currentPositionSec.collectAsState()
    val playbackSpeed by AudioPlaybackManager.playbackSpeed.collectAsState()

    val isThisPlaying = (currentPlayingId == audioId && isPlayingOverall)
    val thisProgress = if (currentPlayingId == audioId) progress else 0f

    val displayedSec = if (currentPlayingId == audioId && currentSec > 0) {
        currentSec
    } else {
        durationSec.coerceAtLeast(1)
    }
    val minutes = displayedSec / 60
    val seconds = displayedSec % 60
    val durationText = String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)

    val surfaceBg = if (isMe) Color(0xFFE7FFDB) else Color.White
    val waveActiveColor = Color(0xFF0160E3)
    val waveInactiveColor = Color(0xFF94A3B8)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = RoundedCornerShape(12.dp),
        color = surfaceBg,
        shadowElevation = 0.5.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: User Mic Avatar Badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0160E3).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice note icon",
                    tint = Color(0xFF0160E3),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Play / Pause Circle Button
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0160E3))
                    .clickable {
                        AudioPlaybackManager.togglePlay(context, audioId, audioUri, scope)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isThisPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isThisPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Center: Waveform + Duration + Speed
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Interactive Waveform
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .pointerInput(audioId) {
                            detectTapGestures { offset ->
                                val width = size.width
                                if (width > 0) {
                                    val tapProgress = (offset.x / width).coerceIn(0f, 1f)
                                    if (currentPlayingId == audioId) {
                                        AudioPlaybackManager.seekTo(tapProgress)
                                    } else {
                                        AudioPlaybackManager.playNew(context, audioId, audioUri, scope)
                                        AudioPlaybackManager.seekTo(tapProgress)
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.CenterStart
                ) {
                    val barCount = 26
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(barCount) { index ->
                            val barProgress = index.toFloat() / barCount.toFloat()
                            val isActiveBar = barProgress <= thisProgress
                            val seed = ((index * 7 + 3) % 17) + 5
                            val barHeight = seed.coerceIn(5, 24).dp

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(barHeight)
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(if (isActiveBar) waveActiveColor else waveInactiveColor)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Bottom row inside bubble: Duration & Speed toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = durationText,
                        fontSize = 11.5.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )

                    // 1x, 1.5x, 2x playback speed toggle
                    if (currentPlayingId == audioId) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { AudioPlaybackManager.cycleSpeed() },
                            color = Color(0xFF0160E3).copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${playbackSpeed}x",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0160E3),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
