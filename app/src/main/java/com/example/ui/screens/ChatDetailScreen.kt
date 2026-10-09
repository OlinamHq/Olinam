package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.CallType
import com.example.model.Conversation
import com.example.model.Message
import com.example.ui.components.formatMessageTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Authentic WhatsApp-style doodle wallpaper background.
 * Cached efficiently with drawWithCache so zero allocations occur during scroll.
 */
@Composable
fun WhatsAppDoodleBackground(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawWithCache {
                val baseBg = Color(0xFFEFEAE2)
                val doodleColor = Color(0xFF54656F).copy(alpha = 0.04f)
                val stroke = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)

                val cachedPath = Path()
                val stepX = 90f
                val stepY = 90f
                var y = 30f
                var row = 0
                while (y < size.height) {
                    var x = if (row % 2 == 0) 40f else 85f
                    while (x < size.width) {
                        when ((row + (x / stepX).toInt()) % 4) {
                            1 -> {
                                cachedPath.moveTo(x - 8f, y - 6f)
                                cachedPath.lineTo(x + 8f, y - 6f)
                                cachedPath.lineTo(x + 8f, y + 4f)
                                cachedPath.lineTo(x - 2f, y + 4f)
                                cachedPath.lineTo(x - 6f, y + 8f)
                                cachedPath.lineTo(x - 6f, y + 4f)
                                cachedPath.lineTo(x - 8f, y + 4f)
                                cachedPath.close()
                            }
                        }
                        x += stepX
                    }
                    y += stepY
                    row++
                }

                onDrawBehind {
                    drawRect(color = baseBg)
                    drawPath(path = cachedPath, color = doodleColor, style = stroke)
                }
            }
    )
}

@Composable
fun ChatDetailScreen(
    conversation: Conversation,
    messages: List<Message>,
    currentUserId: String,
    safetyNumber: String,
    onBackClick: () -> Unit,
    onSendMessage: (String) -> Unit,
    onSendDirectSms: (String) -> Boolean = { false },
    onCallClick: (CallType) -> Unit = {},
    onVerifyAppStatus: () -> Unit = {},
    onLoadMessages: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onDeleteMessage: (String) -> Unit = {},
    onClearChat: () -> Unit = {},
    onDeleteChat: () -> Unit = {}
) {
    BackHandler { onBackClick() }

    val context = LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    var inputText by remember { mutableStateOf("") }
    var pendingSmsText by remember { mutableStateOf("") }
    var menuExpanded by remember { mutableStateOf(false) }
    var showSafetyDialog by remember { mutableStateOf(false) }
    var selectedMessageForCrypto by remember { mutableStateOf<Message?>(null) }
    var messageForAction by remember { mutableStateOf<Message?>(null) }
    var showDeleteMessageConfirm by remember { mutableStateOf<Message?>(null) }
    var showClearChatConfirm by remember { mutableStateOf(false) }
    var showDeleteChatConfirm by remember { mutableStateOf(false) }
    var showAttachSheet by remember { mutableStateOf(false) }

    // Dynamic recipient status: recipient has Olinam vs cellular SMS
    var sendViaSms by remember(conversation.id, conversation.isSmsContact) {
        mutableStateOf(conversation.isSmsContact)
    }

    val listState = rememberLazyListState()

    // Dynamically re-verify if recipient has registered on Olinam and load messages immediately
    LaunchedEffect(conversation.id) {
        onVerifyAppStatus()
        onLoadMessages()
    }

    LaunchedEffect(conversation.isSmsContact) {
        sendViaSms = conversation.isSmsContact
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Permission launcher for sending SMS directly from SIM without opening external apps
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            if (pendingSmsText.isNotBlank()) {
                val ok = onSendDirectSms(pendingSmsText)
                if (ok) {
                    Toast.makeText(context, "Text message sent directly via SIM", Toast.LENGTH_SHORT).show()
                }
                pendingSmsText = ""
            }
        } else {
            Toast.makeText(context, "SMS permission needed to send direct SIM text", Toast.LENGTH_LONG).show()
        }
    }

    fun handleSendAction() {
        val textToSend = inputText.trim()
        if (textToSend.isBlank()) {
            // Send audio voice note simulation if text empty
            onSendMessage("🎙️ Voice message (0:04)")
            return
        }

        if (sendViaSms) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                val ok = onSendDirectSms(textToSend)
                if (ok) {
                    Toast.makeText(context, "SMS sent via SIM to ${conversation.title}", Toast.LENGTH_SHORT).show()
                }
                inputText = ""
            } else {
                pendingSmsText = textToSend
                inputText = ""
                smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
            }
        } else {
            onSendMessage(textToSend)
            inputText = ""
        }
    }

    val isSelf = conversation.title.equals("You", ignoreCase = true) ||
            conversation.id == "self" ||
            conversation.title.contains("Message yourself", ignoreCase = true)

    val topBarTitle = if (isSelf) "Message yourself" else conversation.title
    val topBarSubtitle = when {
        isSelf -> "End-to-End Encrypted 🔒"
        sendViaSms -> "Cellular SMS (Non-app contact)"
        conversation.onlineStatus != null -> conversation.onlineStatus
        else -> "online"
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("chat_detail_screen"),
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            // Top App Bar matching Screenshots 3 & 4
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .background(Color.White)
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("chat_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF111B21),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Clickable Avatar and Title header -> Opens Contact Profile Screen!
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onOpenProfile() }
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Circular Gradient Avatar
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("chat_header_avatar"),
                        contentAlignment = Alignment.Center
                    ) {
                        OlinamGradientRing(
                            modifier = Modifier.size(38.dp),
                            strokeRatio = 0.23f,
                            initialChar = if (isSelf) "Y" else conversation.title.take(1)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = topBarTitle,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF111B21),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = topBarSubtitle ?: "online",
                            fontSize = 12.5.sp,
                            color = if (sendViaSms) Color(0xFFB45309) else Color(0xFF667781),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onCallClick(CallType.VIDEO) },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("chat_video_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Video Call",
                            tint = Color(0xFF54656F),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(
                        onClick = { onCallClick(CallType.VOICE) },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("chat_voice_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Voice Call",
                            tint = Color(0xFF54656F),
                            modifier = Modifier.size(21.dp)
                        )
                    }

                    // 3-dots overflow menu matching Screenshot 3
                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("chat_more_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options",
                                tint = Color(0xFF54656F),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.widthIn(min = 200.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Add to contacts", fontSize = 15.sp, color = Color(0xFF111B21)) },
                            onClick = {
                                menuExpanded = false
                                Toast.makeText(context, "Adding to contacts...", Toast.LENGTH_SHORT).show()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Search", fontSize = 15.sp, color = Color(0xFF111B21)) },
                            onClick = {
                                menuExpanded = false
                                Toast.makeText(context, "Search inside chat", Toast.LENGTH_SHORT).show()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Add to list", fontSize = 15.sp, color = Color(0xFF111B21)) },
                            onClick = {
                                menuExpanded = false
                                Toast.makeText(context, "Added to list", Toast.LENGTH_SHORT).show()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Media, links, and docs", fontSize = 15.sp, color = Color(0xFF111B21)) },
                            onClick = {
                                menuExpanded = false
                                Toast.makeText(context, "Opening shared media...", Toast.LENGTH_SHORT).show()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Disappearing messages", fontSize = 15.sp, color = Color(0xFF111B21)) },
                            onClick = {
                                menuExpanded = false
                                Toast.makeText(context, "Disappearing messages: Off", Toast.LENGTH_SHORT).show()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Chat theme", fontSize = 15.sp, color = Color(0xFF111B21)) },
                            onClick = {
                                menuExpanded = false
                                Toast.makeText(context, "Theme: WhatsApp Default", Toast.LENGTH_SHORT).show()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("More", fontSize = 15.sp, color = Color(0xFF111B21))
                                    Text("›", fontSize = 18.sp, color = Color(0xFF667781))
                                }
                            },
                            onClick = {
                                menuExpanded = false
                                showSafetyDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Clear chat", fontSize = 15.sp, color = Color(0xFFDC2626)) },
                            onClick = {
                                menuExpanded = false
                                showClearChatConfirm = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete chat", fontSize = 15.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.SemiBold) },
                            onClick = {
                                menuExpanded = false
                                showDeleteChatConfirm = true
                            }
                        )
                    }
                }
            }
        }
    },
    bottomBar = {
            // Floating WhatsApp Input Row matching Screenshots 3 & 4
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .background(Color.Transparent)
            ) {
                // Quick Attach Panel
                AnimatedVisibility(visible = showAttachSheet) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            AttachItem("Document", "📄") {
                                onSendMessage("📄 Document shared")
                                showAttachSheet = false
                            }
                            AttachItem("Camera", "📸") {
                                onSendMessage("📸 Photo taken")
                                showAttachSheet = false
                            }
                            AttachItem("Gallery", "🖼️") {
                                onSendMessage("🖼️ Sent photo from gallery")
                                showAttachSheet = false
                            }
                            AttachItem("Audio", "🎵") {
                                onSendMessage("🎵 Audio track")
                                showAttachSheet = false
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 6.dp, end = 6.dp, bottom = 8.dp, top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // White capsule text field
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .shadow(1.5.dp, RoundedCornerShape(25.dp)),
                        shape = RoundedCornerShape(25.dp),
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Emoji button
                            IconButton(
                                onClick = { inputText += " 😊" },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SentimentSatisfiedAlt,
                                    contentDescription = "Emoji",
                                    tint = Color(0xFF8696A0),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // Dynamic SMS chip indicator if recipient is non-app contact
                            if (sendViaSms) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFEF3C7),
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    Text(
                                        text = "SMS",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB45309),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (inputText.isEmpty()) {
                                    Text(
                                        text = if (sendViaSms) "Message (SIM SMS)..." else "Message",
                                        fontSize = 16.sp,
                                        color = Color(0xFF8696A0)
                                    )
                                }
                                BasicTextField(
                                    value = inputText,
                                    onValueChange = { inputText = it },
                                    textStyle = TextStyle(
                                        fontSize = 16.sp,
                                        color = Color(0xFF111B21)
                                    ),
                                    cursorBrush = SolidColor(Color(0xFF00A884)),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                    keyboardActions = KeyboardActions(
                                        onSend = { handleSendAction() }
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("chat_message_input")
                                )
                            }

                            // Attachment paperclip button
                            IconButton(
                                onClick = { showAttachSheet = !showAttachSheet },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AttachFile,
                                    contentDescription = "Attach",
                                    tint = Color(0xFF8696A0),
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            // Camera button
                            IconButton(
                                onClick = {
                                    onSendMessage("📸 Photo sent")
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Camera",
                                    tint = Color(0xFF8696A0),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Floating circular action button (WhatsApp green or dark teal)
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(2.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color(0xFF00A884)) // Authentic WhatsApp Teal Green
                            .clickable { handleSendAction() }
                            .testTag("send_message_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (inputText.isNotBlank()) Icons.AutoMirrored.Filled.Send else Icons.Default.Mic,
                            contentDescription = if (inputText.isNotBlank()) "Send" else "Voice message",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Wallpaper Background
            WhatsAppDoodleBackground()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // Centered Date Chip (Screenshot 4: "18 December 2025")
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.95f),
                        shadowElevation = 0.5.dp
                    ) {
                        Text(
                            text = getChatDisplayDate(messages.lastOrNull()?.timestamp ?: System.currentTimeMillis()),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF54656F),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Amber Security / SMS Announcement Banner matching Screenshot 4
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .widthIn(max = 340.dp)
                            .clickable { showSafetyDialog = true }
                            .testTag("security_announcement_banner"),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFFEECD), // Soft warm parchment / amber card (Screenshot 4)
                        shadowElevation = 0.5.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (sendViaSms) {
                                // Dynamic SMS Notice (Recipient does not have Olinam)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Sms,
                                        contentDescription = "SMS",
                                        tint = Color(0xFF92400E),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Text Message (SMS)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E)
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "This contact does not have Olinam installed. Messages are sent securely from your SIM carrier directly within Olinam without opening any third-party app. Tap for details.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF78350F),
                                    lineHeight = 16.sp,
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                // Authentic WhatsApp End-to-End Encryption Notice (Screenshot 4)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Lock",
                                        tint = Color(0xFF54656F),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isSelf) {
                                            "Messages to yourself are end-to-end encrypted. No one else, not even Olinam, can read, listen to, or share them. "
                                        } else {
                                            "Messages and calls are end-to-end encrypted. No one outside of this chat, not even Olinam, can read or listen to them. "
                                        },
                                        fontSize = 12.sp,
                                        color = Color(0xFF54656F),
                                        lineHeight = 16.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                Text(
                                    text = "Learn more",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111B21),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Messages list
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        val isMe = msg.senderId == currentUserId || msg.senderId == "user_me" || msg.senderId == "me"
                        WhatsAppMessageBubble(
                            message = msg,
                            isMe = isMe,
                            onClick = { selectedMessageForCrypto = msg },
                            onLongClick = { messageForAction = msg }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }

    // Safety / Encryption details modal
    if (showSafetyDialog) {
        AlertDialog(
            onDismissRequest = { showSafetyDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = Color(0xFF00A884),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = if (sendViaSms) "Cellular SMS Carrier Mode" else "End-to-End Encryption",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (sendViaSms) {
                        Text(
                            text = "Because this contact does not have Olinam installed yet, all messages are delivered directly through your phone's SIM card carrier without opening external apps.",
                            fontSize = 13.5.sp,
                            color = Color(0xFF4B5563),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEF3C7),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "💡 When this contact signs up on Olinam, this chat will automatically transition to real-time E2EE messaging!",
                                fontSize = 12.sp,
                                color = Color(0xFF92400E),
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "Messages and calls in this chat are secured with client-side end-to-end encryption. Your messages are encrypted on your device and can only be decrypted by the recipient.",
                            fontSize = 13.5.sp,
                            color = Color(0xFF4B5563),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF3F4F6),
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCode,
                                    contentDescription = "Safety QR",
                                    tint = Color(0xFF111827),
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = safetyNumber,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFF111827)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSafetyDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A884))
                ) {
                    Text("OK")
                }
            }
        )
    }

    // Context menu / action dialog for a long-pressed message
    messageForAction?.let { msg ->
        val isMe = msg.senderId == currentUserId || msg.senderId == "user_me" || msg.senderId == "me"
        AlertDialog(
            onDismissRequest = { messageForAction = null },
            title = {
                Text("Message Options", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "\"${msg.text.take(60)}${if (msg.text.length > 60) "..." else ""}\"",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = {
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(msg.text))
                            Toast.makeText(context, "Message copied to clipboard", Toast.LENGTH_SHORT).show()
                            messageForAction = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Copy Text")
                    }
                    Button(
                        onClick = {
                            val target = messageForAction
                            messageForAction = null
                            showDeleteMessageConfirm = target
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Delete Message")
                    }
                    TextButton(
                        onClick = {
                            val target = messageForAction
                            messageForAction = null
                            selectedMessageForCrypto = target
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View Message Info", color = Color(0xFF0160E3))
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { messageForAction = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Confirmation dialog to delete message
    showDeleteMessageConfirm?.let { msg ->
        AlertDialog(
            onDismissRequest = { showDeleteMessageConfirm = null },
            title = { Text("Delete message?", fontWeight = FontWeight.Bold) },
            text = { Text("Delete this message? It will be removed from your chat history.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteMessage(msg.id)
                        showDeleteMessageConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteMessageConfirm = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Confirmation dialog to clear chat
    if (showClearChatConfirm) {
        AlertDialog(
            onDismissRequest = { showClearChatConfirm = false },
            title = { Text("Clear this chat?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to clear all messages in this conversation?") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearChat()
                        showClearChatConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Clear", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearChatConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Confirmation dialog to delete entire chat
    if (showDeleteChatConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteChatConfirm = false },
            title = { Text("Delete this chat?", fontWeight = FontWeight.Bold) },
            text = { Text("Delete the chat with ${conversation.title} and all its messages?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteChatConfirm = false
                        onDeleteChat()
                        onBackClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteChatConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Inspect individual message crypto details
    selectedMessageForCrypto?.let { msg ->
        AlertDialog(
            onDismissRequest = { selectedMessageForCrypto = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFF00A884),
                    modifier = Modifier.size(30.dp)
                )
            },
            title = {
                Text("Message Info", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Content:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Surface(
                        color = Color(0xFFF3F4F6),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = msg.text, modifier = Modifier.padding(8.dp), fontSize = 13.sp)
                    }

                    Text(
                        text = if (msg.isSms) "Protocol: Direct SIM SMS (Standard Cellular)" else "Protocol: AES-256-GCM (End-to-End Encrypted via Firebase)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (msg.isSms) Color(0xFFB45309) else Color(0xFF00A884)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedMessageForCrypto = null }) {
                    Text("Close")
                }
            }
        )
    }
}

/**
 * WhatsApp Chat Bubble:
 * Sent: Pale green `#D9FDD3` with subtle tail on right, double blue ticks.
 * Received: Pure white `#FFFFFF` with subtle tail on left.
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun WhatsAppMessageBubble(
    message: Message,
    isMe: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.5.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            modifier = Modifier
                .widthIn(min = 60.dp, max = 290.dp)
                .shadow(0.5.dp, RoundedCornerShape(10.dp))
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .testTag("message_bubble_${message.id}"),
            shape = RoundedCornerShape(
                topStart = 10.dp,
                topEnd = 10.dp,
                bottomStart = if (isMe) 10.dp else 2.dp,
                bottomEnd = if (isMe) 2.dp else 10.dp
            ),
            color = if (isMe) Color(0xFFD9FDD3) else Color.White // Authentic WhatsApp Bubble Colors
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
            ) {
                // If SMS, show small SMS indicator tag
                if (message.isSms) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sms,
                            contentDescription = "SMS",
                            tint = Color(0xFFB45309),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "SMS text",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB45309)
                        )
                    }
                }

                Text(
                    text = message.text,
                    fontSize = 15.sp,
                    color = Color(0xFF111B21),
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatMessageTime(message.timestamp),
                        fontSize = 11.sp,
                        color = Color(0xFF667781)
                    )

                    if (isMe) {
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Read",
                            tint = Color(0xFF53BDEB), // Authentic WhatsApp Blue Double Checkmark
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AttachItem(
    title: String,
    emoji: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFFF3F4F6),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = emoji, fontSize = 22.sp)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = title, fontSize = 12.sp, color = Color(0xFF111B21))
    }
}

private val chatDisplayDateFormat = SimpleDateFormat("d MMMM yyyy", Locale.US)
private fun getChatDisplayDate(timestamp: Long): String {
    val date = Date(timestamp)
    return synchronized(chatDisplayDateFormat) { chatDisplayDateFormat.format(date) }
}
