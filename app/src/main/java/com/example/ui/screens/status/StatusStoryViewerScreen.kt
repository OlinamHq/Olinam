package com.example.ui.screens.status

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Story
import com.example.ui.components.formatMessageTime
import com.example.ui.theme.OlinamPrimary
import kotlinx.coroutines.delay

/**
 * Full-screen WhatsApp-grade Status Story Viewer.
 */
@Composable
fun StatusStoryViewerScreen(
    group: UserStatusGroup,
    isMyStatus: Boolean = false,
    onDismiss: () -> Unit,
    onStoryViewed: (String) -> Unit = {},
    onDeleteStory: (String) -> Unit = {},
    onReplyToStory: (Story, String) -> Unit = { _, _ -> },
    onNextGroup: () -> Unit = onDismiss,
    onPrevGroup: () -> Unit = onDismiss
) {
    BackHandler { onDismiss() }

    val context = LocalContext.current
    var currentIndex by remember { mutableIntStateOf(0) }
    var progress by remember { mutableFloatStateOf(0f) }
    var isPaused by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val currentStory = group.items.getOrNull(currentIndex) ?: group.items.firstOrNull()

    // Notify viewed
    LaunchedEffect(currentStory?.id) {
        currentStory?.let {
            onStoryViewed(it.id)
            progress = 0f
        }
    }

    // Auto progression timer (5000ms duration with 50ms tick intervals)
    val storyDurationMs = 5000L
    val tickIntervalMs = 50L
    val step = tickIntervalMs.toFloat() / storyDurationMs.toFloat()

    LaunchedEffect(currentIndex, isPaused) {
        while (!isPaused && progress < 1f) {
            delay(tickIntervalMs)
            if (!isPaused) {
                progress += step
            }
        }
        if (!isPaused && progress >= 1f) {
            if (currentIndex < group.items.size - 1) {
                currentIndex++
                progress = 0f
            } else {
                onNextGroup()
            }
        }
    }

    if (currentStory == null) {
        LaunchedEffect(Unit) { onDismiss() }
        return
    }

    val isTextStatus = !currentStory.isImageStory && !currentStory.isVideo && currentStory.mediaUrl.isNullOrBlank()
    val bgColor = if (isTextStatus) {
        TextStatusPalettes.parseColor(currentStory.backgroundColor)
    } else {
        Color.Black
    }

    val currentFont = if (isTextStatus) {
        val (font, _) = TextStatusPalettes.fonts.getOrElse(currentStory.fontIndex) { TextStatusPalettes.fonts.first() }
        font
    } else null

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .testTag("status_story_viewer")
    ) {
        // STORY CONTENT (BACKGROUND / MEDIA / TEXT)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center)
                .pointerInput(group.items.size, currentIndex) {
                    detectTapGestures(
                        onPress = {
                            isPaused = true
                            tryAwaitRelease()
                            isPaused = false
                        },
                        onTap = { offset ->
                            val screenWidth = size.width
                            if (offset.x < screenWidth * 0.33f) {
                                // Tap Left: Previous story
                                if (progress > 0.2f) {
                                    progress = 0f
                                } else if (currentIndex > 0) {
                                    currentIndex--
                                    progress = 0f
                                } else {
                                    onPrevGroup()
                                }
                            } else {
                                // Tap Right: Next story
                                if (currentIndex < group.items.size - 1) {
                                    currentIndex++
                                    progress = 0f
                                } else {
                                    onNextGroup()
                                }
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (isTextStatus) {
                // Text status formatted display
                Text(
                    text = currentStory.caption,
                    color = Color.White,
                    fontSize = if (currentStory.caption.length < 50) 30.sp else 22.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = currentFont,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                )
            } else {
                // Image or Video Status
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = currentStory.mediaUrl,
                        contentDescription = "Status Media",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )

                    if (currentStory.isVideo) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.45f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = "Play video",
                                tint = Color.White,
                                modifier = Modifier.size(54.dp)
                            )
                        }
                    }

                    // Optional Caption Overlay
                    if (currentStory.caption.isNotBlank() && currentStory.caption != "Status update ✨") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                    )
                                )
                                .padding(horizontal = 20.dp, vertical = if (isMyStatus) 60.dp else 90.dp)
                        ) {
                            Text(
                                text = currentStory.caption,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // TOP BAR CHROME (Segmented Progress Bars + Contact Header)
        AnimatedVisibility(
            visible = !isPaused,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.65f), Color.Transparent)
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Segmented Progress Bars
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    group.items.forEachIndexed { index, _ ->
                        val segmentProgress = when {
                            index < currentIndex -> 1f
                            index == currentIndex -> progress
                            else -> 0f
                        }
                        LinearProgressIndicator(
                            progress = { segmentProgress },
                            modifier = Modifier
                                .weight(1f)
                                .height(2.5.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.35f)
                        )
                    }
                }

                // Header: Avatar, Name, Time, and Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Contact Avatar
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!group.userAvatar.isNullOrBlank()) {
                            AsyncImage(
                                model = group.userAvatar,
                                contentDescription = group.userName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = group.userName.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isMyStatus) "My Status" else group.userName,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = formatMessageTime(currentStory.timestamp),
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 12.sp
                        )
                    }

                    if (isMyStatus) {
                        IconButton(
                            onClick = {
                                isPaused = true
                                showDeleteConfirmDialog = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Status",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // BOTTOM CONTROLS
        AnimatedVisibility(
            visible = !isPaused,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            if (isMyStatus) {
                // My Status Footer: Views count + eye icon
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                            )
                        )
                        .padding(vertical = 16.dp, horizontal = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "Views",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (currentStory.viewsCount > 0)
                            "${currentStory.viewsCount} view${if (currentStory.viewsCount > 1) "s" else ""}"
                        else "No views yet",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                // Contact Status Footer: Quick Reactions + Reply Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Quick emoji reactions
                    val quickEmojis = listOf("❤️", "😂", "😮", "😢", "🙏", "👏", "🎉", "🔥")
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(quickEmojis) { emoji ->
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .clickable {
                                        onReplyToStory(currentStory, emoji)
                                        Toast.makeText(context, "Reaction sent $emoji", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 20.sp)
                            }
                        }
                    }

                    // Reply Text Field Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            BasicTextField(
                                value = replyText,
                                onValueChange = { replyText = it },
                                textStyle = TextStyle(color = Color.White, fontSize = 15.sp),
                                cursorBrush = SolidColor(Color.White),
                                modifier = Modifier.fillMaxWidth(),
                                decorationBox = { innerTextField ->
                                    if (replyText.isEmpty()) {
                                        Text(
                                            text = "Reply...",
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 15.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                if (replyText.isNotBlank()) {
                                    onReplyToStory(currentStory, replyText.trim())
                                    Toast.makeText(context, "Reply sent", Toast.LENGTH_SHORT).show()
                                    replyText = ""
                                }
                            },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF25D366))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send Reply",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmDialog = false
                isPaused = false
            },
            title = { Text("Delete status update?") },
            text = { Text("This status update will be deleted for everyone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteStory(currentStory.id)
                        showDeleteConfirmDialog = false
                        isPaused = false
                        if (group.items.size <= 1) {
                            onDismiss()
                        } else {
                            if (currentIndex >= group.items.size - 1) {
                                currentIndex = (group.items.size - 2).coerceAtLeast(0)
                            }
                        }
                    }
                ) {
                    Text("Delete", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        isPaused = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
