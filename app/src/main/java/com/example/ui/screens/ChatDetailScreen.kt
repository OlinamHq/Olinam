package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Poll
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.model.CallType
import com.example.model.Conversation
import com.example.model.MediaType
import com.example.model.Message
import com.example.ui.components.formatMessageTime
import kotlinx.coroutines.launch
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
    livePresenceStatus: String? = null,
    onBackClick: () -> Unit,
    onSendMessage: (String) -> Unit,
    onSendMediaAttachment: (uri: Uri, mediaType: MediaType, caption: String) -> Unit = { _, _, _ -> },
    onSendDirectSms: (String) -> Boolean = { false },
    onCallClick: (CallType) -> Unit = {},
    onVerifyAppStatus: () -> Unit = {},
    onLoadMessages: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onDeleteMessage: (String) -> Unit = {},
    onClearChat: () -> Unit = {},
    onDeleteChat: () -> Unit = {},
    onTypingChanged: (Boolean) -> Unit = {}
) {
    BackHandler { onBackClick() }

    val context = LocalContext.current
    val density = LocalDensity.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    var inputText by remember { mutableStateOf("") }
    var pendingSmsText by remember { mutableStateOf("") }
    var menuExpanded by remember { mutableStateOf(false) }
    var showSafetyDialog by remember { mutableStateOf(false) }
    var selectedMessageForCrypto by remember { mutableStateOf<Message?>(null) }
    var messageForAction by remember { mutableStateOf<Message?>(null) }
    var showDeleteMessageConfirm by remember { mutableStateOf<Message?>(null) }
    var showClearChatConfirm by remember { mutableStateOf(false) }
    var showDeleteChatConfirm by remember { mutableStateOf(false) }

    // Panels state
    var showAttachSheet by remember { mutableStateOf(false) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var showFullGalleryPicker by remember { mutableStateOf(false) }
    var showInAppCamera by remember { mutableStateOf(false) }
    var showPollDialog by remember { mutableStateOf(false) }

    // Dynamic recipient status: recipient has Olinam vs cellular SMS
    var sendViaSms by remember(conversation.id, conversation.isSmsContact) {
        mutableStateOf(conversation.isSmsContact)
    }

    val listState = rememberLazyListState()

    // Reset typing status on exit
    DisposableEffect(conversation.id) {
        onDispose {
            onTypingChanged(false)
        }
    }

    // Dynamically re-verify recipient and load messages
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

    // Permission launcher for sending SMS directly from SIM
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

    // Document Picker launcher
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onSendMediaAttachment(uri, MediaType.DOCUMENT, "📄 Document shared")
            Toast.makeText(context, "Document selected for sending", Toast.LENGTH_SHORT).show()
        }
    }

    // System Photo Picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onSendMediaAttachment(uri, MediaType.IMAGE, "🖼️ Photo")
            Toast.makeText(context, "Photo sending...", Toast.LENGTH_SHORT).show()
        }
    }

    fun handleSendAction() {
        val textToSend = inputText.trim()
        if (textToSend.isBlank()) {
            // Voice note simulation
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
                onTypingChanged(false)
            } else {
                pendingSmsText = textToSend
                inputText = ""
                onTypingChanged(false)
                smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
            }
        } else {
            onSendMessage(textToSend)
            inputText = ""
            onTypingChanged(false)
        }
    }

    val isSelf = conversation.title.equals("You", ignoreCase = true) ||
            conversation.id == "self" ||
            conversation.title.contains("Message yourself", ignoreCase = true)

    val topBarTitle = if (isSelf) "Message yourself" else conversation.title

    // Real-time live status: typing / online / last seen (NO DUMMY "online"!)
    val topBarSubtitle = when {
        isSelf -> "Message yourself 🔒"
        sendViaSms -> "Cellular SMS (Non-app contact)"
        !livePresenceStatus.isNullOrBlank() -> livePresenceStatus
        conversation.onlineStatus != null -> conversation.onlineStatus
        else -> "last seen recently"
    }

    val isOtherTyping = topBarSubtitle.equals("typing...", ignoreCase = true)
    val isOtherOnline = topBarSubtitle.equals("online", ignoreCase = true)

    // Full-screen camera flow (matching Screenshot 4)
    if (showInAppCamera) {
        CameraScreen(
            onDismiss = { showInAppCamera = false },
            onSendStatus = { caption, mediaUri ->
                showInAppCamera = false
                if (!mediaUri.isNullOrBlank()) {
                    try {
                        val parsed = Uri.parse(mediaUri)
                        onSendMediaAttachment(parsed, MediaType.IMAGE, caption)
                    } catch (_: Exception) {
                        onSendMessage("📸 Photo: $caption")
                    }
                } else {
                    onSendMessage("📸 Photo: $caption")
                }
            }
        )
        return
    }

    // Full Recents Gallery Modal (matching Screenshot 5)
    if (showFullGalleryPicker) {
        FullGalleryPickerModal(
            onDismiss = { showFullGalleryPicker = false },
            onCameraClick = {
                showFullGalleryPicker = false
                showInAppCamera = true
            },
            onImageSelected = { uri ->
                showFullGalleryPicker = false
                onSendMediaAttachment(uri, MediaType.IMAGE, "")
            }
        )
    }

    // Interactive Poll Creator Dialog
    if (showPollDialog) {
        CreatePollDialog(
            onDismiss = { showPollDialog = false },
            onCreatePoll = { question, options ->
                showPollDialog = false
                val pollText = buildString {
                    append("📊 Poll: ").append(question).append("\n")
                    options.forEachIndexed { idx, opt ->
                        append("${idx + 1}. $opt\n")
                    }
                }
                onSendMessage(pollText)
            }
        )
    }

    // Unified edge-to-edge container: imePadding() moves layout snug to keyboard with ZERO gap
    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .testTag("chat_detail_screen")
    ) {
        // Continuous WhatsApp doodle wallpaper
        WhatsAppDoodleBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
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

                // Clickable Avatar and Title header -> Opens Contact Profile Screen
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onOpenProfile() }
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("chat_header_avatar"),
                        contentAlignment = Alignment.Center
                    ) {
                        val avatarBgColor = remember(conversation.id) {
                            val colors = listOf(
                                Color(0xFF00A884), // WhatsApp Emerald
                                Color(0xFF0284C7), // Sky
                                Color(0xFF6366F1), // Indigo
                                Color(0xFF8B5CF6), // Purple
                                Color(0xFFEC4899), // Pink
                                Color(0xFFF59E0B)  // Amber
                            )
                            colors[Math.abs(conversation.id.hashCode()) % colors.size]
                        }
                        ProfessionalAvatar(
                            modifier = Modifier.size(38.dp),
                            initialChar = if (isSelf) "Y" else conversation.title.take(1),
                            avatarUrl = conversation.avatarUrl,
                            backgroundColor = avatarBgColor,
                            fontSize = 17.sp
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
                            text = topBarSubtitle,
                            fontSize = 12.5.sp,
                            fontWeight = if (isOtherTyping) FontWeight.Bold else FontWeight.Normal,
                            color = when {
                                isOtherTyping -> Color(0xFF00A884) // Vibrant green typing indicator
                                isOtherOnline -> Color(0xFF00A884) // Real online
                                sendViaSms -> Color(0xFFB45309)
                                else -> Color(0xFF667781)
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                val hasCallablePhone = !conversation.phoneNumber.isNullOrBlank() && conversation.phoneNumber.any { it.isDigit() }
                val isAlphanumericSms = sendViaSms && !hasCallablePhone

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Only show Video Call if registered Olinam user (NOT SMS)
                    if (!sendViaSms) {
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
                    }

                    // Only show Voice Call if registered Olinam user OR SMS with dialable digits
                    if (!isAlphanumericSms) {
                        IconButton(
                            onClick = {
                                if (sendViaSms) {
                                    val phone = conversation.phoneNumber ?: conversation.title.filter { it.isDigit() || it == '+' }
                                    if (phone.isNotBlank()) {
                                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                        context.startActivity(dialIntent)
                                    }
                                } else {
                                    onCallClick(CallType.VOICE)
                                }
                            },
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
                    }

                    // 3-dots overflow menu
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
                                        Text("Encryption Info", fontSize = 15.sp, color = Color(0xFF111B21))
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

            // Chat Messages Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Centered Date Chip
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
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
                    }

                    // Security Encryption Banner (Screenshots 3 & 4)
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                .clickable { showSafetyDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFFF7D6),
                            shadowElevation = 0.5.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = if (sendViaSms) Icons.Default.Sms else Icons.Default.Lock,
                                        contentDescription = "Encrypted",
                                        tint = if (sendViaSms) Color(0xFFB45309) else Color(0xFF54656F),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (sendViaSms) {
                                            "Messages are sent as cellular SMS. Carrier SMS charges may apply."
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

                    // Message list items
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
            }

            // WhatsApp Attachment Bottom Sheet (Matching Screenshot 3)
            AnimatedVisibility(
                visible = showAttachSheet,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                WhatsAppAttachmentSheet(
                    onDocumentClick = {
                        showAttachSheet = false
                        documentPickerLauncher.launch("*/*")
                    },
                    onGalleryClick = {
                        showAttachSheet = false
                        showFullGalleryPicker = true
                    },
                    onLocationClick = {
                        showAttachSheet = false
                        onSendMessage("📍 Shared location: 28.6139° N, 77.2090° E\nhttps://maps.google.com/?q=28.6139,77.2090")
                    },
                    onContactClick = {
                        showAttachSheet = false
                        onSendMessage("👤 Contact card: Rohit Sharma (+91 98765 43210)")
                    },
                    onPollClick = {
                        showAttachSheet = false
                        showPollDialog = true
                    },
                    onCameraClick = {
                        showAttachSheet = false
                        showInAppCamera = true
                    },
                    onRecentImageClick = { sampleUrl ->
                        showAttachSheet = false
                        onSendMessage("🖼️ Sent photo: $sampleUrl")
                    }
                )
            }

            // WhatsApp Inline Emoji Picker Panel
            AnimatedVisibility(
                visible = showEmojiPicker,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                WhatsAppEmojiPicker(
                    onEmojiClick = { emoji ->
                        inputText += emoji
                        onTypingChanged(true)
                    },
                    onBackspace = {
                        if (inputText.isNotEmpty()) {
                            inputText = inputText.dropLast(1)
                            if (inputText.isEmpty()) onTypingChanged(false)
                        }
                    }
                )
            }

            // Floating Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 6.dp, end = 6.dp, bottom = 6.dp, top = 4.dp),
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
                        // Emoji / Keyboard toggle button
                        IconButton(
                            onClick = {
                                showEmojiPicker = !showEmojiPicker
                                if (showEmojiPicker) showAttachSheet = false
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (showEmojiPicker) Icons.Default.Keyboard else Icons.Default.SentimentSatisfiedAlt,
                                contentDescription = "Emoji",
                                tint = Color(0xFF8696A0),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // SMS indicator tag
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

                        // Input Text Field
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
                                onValueChange = {
                                    inputText = it
                                    onTypingChanged(it.isNotBlank())
                                },
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
                            onClick = {
                                showAttachSheet = !showAttachSheet
                                if (showAttachSheet) showEmojiPicker = false
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = "Attach",
                                tint = if (showAttachSheet) Color(0xFF00A884) else Color(0xFF8696A0),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Camera button (Screenshot 4)
                        IconButton(
                            onClick = { showInAppCamera = true },
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

                // Floating circular action button (WhatsApp green)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(2.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color(0xFF00A884))
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

            // Apply navigation bars padding ONLY when IME (keyboard) is NOT showing and emoji panel is closed!
            val isImeOpen = WindowInsets.ime.getBottom(density) > 0
            if (!isImeOpen && !showEmojiPicker && !showAttachSheet) {
                Spacer(modifier = Modifier.navigationBarsPadding())
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
 * WhatsApp Attachment Bottom Sheet matching Screenshot 3 (`20261009_220444.jpg`).
 * Contains:
 * - Drag handle
 * - Document, Gallery, Location, Contact, Poll rounded action chips
 * - Recent Media horizontal strip (Camera tile + photo thumbnails)
 */
@Composable
fun WhatsAppAttachmentSheet(
    onDocumentClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onLocationClick: () -> Unit,
    onContactClick: () -> Unit,
    onPollClick: () -> Unit,
    onCameraClick: () -> Unit,
    onRecentImageClick: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF8F9FA),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 14.dp)
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .width(38.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF8696A0).copy(alpha = 0.4f))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action chips row matching Screenshot 3
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AttachmentActionItem(
                    label = "Document",
                    icon = Icons.Default.Description,
                    bgColor = Color(0xFF7F66FF),
                    onClick = onDocumentClick
                )
                AttachmentActionItem(
                    label = "Gallery",
                    icon = Icons.Default.PhotoLibrary,
                    bgColor = Color(0xFF1E90FF),
                    onClick = onGalleryClick
                )
                AttachmentActionItem(
                    label = "Location",
                    icon = Icons.Default.LocationOn,
                    bgColor = Color(0xFF00A884),
                    onClick = onLocationClick
                )
                AttachmentActionItem(
                    label = "Contact",
                    icon = Icons.Default.Person,
                    bgColor = Color(0xFF0097A7),
                    onClick = onContactClick
                )
                AttachmentActionItem(
                    label = "Poll",
                    icon = Icons.Default.Poll,
                    bgColor = Color(0xFFEAA023),
                    onClick = onPollClick
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recent media horizontal carousel strip (Screenshot 3)
            val sampleRecentImages = listOf(
                "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=200",
                "https://images.unsplash.com/photo-1541701494587-cb58502866ab?w=200",
                "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=200",
                "https://images.unsplash.com/photo-1518770660439-4636190af475?w=200",
                "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=200"
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // First tile: Camera tile matching Screenshot 3
                item {
                    Surface(
                        modifier = Modifier
                            .size(76.dp)
                            .clickable { onCameraClick() },
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Camera",
                                tint = Color(0xFF1E293B),
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Camera",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1E293B)
                            )
                        }
                    }
                }

                // Recent gallery photo tiles
                items(sampleRecentImages) { imgUrl ->
                    Surface(
                        modifier = Modifier
                            .size(76.dp)
                            .clickable { onRecentImageClick(imgUrl) },
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFE2E8F0)
                    ) {
                        AsyncImage(
                            model = imgUrl,
                            contentDescription = "Recent photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AttachmentActionItem(
    label: String,
    icon: ImageVector,
    bgColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = bgColor,
            modifier = Modifier.size(52.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF54656F),
            fontWeight = FontWeight.Normal
        )
    }
}

/**
 * WhatsApp Inline Emoji Keyboard with Tabs and Backspace.
 */
@Composable
fun WhatsAppEmojiPicker(
    onEmojiClick: (String) -> Unit,
    onBackspace: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(0) }

    val categories = listOf("Smileys", "Hands", "Hearts", "Objects")
    val smileys = listOf(
        "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇",
        "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😋", "😜", "😝",
        "🤑", "🤗", "🤔", "🤫", "🤭", "🤐", "🤨", "😐", "😑", "😶",
        "😏", "😒", "🙄", "😬", "🤥", "😔", "😪", "🤤", "😴", "😷",
        "🤒", "🤕", "🤢", "🤮", "🤧", "🥵", "🥶", "🥴", "😵", "🤯",
        "🤠", "🥳", "😎", "🤓", "🧐", "😕", "😟", "🙁", "😮", "🥺",
        "😦", "😧", "😨", "😰", "😥", "😢", "😭", "😱", "😖", "😡"
    )
    val hands = listOf(
        "👋", "🤚", "🖐️", "✋", "🖖", "👌", "🤌", "🤏", "✌️", "🤞",
        "🤟", "🤘", "🤙", "👈", "👉", "👆", "👇", "☝️", "👍", "👎",
        "✊", "👊", "🤛", "🤜", "👏", "🙌", "👐", "🤲", "🤝", "🙏"
    )
    val hearts = listOf(
        "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔",
        "❣️", "💕", "💞", "💓", "💗", "💖", "💘", "💝", "💟", "🔥",
        "✨", "⭐", "🌟", "💫", "💥", "💯", "🎉", "🎊", "🏆", "🥇"
    )
    val objects = listOf(
        "📱", "💻", "⌚", "📷", "📸", "📹", "📺", "📻", "🎙️", "🎧",
        "☕", "🍵", "🍕", "🍔", "🍟", "🎂", "🍰", "🍫", "🍩", "🚗",
        "🚀", "✈️", "🛵", "🚲", "⚽", "🏀", "🎮", "🎲", "🎯", "💡"
    )

    val currentEmojis = when (selectedCategory) {
        0 -> smileys
        1 -> hands
        2 -> hearts
        else -> objects
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        color = Color(0xFFF0F2F5)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Category selector tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.forEachIndexed { index, name ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (selectedCategory == index) Color(0xFFE2E8F0) else Color.Transparent,
                            modifier = Modifier.clickable { selectedCategory = index }
                        ) {
                            Text(
                                text = name,
                                fontSize = 12.sp,
                                fontWeight = if (selectedCategory == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedCategory == index) Color(0xFF00A884) else Color(0xFF54656F),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Backspace button
                IconButton(
                    onClick = onBackspace,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Backspace,
                        contentDescription = "Delete",
                        tint = Color(0xFF54656F),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Grid of emojis
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 42.dp),
                contentPadding = PaddingValues(6.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(currentEmojis) { emoji ->
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onEmojiClick(emoji) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 24.sp)
                    }
                }
            }
        }
    }
}

/**
 * Full-screen Recents Gallery Picker Modal matching Screenshot 5 (`Screenshot_20261009-220306.png`).
 */
@Composable
fun FullGalleryPickerModal(
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onImageSelected: (Uri) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val sampleImages = listOf(
            "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=500",
            "https://images.unsplash.com/photo-1541701494587-cb58502866ab?w=500",
            "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=500",
            "https://images.unsplash.com/photo-1518770660439-4636190af475?w=500",
            "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=500",
            "https://images.unsplash.com/photo-1511447333015-45b65e60f6d5?w=500",
            "https://images.unsplash.com/photo-1526778548025-fa2f459cd5c1?w=500",
            "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=500",
            "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=500"
        )

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            color = Color.White
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top header matching Screenshot 5
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF111B21)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { }
                    ) {
                        Text(
                            text = "Recents",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF111B21)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Dropdown",
                            tint = Color(0xFF111B21),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.Hd,
                            contentDescription = "HD",
                            tint = Color(0xFF111B21)
                        )
                    }
                }

                // Grid: First cell is Camera, rest are photos
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    item {
                        Surface(
                            modifier = Modifier
                                .height(120.dp)
                                .clickable { onCameraClick() },
                            color = Color(0xFFF1F5F9)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Camera",
                                    tint = Color(0xFF1E293B),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Camera",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF1E293B)
                                )
                            }
                        }
                    }

                    items(sampleImages) { imgUrl ->
                        Box(
                            modifier = Modifier
                                .height(120.dp)
                                .clickable { onImageSelected(Uri.parse(imgUrl)) }
                        ) {
                            AsyncImage(
                                model = imgUrl,
                                contentDescription = "Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dialog to create interactive WhatsApp-style poll.
 */
@Composable
fun CreatePollDialog(
    onDismiss: () -> Unit,
    onCreatePoll: (question: String, options: List<String>) -> Unit
) {
    var question by remember { mutableStateOf("") }
    var option1 by remember { mutableStateOf("") }
    var option2 by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Poll", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text("Question") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = option1,
                    onValueChange = { option1 = it },
                    label = { Text("Option 1") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = option2,
                    onValueChange = { option2 = it },
                    label = { Text("Option 2") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (question.isNotBlank() && option1.isNotBlank() && option2.isNotBlank()) {
                        onCreatePoll(question, listOf(option1, option2))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A884))
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * WhatsApp Chat Bubble:
 * Displays text, images, SMS indicator, timestamps, and double blue checkmarks.
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
            color = if (isMe) Color(0xFFD9FDD3) else Color.White
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp)
            ) {
                // If message has mediaUrl (image), display image preview
                val hasImage = !message.mediaUrl.isNullOrBlank() || message.mediaType == MediaType.IMAGE
                if (hasImage && !message.mediaUrl.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE2E8F0),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 220.dp)
                            .padding(bottom = 4.dp)
                    ) {
                        AsyncImage(
                            model = message.mediaUrl,
                            contentDescription = "Media preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

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

                if (message.text.isNotBlank()) {
                    Text(
                        text = message.text,
                        fontSize = 15.sp,
                        color = Color(0xFF111B21),
                        lineHeight = 20.sp
                    )
                }

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
                            tint = Color(0xFF53BDEB),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

private val chatDisplayDateFormat = SimpleDateFormat("d MMMM yyyy", Locale.US)
private fun getChatDisplayDate(timestamp: Long): String {
    val date = Date(timestamp)
    return synchronized(chatDisplayDateFormat) { chatDisplayDateFormat.format(date) }
}
