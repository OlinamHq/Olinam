package com.example.ui.screens.chats

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.SentimentSatisfied
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ChatViewModel

@Composable
fun ChatSettingsScreen(
    viewModel: ChatViewModel,
    onBackClick: () -> Unit
) {
    BackHandler { onBackClick() }

    val context = LocalContext.current
    val prefs = remember { ChatSettingsPreferences(context) }
    val state by prefs.state.collectAsState()

    var showBackupScreen by remember { mutableStateOf(false) }
    var showWallpaperDialog by remember { mutableStateOf(false) }
    var showClearAllDialog by remember { mutableStateOf(false) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    if (showBackupScreen) {
        ChatBackupRestoreScreen(
            viewModel = viewModel,
            onBackClick = { showBackupScreen = false }
        )
        return
    }

    val switchColors = SwitchDefaults.colors(
        checkedThumbColor = Color.White,
        checkedTrackColor = Color(0xFF3855B3),
        uncheckedThumbColor = Color.White,
        uncheckedTrackColor = Color(0xFFCBD5E1),
        uncheckedBorderColor = Color.Transparent
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F7F9))
            .statusBarsPadding()
    ) {
        // Top App Bar
        Surface(
            color = Color(0xFFF6F7F9),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("chats_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF1E293B),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Chats",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
            }
        }

        // Settings Content List
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // CARD 1: Chat Backup & Restore (Exact match to reference image 20261020_054546_6b80723d.jpg)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 1.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = Color.Black.copy(alpha = 0.04f),
                        spotColor = Color.Black.copy(alpha = 0.04f)
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { showBackupScreen = true }
                    .testTag("card_chat_backup_restore"),
                shape = RoundedCornerShape(20.dp),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CloudQueue,
                        contentDescription = null,
                        tint = Color(0xFF111827),
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(18.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Chat Backup & Restore",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF111827)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Backup your chat history and restore from backups.",
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280),
                            lineHeight = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // CARD 2: Enter to send (Exact match to reference image 20261020_054546_6b80723d.jpg)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 1.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = Color.Black.copy(alpha = 0.04f),
                        spotColor = Color.Black.copy(alpha = 0.04f)
                    )
                    .testTag("card_enter_to_send"),
                shape = RoundedCornerShape(20.dp),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardReturn,
                        contentDescription = null,
                        tint = Color(0xFF111827),
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(18.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enter to send",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF111827)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Hitting the enter key will send your message.",
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280),
                            lineHeight = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = state.enterToSend,
                        onCheckedChange = { prefs.setEnterToSend(it) },
                        colors = switchColors,
                        modifier = Modifier.testTag("switch_enter_to_send")
                    )
                }
            }

            // CARD 3: Quick reaction (Exact match to reference image 20261020_054546_6b80723d.jpg)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 1.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = Color.Black.copy(alpha = 0.04f),
                        spotColor = Color.Black.copy(alpha = 0.04f)
                    )
                    .testTag("card_quick_reaction"),
                shape = RoundedCornerShape(20.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.SentimentSatisfied,
                            contentDescription = null,
                            tint = Color(0xFF111827),
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(18.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Quick reaction",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF111827)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Double-tap to react to any message.",
                                fontSize = 14.sp,
                                color = Color(0xFF6B7280),
                                lineHeight = 20.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = state.quickReaction,
                            onCheckedChange = { prefs.setQuickReaction(it) },
                            colors = switchColors,
                            modifier = Modifier.testTag("switch_quick_reaction")
                        )
                    }

                    // Emoji Selector Row matching screenshot
                    AnimatedVisibility(
                        visible = state.quickReaction,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(14.dp))
                            val popularEmojis = listOf("👍", "❤️", "😂", "😮", "😢", "🙏", "🔥")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                popularEmojis.forEach { emoji ->
                                    val isSelected = state.quickReactionEmoji == emoji
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC)
                                            )
                                            .then(
                                                if (isSelected) {
                                                    Modifier.background(
                                                        color = Color(0xFFEFF6FF),
                                                        shape = CircleShape
                                                    )
                                                } else Modifier
                                            )
                                            .clickable {
                                                prefs.setQuickReactionEmoji(emoji)
                                                Toast.makeText(context, "Double-tap reaction set to $emoji", Toast.LENGTH_SHORT).show()
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Surface(
                                            modifier = Modifier.size(46.dp),
                                            shape = CircleShape,
                                            color = if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                                            border = if (isSelected) BorderStroke(2.dp, Color(0xFF3855B3))
                                                     else BorderStroke(1.dp, Color(0xFFE2E8F0))
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = emoji,
                                                    fontSize = 20.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // CARD 4: Voice message confirmation (Exact match to reference image 20261020_054546_6b80723d.jpg)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 1.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = Color.Black.copy(alpha = 0.04f),
                        spotColor = Color.Black.copy(alpha = 0.04f)
                    )
                    .testTag("card_voice_confirmation"),
                shape = RoundedCornerShape(20.dp),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Mic,
                        contentDescription = null,
                        tint = Color(0xFF111827),
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(18.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Voice message confirmation",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF111827)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "A confirmation will pop up when sending voice messages.",
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280),
                            lineHeight = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = state.voiceMessageConfirmation,
                        onCheckedChange = { prefs.setVoiceMessageConfirmation(it) },
                        colors = switchColors,
                        modifier = Modifier.testTag("switch_voice_confirmation")
                    )
                }
            }

            // CARD 5: Media Visibility & Wallpapers (Production grade additions)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 1.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = Color.Black.copy(alpha = 0.04f),
                        spotColor = Color.Black.copy(alpha = 0.04f)
                    ),
                shape = RoundedCornerShape(20.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    // Wallpaper Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showWallpaperDialog = true }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Palette,
                            contentDescription = null,
                            tint = Color(0xFF111827),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(18.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Wallpaper & theme",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF111827)
                            )
                            Text(
                                text = "Theme: ${state.chatWallpaper}",
                                fontSize = 13.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Media visibility
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Visibility,
                            contentDescription = null,
                            tint = Color(0xFF111827),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(18.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Media visibility",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF111827)
                            )
                            Text(
                                text = "Show newly downloaded media in device gallery",
                                fontSize = 13.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                        Switch(
                            checked = state.mediaVisibility,
                            onCheckedChange = { prefs.setMediaVisibility(it) },
                            colors = switchColors
                        )
                    }
                }
            }

            // CARD 6: Chat History Management (Clear all / Delete all)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 1.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = Color.Black.copy(alpha = 0.04f),
                        spotColor = Color.Black.copy(alpha = 0.04f)
                    ),
                shape = RoundedCornerShape(20.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteSweep,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(18.dp))
                        Text(
                            text = "Chat history",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF111827)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showClearAllDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF1F5F9),
                                contentColor = Color(0xFF1E293B)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Clear all chats", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }

                        Button(
                            onClick = { showDeleteAllDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFEE2E2),
                                contentColor = Color(0xFFDC2626)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Delete all chats", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Wallpaper Dialog
    if (showWallpaperDialog) {
        val wallpapers = listOf("Default (Light blue doodle)", "Dark Slate", "Warm Sand", "Mint Green")
        AlertDialog(
            onDismissRequest = { showWallpaperDialog = false },
            title = {
                Text("Chat Theme & Wallpaper", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column {
                    wallpapers.forEach { wp ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    prefs.setChatWallpaper(wp)
                                    showWallpaperDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = state.chatWallpaper == wp,
                                onClick = {
                                    prefs.setChatWallpaper(wp)
                                    showWallpaperDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF3855B3))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = wp, fontSize = 15.sp, color = Color(0xFF1E293B))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showWallpaperDialog = false }) {
                    Text("Close", color = Color(0xFF3855B3))
                }
            }
        )
    }

    // Clear All Dialog
    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = {
                Text("Clear all chats?", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Text(
                    text = "Messages in all conversations will be cleared from this device. Conversations will remain in your chat list.",
                    fontSize = 14.sp,
                    color = Color(0xFF4B5563)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllChats()
                        showClearAllDialog = false
                        Toast.makeText(context, "All chats cleared", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Clear messages")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete All Dialog
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = {
                Text("Delete all chats?", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Text(
                    text = "Are you sure you want to delete all conversations and message history from this device? This cannot be undone.",
                    fontSize = 14.sp,
                    color = Color(0xFF4B5563)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAllChats()
                        showDeleteAllDialog = false
                        Toast.makeText(context, "All conversations deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Delete all")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
