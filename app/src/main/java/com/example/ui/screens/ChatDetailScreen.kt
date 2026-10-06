package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.model.CallType
import com.example.model.Conversation
import com.example.model.Message
import com.example.ui.components.formatMessageTime
import com.example.ui.theme.OlinamCheckBlue
import com.example.ui.theme.OlinamOnlineGreen
import com.example.ui.theme.OlinamPrimary
import com.example.ui.theme.OlinamPrimaryContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    conversation: Conversation,
    messages: List<Message>,
    currentUserId: String,
    safetyNumber: String,
    onBackClick: () -> Unit,
    onSendMessage: (String) -> Unit,
    onCallClick: (CallType) -> Unit
) {
    BackHandler {
        onBackClick()
    }

    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var sendViaSms by remember(conversation.id) { mutableStateOf(conversation.isSmsContact) }
    var showSafetyDialog by remember { mutableStateOf(false) }
    var selectedMessageForCrypto by remember { mutableStateOf<Message?>(null) }
    var showQuickAttachMenu by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .testTag("chat_detail_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { showSafetyDialog = true }
                            .padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(OlinamPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = conversation.title.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = conversation.title,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Encrypted",
                                    tint = OlinamPrimary,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            Text(
                                text = conversation.onlineStatus ?: "End-to-End Encrypted",
                                fontSize = 12.sp,
                                color = if (conversation.onlineStatus == "online") OlinamOnlineGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("chat_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onCallClick(CallType.VIDEO) },
                        modifier = Modifier.testTag("video_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Video call",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = { onCallClick(CallType.VOICE) },
                        modifier = Modifier.testTag("voice_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Voice call",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = { showSafetyDialog = true },
                        modifier = Modifier.testTag("chat_options_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Security verification",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Column {
                AnimatedVisibility(visible = showQuickAttachMenu) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            AttachOption("Document", "📄") {
                                onSendMessage("📄 Document: Project_Architecture_v2.pdf")
                                showQuickAttachMenu = false
                            }
                            AttachOption("Camera", "📸") {
                                onSendMessage("📸 Photo sent from camera")
                                showQuickAttachMenu = false
                            }
                            AttachOption("Gallery", "🖼️") {
                                onSendMessage("🖼️ Sent image from gallery")
                                showQuickAttachMenu = false
                            }
                            AttachOption("Location", "📍") {
                                onSendMessage("📍 Current location shared")
                                showQuickAttachMenu = false
                            }
                        }
                    }
                }

                // Chat input bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { showQuickAttachMenu = !showQuickAttachMenu },
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("attach_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = "Attach file",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Message input box with SMS / E2EE mode
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // SMS/E2EE Toggle Chip
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (sendViaSms) Color(0xFFFEF3C7) else OlinamPrimaryContainer,
                                    modifier = Modifier
                                        .clickable { sendViaSms = !sendViaSms }
                                        .testTag("sms_mode_toggle_button")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (sendViaSms) Icons.Default.Sms else Icons.Default.Lock,
                                            contentDescription = if (sendViaSms) "SMS" else "E2EE",
                                            tint = if (sendViaSms) Color(0xFFB45309) else Color(0xFF0160E3),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = if (sendViaSms) "SMS" else "E2EE",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (sendViaSms) Color(0xFFB45309) else Color(0xFF0160E3)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Box(modifier = Modifier.weight(1f)) {
                                    if (inputText.isEmpty()) {
                                        Text(
                                            text = if (sendViaSms) "SMS/MMS to non-app user..." else "Message",
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                    BasicTextField(
                                        value = inputText,
                                        onValueChange = { inputText = it },
                                        textStyle = TextStyle(
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        ),
                                        cursorBrush = SolidColor(Color(0xFF0160E3)),
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                        keyboardActions = KeyboardActions(
                                            onSend = {
                                                if (inputText.isNotBlank()) {
                                                    if (sendViaSms) {
                                                        val fullSmsBody = "$inputText\n\n— Powered by Olinam\nDownload app: https://olinam.app/download"
                                                        val rawPhone = conversation.phoneNumber ?: conversation.title.filter { it.isDigit() || it == '+' }
                                                        val targetUri = if (rawPhone.isNotEmpty()) "smsto:$rawPhone" else "smsto:"
                                                        val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse(targetUri)).apply {
                                                            putExtra("sms_body", fullSmsBody)
                                                        }
                                                        try {
                                                            context.startActivity(smsIntent)
                                                        } catch (_: Exception) {}
                                                        onSendMessage("💬 [SMS] $inputText\n\n— Powered by Olinam (https://olinam.app/download)")
                                                    } else {
                                                        onSendMessage(inputText)
                                                    }
                                                    inputText = ""
                                                }
                                            }
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("chat_message_input")
                                    )
                                }

                                IconButton(
                                    onClick = { inputText += " 😊" },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SentimentSatisfiedAlt,
                                        contentDescription = "Emoji",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Send / Mic FAB with royal blue #0160E3
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0160E3)) // Royal Blue #0160E3
                                .clickable {
                                    if (inputText.isNotBlank()) {
                                        if (sendViaSms) {
                                            val fullSmsBody = "$inputText\n\n— Powered by Olinam\nDownload app: https://olinam.app/download"
                                            val rawPhone = conversation.phoneNumber ?: conversation.title.filter { it.isDigit() || it == '+' }
                                            val targetUri = if (rawPhone.isNotEmpty()) "smsto:$rawPhone" else "smsto:"
                                            val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse(targetUri)).apply {
                                                putExtra("sms_body", fullSmsBody)
                                            }
                                            try {
                                                context.startActivity(smsIntent)
                                            } catch (_: Exception) {}
                                            onSendMessage("💬 [SMS] $inputText\n\n— Powered by Olinam (https://olinam.app/download)")
                                        } else {
                                            onSendMessage(inputText)
                                        }
                                        inputText = ""
                                    } else {
                                        onSendMessage("🎙️ Voice note (0:04)")
                                    }
                                }
                                .testTag("send_message_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (inputText.isNotBlank()) Icons.AutoMirrored.Filled.Send else Icons.Default.Mic,
                                contentDescription = if (inputText.isNotBlank()) "Send" else "Voice Note",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // E2EE Notice Banner or SMS Notice Banner
            if (conversation.isSmsContact) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEF3C7)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sms,
                            contentDescription = "SMS Contact",
                            tint = Color(0xFFB45309),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Non-App Contact: Messages are delivered via SMS with Olinam download link so they can reply. Once they install Olinam, this chat upgrades to End-to-End Encryption.",
                            fontSize = 11.5.sp,
                            color = Color(0xFF92400E),
                            lineHeight = 15.sp,
                            textAlign = TextAlign.Start
                        )
                    }
                }
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .clickable { showSafetyDialog = true }
                        .testTag("e2ee_security_banner"),
                    shape = RoundedCornerShape(12.dp),
                    color = OlinamPrimaryContainer.copy(alpha = 0.65f)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Encrypted lock",
                            tint = OlinamPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Messages and calls are end-to-end encrypted. No one outside of this chat, not even Olinam or Firebase, can read or listen to them. Tap to verify.",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 15.sp,
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }

            // Messages LazyColumn
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    val isMe = msg.senderId == currentUserId
                    MessageBubble(
                        message = msg,
                        isMe = isMe,
                        onCryptoDetailsClick = { selectedMessageForCrypto = msg }
                    )
                }
            }
        }
    }

    // Safety Code Verification Dialog (WhatsApp style)
    if (showSafetyDialog) {
        AlertDialog(
            onDismissRequest = { showSafetyDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = OlinamPrimary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Verify Security Code",
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "To verify that end-to-end encryption is active with ${conversation.title}, compare this 60-digit safety code on both devices or scan QR code.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = "Security QR",
                                tint = OlinamPrimary,
                                modifier = Modifier.size(72.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = safetyNumber,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = OlinamPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Cipher: AES-256-GCM / PBKDF2-HMAC",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = OlinamPrimary
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSafetyDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = OlinamPrimary)
                ) {
                    Text("Verified")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSafetyDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Cryptographic Details Inspector Modal
    selectedMessageForCrypto?.let { msg ->
        AlertDialog(
            onDismissRequest = { selectedMessageForCrypto = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = OlinamPrimary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Encrypted Payload Inspection",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "This message is encrypted end-to-end on device before sending over Firebase Firestore.",
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Plaintext:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = msg.text,
                            modifier = Modifier.padding(8.dp),
                            fontSize = 13.sp
                        )
                    }

                    Text(
                        text = "Stored Firestore Ciphertext (Base64):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = msg.encryptedPayload?.ciphertext ?: "[Encrypted AES-256 String]",
                            modifier = Modifier.padding(8.dp),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = "Algorithm: ${msg.encryptedPayload?.algorithm ?: "AES-256-GCM"}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OlinamPrimary
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

@Composable
fun MessageBubble(
    message: Message,
    isMe: Boolean,
    onCryptoDetailsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clickable(onClick = onCryptoDetailsClick)
                .testTag("message_bubble_${message.id}"),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMe) 16.dp else 4.dp,
                bottomEnd = if (isMe) 4.dp else 16.dp
            ),
            color = if (isMe) OlinamPrimary else MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                if (message.text.contains("[SMS") || message.text.contains("Powered by Olinam")) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sms,
                            contentDescription = "SMS",
                            tint = if (isMe) Color(0xFFFEF3C7) else Color(0xFFB45309),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "SMS • Powered by Olinam",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isMe) Color(0xFFFEF3C7) else Color(0xFFB45309)
                        )
                    }
                }

                if (!isMe && message.senderName.isNotEmpty()) {
                    Text(
                        text = message.senderName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = OlinamPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                Text(
                    text = message.text,
                    fontSize = 15.sp,
                    color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (message.isEncrypted) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Encrypted",
                            tint = if (isMe) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                    }

                    Text(
                        text = formatMessageTime(message.timestamp),
                        fontSize = 11.sp,
                        color = if (isMe) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )

                    if (isMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Read",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AttachOption(
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
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.size(50.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = emoji, fontSize = 24.sp)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
