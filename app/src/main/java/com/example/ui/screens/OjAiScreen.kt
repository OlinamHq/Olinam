@file:OptIn(
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.AiPlugin
import com.example.ai.DelegatedContact
import com.example.ai.OjAiAgentManager
import com.example.ai.OjAiChatMessage
import com.example.ai.OjAiService
import com.example.data.ContactHelper
import com.example.model.AppTab
import com.example.ui.ChatViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private val GeminiBlue = Color(0xFF0160E3)
private val GeminiPurple = Color(0xFF7C3AED)
private val WhatsAppGreenBubble = Color(0xFFE7FFDB) // Signature WhatsApp outgoing bubble
private val WhatsAppDarkText = Color(0xFF111B21)
private val WhatsAppSubText = Color(0xFF54656F)
private val WhatsAppTickBlue = Color(0xFF53BDEB)

/**
 * Authentic WhatsApp Meta AI style Chat Screen for Oj AI:
 * - Real WhatsApp doodle wallpaper background
 * - Authentic WhatsApp chat Top Bar with Meta AI glowing ring & call/menu actions
 * - Center date pill and E2EE yellow privacy banner
 * - Light green outgoing user bubbles with double ticks & crisp white incoming AI bubbles
 * - Exact WhatsApp bottom input bar: floating capsule with emoji, attach, camera + circular blue send/mic button
 * - Clean ModalBottomSheet for Agent Tools & Workspace Plugins accessible from menu
 */
@Composable
fun OjAiScreen(
    viewModel: ChatViewModel,
    onBackClick: () -> Unit = { viewModel.setTab(AppTab.CHATS) },
    onOpenMorph: () -> Unit = { viewModel.setTab(AppTab.MORPH) }
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val agentManager = remember { viewModel.getOrCreateAiAgentManager(context) }

    // Intercept hardware/system back gesture to smoothly return to Chats
    BackHandler {
        onBackClick()
    }

    val messages by viewModel.ojAiMessages.collectAsState()
    val isTyping by viewModel.isOjAiTyping.collectAsState()
    val listState = rememberLazyListState()

    var inputPrompt by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showProfileScreen by remember { mutableStateOf(false) }
    var showAgentToolsSheet by remember { mutableStateOf(false) }
    var showEmojiQuickPicker by remember { mutableStateOf(false) }
    var showAttachMenu by remember { mutableStateOf(false) }

    // When profile/settings is opened, show full-screen OjAiProfileScreen (matching ContactProfileScreen)
    if (showProfileScreen) {
        OjAiProfileScreen(
            agentManager = agentManager,
            viewModel = viewModel,
            onBackClick = { showProfileScreen = false }
        )
        return
    }

    // Auto-scroll to bottom on new message or typing state change
    LaunchedEffect(messages.size, isTyping) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("oj_ai_screen")
    ) {
        // 1. Authentic WhatsApp Doodle Background
        WhatsAppDoodleBackground(modifier = Modifier.fillMaxSize())

        Column(modifier = Modifier.fillMaxSize()) {
            // 2. WhatsApp Authentic Chat Top Bar
            Surface(
                color = Color.White,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back Button (Switches back to main Chats tab)
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Chats",
                                tint = WhatsAppDarkText,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Meta AI Style Glowing Ring Avatar
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            GeminiBlue,
                                            GeminiPurple,
                                            Color(0xFF06B6D4)
                                        )
                                    )
                                )
                                .clickable { showProfileScreen = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Oj AI Profile",
                                    tint = GeminiBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Name & Online Status
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showProfileScreen = true }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Oj AI",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WhatsAppDarkText,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified",
                                    tint = GeminiBlue,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF22C55E))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isTyping) "typing..." else "with Google Gemini • online",
                                    fontSize = 12.sp,
                                    color = if (isTyping) GeminiBlue else WhatsAppSubText,
                                    maxLines = 1
                                )
                            }
                        }

                        // Action Icons: Overflow Menu

                        // Omkun Morph Shortcut Button
                        IconButton(
                            onClick = onOpenMorph,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_morph_notes),
                                contentDescription = "Omkun Morph Notes",
                                tint = GeminiBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Menu",
                                    tint = WhatsAppSubText,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Omkun Morph Notes") },
                                    leadingIcon = {
                                        Icon(
                                            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_morph_notes),
                                            contentDescription = null,
                                            tint = GeminiBlue,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        onOpenMorph()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Profile & Settings") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null,
                                            tint = GeminiBlue,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        showProfileScreen = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Agent Tools & Rules") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Settings,
                                            contentDescription = null,
                                            tint = GeminiBlue,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        showProfileScreen = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Clear Chat") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        showClearDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("About Gemini 3.5 Flash") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = GeminiPurple,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        Toast.makeText(
                                            context,
                                            "Oj AI is powered by Google Gemini 3.5 Flash with end-to-end privacy.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 3. Chat Messages LazyColumn
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 8.dp)
            ) {
                // Top Centered Date Pill (Matching WhatsApp screenshot)
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.92f),
                            shadowElevation = 0.5.dp
                        ) {
                            Text(
                                text = "Today",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = WhatsAppSubText,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Yellow End-to-End Encryption Banner (Matching Screenshot 1)
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFFF9C4), // Soft warm yellow banner
                            shadowElevation = 0.5.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Encrypted",
                                    tint = Color(0xFF854D0E),
                                    modifier = Modifier
                                        .size(13.dp)
                                        .offset(y = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Messages and calls are end-to-end encrypted. Generated with Google Gemini 3.5 Flash. No one outside of this chat, not even Olinam, can read them. Learn more",
                                    fontSize = 12.sp,
                                    color = Color(0xFF713F12),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                // Suggested Prompts Row if chat has 1 or fewer messages
                if (messages.size <= 1) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "💡 Quick Prompts",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = WhatsAppSubText,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                items(OjAiService.initialSuggestions) { suggestion ->
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color.White,
                                        shadowElevation = 1.dp,
                                        modifier = Modifier.clickable {
                                            viewModel.sendOjAiPrompt(suggestion)
                                        }
                                    ) {
                                        Text(
                                            text = suggestion,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = WhatsAppDarkText,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Messages List (WhatsApp Bubbles)
                items(messages, key = { it.id }) { msg ->
                    if (msg.isUser) {
                        WhatsAppUserBubble(message = msg)
                    } else {
                        WhatsAppAiBubble(
                            message = msg,
                            onCopy = { text ->
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Oj AI response", text))
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            onShare = { text ->
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, text)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Oj AI response"))
                            },
                            onFeedback = { feedback ->
                                viewModel.setOjAiFeedback(msg.id, feedback)
                                Toast.makeText(context, if (feedback > 0) "Helpful! 👍" else "Thanks for feedback 👎", Toast.LENGTH_SHORT).show()
                            },
                            onRegenerate = {
                                val lastUserMsg = messages.filter { it.isUser }.lastOrNull()
                                if (lastUserMsg != null) {
                                    viewModel.sendOjAiPrompt(lastUserMsg.text)
                                }
                            }
                        )
                    }
                }

                // Typing Indicator Bubble
                if (isTyping) {
                    item {
                        WhatsAppAiTypingBubble()
                    }
                }
            }

            // Quick emoji picker bar when emoji icon is clicked
            AnimatedVisibility(visible = showEmojiQuickPicker) {
                Surface(
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val quickEmojis = listOf("😊", "👍", "❤️", "🔥", "✨", "😂", "🙏", "💡", "🚀", "🎉")
                        quickEmojis.forEach { emoji ->
                            Text(
                                text = emoji,
                                fontSize = 22.sp,
                                modifier = Modifier
                                    .clickable {
                                        inputPrompt += emoji
                                    }
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }

            // 4. WhatsApp Bottom Input Bar (Exact match to Screenshot 1)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 6.dp, end = 6.dp, bottom = 6.dp, top = 2.dp)
                    .navigationBarsPadding()
                    .imePadding(),
                verticalAlignment = Alignment.Bottom
            ) {
                // Floating Capsule Input on Left
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = Color.White,
                    shadowElevation = 1.5.dp,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Smiley / Emoji Button
                        IconButton(
                            onClick = { showEmojiQuickPicker = !showEmojiQuickPicker },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SentimentSatisfiedAlt,
                                contentDescription = "Emoji",
                                tint = if (showEmojiQuickPicker) GeminiBlue else WhatsAppSubText,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Message Input Field
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp, vertical = 6.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (inputPrompt.isEmpty()) {
                                Text(
                                    text = "Message",
                                    fontSize = 16.sp,
                                    color = Color(0xFF8696A0)
                                )
                            }
                            BasicTextField(
                                value = inputPrompt,
                                onValueChange = { inputPrompt = it },
                                textStyle = TextStyle(
                                    fontSize = 16.sp,
                                    color = WhatsAppDarkText
                                ),
                                cursorBrush = SolidColor(GeminiBlue),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(
                                    onSend = {
                                        if (inputPrompt.isNotBlank()) {
                                            val promptToSend = inputPrompt
                                            inputPrompt = ""
                                            viewModel.sendOjAiPrompt(promptToSend)
                                        }
                                    }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("oj_ai_chat_input")
                            )
                        }

                        // Paperclip Attachment Button
                        Box {
                            IconButton(
                                onClick = { showAttachMenu = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AttachFile,
                                    contentDescription = "Attach",
                                    tint = WhatsAppSubText,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showAttachMenu,
                                onDismissRequest = { showAttachMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Open Omkun Morph") },
                                    leadingIcon = {
                                        Icon(
                                            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_morph_notes),
                                            contentDescription = null,
                                            tint = GeminiBlue,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    onClick = {
                                        showAttachMenu = false
                                        onOpenMorph()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Insert Omkun Morph Note") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Description,
                                            contentDescription = null,
                                            tint = GeminiPurple,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    onClick = {
                                        showAttachMenu = false
                                        val morphRepo = com.example.data.MorphRepository.getInstance(context)
                                        val latestNote = morphRepo.notesFlow.value.firstOrNull()
                                        if (latestNote != null) {
                                            inputPrompt = if (inputPrompt.isBlank()) {
                                                "Note: ${latestNote.title}\n${latestNote.content}"
                                            } else {
                                                "$inputPrompt\n\n[Note: ${latestNote.title}]\n${latestNote.content}"
                                            }
                                        } else {
                                            Toast.makeText(context, "No notes found in Morph", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Attach Document / File") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.AttachFile,
                                            contentDescription = null,
                                            tint = WhatsAppDarkText,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    onClick = {
                                        showAttachMenu = false
                                        Toast.makeText(context, "Attach doc, photo or audio for Oj AI", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }

                        // Camera Button
                        IconButton(
                            onClick = {
                                Toast.makeText(context, "Camera input for Oj AI Vision", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Camera",
                                tint = WhatsAppSubText,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Floating Circular Blue Action Button (Mic / Send)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(2.dp, CircleShape)
                        .clip(CircleShape)
                        .background(GeminiBlue)
                        .clickable {
                            if (inputPrompt.isNotBlank()) {
                                val promptToSend = inputPrompt
                                inputPrompt = ""
                                viewModel.sendOjAiPrompt(promptToSend)
                            } else {
                                Toast.makeText(context, "Listening... speak your prompt to Oj AI", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .testTag("oj_ai_send_mic_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (inputPrompt.isNotBlank()) Icons.AutoMirrored.Filled.Send else Icons.Default.Mic,
                        contentDescription = if (inputPrompt.isNotBlank()) "Send" else "Voice input",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }

    // Clear Chat Confirmation Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear Oj AI chat?") },
            text = { Text("This will reset your chat history with Oj AI. Your saved agent tools and rules will be preserved.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearOjAiChat()
                        showClearDialog = false
                        Toast.makeText(context, "Chat cleared", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal Bottom Sheet for Agent Tools & Workspace Plugins
    if (showAgentToolsSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showAgentToolsSheet = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            OjAiAgentBottomSheetContent(
                agentManager = agentManager,
                onClose = {
                    scope.launch {
                        sheetState.hide()
                        showAgentToolsSheet = false
                    }
                }
            )
        }
    }
}

/**
 * Authentic WhatsApp User Bubble (Outgoing - Light Green with double ticks)
 */
@Composable
private fun WhatsAppUserBubble(message: OjAiChatMessage) {
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) { timeFormat.format(Date(message.timestamp)) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 2.dp,
                bottomStart = 14.dp,
                bottomEnd = 14.dp
            ),
            color = WhatsAppGreenBubble,
            shadowElevation = 0.8.dp,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)) {
                Text(
                    text = message.text,
                    fontSize = 15.sp,
                    color = WhatsAppDarkText,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formattedTime,
                        fontSize = 11.sp,
                        color = WhatsAppSubText
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(
                        imageVector = Icons.Default.DoneAll,
                        contentDescription = "Read",
                        tint = WhatsAppTickBlue,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

/**
 * Authentic WhatsApp Incoming AI Bubble (White, left-aligned, markdown supported, subtle actions)
 */
@Composable
private fun WhatsAppAiBubble(
    message: OjAiChatMessage,
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit,
    onFeedback: (Int) -> Unit,
    onRegenerate: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) { timeFormat.format(Date(message.timestamp)) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 2.dp,
                topEnd = 14.dp,
                bottomStart = 14.dp,
                bottomEnd = 14.dp
            ),
            color = Color.White,
            shadowElevation = 0.8.dp,
            modifier = Modifier.widthIn(max = 330.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                // Header badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = GeminiBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Oj AI",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GeminiBlue
                    )
                }

                // AI Response Text
                Text(
                    text = message.text,
                    fontSize = 14.5.sp,
                    color = WhatsAppDarkText,
                    lineHeight = 21.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Bottom Row: Timestamp & Compact Action Icons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Actions on Left
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onCopy(message.text) },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = WhatsAppSubText,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        IconButton(
                            onClick = { onShare(message.text) },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = WhatsAppSubText,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        IconButton(
                            onClick = { onFeedback(1) },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ThumbUp,
                                contentDescription = "Like",
                                tint = if (message.feedback == 1) GeminiBlue else WhatsAppSubText,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        IconButton(
                            onClick = { onRegenerate() },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Regenerate",
                                tint = WhatsAppSubText,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    // Timestamp on Right
                    Text(
                        text = formattedTime,
                        fontSize = 11.sp,
                        color = WhatsAppSubText
                    )
                }
            }
        }
    }
}

/**
 * Animated Typing Bubble for Oj AI (WhatsApp style with bouncing dots)
 */
@Composable
private fun WhatsAppAiTypingBubble() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    val dot1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val dot2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 150, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val dot3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 2.dp,
                topEnd = 14.dp,
                bottomStart = 14.dp,
                bottomEnd = 14.dp
            ),
            color = Color.White,
            shadowElevation = 0.8.dp,
            modifier = Modifier.padding(vertical = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = GeminiBlue,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Oj AI is thinking",
                    fontSize = 13.sp,
                    color = WhatsAppSubText
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .offset(y = dot1.dp)
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(GeminiBlue)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Box(
                    modifier = Modifier
                        .offset(y = dot2.dp)
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(GeminiPurple)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Box(
                    modifier = Modifier
                        .offset(y = dot3.dp)
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF06B6D4))
                )
            }
        }
    }
}

/**
 * Clean ModalBottomSheet Content for Autonomous Agent Tools & Plugins
 */
@Composable
private fun OjAiAgentBottomSheetContent(
    agentManager: OjAiAgentManager,
    onClose: () -> Unit
) {
    val plugins by agentManager.plugins.collectAsState()
    val settings by agentManager.settings.collectAsState()
    val delegatedContacts by agentManager.delegatedContacts.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .padding(bottom = 28.dp)
            .navigationBarsPadding()
    ) {
        // Sheet Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = GeminiBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Agent Tools & Plugins",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = WhatsAppDarkText
                    )
                    Text(
                        text = "Executive Auto-Pilot & Workspace Sync",
                        fontSize = 12.5.sp,
                        color = WhatsAppSubText
                    )
                }
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = WhatsAppSubText)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = Color(0xFFF1F5F9))
        Spacer(modifier = Modifier.height(14.dp))

        // Executive Auto-Pilot Switch
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Executive Auto-Pilot",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = WhatsAppDarkText
                    )
                    Text(
                        text = if (settings.isMasterEnabled) "Actively assists delegated contacts" else "Auto-replies are paused",
                        fontSize = 12.5.sp,
                        color = WhatsAppSubText
                    )
                }
                Switch(
                    checked = settings.isMasterEnabled,
                    onCheckedChange = { agentManager.setMasterEnabled(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = GeminiBlue)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Integrated Capabilities",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = WhatsAppDarkText,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        // Integrated Plugins
        plugins.forEach { plugin ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEFF6FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (plugin.iconType == "MORPH") {
                            Icon(
                                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_morph_notes),
                                contentDescription = plugin.name,
                                tint = GeminiBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Icon(
                                imageVector = when (plugin.iconType) {
                                    "GMAIL" -> Icons.Default.Email
                                    "DRIVE" -> Icons.Default.Description
                                    "CALENDAR" -> Icons.Default.CalendarMonth
                                    "PHOTOS" -> Icons.Default.PhotoLibrary
                                    "SMS" -> Icons.Default.SimCard
                                    else -> Icons.Default.Lightbulb
                                },
                                contentDescription = plugin.name,
                                tint = GeminiBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = plugin.name,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = WhatsAppDarkText
                        )
                        Text(
                            text = plugin.syncSummary.ifBlank { plugin.description },
                            fontSize = 11.5.sp,
                            color = WhatsAppSubText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Switch(
                    checked = plugin.isConnected,
                    onCheckedChange = { agentManager.togglePlugin(plugin.id, it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = GeminiBlue)
                )
            }
        }
    }
}
