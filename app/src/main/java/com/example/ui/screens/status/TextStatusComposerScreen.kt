package com.example.ui.screens.status

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OlinamPrimary

/**
 * Full-screen WhatsApp-grade Text Status Composer.
 */
@Composable
fun TextStatusComposerScreen(
    onDismiss: () -> Unit,
    onPostStatus: (text: String, colorHex: String, fontIndex: Int) -> Unit
) {
    BackHandler { onDismiss() }

    var statusText by remember { mutableStateOf("") }
    var colorIndex by remember { mutableIntStateOf(0) }
    var fontIndex by remember { mutableIntStateOf(0) }
    var showEmojiBar by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }

    val currentColor = TextStatusPalettes.colors[colorIndex % TextStatusPalettes.colors.size]
    val currentColorHex = TextStatusPalettes.colorHexStrings[colorIndex % TextStatusPalettes.colorHexStrings.size]
    val (currentFont, fontName) = TextStatusPalettes.fonts[fontIndex % TextStatusPalettes.fonts.size]

    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    val dynamicFontSize = when {
        statusText.length < 35 -> 32.sp
        statusText.length < 80 -> 24.sp
        else -> 19.sp
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(currentColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .testTag("text_status_composer")
    ) {
        // TOP CONTROLS BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dismiss (X)
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.25f))
                    .testTag("composer_close_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Right Action Controls: Emoji, Font Switcher (T), Color Palette Switcher
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Emoji picker toggle
                IconButton(
                    onClick = { showEmojiBar = !showEmojiBar },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (showEmojiBar) Color.White.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.25f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Mood,
                        contentDescription = "Emojis",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Font switcher (T)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.25f))
                        .clickable {
                            fontIndex = (fontIndex + 1) % TextStatusPalettes.fonts.size
                        }
                        .testTag("composer_font_toggle"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TextFields,
                        contentDescription = "Change font ($fontName)",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Color palette switcher
                IconButton(
                    onClick = {
                        colorIndex = (colorIndex + 1) % TextStatusPalettes.colors.size
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.25f))
                        .testTag("composer_color_palette")
                ) {
                    Icon(
                        imageVector = Icons.Default.ColorLens,
                        contentDescription = "Change background color",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // CENTER AUTO-SCALING TEXT INPUT
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 70.dp),
            contentAlignment = Alignment.Center
        ) {
            BasicTextField(
                value = statusText,
                onValueChange = { if (it.length <= 500) statusText = it },
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = dynamicFontSize,
                    fontWeight = FontWeight.Medium,
                    fontFamily = currentFont,
                    textAlign = TextAlign.Center
                ),
                cursorBrush = SolidColor(Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .testTag("text_status_input"),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (statusText.isEmpty()) {
                            Text(
                                text = "Type a status",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = dynamicFontSize,
                                fontFamily = currentFont,
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Center
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }

        // OPTIONAL EMOJI PICKER STRIP
        AnimatedVisibility(
            visible = showEmojiBar,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 76.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.Black.copy(alpha = 0.65f),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                val emojiList = listOf("❤️", "😂", "🔥", "🙏", "😍", "🎉", "✨", "💯", "😎", "🚀", "👍", "🥳", "🌟", "🤩")
                LazyRow(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(emojiList) { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 28.sp,
                            modifier = Modifier
                                .clickable {
                                    statusText += emoji
                                }
                                .padding(4.dp)
                        )
                    }
                }
            }
        }

        // BOTTOM BAR: Privacy Chip + Floating Send Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Privacy Chip
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.35f))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Status (Contacts)",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Floating Send Button (Active only when text is not blank)
            FloatingActionButton(
                onClick = {
                    if (statusText.isNotBlank()) {
                        onPostStatus(statusText.trim(), currentColorHex, fontIndex)
                    }
                },
                shape = CircleShape,
                containerColor = if (statusText.isNotBlank()) Color(0xFF25D366) else Color.White.copy(alpha = 0.4f),
                contentColor = Color.White,
                modifier = Modifier
                    .size(54.dp)
                    .testTag("post_text_status_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Post Status",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
