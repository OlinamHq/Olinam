package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.model.Conversation
import java.util.Locale

/**
 * WhatsApp-style Profile Quick View Popup Dialog.
 * Opens when a user taps on any contact DP in the chat list.
 * Displays full large DP with translucent contact name overlay,
 * and bottom action icons: Chat, Audio Call (if supported), Video Call (if supported), and About ('i').
 */
@Composable
fun ContactQuickViewDialog(
    conversation: Conversation,
    onDismiss: () -> Unit,
    onChatClick: () -> Unit,
    onAudioCallClick: () -> Unit,
    onVideoCallClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    val avatarColor = remember(conversation.id) {
        val colors = listOf(
            Color(0xFF0160E3), // Olinam Royal Blue
            Color(0xFF0284C7), // Sky
            Color(0xFF6366F1), // Indigo
            Color(0xFF8B5CF6), // Purple
            Color(0xFFEC4899), // Pink
            Color(0xFFF59E0B)  // Amber
        )
        colors[Math.abs(conversation.id.hashCode()) % colors.size]
    }

    val phoneNumber = conversation.phoneNumber ?: ""
    val hasCallableNumber = phoneNumber.any { it.isDigit() }
    val isSms = conversation.isSmsContact
    val isSystemAlert = conversation.isSystemAlert

    // Determine audio and video support dynamically (Groups now fully supported for voice & video call)
    val supportsAudioCall = !isSystemAlert
    val supportsVideoCall = !isSystemAlert && !isSms

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .width(280.dp)
                .clip(RoundedCornerShape(12.dp))
                .testTag("contact_quick_view_dialog"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Large Full DP Display (1:1 Square Aspect Ratio)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(avatarColor)
                        .clickable(onClick = onAboutClick)
                        .testTag("quick_view_dp_image"),
                    contentAlignment = Alignment.Center
                ) {
                    if (!conversation.avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = conversation.avatarUrl,
                            contentDescription = conversation.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (conversation.isGroup) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = "Group",
                            tint = Color.White.copy(alpha = 0.95f),
                            modifier = Modifier.size(105.dp)
                        )
                    } else {
                        Text(
                            text = conversation.title.take(1).uppercase(Locale.getDefault()),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 86.sp
                        )
                    }

                    // Top translucent gradient overlay showing Contact/Group Name
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x8C000000),
                                        Color(0x35000000),
                                        Color.Transparent
                                    )
                                )
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = conversation.title,
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (conversation.isVerified) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified",
                                    tint = Color(0xFF0160E3),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(
                    thickness = 0.8.dp,
                    color = Color(0xFFE5E7EB)
                )

                // Olinam Royal Blue Action Icon Row (0xFF0160E3)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(Color.White)
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Message / Chat Button
                    IconButton(
                        onClick = onChatClick,
                        modifier = Modifier.testTag("quick_view_chat_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "Chat",
                            tint = Color(0xFF0160E3),
                            modifier = Modifier.size(23.dp)
                        )
                    }

                    // 2. Audio Call Button (Only shown if audio call is supported)
                    if (supportsAudioCall) {
                        IconButton(
                            onClick = onAudioCallClick,
                            modifier = Modifier.testTag("quick_view_audio_call_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Audio Call",
                                tint = Color(0xFF0160E3),
                                modifier = Modifier.size(23.dp)
                            )
                        }
                    }

                    // 3. Video Call Button (Only shown if video call is supported)
                    if (supportsVideoCall) {
                        IconButton(
                            onClick = onVideoCallClick,
                            modifier = Modifier.testTag("quick_view_video_call_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Video Call",
                                tint = Color(0xFF0160E3),
                                modifier = Modifier.size(25.dp)
                            )
                        }
                    }

                    // 4. About / Info Button ('i' button - opens user profile)
                    IconButton(
                        onClick = onAboutClick,
                        modifier = Modifier.testTag("quick_view_about_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "About / Profile",
                            tint = Color(0xFF0160E3),
                            modifier = Modifier.size(23.dp)
                        )
                    }
                }
            }
        }
    }
}
