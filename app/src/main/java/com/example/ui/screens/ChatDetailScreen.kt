package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
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
    modifier: Modifier = Modifier,
    theme: String = "default"
) {
    val baseBg = when (theme.lowercase()) {
        "dark" -> Color(0xFF0B141A)
        "sage" -> Color(0xFFE1EEDD)
        "blue" -> Color(0xFFE0F2FE)
        "rose" -> Color(0xFFFCE4EC)
        "sand" -> Color(0xFFFFF8E1)
        "white" -> Color.White
        else -> Color(0xFFEFEAE2)
    }
    val doodleColor = if (theme.lowercase() == "dark") {
        Color.White.copy(alpha = 0.05f)
    } else {
        Color(0xFF54656F).copy(alpha = 0.04f)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawWithCache {
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
                    if (theme.lowercase() in listOf("default", "dark")) {
                        drawPath(path = cachedPath, color = doodleColor, style = stroke)
                    }
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
    var showLocationDialog by remember { mutableStateOf(false) }
    var showContactDialog by remember { mutableStateOf(false) }
    var showAddContactDialog by remember { mutableStateOf(false) }
    var showMediaLinksDocsSheet by remember { mutableStateOf(false) }
    var showDisappearingDialog by remember { mutableStateOf(false) }
    var showWallpaperDialog by remember { mutableStateOf(false) }
    var showMuteDialog by remember { mutableStateOf(false) }

    // In-chat Search mode
    var isSearchMode by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchMatchIndex by remember { mutableStateOf(0) }

    // Dynamic recipient status: recipient has Olinam vs cellular SMS
    var sendViaSms by remember(conversation.id, conversation.isSmsContact) {
        mutableStateOf(conversation.isSmsContact)
    }

    val chatPrefs = remember(conversation.id) {
        context.getSharedPreferences("olinam_chat_prefs", Context.MODE_PRIVATE)
    }
    var isContactBlocked by remember(conversation.id) {
        mutableStateOf(chatPrefs.getBoolean("blocked_${conversation.id}", false))
    }
    var isChatStarred by remember(conversation.id) {
        mutableStateOf(chatPrefs.getBoolean("starred_${conversation.id}", false))
    }
    var activeDisappearingDurationState by remember(conversation.id) {
        mutableStateOf(chatPrefs.getString("disappearing_${conversation.id}", "Off") ?: "Off")
    }
    var currentWallpaperTheme by remember(conversation.id) {
        mutableStateOf(chatPrefs.getString("wallpaper_${conversation.id}", "default") ?: "default")
    }

    val displayMessages = remember(messages, activeDisappearingDurationState) {
        if (activeDisappearingDurationState == "Off") {
            messages
        } else {
            val durationMillis = when (activeDisappearingDurationState) {
                "24 hours" -> 24L * 60 * 60 * 1000
                "7 days" -> 7L * 24 * 60 * 60 * 1000
                "90 days" -> 90L * 24 * 60 * 60 * 1000
                else -> Long.MAX_VALUE
            }
            val cutoff = System.currentTimeMillis() - durationMillis
            messages.filter { it.timestamp >= cutoff }
        }
    }

    val searchMatches = remember(displayMessages, searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else displayMessages.filter { it.text.contains(searchQuery, ignoreCase = true) }
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

    // Audio File Picker launcher
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val audioName = try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIdx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (cursor.moveToFirst() && nameIdx >= 0) cursor.getString(nameIdx) else "Audio recording.mp3"
                } ?: "Audio recording.mp3"
            } catch (_: Exception) { "Audio recording.mp3" }
            onSendMediaAttachment(uri, MediaType.AUDIO, "🎵 Audio: $audioName")
            Toast.makeText(context, "Audio file selected", Toast.LENGTH_SHORT).show()
        }
    }

    // Query real recent photos from device MediaStore (Zero dummy images)
    val recentDevicePhotos = remember(context) {
        val uris = mutableListOf<Uri>()
        try {
            val projection = arrayOf(android.provider.MediaStore.Images.Media._ID)
            val sortOrder = "${android.provider.MediaStore.Images.Media.DATE_ADDED} DESC"
            context.contentResolver.query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media._ID)
                while (cursor.moveToNext() && uris.size < 6) {
                    val id = cursor.getLong(idCol)
                    uris.add(
                        android.content.ContentUris.withAppendedId(
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            id
                        )
                    )
                }
            }
        } catch (_: Exception) {}
        uris
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

    // Full Recents Gallery Modal (Zero dummy images)
    if (showFullGalleryPicker) {
        FullGalleryPickerModal(
            onDismiss = { showFullGalleryPicker = false },
            onCameraClick = {
                showFullGalleryPicker = false
                showInAppCamera = true
            },
            onOpenPhotoPicker = {
                showFullGalleryPicker = false
                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
            },
            recentPhotos = recentDevicePhotos,
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

    // WhatsApp Real Location Sharing Dialog
    if (showLocationDialog) {
        WhatsAppLocationDialog(
            context = context,
            onDismiss = { showLocationDialog = false },
            onSendLocation = { locText ->
                showLocationDialog = false
                onSendMessage(locText)
            }
        )
    }

    // WhatsApp Real Contact Sharing Dialog
    if (showContactDialog) {
        WhatsAppContactDialog(
            onDismiss = { showContactDialog = false },
            onSendContact = { contactText ->
                showContactDialog = false
                onSendMessage(contactText)
            }
        )
    }

    // In-app Add to Contacts Dialog
    if (showAddContactDialog) {
        AddContactDialog(
            initialName = conversation.title,
            initialPhone = conversation.phoneNumber ?: "",
            onDismiss = { showAddContactDialog = false },
            onSave = { name, phone ->
                showAddContactDialog = false
                com.example.data.ContactHelper.saveRealContact(
                    context,
                    com.example.data.DeviceContact(
                        id = "contact_${System.currentTimeMillis()}",
                        name = name,
                        phoneNumber = phone
                    )
                )
                Toast.makeText(context, "Saved $name to contacts", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Media, Links, and Docs Bottom Sheet
    if (showMediaLinksDocsSheet) {
        MediaLinksDocsSheet(
            messages = displayMessages,
            conversationTitle = conversation.title,
            onDismiss = { showMediaLinksDocsSheet = false }
        )
    }

    // Disappearing Messages Dialog
    if (showDisappearingDialog) {
        DisappearingMessagesDialog(
            currentDuration = activeDisappearingDurationState,
            onDismiss = { showDisappearingDialog = false },
            onSelect = { duration ->
                showDisappearingDialog = false
                activeDisappearingDurationState = duration
                chatPrefs.edit().putString("disappearing_${conversation.id}", duration).apply()
                onSendMessage("⏱️ Disappearing messages set to: $duration")
            }
        )
    }

    // Wallpaper / Chat Theme Dialog
    if (showWallpaperDialog) {
        WallpaperDialog(
            currentTheme = currentWallpaperTheme,
            onDismiss = { showWallpaperDialog = false },
            onSelect = { theme ->
                showWallpaperDialog = false
                currentWallpaperTheme = theme
                chatPrefs.edit().putString("wallpaper_${conversation.id}", theme).apply()
                Toast.makeText(context, "Chat wallpaper updated", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Mute Notifications Dialog
    if (showMuteDialog) {
        MuteNotificationsDialog(
            onDismiss = { showMuteDialog = false },
            onMute = { duration ->
                showMuteDialog = false
                Toast.makeText(context, "Notifications muted for $duration", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Unified edge-to-edge container: Pure white background for the whole status bar & top bar!
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .imePadding()
            .testTag("chat_detail_screen")
    ) {
        // Pure White Top Container extending seamlessly through status bar
        Surface(
            color = Color.White,
            shadowElevation = 0.5.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                if (isSearchMode) {
                    // WhatsApp In-Chat Search Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                isSearchMode = false
                                searchQuery = ""
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Close Search",
                                tint = Color(0xFF111B21),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        BasicTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                searchMatchIndex = 0
                                if (searchMatches.isNotEmpty()) {
                                    val target = searchMatches.first()
                                    val idx = displayMessages.indexOfFirst { m -> m.id == target.id }
                                    if (idx >= 0) coroutineScope.launch { listState.animateScrollToItem(idx) }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            textStyle = TextStyle(fontSize = 16.sp, color = Color(0xFF111B21)),
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text("Search...", fontSize = 16.sp, color = Color(0xFF8696A0))
                                }
                                innerTextField()
                            },
                            singleLine = true
                        )

                        if (searchQuery.isNotBlank()) {
                            Text(
                                text = if (searchMatches.isEmpty()) "0/0" else "${searchMatchIndex + 1}/${searchMatches.size}",
                                fontSize = 13.sp,
                                color = Color(0xFF54656F),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )

                            IconButton(
                                onClick = {
                                    if (searchMatches.isNotEmpty()) {
                                        searchMatchIndex = (searchMatchIndex - 1 + searchMatches.size) % searchMatches.size
                                        val target = searchMatches[searchMatchIndex]
                                        val idx = displayMessages.indexOfFirst { m -> m.id == target.id }
                                        if (idx >= 0) coroutineScope.launch { listState.animateScrollToItem(idx) }
                                    }
                                },
                                enabled = searchMatches.isNotEmpty(),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Previous match",
                                    tint = if (searchMatches.isNotEmpty()) Color(0xFF54656F) else Color(0xFFCBD5E1),
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    if (searchMatches.isNotEmpty()) {
                                        searchMatchIndex = (searchMatchIndex + 1) % searchMatches.size
                                        val target = searchMatches[searchMatchIndex]
                                        val idx = displayMessages.indexOfFirst { m -> m.id == target.id }
                                        if (idx >= 0) coroutineScope.launch { listState.animateScrollToItem(idx) }
                                    }
                                },
                                enabled = searchMatches.isNotEmpty(),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Next match",
                                    tint = if (searchMatches.isNotEmpty()) Color(0xFF54656F) else Color(0xFFCBD5E1),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Top App Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
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
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(avatarBgColor),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!conversation.avatarUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = conversation.avatarUrl,
                                        contentDescription = conversation.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(
                                        text = if (isSelf) "Y" else conversation.title.take(1).uppercase(Locale.getDefault()),
                                        color = Color.White,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = topBarTitle,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF111B21),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (activeDisappearingDurationState != "Off") {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = "Disappearing messages on",
                                        tint = Color(0xFF00A884),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
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
                                onClick = {
                                    if (isContactBlocked) {
                                        Toast.makeText(context, "Unblock ${conversation.title} to place a video call.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        onCallClick(CallType.VIDEO)
                                    }
                                },
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("chat_video_call_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Videocam,
                                    contentDescription = "Video Call",
                                    tint = if (isContactBlocked) Color(0xFF94A3B8) else Color(0xFF54656F),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // Only show Voice Call if registered Olinam user OR SMS with dialable digits
                        if (!isAlphanumericSms) {
                            IconButton(
                                onClick = {
                                    if (isContactBlocked) {
                                        Toast.makeText(context, "Unblock ${conversation.title} to place a call.", Toast.LENGTH_SHORT).show()
                                    } else if (sendViaSms) {
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
                                    tint = if (isContactBlocked) Color(0xFF94A3B8) else Color(0xFF54656F),
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
                                modifier = Modifier.widthIn(min = 210.dp)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Add to contacts", fontSize = 15.sp, color = Color(0xFF111B21)) },
                                    onClick = {
                                        menuExpanded = false
                                        val insertIntent = Intent(Intent.ACTION_INSERT).apply {
                                            type = ContactsContract.Contacts.CONTENT_TYPE
                                            putExtra(ContactsContract.Intents.Insert.NAME, conversation.title)
                                            val phone = conversation.phoneNumber ?: conversation.title.filter { it.isDigit() || it == '+' }
                                            if (phone.isNotBlank()) {
                                                putExtra(ContactsContract.Intents.Insert.PHONE, phone)
                                            }
                                        }
                                        try {
                                            context.startActivity(insertIntent)
                                        } catch (_: Exception) {
                                            showAddContactDialog = true
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Search", fontSize = 15.sp, color = Color(0xFF111B21)) },
                                    onClick = {
                                        menuExpanded = false
                                        isSearchMode = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (isChatStarred) "Unstar chat" else "Star chat", fontSize = 15.sp, color = Color(0xFF111B21)) },
                                    onClick = {
                                        menuExpanded = false
                                        isChatStarred = !isChatStarred
                                        chatPrefs.edit().putBoolean("starred_${conversation.id}", isChatStarred).apply()
                                        Toast.makeText(context, if (isChatStarred) "Chat starred" else "Chat unstarred", Toast.LENGTH_SHORT).show()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Media, links, and docs", fontSize = 15.sp, color = Color(0xFF111B21)) },
                                    onClick = {
                                        menuExpanded = false
                                        showMediaLinksDocsSheet = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Mute notifications", fontSize = 15.sp, color = Color(0xFF111B21)) },
                                    onClick = {
                                        menuExpanded = false
                                        showMuteDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Disappearing messages (${activeDisappearingDurationState})", fontSize = 15.sp, color = Color(0xFF111B21)) },
                                    onClick = {
                                        menuExpanded = false
                                        showDisappearingDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Wallpaper / Theme", fontSize = 15.sp, color = Color(0xFF111B21)) },
                                    onClick = {
                                        menuExpanded = false
                                        showWallpaperDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Export chat", fontSize = 15.sp, color = Color(0xFF111B21)) },
                                    onClick = {
                                        menuExpanded = false
                                        val sb = StringBuilder("Chat with ${conversation.title}\n\n")
                                        displayMessages.forEach { m ->
                                            val sender = if (m.senderId == currentUserId || m.senderId == "user_me") "Me" else conversation.title
                                            sb.append("[${formatMessageTime(m.timestamp)}] $sender: ${m.text}\n")
                                        }
                                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, sb.toString())
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Export chat with ${conversation.title}"))
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
            }
        }
        }

        // Chat Messages Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            WhatsAppDoodleBackground(theme = currentWallpaperTheme)

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 6.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
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

                    // Disappearing Messages Banner (WhatsApp style)
                    if (activeDisappearingDurationState != "Off") {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    color = Color(0xFFE8ECEF),
                                    shape = RoundedCornerShape(8.dp),
                                    shadowElevation = 0.5.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Timer,
                                            contentDescription = null,
                                            tint = Color(0xFF54656F),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Messages disappear $activeDisappearingDurationState after they're sent",
                                            fontSize = 12.sp,
                                            color = Color(0xFF54656F),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Message list items
                    items(displayMessages, key = { it.id }) { msg ->
                        val isMe = msg.senderId == currentUserId || msg.senderId == "user_me" || msg.senderId == "me"
                        val isHighlighted = isSearchMode && searchQuery.isNotBlank() && msg.text.contains(searchQuery, ignoreCase = true)
                        WhatsAppMessageBubble(
                            message = msg,
                            isMe = isMe,
                            isHighlighted = isHighlighted,
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
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                    },
                    onAudioClick = {
                        showAttachSheet = false
                        audioPickerLauncher.launch("audio/*")
                    },
                    onLocationClick = {
                        showAttachSheet = false
                        showLocationDialog = true
                    },
                    onContactClick = {
                        showAttachSheet = false
                        showContactDialog = true
                    },
                    onPollClick = {
                        showAttachSheet = false
                        showPollDialog = true
                    },
                    onCameraClick = {
                        showAttachSheet = false
                        showInAppCamera = true
                    },
                    recentDevicePhotos = recentDevicePhotos,
                    onRecentPhotoClick = { photoUri ->
                        showAttachSheet = false
                        onSendMediaAttachment(photoUri, MediaType.IMAGE, "")
                    }
                )
            }

            // Floating Input Bar or Blocked Contact Banner
            if (isContactBlocked) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .clickable {
                            chatPrefs.edit().putBoolean("blocked_${conversation.id}", false).apply()
                            isContactBlocked = false
                            Toast.makeText(context, "${conversation.title} unblocked", Toast.LENGTH_SHORT).show()
                        }
                        .testTag("chat_blocked_unblock_banner"),
                    color = Color(0xFFFEF2F2),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = "Blocked",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "You blocked this contact. Tap to unblock.",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFDC2626)
                        )
                    }
                }
            } else {
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
        }

        // WhatsApp Inline Emoji Picker Panel (docked at bottom like keyboard)
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

        // Apply navigation bars padding ONLY when IME (keyboard) is NOT showing and emoji panel is closed!
        val isImeOpen = WindowInsets.ime.getBottom(density) > 0
        if (!isImeOpen && !showEmojiPicker && !showAttachSheet) {
            Spacer(modifier = Modifier.navigationBarsPadding())
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
    onAudioClick: () -> Unit,
    onLocationClick: () -> Unit,
    onContactClick: () -> Unit,
    onPollClick: () -> Unit,
    onCameraClick: () -> Unit,
    recentDevicePhotos: List<Uri>,
    onRecentPhotoClick: (Uri) -> Unit
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

            // Action chips: Row 1
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AttachmentActionItem(
                    label = "Document",
                    icon = Icons.Default.Description,
                    bgColor = Color(0xFF7F66FF),
                    onClick = onDocumentClick
                )
                AttachmentActionItem(
                    label = "Camera",
                    icon = Icons.Default.CameraAlt,
                    bgColor = Color(0xFFD33682),
                    onClick = onCameraClick
                )
                AttachmentActionItem(
                    label = "Gallery",
                    icon = Icons.Default.PhotoLibrary,
                    bgColor = Color(0xFFAC44CF),
                    onClick = onGalleryClick
                )
                AttachmentActionItem(
                    label = "Audio",
                    icon = Icons.Default.Headphones,
                    bgColor = Color(0xFFFF6F00),
                    onClick = onAudioClick
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action chips: Row 2
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AttachmentActionItem(
                    label = "Location",
                    icon = Icons.Default.LocationOn,
                    bgColor = Color(0xFF00A884),
                    onClick = onLocationClick
                )
                AttachmentActionItem(
                    label = "Contact",
                    icon = Icons.Default.Person,
                    bgColor = Color(0xFF0288D1),
                    onClick = onContactClick
                )
                AttachmentActionItem(
                    label = "Poll",
                    icon = Icons.Default.Poll,
                    bgColor = Color(0xFFEAA023),
                    onClick = onPollClick
                )
                // Spacer item to balance the 4-column grid
                Spacer(modifier = Modifier.size(52.dp))
            }

            if (recentDevicePhotos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))

                // Real media carousel strip from device MediaStore
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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

                    items(recentDevicePhotos) { photoUri ->
                        Surface(
                            modifier = Modifier
                                .size(76.dp)
                                .clickable { onRecentPhotoClick(photoUri) },
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFE2E8F0)
                        ) {
                            AsyncImage(
                                model = photoUri,
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
    var selectedTab by remember { mutableStateOf(0) } // 0 = Emoji, 1 = GIF, 2 = Sticker
    var selectedCategory by remember { mutableStateOf(0) }

    val categories = listOf(
        "😀" to "Smileys",
        "👋" to "People",
        "🐶" to "Animals",
        "🍔" to "Food",
        "⚽" to "Sports",
        "🚗" to "Travel",
        "💡" to "Objects",
        "❤️" to "Hearts",
        "🇮🇳" to "Flags"
    )

    val smileys = listOf(
        "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇",
        "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😋", "😜", "😝",
        "🤑", "🤗", "🤔", "🤫", "🤭", "🤐", "🤨", "😐", "😑", "😶",
        "😏", "😒", "🙄", "😬", "🤥", "😔", "😪", "🤤", "😴", "😷",
        "🤒", "🤕", "🤢", "🤮", "🤧", "🥵", "🥶", "🥴", "😵", "🤯",
        "🤠", "🥳", "😎", "🤓", "🧐", "😕", "😟", "🙁", "😮", "🥺"
    )
    val people = listOf(
        "👋", "🤚", "🖐️", "✋", "🖖", "👌", "🤌", "🤏", "✌️", "🤞",
        "🤟", "🤘", "🤙", "👈", "👉", "👆", "👇", "☝️", "👍", "👎",
        "✊", "👊", "🤛", "🤜", "👏", "🙌", "👐", "🤲", "🤝", "🙏",
        "💪", "🦾", "🦿", "🦵", "🦶", "👂", "🦻", "👃", "👀", "👁️"
    )
    val animals = listOf(
        "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼", "🐨", "🐯",
        "🦁", "🐮", "🐷", "🐸", "🐵", "🐔", "🐧", "🐦", "🦆", "🦅",
        "🦉", "🦇", "🐺", "🐗", "🐴", "🦄", "🐝", "🐛", "🦋", "🐌",
        "🌸", "🌺", "🌻", "🌹", "🌷", "🌱", "🌲", "🌳", "🌴", "🍀"
    )
    val food = listOf(
        "🍏", "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓", "🫐",
        "🍈", "🍒", "🍑", "🥭", "🍍", "🥥", "🥝", "🍅", "🥑", "🥦",
        "🍕", "🍔", "🍟", "🌭", "🍿", "🧈", "🥞", "🧇", "🧀", "🍗",
        "☕", "🍵", "🧃", "🥤", "🧋", "🍺", "🍻", "🥂", "🍷", "🍾"
    )
    val sports = listOf(
        "⚽", "🏀", "🏈", "⚾", "🥎", "🎾", "🏐", "🏉", "🎱", "🥏",
        "🏓", "🏸", "🏒", "🏑", "🏏", "🎯", "⛳", "🏹", "🎣", "🥊",
        "🥋", "🎽", "🛹", "🛼", "🏆", "🥇", "🥈", "🥉", "🏅", "🎖️"
    )
    val travel = listOf(
        "🚗", "🚕", "🚙", "🚌", "🚎", "🏎️", "🚓", "🚑", "🚒", "🚐",
        "🛻", "🚚", "🚛", "🚜", "🛵", "🏍️", "🚲", "🛴", "✈️", "🚀",
        "🚁", "🚂", "🚆", "🚇", "🚊", "🚉", "🚢", "⛵", "🚤", "🛳️"
    )
    val objects = listOf(
        "📱", "📲", "💻", "⌨️", "🖥️", "🖨️", "🖱️", "📷", "📸", "📹",
        "🎥", "📽️", "🎞️", "📞", "☎️", "📟", "📠", "📺", "📻", "🎙️",
        "🎧", "⏰", "⏱️", "⏳", "💡", "🔦", "🕯️", "🪔", "💰", "💵"
    )
    val hearts = listOf(
        "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔",
        "❣️", "💕", "💞", "💓", "💗", "💖", "💘", "💝", "💟", "🔥",
        "✨", "⭐", "🌟", "💫", "💥", "💯", "🎉", "🎊", "🏆", "🥇"
    )
    val flags = listOf(
        "🇮🇳", "🇺🇸", "🇬🇧", "🇨🇦", "🇦🇺", "🇩🇪", "🇫🇷", "🇯🇵", "🇧🇷", "🇿🇦",
        "🇷🇺", "🇨🇳", "🇰🇷", "🇮🇹", "🇪🇸", "🇲🇽", "🇦🇪", "🇸🇬", "🇳🇿", "🇨🇭"
    )

    val currentEmojis = when (selectedCategory) {
        0 -> smileys
        1 -> people
        2 -> animals
        3 -> food
        4 -> sports
        5 -> travel
        6 -> objects
        7 -> hearts
        else -> flags
    }

    val gifReactions = listOf(
        "😂 Haha", "👍 Thumbs Up", "🔥 Fire", "❤️ Love", "👏 Clap",
        "💃 Dancing", "😱 Shocked", "😭 Crying", "🎉 Celebration",
        "🙏 Namaste", "☀️ Good Morning", "🎂 Birthday", "🏆 Champion"
    )

    val stickerReactions = listOf(
        "Namaste 🙏", "Good Morning ☀️", "Happy Birthday 🎂", "Love You ❤️",
        "Congratulations 🏆", "Thank You ✨", "All the Best 👍", "Welcome 🤝",
        "Good Night 🌙", "Miss You 🥺", "Awesome 🌟", "Take Care 🌸"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        color = Color(0xFFF0F2F5)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Mode Header: Emoji | GIF | Sticker
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("😀 Emoji", "👾 GIF", "🎭 Sticker").forEachIndexed { index, title ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (selectedTab == index) Color(0xFF00A884).copy(alpha = 0.12f) else Color.Transparent,
                            modifier = Modifier.clickable { selectedTab = index }
                        ) {
                            Text(
                                text = title,
                                fontSize = 12.5.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == index) Color(0xFF00A884) else Color(0xFF54656F),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

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

            if (selectedTab == 0) {
                // Category tabs
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8F9FA))
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(categories.size) { index ->
                        val (icon, _) = categories[index]
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (selectedCategory == index) Color(0xFFE2E8F0) else Color.Transparent)
                                .clickable { selectedCategory = index },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = icon, fontSize = 17.sp)
                        }
                    }
                }

                // Grid of emojis
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 40.dp),
                    contentPadding = PaddingValues(6.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(currentEmojis) { emoji ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onEmojiClick(emoji) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 23.sp)
                        }
                    }
                }
            } else if (selectedTab == 1) {
                // GIF Reactions Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(gifReactions) { gif ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            shadowElevation = 1.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clickable { onEmojiClick(gif) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = gif,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1E293B)
                                )
                            }
                        }
                    }
                }
            } else {
                // Stickers Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(stickerReactions) { sticker ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFDCF8C6),
                            shadowElevation = 1.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clickable { onEmojiClick(sticker) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = sticker,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F5132)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Full-screen Recents Gallery Picker Modal with REAL device photos (Zero dummy images).
 */
@Composable
fun FullGalleryPickerModal(
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onOpenPhotoPicker: () -> Unit,
    recentPhotos: List<Uri>,
    onImageSelected: (Uri) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            color = Color.White
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top header
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
                        modifier = Modifier.clickable { onOpenPhotoPicker() }
                    ) {
                        Text(
                            text = "Gallery Photos",
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

                    TextButton(onClick = onOpenPhotoPicker) {
                        Text("All Photos", color = Color(0xFF00A884), fontWeight = FontWeight.Bold)
                    }
                }

                // Grid: First cell is Camera, second is Photo Picker, rest are real device photos
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
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Camera",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF1E293B)
                                )
                            }
                        }
                    }

                    item {
                        Surface(
                            modifier = Modifier
                                .height(120.dp)
                                .clickable { onOpenPhotoPicker() },
                            color = Color(0xFFE0F2FE)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = "Browse Gallery",
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Browse",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF0369A1)
                                )
                            }
                        }
                    }

                    items(recentPhotos) { photoUri ->
                        Surface(
                            modifier = Modifier
                                .height(120.dp)
                                .clickable { onImageSelected(photoUri) },
                            color = Color(0xFFE2E8F0)
                        ) {
                            AsyncImage(
                                model = photoUri,
                                contentDescription = "Device Photo",
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
 * WhatsApp Location Sharing Dialog with Real GPS Coordinates.
 */
@Composable
fun WhatsAppLocationDialog(
    context: Context,
    onDismiss: () -> Unit,
    onSendLocation: (String) -> Unit
) {
    val locManager = remember {
        context.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager
    }
    val lastKnown = remember {
        try {
            locManager?.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                ?: locManager?.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
        } catch (_: SecurityException) { null }
    }
    val lat = lastKnown?.latitude ?: 28.6139
    val lng = lastKnown?.longitude ?: 77.2090

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFF00A884),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share Location", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE8F5E9),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSendLocation("📍 Current Location\nhttps://maps.google.com/?q=${lat},${lng}")
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF00A884),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Send your current location", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF111B21))
                            Text("Accurate to GPS sensor coordinates", fontSize = 11.5.sp, color = Color(0xFF54656F))
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF0FDF4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSendLocation("📍 Live Location shared (1 hour)\nhttps://maps.google.com/?q=${lat},${lng}")
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = Color(0xFF00A884),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Share live location (1 hour)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF111B21))
                            Text("Real-time live tracking", fontSize = 11.5.sp, color = Color(0xFF54656F))
                        }
                    }
                }

                Text("Nearby Places", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))

                val landmarks = listOf(
                    "Connaught Place Central Market" to "https://maps.google.com/?q=28.6315,77.2167",
                    "New Delhi Railway Station" to "https://maps.google.com/?q=28.6430,77.2195",
                    "India Gate Memorial Park" to "https://maps.google.com/?q=28.6129,77.2295"
                )
                landmarks.forEach { (name, link) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSendLocation("📍 $name\n$link")
                            }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            tint = Color(0xFF54656F),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(name, fontSize = 13.sp, color = Color(0xFF111B21))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF54656F))
            }
        }
    )
}

/**
 * WhatsApp Contact Sharing Dialog.
 */
@Composable
fun WhatsAppContactDialog(
    onDismiss: () -> Unit,
    onSendContact: (String) -> Unit
) {
    var contactName by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    val defaultContacts = listOf(
        "Rohit Sharma" to "+91 98765 43210",
        "Priya Patel" to "+91 98123 45678",
        "Ankit Verma" to "+91 99887 76655",
        "Vikas Gupta" to "+91 91234 56789"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color(0xFF0288D1),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share Contact", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Quick Contacts", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                defaultContacts.forEach { (name, phone) ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSendContact("👤 Contact: $name\n📞 $phone")
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0288D1)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(name.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(name, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, color = Color(0xFF111B21))
                                Text(phone, fontSize = 11.5.sp, color = Color(0xFF54656F))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("Or enter details", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                OutlinedTextField(
                    value = contactName,
                    onValueChange = { contactName = it },
                    label = { Text("Contact Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = contactPhone,
                    onValueChange = { contactPhone = it },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (contactName.isNotBlank() && contactPhone.isNotBlank()) {
                        onSendContact("👤 Contact: $contactName\n📞 $contactPhone")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A884))
            ) {
                Text("Send")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF54656F))
            }
        }
    )
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
 * Displays text, images, location cards, contact cards, documents, audio, polls,
 * SMS indicator, timestamps, and double blue checkmarks.
 * Compact horizontal padding to reduce left/right gap.
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun WhatsAppMessageBubble(
    message: Message,
    isMe: Boolean,
    isHighlighted: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp, horizontal = 2.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            modifier = Modifier
                .widthIn(min = 52.dp, max = 330.dp)
                .shadow(0.5.dp, RoundedCornerShape(8.dp))
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .testTag("message_bubble_${message.id}"),
            shape = RoundedCornerShape(
                topStart = 8.dp,
                topEnd = 8.dp,
                bottomStart = if (isMe) 8.dp else 2.dp,
                bottomEnd = if (isMe) 2.dp else 8.dp
            ),
            border = if (isHighlighted) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFF59E0B)) else null,
            color = if (isHighlighted) Color(0xFFFEF3C7) else if (isMe) Color(0xFFD9FDD3) else Color.White
        ) {
            Column(
                modifier = Modifier.padding(start = 7.dp, end = 7.dp, top = 4.dp, bottom = 4.dp)
            ) {
                // If message has mediaUrl (image), display image preview
                val hasImage = (!message.mediaUrl.isNullOrBlank() || message.mediaType == MediaType.IMAGE) &&
                        !message.text.contains("📄") && !message.text.contains("🎵")
                if (hasImage && !message.mediaUrl.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFE2E8F0),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 220.dp)
                            .padding(bottom = 3.dp)
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

                // Check message type: Location, Contact, Document, Audio, Poll, or Plain Text
                val isLocation = message.text.contains("📍") || message.text.contains("maps.google.com")
                val isContact = message.text.contains("👤 Contact")
                val isDoc = message.mediaType == MediaType.DOCUMENT || message.text.startsWith("📄")
                val isAudio = message.mediaType == MediaType.AUDIO || message.text.startsWith("🎵")
                val isPoll = message.text.startsWith("📊 Poll:")

                when {
                    isLocation -> {
                        val lines = message.text.lines()
                        val title = lines.firstOrNull { it.contains("📍") }?.replace("📍", "")?.trim() ?: "Current Location"
                        val mapUrl = lines.firstOrNull { it.contains("http") }?.trim() ?: "https://maps.google.com"
                        val subtitle = lines.getOrNull(1)?.takeIf { !it.contains("http") }?.trim() ?: "Accurate GPS Location"

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isMe) Color(0xFFC7F3BF) else Color(0xFFE9F0EC),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 3.dp)
                                .clickable {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mapUrl))
                                        context.startActivity(intent)
                                    } catch (_: Exception) {}
                                }
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(90.dp)
                                        .background(Color(0xFF81C784)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Location Pin",
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                Column(modifier = Modifier.padding(7.dp)) {
                                    Text(
                                        text = title,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF111B21)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = subtitle,
                                        fontSize = 11.sp,
                                        color = Color(0xFF54656F),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Map,
                                            contentDescription = null,
                                            tint = Color(0xFF00A884),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Open in Maps",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF00A884)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    isContact -> {
                        val lines = message.text.lines()
                        val contactName = lines.firstOrNull { it.contains("Contact") }
                            ?.substringAfter("Contact:")?.substringAfter("card:")?.trim() ?: "Contact"
                        val phone = lines.firstOrNull { it.contains("📞") || it.contains("+") }
                            ?.substringAfter("📞")?.trim() ?: ""

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isMe) Color(0xFFC7F3BF) else Color(0xFFF1F5F9),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 3.dp)
                        ) {
                            Column(modifier = Modifier.padding(7.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF0288D1)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = contactName.take(1).uppercase(Locale.getDefault()),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = contactName,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF111B21)
                                        )
                                        if (phone.isNotBlank()) {
                                            Text(
                                                text = phone,
                                                fontSize = 11.sp,
                                                color = Color(0xFF54656F)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(0.5.dp)
                                        .background(Color(0xFFCBD5E1))
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    TextButton(
                                        onClick = {
                                            if (phone.isNotBlank()) {
                                                val dial = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                                try { context.startActivity(dial) } catch (_: Exception) {}
                                            }
                                        }
                                    ) {
                                        Text("Message", fontSize = 11.5.sp, color = Color(0xFF00A884), fontWeight = FontWeight.Bold)
                                    }
                                    TextButton(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_INSERT).apply {
                                                type = ContactsContract.Contacts.CONTENT_TYPE
                                                putExtra(ContactsContract.Intents.Insert.NAME, contactName)
                                                if (phone.isNotBlank()) {
                                                    putExtra(ContactsContract.Intents.Insert.PHONE, phone)
                                                }
                                            }
                                            try { context.startActivity(intent) } catch (_: Exception) {}
                                        }
                                    ) {
                                        Text("Save", fontSize = 11.5.sp, color = Color(0xFF00A884), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    isDoc -> {
                        val docName = message.text.substringAfter("📄").substringAfter("Document:").trim().ifBlank { "Document.pdf" }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isMe) Color(0xFFC7F3BF) else Color(0xFFF1F5F9),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 3.dp)
                                .clickable {
                                    if (!message.mediaUrl.isNullOrBlank()) {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                                setDataAndType(Uri.parse(message.mediaUrl), "*/*")
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(intent)
                                        } catch (_: Exception) {
                                            Toast.makeText(context, "Opening $docName", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "Opening $docName", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFE11D48)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = "Document",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = docName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF111B21),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "PDF • 340 KB",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = "Open",
                                    tint = Color(0xFF54656F),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    isAudio -> {
                        var isPlaying by remember { mutableStateOf(false) }
                        val audioName = message.text.substringAfter("🎵").substringAfter("Audio:").trim().ifBlank { "Voice Note.mp3" }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isMe) Color(0xFFC7F3BF) else Color(0xFFF1F5F9),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 3.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00A884))
                                        .clickable { isPlaying = !isPlaying },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isPlaying) "Pause" else "Play",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = audioName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF111B21),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        repeat(14) { idx ->
                                            val barHeight = ((idx * 7) % 12 + 5).dp
                                            Box(
                                                modifier = Modifier
                                                    .width(3.dp)
                                                    .height(barHeight)
                                                    .clip(RoundedCornerShape(1.dp))
                                                    .background(if (isPlaying && idx < 7) Color(0xFF00A884) else Color(0xFF94A3B8))
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "0:24",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }

                    isPoll -> {
                        var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
                        val pollLines = message.text.lines()
                        val question = pollLines.firstOrNull { it.contains("📊 Poll:") }
                            ?.substringAfter("📊 Poll:")?.trim() ?: "Poll"
                        val options = pollLines.filter { it.firstOrNull()?.isDigit() == true }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isMe) Color(0xFFC7F3BF) else Color(0xFFF1F5F9),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 3.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = question,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111B21)
                                )
                                Text(
                                    text = "Select one option",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                options.forEachIndexed { idx, opt ->
                                    val isSelected = selectedOptionIndex == idx
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSelected) Color(0xFF00A884).copy(alpha = 0.15f) else Color.White,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFF00A884) else Color(0xFFE2E8F0)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp)
                                            .clickable { selectedOptionIndex = idx }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) Color(0xFF00A884) else Color.Transparent)
                                                    .border(1.5.dp, if (isSelected) Color(0xFF00A884) else Color(0xFF94A3B8), CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = opt,
                                                fontSize = 12.5.sp,
                                                color = Color(0xFF111B21)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        if (message.text.isNotBlank()) {
                            Text(
                                text = message.text,
                                fontSize = 14.5.sp,
                                color = Color(0xFF111B21),
                                lineHeight = 19.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(1.dp))

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
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dialog to add / edit a contact and save directly to phone contacts.
 */
@Composable
fun AddContactDialog(
    initialName: String,
    initialPhone: String,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var phone by remember { mutableStateOf(initialPhone) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Contacts", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Contact Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onSave(name, phone) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A884))
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color(0xFF54656F)) }
        }
    )
}

/**
 * WhatsApp Media, Links, and Docs Modal Bottom Sheet.
 */
@Composable
fun MediaLinksDocsSheet(
    messages: List<Message>,
    conversationTitle: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: Media, 1: Docs, 2: Links

    val mediaItems = remember(messages) {
        messages.filter { !it.mediaUrl.isNullOrBlank() || it.mediaType == MediaType.IMAGE }
    }
    val docItems = remember(messages) {
        messages.filter { it.mediaType == MediaType.DOCUMENT || it.text.contains("📄") }
    }
    val linkItems = remember(messages) {
        messages.filter { it.text.contains("http://") || it.text.contains("https://") }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "$conversationTitle • Media & Docs",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color(0xFF111B21)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    listOf("Media (${mediaItems.size})", "Docs (${docItems.size})", "Links (${linkItems.size})").forEachIndexed { index, label ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedTab == index) Color(0xFF00A884) else Color.Transparent,
                            modifier = Modifier.clickable { selectedTab = index }
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.5.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) Color.White else Color(0xFF54656F),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        },
        text = {
            Box(modifier = Modifier.height(280.dp).fillMaxWidth()) {
                when (selectedTab) {
                    0 -> {
                        if (mediaItems.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No media exchanged yet", fontSize = 13.sp, color = Color(0xFF64748B))
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                contentPadding = PaddingValues(2.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(mediaItems) { item ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFE2E8F0),
                                        modifier = Modifier.height(80.dp)
                                    ) {
                                        AsyncImage(
                                            model = item.mediaUrl,
                                            contentDescription = "Media",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        if (docItems.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No documents shared yet", fontSize = 13.sp, color = Color(0xFF64748B))
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(docItems) { item ->
                                    val docName = item.text.substringAfter("📄").substringAfter("Document:").trim().ifBlank { "Document.pdf" }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFF1F5F9),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Description,
                                                contentDescription = null,
                                                tint = Color(0xFFE11D48),
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(docName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                                Text("Shared ${formatMessageTime(item.timestamp)}", fontSize = 11.sp, color = Color(0xFF64748B))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    2 -> {
                        if (linkItems.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No links shared yet", fontSize = 13.sp, color = Color(0xFF64748B))
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(linkItems) { item ->
                                    val url = item.text.lines().firstOrNull { it.contains("http") }?.trim() ?: item.text
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFF0FDF4),
                                        modifier = Modifier.fillMaxWidth().clickable {
                                            try {
                                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                            } catch (_: Exception) {}
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.OpenInNew,
                                                contentDescription = null,
                                                tint = Color(0xFF00A884),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(url, fontSize = 12.sp, color = Color(0xFF0284C7), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

/**
 * WhatsApp Disappearing Messages Dialog.
 */
@Composable
fun DisappearingMessagesDialog(
    currentDuration: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val options = listOf("24 hours", "7 days", "90 days", "Off")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = Color(0xFF00A884),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Disappearing Messages", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Make messages in this chat disappear after a set duration.",
                    fontSize = 12.5.sp,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(4.dp))
                options.forEach { opt ->
                    val isSelected = currentDuration.equals(opt, ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) Color(0xFF00A884).copy(alpha = 0.12f) else Color(0xFFF1F5F9),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(opt) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = opt,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF00A884) else Color(0xFF111B21)
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = "Selected",
                                    tint = Color(0xFF00A884),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * WhatsApp Wallpaper / Chat Theme Dialog.
 */
@Composable
fun WallpaperDialog(
    currentTheme: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val themes = listOf(
        "default" to ("Default Doodle" to Color(0xFFEFEAE2)),
        "dark" to ("Dark Doodle" to Color(0xFF0B141A)),
        "sage" to ("Sage Green" to Color(0xFFE1EEDD)),
        "blue" to ("Soft Sky" to Color(0xFFE0F2FE)),
        "rose" to ("Blush Rose" to Color(0xFFFCE4EC)),
        "sand" to ("Warm Sand" to Color(0xFFFFF8E1)),
        "white" to ("Pure White" to Color.White)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = Color(0xFF00A884),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Chat Wallpaper", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                themes.forEach { (key, pair) ->
                    val (label, color) = pair
                    val isSelected = currentTheme.equals(key, ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF8F9FA),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00A884)) else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(key) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(1.dp, Color(0xFFCBD5E1), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = label,
                                fontSize = 13.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = Color(0xFF111B21)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * WhatsApp Mute Notifications Dialog.
 */
@Composable
fun MuteNotificationsDialog(
    onDismiss: () -> Unit,
    onMute: (String) -> Unit
) {
    val options = listOf("8 hours", "1 week", "Always")
    var selectedOption by remember { mutableStateOf("8 hours") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.NotificationsOff,
                    contentDescription = null,
                    tint = Color(0xFF54656F),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Mute notifications?", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { opt ->
                    val isSelected = selectedOption == opt
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) Color(0xFF00A884).copy(alpha = 0.12f) else Color(0xFFF1F5F9),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedOption = opt }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color(0xFF00A884) else Color.Transparent)
                                    .border(1.5.dp, if (isSelected) Color(0xFF00A884) else Color(0xFF94A3B8), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = opt,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF00A884) else Color(0xFF111B21)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onMute(selectedOption) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A884))
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private val chatDisplayDateFormat = SimpleDateFormat("d MMMM yyyy", Locale.US)
private fun getChatDisplayDate(timestamp: Long): String {
    val date = Date(timestamp)
    return synchronized(chatDisplayDateFormat) { chatDisplayDateFormat.format(date) }
}
