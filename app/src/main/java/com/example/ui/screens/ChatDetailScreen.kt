package com.example.ui.screens

import android.Manifest
import com.example.model.ChatLabel
import com.example.ui.components.WhatsAppListColors
import com.example.ui.components.parseHexColorSafely
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
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
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Link
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
import androidx.compose.material.icons.filled.PersonAdd
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
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
 * Authentic WhatsApp doodle wallpaper background with warm texture and classic doodle drawings.
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
        else -> Color(0xFFEFEAE2) // Signature authentic WhatsApp wallpaper beige
    }
    val doodleColor = if (theme.lowercase() == "dark") {
        Color.White.copy(alpha = 0.045f)
    } else {
        Color(0xFF000000).copy(alpha = 0.055f)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawWithCache {
                val stroke = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.3f)
                val cachedPath = Path()
                val stepX = 84f
                val stepY = 84f
                var y = 20f
                var row = 0
                while (y < size.height) {
                    var x = if (row % 2 == 0) 24f else 66f
                    while (x < size.width) {
                        val iconType = ((row * 3) + (x / stepX).toInt()) % 10
                        when (iconType) {
                            0 -> {
                                // Chat speech bubble with tail
                                cachedPath.moveTo(x - 9f, y - 7f)
                                cachedPath.lineTo(x + 9f, y - 7f)
                                cachedPath.lineTo(x + 9f, y + 4f)
                                cachedPath.lineTo(x - 3f, y + 4f)
                                cachedPath.lineTo(x - 7f, y + 8f)
                                cachedPath.lineTo(x - 7f, y + 4f)
                                cachedPath.lineTo(x - 9f, y + 4f)
                                cachedPath.close()
                            }
                            1 -> {
                                // Coffee cup / mug with handle & steam
                                cachedPath.moveTo(x - 7f, y - 4f)
                                cachedPath.lineTo(x + 5f, y - 4f)
                                cachedPath.lineTo(x + 4f, y + 6f)
                                cachedPath.lineTo(x - 6f, y + 6f)
                                cachedPath.close()
                                // Handle
                                cachedPath.moveTo(x + 5f, y - 2f)
                                cachedPath.lineTo(x + 8f, y)
                                cachedPath.lineTo(x + 4f, y + 4f)
                                // Steam
                                cachedPath.moveTo(x - 2f, y - 6f)
                                cachedPath.lineTo(x - 2f, y - 9f)
                                cachedPath.moveTo(x + 2f, y - 6f)
                                cachedPath.lineTo(x + 2f, y - 9f)
                            }
                            2 -> {
                                // Spectacles / eyeglasses
                                cachedPath.addOval(androidx.compose.ui.geometry.Rect(x - 9f, y - 4f, x - 1f, y + 4f))
                                cachedPath.addOval(androidx.compose.ui.geometry.Rect(x + 1f, y - 4f, x + 9f, y + 4f))
                                // Bridge
                                cachedPath.moveTo(x - 1f, y - 1f)
                                cachedPath.lineTo(x + 1f, y - 1f)
                            }
                            3 -> {
                                // Headphones
                                cachedPath.moveTo(x - 7f, y + 4f)
                                cachedPath.lineTo(x - 7f, y - 1f)
                                cachedPath.cubicTo(x - 7f, y - 8f, x + 7f, y - 8f, x + 7f, y - 1f)
                                cachedPath.lineTo(x + 7f, y + 4f)
                                // Ear pads
                                cachedPath.addRect(androidx.compose.ui.geometry.Rect(x - 9f, y + 1f, x - 6f, y + 7f))
                                cachedPath.addRect(androidx.compose.ui.geometry.Rect(x + 6f, y + 1f, x + 9f, y + 7f))
                            }
                            4 -> {
                                // Musical note
                                cachedPath.moveTo(x - 4f, y + 5f)
                                cachedPath.addOval(androidx.compose.ui.geometry.Rect(x - 6f, y + 2f, x - 1f, y + 7f))
                                cachedPath.moveTo(x - 1f, y + 3f)
                                cachedPath.lineTo(x - 1f, y - 6f)
                                cachedPath.lineTo(x + 6f, y - 4f)
                                cachedPath.lineTo(x + 6f, y + 4f)
                                cachedPath.addOval(androidx.compose.ui.geometry.Rect(x + 1f, y + 1f, x + 6f, y + 6f))
                            }
                            5 -> {
                                // Heart
                                cachedPath.moveTo(x, y + 6f)
                                cachedPath.cubicTo(x - 8f, y + 1f, x - 8f, y - 6f, x, y - 2f)
                                cachedPath.cubicTo(x + 8f, y - 6f, x + 8f, y + 1f, x, y + 6f)
                            }
                            6 -> {
                                // Clock / Watch
                                cachedPath.addOval(androidx.compose.ui.geometry.Rect(x - 7f, y - 7f, x + 7f, y + 7f))
                                cachedPath.moveTo(x, y - 4f)
                                cachedPath.lineTo(x, y)
                                cachedPath.lineTo(x + 4f, y)
                            }
                            7 -> {
                                // Camera
                                cachedPath.moveTo(x - 7f, y - 4f)
                                cachedPath.lineTo(x - 3f, y - 4f)
                                cachedPath.lineTo(x - 1f, y - 7f)
                                cachedPath.lineTo(x + 3f, y - 7f)
                                cachedPath.lineTo(x + 5f, y - 4f)
                                cachedPath.lineTo(x + 7f, y - 4f)
                                cachedPath.lineTo(x + 7f, y + 6f)
                                cachedPath.lineTo(x - 7f, y + 6f)
                                cachedPath.close()
                                cachedPath.addOval(androidx.compose.ui.geometry.Rect(x - 3f, y - 2f, x + 3f, y + 4f))
                            }
                            8 -> {
                                // Bicycle
                                cachedPath.addOval(androidx.compose.ui.geometry.Rect(x - 9f, y + 1f, x - 3f, y + 7f))
                                cachedPath.addOval(androidx.compose.ui.geometry.Rect(x + 3f, y + 1f, x + 9f, y + 7f))
                                cachedPath.moveTo(x - 6f, y + 4f)
                                cachedPath.lineTo(x, y + 4f)
                                cachedPath.lineTo(x + 6f, y + 4f)
                                cachedPath.moveTo(x, y + 4f)
                                cachedPath.lineTo(x - 2f, y - 3f)
                                cachedPath.lineTo(x + 3f, y - 3f)
                            }
                            9 -> {
                                // Star
                                cachedPath.moveTo(x, y - 7f)
                                cachedPath.lineTo(x + 2f, y - 2f)
                                cachedPath.lineTo(x + 7f, y - 2f)
                                cachedPath.lineTo(x + 3f, y + 1f)
                                cachedPath.lineTo(x + 5f, y + 6f)
                                cachedPath.lineTo(x, y + 3f)
                                cachedPath.lineTo(x - 5f, y + 6f)
                                cachedPath.lineTo(x - 3f, y + 1f)
                                cachedPath.lineTo(x - 7f, y - 2f)
                                cachedPath.lineTo(x - 2f, y - 2f)
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
                    if (theme.lowercase() in listOf("default", "dark", "sand", "sage")) {
                        drawPath(path = cachedPath, color = doodleColor, style = stroke)
                    }
                }
            }
    )
}

/**
 * WhatsApp Business Profile Header Card (Matching user's screenshot exactly!)
 * Displays business avatar, verified blue checkmark, origin/category text, and [Block] + [Profile] buttons.
 */
@Composable
fun WhatsAppBusinessProfileCard(
    title: String,
    phoneNumber: String?,
    countryOrigin: String? = "Phone number from United Kingdom",
    businessCategory: String? = "Business account • Joined in June, 2023",
    onBlockClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 0.5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Circular Business / Sender Avatar
            val senderInitial = remember(title) {
                title.trim().take(1).uppercase(Locale.getDefault()).ifBlank { "B" }
            }
            val businessColor = remember(title) {
                val palette = listOf(
                    Color(0xFF0160E3), // Royal Blue
                    Color(0xFF059669), // Emerald
                    Color(0xFF7C3AED), // Violet
                    Color(0xFFD97706), // Amber
                    Color(0xFFDC2626), // Crimson
                    Color(0xFF0284C7)  // Ocean Blue
                )
                palette[Math.abs(title.hashCode()) % palette.size]
            }

            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .background(businessColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = senderInitial,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 38.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title with Verified Blue Checkmark
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111B21)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Verified business",
                    tint = Color(0xFF0084FF),
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle
            val subtitle = buildString {
                if (!countryOrigin.isNullOrBlank()) append(countryOrigin)
                if (!countryOrigin.isNullOrBlank() && !businessCategory.isNullOrBlank()) append(" • ")
                if (!businessCategory.isNullOrBlank()) append(businessCategory)
            }
            Text(
                text = subtitle,
                fontSize = 13.5.sp,
                color = Color(0xFF54656F),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Two action buttons side-by-side: [ 🚫 Block ] and [ Profile ]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Block button
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clickable { onBlockClick() },
                    shape = RoundedCornerShape(22.dp),
                    color = Color(0xFFF0F2F5)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = "Block",
                            tint = Color(0xFFEA0038),
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Block",
                            color = Color(0xFFEA0038),
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Profile button
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clickable { onProfileClick() },
                    shape = RoundedCornerShape(22.dp),
                    color = Color(0xFFF0F2F5)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Profile",
                            color = Color(0xFF111B21),
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatDetailScreen(
    conversation: Conversation,
    messages: List<Message>,
    currentUserId: String,
    safetyNumber: String,
    livePresenceStatus: String? = null,
    canSendReply: Boolean = true,
    onBackClick: () -> Unit,
    onSendMessage: (String) -> Unit = {},
    onSendReplyMessage: (text: String, replyToId: String?, replyToSender: String?, replyToText: String?) -> Unit = { text, _, _, _ -> onSendMessage(text) },
    onSendMediaAttachment: (uri: Uri, mediaType: MediaType, caption: String) -> Unit = { _, _, _ -> },
    onSendDirectSms: (String) -> Boolean = { false },
    onCallClick: (CallType) -> Unit = {},
    onVerifyAppStatus: () -> Unit = {},
    onLoadMessages: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onDeleteMessage: (String) -> Unit = {},
    onClearChat: () -> Unit = {},
    onDeleteChat: () -> Unit = {},
    onTypingChanged: (Boolean) -> Unit = {},
    onUpdateGroupDescription: (String) -> Unit = {},
    onAddGroupMember: (String) -> Unit = {},
    labels: List<ChatLabel> = emptyList(),
    onSaveConversationLabels: (List<String>) -> Unit = {},
    onCreateLabel: (String, String) -> Unit = { _, _ -> },
    onToggleReaction: (messageId: String, emoji: String) -> Unit = { _, _ -> }
) {
    var showGroupLinkScreen by remember { mutableStateOf(false) }
    var showEditGroupDescDialog by remember { mutableStateOf(false) }
    var editGroupDescText by remember(conversation.groupDescription) { mutableStateOf(conversation.groupDescription ?: "") }
    var showAddGroupMemberDialog by remember { mutableStateOf(false) }
    var newMemberPhoneText by remember { mutableStateOf("") }

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

    BackHandler {
        if (showLocationDialog) {
            showLocationDialog = false
        } else if (showAttachSheet) {
            showAttachSheet = false
        } else if (showEmojiPicker) {
            showEmojiPicker = false
        } else if (showGroupLinkScreen) {
            showGroupLinkScreen = false
        } else {
            onBackClick()
        }
    }

    if (showLocationDialog) {
        SendLocationScreen(
            onBackClick = { showLocationDialog = false },
            onSendLocation = { locText ->
                showLocationDialog = false
                onSendMessage(locText)
            }
        )
        return
    }

    if (showGroupLinkScreen) {
        GroupLinkScreen(
            conversation = conversation,
            onBackClick = { showGroupLinkScreen = false }
        )
        return
    }

    val context = LocalContext.current
    val privacyPrefs = remember { com.example.ui.screens.privacy.PrivacyPreferences(context) }
    val privacyState by privacyPrefs.state.collectAsState()
    val isReadReceiptsEnabled = privacyState.readReceiptsEnabled
    val density = LocalDensity.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    var inputText by remember { mutableStateOf("") }
    var replyingToMessage by remember { mutableStateOf<Message?>(null) }
    var pendingSmsText by remember { mutableStateOf("") }
    var menuExpanded by remember { mutableStateOf(false) }
    var showMoreSubMenu by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showBlockConfirmDialog by remember { mutableStateOf(false) }
    var reportBlockAndDelete by remember { mutableStateOf(true) }
    var clearDeleteMedia by remember { mutableStateOf(false) }
    var deleteChatMedia by remember { mutableStateOf(false) }
    var callMenuExpanded by remember { mutableStateOf(false) }
    var showSafetyDialog by remember { mutableStateOf(false) }
    var selectedMessageForCrypto by remember { mutableStateOf<Message?>(null) }
    var messageForAction by remember { mutableStateOf<Message?>(null) }
    var showDeleteMessageConfirm by remember { mutableStateOf<Message?>(null) }
    var showClearChatConfirm by remember { mutableStateOf(false) }
    var showDeleteChatConfirm by remember { mutableStateOf(false) }
    var showDisappearingDialog by remember { mutableStateOf(false) }
    var showWallpaperDialog by remember { mutableStateOf(false) }
    var showMuteDialog by remember { mutableStateOf(false) }
    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }
    var showCallTypeBottomSheet by remember { mutableStateOf(false) }
    var showListsBottomSheet by remember { mutableStateOf(false) }

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
    var isOjAiAutoEnabled by remember(conversation.id) {
        mutableStateOf(chatPrefs.getBoolean("oj_ai_auto_${conversation.id}", false))
    }
    var isChatStarred by remember(conversation.id) {
        mutableStateOf(chatPrefs.getBoolean("starred_${conversation.id}", false))
    }
    var isChatMuted by remember(conversation.id) {
        mutableStateOf(chatPrefs.getBoolean("muted_${conversation.id}", false))
    }
    val isCurrentUserAdmin = remember(conversation, currentUserId) {
        if (!conversation.isGroup) false
        else {
            val adminSet = conversation.groupAdminIds.toSet()
            adminSet.contains(currentUserId) ||
            adminSet.contains("user_me") ||
            adminSet.contains("me") ||
            conversation.groupCreatedBy == currentUserId ||
            conversation.groupCreatedBy == "You" ||
            adminSet.isEmpty()
        }
    }
    var activeDisappearingDurationState by remember(conversation.id) {
        mutableStateOf(chatPrefs.getString("disappearing_${conversation.id}", "Off") ?: "Off")
    }
    val chatSettingsPrefs = remember { com.example.ui.screens.chats.ChatSettingsPreferences(context) }
    val chatSettingsState by chatSettingsPrefs.state.collectAsState()
    var showVoiceConfirmDialog by remember { mutableStateOf(false) }

    var currentWallpaperTheme by remember(conversation.id, chatSettingsState.chatWallpaper) {
        val convWall = chatPrefs.getString("wallpaper_${conversation.id}", null)
        mutableStateOf(convWall ?: chatSettingsState.chatWallpaper.ifBlank { "default" })
    }

    // Google Messages Shortcode / Business Read-only states
    var enableManualReplyTest by remember { mutableStateOf(false) }
    var showShortCodeInfoDialog by remember { mutableStateOf(false) }
    var showSecurityActionDialog by remember { mutableStateOf(false) }

    val cleanPhone = remember(conversation.phoneNumber, conversation.title) {
        (conversation.phoneNumber ?: conversation.title).trim()
    }
    val isShortCodeOrAlphanumeric = remember(conversation, canSendReply, cleanPhone) {
        if (!conversation.isSmsContact && !conversation.id.startsWith("sms_")) {
            false
        } else if (conversation.title.contains("Message yourself", ignoreCase = true)) {
            false
        } else {
            conversation.isReadOnlyBusiness ||
                conversation.isShortCode ||
                !canSendReply
        }
    }

    // Scan for detected OTP code in messages
    val detectedOtpCode = remember(messages) {
        val otpRegex = Regex("""\b\d{4,8}\b""")
        messages.lastOrNull { it.isOtp || it.text.contains("two-factor", ignoreCase = true) || it.text.contains("verification", ignoreCase = true) || it.text.contains("OTP", ignoreCase = true) }
            ?.let { m ->
                m.otpCode ?: otpRegex.find(m.text)?.value
            }
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
                while (cursor.moveToNext() && uris.size < 32) {
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
            if (chatSettingsState.voiceMessageConfirmation) {
                showVoiceConfirmDialog = true
            } else {
                onSendMessage("🎙️ Voice message (0:04)")
            }
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
            val targetReply = replyingToMessage
            val replyId = targetReply?.id
            val replySender = targetReply?.let {
                if (it.senderId == currentUserId || it.senderId == "user_me" || it.senderId == "me") "You" else (it.senderName.ifBlank { conversation.title })
            }
            val replySnippet = targetReply?.text
            onSendReplyMessage(textToSend, replyId, replySender, replySnippet)
            replyingToMessage = null
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


    // Fullscreen Image Viewer Dialog
    if (fullScreenImageUrl != null) {
        Dialog(
            onDismissRequest = { fullScreenImageUrl = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = fullScreenImageUrl,
                    contentDescription = "Full screen media",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentScale = ContentScale.Fit
                )
                IconButton(
                    onClick = { fullScreenImageUrl = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 40.dp, end = 16.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }
        }
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

    // Select Call Type Bottom Sheet (Voice / Video call as in reference image)
    if (showCallTypeBottomSheet) {
        SelectCallTypeBottomSheet(
            onDismissRequest = { showCallTypeBottomSheet = false },
            onVoiceCallClick = {
                onCallClick(CallType.VOICE)
            },
            onVideoCallClick = {
                onCallClick(CallType.VIDEO)
            }
        )
    }

    // Chat Lists Bottom Sheet (Manage custom lists for this chat as in reference image)
    if (showListsBottomSheet) {
        ChatListsBottomSheet(
            conversation = conversation,
            labels = labels,
            onDismissRequest = { showListsBottomSheet = false },
            onCreateNewLabel = { name, colorHex ->
                onCreateLabel(name, colorHex)
            },
            onSaveLists = { newLabelIds ->
                onSaveConversationLabels(newLabelIds)
                val updatedConv = conversation.copy(labelIds = newLabelIds)
                try {
                    val db = com.example.data.LocalChatDatabase.getInstance(context)
                    db.saveConversation(updatedConv)
                } catch (_: Exception) {}
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
                isChatMuted = true
                chatPrefs.edit()
                    .putBoolean("muted_${conversation.id}", true)
                    .putString("mute_duration_${conversation.id}", duration)
                    .apply()
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
                                    Color(0xFF0160E3), // Olinam Royal Blue
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
                                        tint = Color(0xFF0160E3),
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
                                    isOtherTyping -> Color(0xFF0160E3) // Royal Blue typing indicator
                                    isOtherOnline -> Color(0xFF0160E3) // Real online
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
                        if (conversation.isGroup) {
                            // 1. Gallery / Media icon (Screenshot 8)
                            IconButton(
                                onClick = { showMediaLinksDocsSheet = true },
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("chat_group_media_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = "Media",
                                    tint = Color(0xFF111827),
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            // 2. Video Call with dropdown arrow (Screenshots 8 & 9)
                            Box {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { callMenuExpanded = true }
                                        .padding(horizontal = 4.dp, vertical = 6.dp)
                                        .testTag("chat_group_call_dropdown_button"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Videocam,
                                        contentDescription = "Video call options",
                                        tint = Color(0xFF111827),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = Color(0xFF111827),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Dropdown menu matching Screenshot 9
                                DropdownMenu(
                                    expanded = callMenuExpanded,
                                    onDismissRequest = { callMenuExpanded = false },
                                    modifier = Modifier.widthIn(min = 200.dp)
                                ) {
                                    DropdownMenuItem(
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Call,
                                                contentDescription = null,
                                                tint = Color(0xFF54656F),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        },
                                        text = { Text("Voice call", fontSize = 15.sp, color = Color(0xFF111827)) },
                                        onClick = {
                                            callMenuExpanded = false
                                            onCallClick(CallType.VOICE)
                                        }
                                    )
                                    DropdownMenuItem(
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Videocam,
                                                contentDescription = null,
                                                tint = Color(0xFF54656F),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        },
                                        text = { Text("Video call", fontSize = 15.sp, color = Color(0xFF111827)) },
                                        onClick = {
                                            callMenuExpanded = false
                                            onCallClick(CallType.VIDEO)
                                        }
                                    )
                                    DropdownMenuItem(
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = Color(0xFF54656F),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        },
                                        text = { Text("Select people", fontSize = 15.sp, color = Color(0xFF111827)) },
                                        onClick = {
                                            callMenuExpanded = false
                                            showAddGroupMemberDialog = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Link,
                                                contentDescription = null,
                                                tint = Color(0xFF54656F),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        },
                                        text = { Text("Send call link", fontSize = 15.sp, color = Color(0xFF111827)) },
                                        onClick = {
                                            callMenuExpanded = false
                                            val callLink = "https://call.olinam.com/${conversation.id}"
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, "Join our Olinam group call: $callLink")
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "Share call link"))
                                        }
                                    )
                                }
                            }
                        } else {
                            // 1. List icon (stacked cards with person avatar as in reference image)
                            IconButton(
                                onClick = { showListsBottomSheet = true },
                                modifier = Modifier
                                    .size(40.dp)
                                    .testTag("chat_lists_button")
                            ) {
                                ChatListIcon(
                                    tint = Color(0xFF111827)
                                )
                            }

                            // 2. Call icon (handset with plus sign as in reference image)
                            IconButton(
                                onClick = {
                                    if (isContactBlocked) {
                                        Toast.makeText(context, "Unblock ${conversation.title} to place a call.", Toast.LENGTH_SHORT).show()
                                    } else if (sendViaSms) {
                                        val phone = conversation.phoneNumber ?: conversation.title.filter { it.isDigit() || it == '+' }
                                        if (phone.isNotBlank()) {
                                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                            context.startActivity(dialIntent)
                                        } else {
                                            Toast.makeText(context, "No valid phone number for SMS contact.", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        showCallTypeBottomSheet = true
                                    }
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .testTag("chat_unified_call_button")
                            ) {
                                ChatCallWithPlusIcon(
                                    tint = if (isContactBlocked) Color(0xFF94A3B8) else Color(0xFF111827)
                                )
                            }
                        }

                        // Beside it: 3-dots list / options button
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
                                onDismissRequest = {
                                    menuExpanded = false
                                    showMoreSubMenu = false
                                },
                                modifier = Modifier.widthIn(min = 210.dp)
                            ) {
                                if (!showMoreSubMenu) {
                                    // Main 3-dots Menu: ONLY Add contact, Search, Mute notifications, Disappearing messages, More >
                                    if (conversation.isGroup) {
                                        DropdownMenuItem(
                                            text = { Text("Group info", fontSize = 15.sp, color = Color(0xFF111B21)) },
                                            onClick = {
                                                menuExpanded = false
                                                onOpenProfile()
                                            }
                                        )
                                    } else {
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
                                    }

                                    DropdownMenuItem(
                                        text = { Text("Search", fontSize = 15.sp, color = Color(0xFF111B21)) },
                                        onClick = {
                                            menuExpanded = false
                                            isSearchMode = true
                                        }
                                    )

                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                if (isChatMuted) "Unmute notifications" else "Mute notifications",
                                                fontSize = 15.sp,
                                                color = Color(0xFF111B21)
                                            )
                                        },
                                        onClick = {
                                            menuExpanded = false
                                            if (isChatMuted) {
                                                isChatMuted = false
                                                chatPrefs.edit().putBoolean("muted_${conversation.id}", false).apply()
                                                Toast.makeText(context, "Notifications unmuted", Toast.LENGTH_SHORT).show()
                                            } else {
                                                showMuteDialog = true
                                            }
                                        }
                                    )

                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "Disappearing messages (${activeDisappearingDurationState})",
                                                fontSize = 15.sp,
                                                color = Color(0xFF111B21)
                                            )
                                        },
                                        onClick = {
                                            menuExpanded = false
                                            showDisappearingDialog = true
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
                                            showMoreSubMenu = true
                                        }
                                    )
                                } else {
                                    // WhatsApp "More" Sub-Menu (Exact match to user screenshot)
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Start
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                    contentDescription = "Back",
                                                    modifier = Modifier.size(16.dp),
                                                    tint = Color(0xFF111B21)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("More", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111B21))
                                            }
                                        },
                                        onClick = {
                                            showMoreSubMenu = false
                                        }
                                    )

                                    DropdownMenuItem(
                                        text = { Text("Report", fontSize = 15.sp, color = Color(0xFF111B21)) },
                                        onClick = {
                                            menuExpanded = false
                                            showMoreSubMenu = false
                                            showReportDialog = true
                                        }
                                    )

                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                if (isContactBlocked) "Unblock" else "Block",
                                                fontSize = 15.sp,
                                                color = Color(0xFF111B21)
                                            )
                                        },
                                        onClick = {
                                            menuExpanded = false
                                            showMoreSubMenu = false
                                            if (isContactBlocked) {
                                                isContactBlocked = false
                                                chatPrefs.edit().putBoolean("blocked_${conversation.id}", false).apply()
                                                Toast.makeText(context, "${conversation.title} was unblocked", Toast.LENGTH_SHORT).show()
                                            } else {
                                                showBlockConfirmDialog = true
                                            }
                                        }
                                    )

                                    DropdownMenuItem(
                                        text = { Text("Clear chat", fontSize = 15.sp, color = Color(0xFF111B21)) },
                                        onClick = {
                                            menuExpanded = false
                                            showMoreSubMenu = false
                                            showClearChatConfirm = true
                                        }
                                    )

                                    DropdownMenuItem(
                                        text = { Text("Export chat", fontSize = 15.sp, color = Color(0xFF111B21)) },
                                        onClick = {
                                            menuExpanded = false
                                            showMoreSubMenu = false
                                            val sb = StringBuilder("WhatsApp Chat with ${conversation.title}\n\n")
                                            displayMessages.forEach { m ->
                                                val sender = if (m.senderId == currentUserId || m.senderId == "user_me" || m.senderId == "me") "You" else conversation.title
                                                sb.append("[${formatMessageTime(m.timestamp)}] $sender: ${m.text}\n")
                                            }
                                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_SUBJECT, "Chat with ${conversation.title}")
                                                putExtra(Intent.EXTRA_TEXT, sb.toString())
                                            }
                                            context.startActivity(Intent.createChooser(sendIntent, "Export chat with ${conversation.title}"))
                                        }
                                    )

                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                if (conversation.isGroup) "Exit group" else "Delete chat",
                                                fontSize = 15.sp,
                                                color = Color(0xFF111B21)
                                            )
                                        },
                                        onClick = {
                                            menuExpanded = false
                                            showMoreSubMenu = false
                                            showDeleteChatConfirm = true
                                        }
                                    )

                                    DropdownMenuItem(
                                        text = { Text("Add shortcut", fontSize = 15.sp, color = Color(0xFF111B21)) },
                                        onClick = {
                                            menuExpanded = false
                                            showMoreSubMenu = false
                                            try {
                                                if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
                                                    val shortcutIntent = Intent(context, com.example.MainActivity::class.java).apply {
                                                        action = Intent.ACTION_VIEW
                                                        putExtra("conversation_id", conversation.id)
                                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                                    }
                                                    val pinShortcutInfo = ShortcutInfoCompat.Builder(context, "chat_${conversation.id}")
                                                        .setShortLabel(conversation.title.take(12))
                                                        .setLongLabel("Chat with ${conversation.title}")
                                                        .setIcon(IconCompat.createWithResource(context, com.example.R.mipmap.ic_launcher))
                                                        .setIntent(shortcutIntent)
                                                        .build()
                                                    ShortcutManagerCompat.requestPinShortcut(context, pinShortcutInfo, null)
                                                    Toast.makeText(context, "Shortcut added to home screen", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "Shortcut added to home screen", Toast.LENGTH_SHORT).show()
                                                }
                                            } catch (_: Exception) {
                                                Toast.makeText(context, "Shortcut added to home screen", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        }

        // Chat Messages & Floating Input Container
        // WhatsApp Doodle Background spans the ENTIRE viewport through navigation bars!
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            WhatsAppDoodleBackground(theme = currentWallpaperTheme)

            Column(
                modifier = Modifier.fillMaxSize()
            ) {

                // Messages list filling the available space above the input
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    // WhatsApp Business Profile Card
                    if (conversation.isBusinessAccount || conversation.isVerified) {
                        item {
                            WhatsAppBusinessProfileCard(
                                title = conversation.title,
                                phoneNumber = conversation.phoneNumber,
                                countryOrigin = conversation.countryOrigin ?: "Phone number from United Kingdom",
                                businessCategory = conversation.businessCategory ?: "Business account • Joined in June, 2023",
                                onBlockClick = {
                                    chatPrefs.edit().putBoolean("blocked_${conversation.id}", true).apply()
                                    isContactBlocked = true
                                    Toast.makeText(context, "${conversation.title} blocked", Toast.LENGTH_SHORT).show()
                                },
                                onProfileClick = onOpenProfile
                            )
                        }
                    }

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

                    // Security Encryption Banner (for standard P2P chats)
                    if (!conversation.isBusinessAccount && !isShortCodeOrAlphanumeric) {
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
                    }

                    // WhatsApp Group Hero Welcome Card (Screenshot 8)
                    if (conversation.isGroup) {
                        item {
                            WhatsAppGroupHeroCard(
                                groupTitle = conversation.title,
                                participantsCount = conversation.participantIds.size,
                                groupDescription = conversation.groupDescription,
                                onAddDescriptionClick = {
                                    editGroupDescText = conversation.groupDescription ?: ""
                                    showEditGroupDescDialog = true
                                },
                                onAddMembersClick = {
                                    showAddGroupMemberDialog = true
                                },
                                onInviteLinkClick = {
                                    showGroupLinkScreen = true
                                }
                            )
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
                        if (msg.senderId == "system" || msg.isUnreadDivider) {
                            com.example.ui.screens.group.GroupSystemMessageBubble(text = msg.text)
                        } else {
                            val isMe = msg.senderId == currentUserId || msg.senderId == "user_me" || msg.senderId == "me"
                            val isHighlighted = isSearchMode && searchQuery.isNotBlank() && msg.text.contains(searchQuery, ignoreCase = true)
                            WhatsAppMessageBubble(
                                message = msg,
                                isMe = isMe,
                                isGroup = conversation.isGroup,
                                isHighlighted = isHighlighted,
                                isReadReceiptsEnabled = isReadReceiptsEnabled,
                                quickReactionEnabled = chatSettingsState.quickReaction,
                                quickReactionEmoji = chatSettingsState.quickReactionEmoji,
                                onClick = { selectedMessageForCrypto = msg },
                                onLongClick = { messageForAction = msg },
                                onReactionClick = { emoji ->
                                    onToggleReaction(msg.id, emoji)
                                },
                                onImageClick = { url -> fullScreenImageUrl = url },
                                onReplyClick = { replyTargetId ->
                                    if (!replyTargetId.isNullOrBlank()) {
                                        val idx = messages.indexOfFirst { it.id == replyTargetId }
                                        if (idx >= 0) {
                                            coroutineScope.launch {
                                                listState.animateScrollToItem(idx)
                                            }
                                        }
                                    }
                                },
                                onActionClick = { showSecurityActionDialog = true }
                            )
                        }
                    }
                }

                // Bottom Chat Area FLOATING OVER DOODLE TEXTURE (NO SOLID WHITE BAR!)
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
                } else if (conversation.isGroup && !conversation.canMembersSendMessages && !isCurrentUserAdmin) {
                    com.example.ui.screens.group.AdminOnlyNoticeBar(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                } else if (isShortCodeOrAlphanumeric && !enableManualReplyTest) {
                    // Sleek, minimal read-only banner matching Google Messages production UX
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("chat_shortcode_readonly_banner"),
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White.copy(alpha = 0.98f),
                        shadowElevation = 1.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Can't reply to this short code",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF334155),
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "Learn more",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0160E3),
                                    modifier = Modifier.clickable { showShortCodeInfoDialog = true }
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                if (detectedOtpCode != null) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFE8F5E9),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA5D6A7)),
                                        modifier = Modifier.clickable {
                                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(detectedOtpCode))
                                            Toast.makeText(context, "Copied OTP: $detectedOtpCode", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = null,
                                                tint = Color(0xFF2E7D32),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Copy OTP $detectedOtpCode",
                                                fontSize = 12.sp,
                                                color = Color(0xFF1B5E20),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFF8FAFC),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier.clickable {
                                        chatPrefs.edit().putBoolean("blocked_${conversation.id}", true).apply()
                                        isContactBlocked = true
                                        Toast.makeText(context, "${conversation.title} blocked", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Block,
                                            contentDescription = null,
                                            tint = Color(0xFF64748B),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Block sender",
                                            fontSize = 12.sp,
                                            color = Color(0xFF475569)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Floating Input Container with Quoted Reply Bar
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 6.dp, end = 6.dp, bottom = 6.dp, top = 2.dp)
                    ) {
                        // Tagged / Quoted Reply Banner above text input
                        AnimatedVisibility(
                            visible = replyingToMessage != null,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            replyingToMessage?.let { targetMsg ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFEFF6FF),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(IntrinsicSize.Min),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(4.dp)
                                                .fillMaxHeight()
                                                .background(Color(0xFF0160E3))
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(vertical = 5.dp)
                                        ) {
                                            val replySenderName = if (targetMsg.senderId == currentUserId || targetMsg.senderId == "user_me" || targetMsg.senderId == "me") {
                                                "You"
                                            } else {
                                                targetMsg.senderName.ifBlank { conversation.title }
                                            }
                                            Text(
                                                text = "Replying to $replySenderName",
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0160E3)
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = targetMsg.text.ifBlank { "[Media attachment]" },
                                                fontSize = 12.sp,
                                                color = Color(0xFF475569),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        IconButton(
                                            onClick = { replyingToMessage = null },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Cancel reply",
                                                tint = Color(0xFF64748B),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Rounded White Pill Text Field
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .shadow(1.5.dp, RoundedCornerShape(26.dp)),
                                shape = RoundedCornerShape(26.dp),
                                color = Color.White
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Emoji toggle button
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
                                            tint = if (showEmojiPicker) Color(0xFF0160E3) else Color(0xFF54656F),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    // SMS indicator tag if SMS mode
                                    if (sendViaSms) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFFEF3C7),
                                            modifier = Modifier.padding(end = 4.dp)
                                        ) {
                                            Text(
                                                text = "SMS",
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFB45309),
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
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
                                                if (chatSettingsState.enterToSend && it.endsWith("\n")) {
                                                    inputText = it.removeSuffix("\n")
                                                    handleSendAction()
                                                } else {
                                                    inputText = it
                                                    onTypingChanged(it.isNotBlank())
                                                    if (showAttachSheet) showAttachSheet = false
                                                    if (showEmojiPicker) showEmojiPicker = false
                                                }
                                            },
                                            textStyle = TextStyle(
                                                fontSize = 16.sp,
                                                color = Color(0xFF111B21)
                                            ),
                                            cursorBrush = SolidColor(Color(0xFF0160E3)),
                                            keyboardOptions = KeyboardOptions(
                                                imeAction = if (chatSettingsState.enterToSend) ImeAction.Send else ImeAction.Default
                                            ),
                                            keyboardActions = KeyboardActions(
                                                onSend = { handleSendAction() }
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("chat_message_input")
                                        )
                                    }

                                    // Paperclip attachment button
                                    IconButton(
                                        onClick = {
                                            keyboardController?.hide()
                                            focusManager.clearFocus()
                                            showAttachSheet = !showAttachSheet
                                            if (showAttachSheet) showEmojiPicker = false
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AttachFile,
                                            contentDescription = "Attach",
                                            tint = if (showAttachSheet) Color(0xFF0160E3) else Color(0xFF54656F),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    // Camera button
                                    IconButton(
                                        onClick = { showInAppCamera = true },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = "Camera",
                                            tint = Color(0xFF54656F),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Floating circular action button in Official Olinam Royal Blue
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .shadow(2.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0160E3))
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

                // WhatsApp Attachment Sheet docked at bottom (opens downwards below input bar)
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
                        onGalleryClick = {
                            showAttachSheet = false
                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                        },
                        recentDevicePhotos = recentDevicePhotos,
                        onRecentPhotoClick = { photoUri ->
                            showAttachSheet = false
                            onSendMediaAttachment(photoUri, MediaType.IMAGE, "")
                        }
                    )
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

                // Navigation bars padding INSIDE doodle container so texture extends all the way to screen bottom!
                val isImeOpen = WindowInsets.ime.getBottom(density) > 0
                if (!isImeOpen && !showEmojiPicker && !showAttachSheet) {
                    Spacer(modifier = Modifier.navigationBarsPadding())
                }
            }
        }
    }

    // Google Messages Shortcode Info Dialog
    if (showShortCodeInfoDialog) {
        AlertDialog(
            onDismissRequest = { showShortCodeInfoDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF0160E3),
                    modifier = Modifier.size(34.dp)
                )
            },
            title = {
                Text(
                    text = "Can't reply to this sender",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "This sender is an automated notification service or short code (A2P message).\n\nStandard carrier replies cannot be delivered to automated notification services. You can copy OTP codes or tap links directly inside the message.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF54656F)
                )
            },
            confirmButton = {
                Button(
                    onClick = { showShortCodeInfoDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0160E3))
                ) {
                    Text("Got it", color = Color.White)
                }
            }
        )
    }

    // Meta Account Security Dialog (matching "This wasn't me" button)
    if (showSecurityActionDialog) {
        AlertDialog(
            onDismissRequest = { showSecurityActionDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = Color(0xFF0084FF),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Meta Account Activity Secured",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "We have blocked the unrecognized device login from United Kingdom. Your Meta account password has been temporarily protected and an account recovery link was sent to your email.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF54656F)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSecurityActionDialog = false
                        Toast.makeText(context, "Account secured successfully", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0084FF))
                ) {
                    Text("Done", color = Color.White)
                }
            }
        )
    }

    // Safety / Encryption details modal
    if (showSafetyDialog) {
        AlertDialog(
            onDismissRequest = { showSafetyDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = Color(0xFF0160E3),
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0160E3))
                ) {
                    Text("OK")
                }
            }
        )
    }

    // Confirmation dialog before sending voice message (when enabled in Chats settings)
    if (showVoiceConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showVoiceConfirmDialog = false },
            title = { Text("Send voice message?", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
            text = {
                Text(
                    text = "Voice message confirmation is turned on in Chats settings. Do you want to send this voice note now?",
                    fontSize = 14.sp,
                    color = Color(0xFF4B5563)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showVoiceConfirmDialog = false
                        onSendMessage("🎙️ Voice message (0:04)")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0160E3))
                ) {
                    Text("Send")
                }
            },
            dismissButton = {
                TextButton(onClick = { showVoiceConfirmDialog = false }) {
                    Text("Cancel")
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
                    // Quick Emoji Reaction Bar (Production-grade WhatsApp style)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf("❤️", "👍", "😂", "😮", "😢", "🙏", "🔥").forEach { emoji ->
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (msg.reaction == emoji) Color(0xFFDBEAFE) else Color.Transparent)
                                        .clickable {
                                            onToggleReaction(msg.id, emoji)
                                            messageForAction = null
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = emoji, fontSize = 20.sp)
                                }
                            }
                        }
                    }
                    Text(
                        text = "\"${msg.text.take(60)}${if (msg.text.length > 60) "..." else ""}\"",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = {
                            val target = messageForAction
                            messageForAction = null
                            replyingToMessage = target
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0160E3)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Reply,
                            contentDescription = "Reply",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reply / Tag Message", fontWeight = FontWeight.Bold, color = Color.White)
                    }
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

    // WhatsApp Report Contact / Group Dialog
    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = {
                Text(
                    text = "Report ${conversation.title}?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF111B21)
                )
            },
            text = {
                Column {
                    Text(
                        text = "The last 5 messages from this contact will be forwarded to Olinam. This contact will not be notified.",
                        fontSize = 14.sp,
                        color = Color(0xFF54656F),
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { reportBlockAndDelete = !reportBlockAndDelete }
                    ) {
                        Checkbox(
                            checked = reportBlockAndDelete,
                            onCheckedChange = { reportBlockAndDelete = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00A884))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Block contact and delete chat",
                            fontSize = 14.sp,
                            color = Color(0xFF111B21)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showReportDialog = false
                        chatPrefs.edit().putBoolean("reported_${conversation.id}", true).apply()
                        if (reportBlockAndDelete) {
                            chatPrefs.edit().putBoolean("blocked_${conversation.id}", true).apply()
                            isContactBlocked = true
                            onDeleteChat()
                            Toast.makeText(context, "Report sent. Contact blocked and chat deleted.", Toast.LENGTH_SHORT).show()
                            onBackClick()
                        } else {
                            Toast.makeText(context, "Report sent to Olinam.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A884))
                ) {
                    Text("Report", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Cancel", color = Color(0xFF54656F))
                }
            }
        )
    }

    // WhatsApp Block Contact Confirmation Dialog
    if (showBlockConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showBlockConfirmDialog = false },
            title = {
                Text(
                    text = "Block ${conversation.title}?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF111B21)
                )
            },
            text = {
                Text(
                    text = "Blocked contacts will no longer be able to call you or send you messages.",
                    fontSize = 14.sp,
                    color = Color(0xFF54656F),
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBlockConfirmDialog = false
                        chatPrefs.edit().putBoolean("blocked_${conversation.id}", true).apply()
                        isContactBlocked = true
                        Toast.makeText(context, "${conversation.title} was blocked", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Block", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlockConfirmDialog = false }) {
                    Text("Cancel", color = Color(0xFF54656F))
                }
            }
        )
    }

    // Confirmation dialog to clear chat (Production-grade with media checkbox)
    if (showClearChatConfirm) {
        AlertDialog(
            onDismissRequest = { showClearChatConfirm = false },
            title = {
                Text(
                    text = "Clear this chat?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF111B21)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Messages will only be removed from this device.",
                        fontSize = 14.sp,
                        color = Color(0xFF54656F)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { clearDeleteMedia = !clearDeleteMedia }
                    ) {
                        Checkbox(
                            checked = clearDeleteMedia,
                            onCheckedChange = { clearDeleteMedia = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00A884))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Also delete media received in this chat from the device gallery",
                            fontSize = 13.5.sp,
                            color = Color(0xFF111B21)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearChatConfirm = false
                        onClearChat()
                        Toast.makeText(context, "Chat cleared", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Clear chat", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearChatConfirm = false }) {
                    Text("Cancel", color = Color(0xFF54656F))
                }
            }
        )
    }

    // Confirmation dialog to delete entire chat (Production-grade with media checkbox)
    if (showDeleteChatConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteChatConfirm = false },
            title = {
                Text(
                    text = if (conversation.isGroup) "Exit \"${conversation.title}\" group?" else "Delete this chat?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF111B21)
                )
            },
            text = {
                Column {
                    Text(
                        text = if (conversation.isGroup)
                            "Are you sure you want to exit this group and delete your copy of the conversation?"
                        else
                            "Delete the chat with ${conversation.title} and all its messages?",
                        fontSize = 14.sp,
                        color = Color(0xFF54656F)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { deleteChatMedia = !deleteChatMedia }
                    ) {
                        Checkbox(
                            checked = deleteChatMedia,
                            onCheckedChange = { deleteChatMedia = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00A884))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Also delete media received in this chat from the device gallery",
                            fontSize = 13.5.sp,
                            color = Color(0xFF111B21)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteChatConfirm = false
                        onDeleteChat()
                        onBackClick()
                        Toast.makeText(context, if (conversation.isGroup) "Group exited and deleted" else "Chat deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text(if (conversation.isGroup) "Exit group" else "Delete chat", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteChatConfirm = false }) {
                    Text("Cancel", color = Color(0xFF54656F))
                }
            }
        )
    }

    // Edit Group Description Dialog (Screenshot 8)
    if (showEditGroupDescDialog) {
        AlertDialog(
            onDismissRequest = { showEditGroupDescDialog = false },
            title = { Text("Group description", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = editGroupDescText,
                    onValueChange = { editGroupDescText = it },
                    placeholder = { Text("Add group description...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEditGroupDescDialog = false
                        onUpdateGroupDescription(editGroupDescText.trim())
                        Toast.makeText(context, "Group description updated", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditGroupDescDialog = false }) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            }
        )
    }

    // Add Group Member Dialog (Screenshot 8)
    if (showAddGroupMemberDialog) {
        AlertDialog(
            onDismissRequest = { showAddGroupMemberDialog = false },
            title = { Text("Add to group", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Enter phone number or contact ID to add to ${conversation.title}:",
                        fontSize = 13.5.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newMemberPhoneText,
                        onValueChange = { newMemberPhoneText = it },
                        singleLine = true,
                        placeholder = { Text("+91 98765 43210") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clean = newMemberPhoneText.trim()
                        if (clean.isNotBlank()) {
                            onAddGroupMember(clean)
                            newMemberPhoneText = ""
                            showAddGroupMemberDialog = false
                            Toast.makeText(context, "$clean added to group", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGroupMemberDialog = false }) {
                    Text("Cancel", color = Color(0xFF64748B))
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
                    tint = Color(0xFF0160E3),
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
                        color = if (msg.isSms) Color(0xFFB45309) else Color(0xFF0160E3)
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
 * WhatsApp Group Hero Card shown at the top of a group chat (Screenshot 8).
 * Features:
 * - Centered group avatar circle
 * - Group title in bold
 * - Subtitle: "Group · X members"
 * - Clickable "Add group description" link or description text
 * - "Add members" and "Invite to group via link" rounded action chips
 */
@Composable
fun WhatsAppGroupHeroCard(
    groupTitle: String,
    participantsCount: Int,
    groupDescription: String?,
    onAddDescriptionClick: () -> Unit,
    onAddMembersClick: () -> Unit,
    onInviteLinkClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.94f),
        shadowElevation = 0.5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Group Avatar Icon
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE2EDFC)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = "Group",
                    tint = Color(0xFF0160E3),
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Group Name
            Text(
                text = groupTitle,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            // Subtitle: Group · X members
            Text(
                text = "Group · $participantsCount members",
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Group Description or "Add group description"
            if (!groupDescription.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAddDescriptionClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = groupDescription,
                            fontSize = 13.sp,
                            color = Color(0xFF334155),
                            modifier = Modifier.weight(1f),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit description",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else {
                Text(
                    text = "Add group description",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF0160E3),
                    modifier = Modifier
                        .clickable { onAddDescriptionClick() }
                        .padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: Add members & Invite via link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFE2EDFC),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onAddMembersClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = Color(0xFF0160E3),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add members",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0160E3)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onInviteLinkClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Invite via link",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF334155)
                        )
                    }
                }
            }
        }
    }
}

/**
 * WhatsApp Attachment Bottom Sheet:
 * - 4 core options: Document, Location, Contact, Poll (compact pill/capsule style)
 * - Bottom: Camera tile + Full Gallery photo grid
 */
@Composable
fun WhatsAppAttachmentSheet(
    onDocumentClick: () -> Unit,
    onLocationClick: () -> Unit,
    onContactClick: () -> Unit,
    onPollClick: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    recentDevicePhotos: List<Uri>,
    onRecentPhotoClick: (Uri) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
        color = Color(0xFFF8F9FA),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp)
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF8696A0).copy(alpha = 0.45f))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action chips Row: File Manager, Location, Contact, Poll (compact pill style with smaller icons & spacing)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AttachmentActionItem(
                    label = "File Manager",
                    icon = Icons.Default.Folder,
                    iconTint = Color(0xFF7F66FF),
                    onClick = onDocumentClick
                )
                AttachmentActionItem(
                    label = "Location",
                    icon = Icons.Default.LocationOn,
                    iconTint = Color(0xFF00A884),
                    onClick = onLocationClick
                )
                AttachmentActionItem(
                    label = "Contact",
                    icon = Icons.Default.Person,
                    iconTint = Color(0xFF0288D1),
                    onClick = onContactClick
                )
                AttachmentActionItem(
                    label = "Poll",
                    icon = Icons.Default.Poll,
                    iconTint = Color(0xFFEAA023),
                    onClick = onPollClick
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Camera + Full Gallery Grid exactly as in WhatsApp
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .padding(horizontal = 2.dp),
                contentPadding = PaddingValues(2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // First tile: Camera
                item {
                    Surface(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clickable { onCameraClick() },
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Camera",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Camera",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Second tile: Gallery (Full device photo album shortcut)
                item {
                    Surface(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clickable { onGalleryClick() },
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF1F5F9),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = "Gallery",
                                tint = Color(0xFF0160E3),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Gallery",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0160E3)
                            )
                        }
                    }
                }

                // Recent Gallery photos from device MediaStore
                items(recentDevicePhotos) { photoUri ->
                    Surface(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clickable { onRecentPhotoClick(photoUri) },
                        shape = RoundedCornerShape(6.dp),
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

@Composable
fun AttachmentActionItem(
    label: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 1.dp, horizontal = 1.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(17.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
            shadowElevation = 0.5.dp,
            modifier = Modifier
                .width(58.dp)
                .height(34.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 10.5.sp,
            color = Color(0xFF475569),
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
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
                            color = if (selectedTab == index) Color(0xFF0160E3).copy(alpha = 0.12f) else Color.Transparent,
                            modifier = Modifier.clickable { selectedTab = index }
                        ) {
                            Text(
                                text = title,
                                fontSize = 12.5.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == index) Color(0xFF0160E3) else Color(0xFF54656F),
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
                        Text("All Photos", color = Color(0xFF0160E3), fontWeight = FontWeight.Bold)
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
                    tint = Color(0xFF0160E3),
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
                    color = Color(0xFFEFF6FF),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
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
                            tint = Color(0xFF0160E3),
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
                    color = Color(0xFFE0F2FE),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBAE6FD)),
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
                            tint = Color(0xFF0160E3),
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
                            tint = Color(0xFF0160E3),
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0160E3))
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
    var options by remember { mutableStateOf(listOf("", "")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Poll,
                    contentDescription = null,
                    tint = Color(0xFF0160E3),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Poll", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF111827))
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text("Ask a question...") },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0160E3),
                        focusedLabelColor = Color(0xFF0160E3)
                    )
                )

                Text(
                    text = "Options",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )

                options.forEachIndexed { index, opt ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = opt,
                            onValueChange = { newVal ->
                                val updated = options.toMutableList()
                                updated[index] = newVal
                                options = updated
                            },
                            label = { Text("Option ${index + 1}") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0160E3),
                                focusedLabelColor = Color(0xFF0160E3)
                            )
                        )
                        if (options.size > 2) {
                            IconButton(
                                onClick = {
                                    val updated = options.toMutableList()
                                    updated.removeAt(index)
                                    options = updated
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove option",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                if (options.size < 6) {
                    TextButton(
                        onClick = {
                            options = options + ""
                        },
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color(0xFF0160E3),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add Option",
                            color = Color(0xFF0160E3),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val validOptions = options.map { it.trim() }.filter { it.isNotBlank() }
                    if (question.isNotBlank() && validOptions.size >= 2) {
                        onCreatePoll(question.trim(), validOptions)
                    }
                },
                enabled = question.isNotBlank() && options.count { it.isNotBlank() } >= 2,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0160E3))
            ) {
                Text("Create Poll", fontWeight = FontWeight.Bold)
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
    isGroup: Boolean = false,
    isHighlighted: Boolean = false,
    isReadReceiptsEnabled: Boolean = true,
    quickReactionEnabled: Boolean = true,
    quickReactionEmoji: String = "❤️",
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onReactionClick: (String) -> Unit = {},
    onImageClick: (String) -> Unit = {},
    onReplyClick: (String?) -> Unit = {},
    onActionClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val bubbleShape = RoundedCornerShape(
        topStart = if (isMe) 14.dp else 4.dp,
        topEnd = if (isMe) 4.dp else 14.dp,
        bottomStart = 14.dp,
        bottomEnd = 14.dp
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.5.dp, horizontal = 4.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Column(horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {
            Surface(
                modifier = Modifier
                    .widthIn(min = 60.dp, max = 320.dp)
                    .shadow(0.8.dp, bubbleShape)
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick,
                        onDoubleClick = {
                            if (quickReactionEnabled) {
                                onReactionClick(quickReactionEmoji)
                            }
                        }
                    )
                    .testTag("message_bubble_${message.id}"),
                shape = bubbleShape,
                border = if (isHighlighted) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFF59E0B))
                         else null,
                color = if (isHighlighted) Color(0xFFFEF3C7) else if (isMe) Color(0xFFDCEBFC) else Color.White
            ) {
            Column(
                modifier = Modifier.padding(start = 9.dp, end = 9.dp, top = 6.dp, bottom = 5.dp)
            ) {
                if (isGroup && !isMe && message.senderName.isNotBlank()) {
                    com.example.ui.screens.group.GroupSenderHeader(
                        senderName = message.senderName,
                        senderId = message.senderId
                    )
                }

                // Quoted / Tagged Reply Preview Card inside Bubble (Matching reference image)
                if (!message.replyToText.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isMe) Color(0xFFCCE4FD) else Color(0xFFF1F5F9),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 5.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onReplyClick(message.replyToMessageId) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                        ) {
                            // Left vertical bar in Official Royal Blue
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .fillMaxHeight()
                                    .background(Color(0xFF0160E3))
                            )
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = message.replyToSenderName?.ifBlank { "You" } ?: "You",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0160E3),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = message.replyToText ?: "",
                                    fontSize = 12.sp,
                                    color = Color(0xFF475569),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                // If message has mediaUrl (image), display image preview
                val hasImage = (!message.mediaUrl.isNullOrBlank() || message.mediaType == MediaType.IMAGE) &&
                        !message.text.contains("📄") && !message.text.contains("🎵")
                val hasCustomCaption = hasImage && message.text.isNotBlank() &&
                        message.text != "🖼️ Photo" &&
                        message.text != "Photo"

                if (hasImage && !message.mediaUrl.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFE2E8F0),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 140.dp, max = 240.dp)
                            .padding(bottom = if (hasCustomCaption) 4.dp else 2.dp)
                            .clickable { onImageClick(message.mediaUrl) }
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = message.mediaUrl,
                                contentDescription = "Media preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // If no custom caption, overlay time & ticks directly on image
                            if (!hasCustomCaption) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.Black.copy(alpha = 0.55f),
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = formatMessageTime(message.timestamp),
                                            fontSize = 10.5.sp,
                                            color = Color.White
                                        )
                                        if (isMe) {
                                            Spacer(modifier = Modifier.width(3.dp))
                                            when (message.status) {
                                                com.example.model.MessageStatus.SENDING -> {
                                                    Icon(
                                                        imageVector = Icons.Default.Schedule,
                                                        contentDescription = "Sending",
                                                        tint = Color.White.copy(alpha = 0.7f),
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                }
                                                com.example.model.MessageStatus.SENT -> {
                                                    Icon(
                                                        imageVector = Icons.Default.Done,
                                                        contentDescription = "Sent",
                                                        tint = Color.White.copy(alpha = 0.85f),
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                                com.example.model.MessageStatus.DELIVERED -> {
                                                    Icon(
                                                        imageVector = Icons.Default.DoneAll,
                                                        contentDescription = "Delivered",
                                                        tint = Color.White.copy(alpha = 0.9f),
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                                com.example.model.MessageStatus.READ -> {
                                                    Icon(
                                                        imageVector = Icons.Default.DoneAll,
                                                        contentDescription = "Read",
                                                        tint = if (isReadReceiptsEnabled) Color(0xFF60A5FA) else Color.White.copy(alpha = 0.9f),
                                                        modifier = Modifier.size(14.dp)
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

                // If SMS, show small SMS indicator tag
                if (message.isSms) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sms,
                            contentDescription = "SMS",
                            tint = if (isMe) Color(0xFFFEF3C7) else Color(0xFFB45309),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "SMS text",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isMe) Color(0xFFFEF3C7) else Color(0xFFB45309)
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
                            shape = RoundedCornerShape(10.dp),
                            color = if (isMe) Color(0xFFCCE4FD) else Color(0xFFF1F5F9),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isMe) Color(0xFFBFDBFE) else Color(0xFFE2E8F0)),
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
                                        .height(100.dp)
                                        .clip(RoundedCornerShape(topStart = 9.dp, topEnd = 9.dp))
                                        .background(Color(0xFFE2E8F0)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                        val w = size.width
                                        val h = size.height
                                        // Blue water band
                                        drawRect(
                                            color = Color(0xFFBFDBFE),
                                            topLeft = androidx.compose.ui.geometry.Offset(0f, h * 0.65f),
                                            size = androidx.compose.ui.geometry.Size(w, h * 0.35f)
                                        )
                                        // Road grids
                                        drawLine(
                                            color = Color.White,
                                            start = androidx.compose.ui.geometry.Offset(0f, h * 0.35f),
                                            end = androidx.compose.ui.geometry.Offset(w, h * 0.45f),
                                            strokeWidth = 6f
                                        )
                                        drawLine(
                                            color = Color.White,
                                            start = androidx.compose.ui.geometry.Offset(w * 0.45f, 0f),
                                            end = androidx.compose.ui.geometry.Offset(w * 0.55f, h),
                                            strokeWidth = 7f
                                        )
                                        drawLine(
                                            color = Color(0xFFCBD5E1),
                                            start = androidx.compose.ui.geometry.Offset(w * 0.1f, 0f),
                                            end = androidx.compose.ui.geometry.Offset(w * 0.9f, h),
                                            strokeWidth = 3f
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF0160E3).copy(alpha = 0.18f))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                            .shadow(2.dp, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = "Location Pin",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color.White.copy(alpha = 0.92f),
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF0160E3))
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "GPS Location",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0160E3)
                                            )
                                        }
                                    }
                                }
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = title,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF111827)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = subtitle,
                                        fontSize = 11.sp,
                                        color = Color(0xFF475569),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.OpenInNew,
                                            contentDescription = null,
                                            tint = Color(0xFF0160E3),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Open in Google Maps",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF0160E3)
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
                            shape = RoundedCornerShape(10.dp),
                            color = if (isMe) Color(0xFFCCE4FD) else Color(0xFFF1F5F9),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isMe) Color(0xFFBFDBFE) else Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 3.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF0160E3)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = contactName.take(1).uppercase(Locale.getDefault()),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = contactName,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF111827)
                                        )
                                        if (phone.isNotBlank()) {
                                            Text(
                                                text = phone,
                                                fontSize = 11.5.sp,
                                                color = Color(0xFF475569)
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
                                        Text("Message", fontSize = 11.5.sp, color = Color(0xFF0160E3), fontWeight = FontWeight.Bold)
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
                                        Text("Save", fontSize = 11.5.sp, color = Color(0xFF0160E3), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    isDoc -> {
                        val docName = message.text.substringAfter("📄").substringAfter("Document:").trim().ifBlank { "Document.pdf" }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isMe) Color(0xFFCCE4FD) else Color(0xFFF1F5F9),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isMe) Color(0xFFBFDBFE) else Color(0xFFE2E8F0)),
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
                                modifier = Modifier.padding(8.dp),
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
                                        color = Color(0xFF111827),
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
                                    tint = Color(0xFF0160E3),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    isAudio -> {
                        var isPlaying by remember { mutableStateOf(false) }
                        val audioName = message.text.substringAfter("🎵").substringAfter("Audio:").trim().ifBlank { "Voice Note.mp3" }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isMe) Color(0xFFCCE4FD) else Color(0xFFF1F5F9),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isMe) Color(0xFFBFDBFE) else Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 3.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF0160E3))
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
                                        color = Color(0xFF111827),
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
                                                    .background(if (isPlaying && idx < 7) Color(0xFF0160E3) else Color(0xFF94A3B8))
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
                            shape = RoundedCornerShape(10.dp),
                            color = if (isMe) Color(0xFFCCE4FD) else Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isMe) Color(0xFFBFDBFE) else Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 3.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Poll,
                                        contentDescription = null,
                                        tint = Color(0xFF0160E3),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = question,
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF111827)
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Select one option • Tap to vote",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                options.forEachIndexed { idx, opt ->
                                    val isSelected = selectedOptionIndex == idx
                                    val hasAnyVote = selectedOptionIndex != null
                                    val pct = if (isSelected) 100 else 0
                                    val voteText = if (isSelected) "1 vote (100%)" else if (hasAnyVote) "0 votes (0%)" else ""

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.White,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isSelected) Color(0xFF0160E3) else Color(0xFFE2E8F0)
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp)
                                            .clickable {
                                                selectedOptionIndex = if (selectedOptionIndex == idx) null else idx
                                            }
                                    ) {
                                        Box(modifier = Modifier.fillMaxWidth()) {
                                            if (hasAnyVote && pct > 0) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth(pct / 100f)
                                                        .height(40.dp)
                                                        .background(Color(0xFFDBEAFE).copy(alpha = 0.6f))
                                                )
                                            }
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(18.dp)
                                                        .clip(CircleShape)
                                                        .border(
                                                            1.8.dp,
                                                            if (isSelected) Color(0xFF0160E3) else Color(0xFF94A3B8),
                                                            CircleShape
                                                        )
                                                        .background(if (isSelected) Color(0xFF0160E3) else Color.Transparent),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (isSelected) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = null,
                                                            tint = Color.White,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = opt.substringAfter(". ").trim(),
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = Color(0xFF111827),
                                                    modifier = Modifier.weight(1f)
                                                )
                                                if (voteText.isNotBlank()) {
                                                    Text(
                                                        text = voteText,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = if (isSelected) Color(0xFF0160E3) else Color(0xFF64748B)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Total votes: ${if (selectedOptionIndex != null) 1 else 0}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }

                    else -> {
                        if (!(hasImage && !hasCustomCaption)) {
                            val otpRegex = remember { Regex("""\b\d{4,8}\b""") }
                            val foundOtp = message.otpCode ?: if (message.isOtp || message.text.contains("two-factor", ignoreCase = true) || message.text.contains("OTP", ignoreCase = true)) otpRegex.find(message.text)?.value else null

                            val isShortSingleLine = message.text.length < 32 &&
                                    !message.text.contains('\n') &&
                                    !message.text.contains("http") &&
                                    message.boldHeader.isNullOrBlank() &&
                                    message.actionButtonText.isNullOrBlank() &&
                                    (foundOtp == null || isMe)

                        if (isShortSingleLine) {
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.padding(bottom = 1.dp)
                            ) {
                                Text(
                                    text = message.text,
                                    fontSize = 14.5.sp,
                                    color = Color(0xFF111827),
                                    lineHeight = 20.sp
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 1.dp)
                                ) {
                                    Text(
                                        text = formatMessageTime(message.timestamp),
                                        fontSize = 10.5.sp,
                                        color = Color(0xFF64748B)
                                    )
                                    if (isMe) {
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Icon(
                                            imageVector = Icons.Default.DoneAll,
                                            contentDescription = if (isReadReceiptsEnabled) "Read" else "Delivered",
                                            tint = if (isReadReceiptsEnabled) Color(0xFF0284C7) else Color(0xFF94A3B8),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            if (!message.boldHeader.isNullOrBlank()) {
                                Text(
                                    text = message.boldHeader,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111827),
                                    lineHeight = 20.sp,
                                    modifier = Modifier.padding(bottom = 3.dp)
                                )
                            }

                            if (message.text.isNotBlank()) {
                                Text(
                                    text = message.text,
                                    fontSize = 14.5.sp,
                                    color = Color(0xFF111827),
                                    lineHeight = 20.sp
                                )
                            }

                            // OTP quick copy badge in Official Blue style
                            if (foundOtp != null && !isMe) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFEFF6FF),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                                    modifier = Modifier.clickable {
                                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(foundOtp))
                                        android.widget.Toast.makeText(context, "Copied code: $foundOtp", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy code",
                                            tint = Color(0xFF0160E3),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Copy $foundOtp",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF0160E3)
                                        )
                                    }
                                }
                            }

                            // Interactive card action button in Official Blue style
                            if (!message.actionButtonText.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(0.8.dp)
                                        .background(Color(0xFFE2E8F0))
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onActionClick(message.actionButtonText) }
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.OpenInNew,
                                        contentDescription = null,
                                        tint = Color(0xFF0160E3),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = message.actionButtonText,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF0160E3)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Row(
                                modifier = Modifier.align(Alignment.End),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = formatMessageTime(message.timestamp),
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )

                                if (isMe) {
                                    Spacer(modifier = Modifier.width(3.dp))
                                    when (message.status) {
                                        com.example.model.MessageStatus.SENDING -> {
                                            Icon(
                                                imageVector = Icons.Default.Schedule,
                                                contentDescription = "Sending",
                                                tint = Color(0xFF94A3B8),
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                        com.example.model.MessageStatus.SENT -> {
                                            Icon(
                                                imageVector = Icons.Default.Done,
                                                contentDescription = "Sent",
                                                tint = Color(0xFF94A3B8),
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                        com.example.model.MessageStatus.DELIVERED -> {
                                            Icon(
                                                imageVector = Icons.Default.DoneAll,
                                                contentDescription = "Delivered",
                                                tint = Color(0xFF94A3B8),
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                        com.example.model.MessageStatus.READ -> {
                                            Icon(
                                                imageVector = Icons.Default.DoneAll,
                                                contentDescription = "Read",
                                                tint = if (isReadReceiptsEnabled) Color(0xFF0284C7) else Color(0xFF94A3B8),
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        }
                    }
                }

                // If special card type (media, location, contact, audio, poll), show timestamp at bottom
                val isSpecialMediaCard = isLocation || isContact || isDoc || isAudio || isPoll
                if (isSpecialMediaCard) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatMessageTime(message.timestamp),
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )

                        if (isMe) {
                            Spacer(modifier = Modifier.width(3.dp))
                            when (message.status) {
                                com.example.model.MessageStatus.SENDING -> {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = "Sending",
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                com.example.model.MessageStatus.SENT -> {
                                    Icon(
                                        imageVector = Icons.Default.Done,
                                        contentDescription = "Sent",
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                com.example.model.MessageStatus.DELIVERED -> {
                                    Icon(
                                        imageVector = Icons.Default.DoneAll,
                                        contentDescription = "Delivered",
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                com.example.model.MessageStatus.READ -> {
                                    Icon(
                                        imageVector = Icons.Default.DoneAll,
                                        contentDescription = "Read",
                                        tint = if (isReadReceiptsEnabled) Color(0xFF0284C7) else Color(0xFF94A3B8),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!message.reaction.isNullOrBlank()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shadowElevation = 1.5.dp,
                modifier = Modifier
                    .offset(y = (-6).dp, x = if (isMe) (-6).dp else 6.dp)
                    .clickable { onReactionClick(message.reaction) }
                    .testTag("message_reaction_${message.id}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = message.reaction, fontSize = 13.sp)
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0160E3))
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
                            color = if (selectedTab == index) Color(0xFF0160E3) else Color.Transparent,
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
                                                tint = Color(0xFF0160E3),
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
                    tint = Color(0xFF0160E3),
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
                        color = if (isSelected) Color(0xFF0160E3).copy(alpha = 0.12f) else Color(0xFFF1F5F9),
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
                                color = if (isSelected) Color(0xFF0160E3) else Color(0xFF111B21)
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = "Selected",
                                    tint = Color(0xFF0160E3),
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
                    tint = Color(0xFF0160E3),
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
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF0160E3)) else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
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
                        color = if (isSelected) Color(0xFF0160E3).copy(alpha = 0.12f) else Color(0xFFF1F5F9),
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
                                    .background(if (isSelected) Color(0xFF0160E3) else Color.Transparent)
                                    .border(1.5.dp, if (isSelected) Color(0xFF0160E3) else Color(0xFF94A3B8), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = opt,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF0160E3) else Color(0xFF111B21)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onMute(selectedOption) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0160E3))
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

/**
 * List / Contact Card icon as shown in user's reference image:
 * Features two stacked rounded rectangle cards with a person silhouette (head circle & shoulder arc).
 */
@Composable
fun ChatListIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color(0xFF111827)
) {
    androidx.compose.foundation.Canvas(modifier = modifier.size(24.dp)) {
        val stroke = androidx.compose.ui.graphics.drawscope.Stroke(
            width = 1.8.dp.toPx(),
            cap = androidx.compose.ui.graphics.StrokeCap.Round,
            join = androidx.compose.ui.graphics.StrokeJoin.Round
        )
        val w = size.width
        val h = size.height

        // Back layer card (offset to the bottom-left)
        val backPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.12f, h * 0.30f)
            lineTo(w * 0.12f, h * 0.88f)
            quadraticTo(w * 0.12f, h * 0.96f, w * 0.20f, h * 0.96f)
            lineTo(w * 0.72f, h * 0.96f)
        }
        drawPath(backPath, color = tint, style = stroke)

        // Front card (rounded rectangle)
        val frontCorner = 3.dp.toPx()
        drawRoundRect(
            color = tint,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.28f, h * 0.08f),
            size = androidx.compose.ui.geometry.Size(w * 0.68f, h * 0.76f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(frontCorner, frontCorner),
            style = stroke
        )

        // Avatar head inside front card
        val headRadius = 2.4.dp.toPx()
        drawCircle(
            color = tint,
            radius = headRadius,
            center = androidx.compose.ui.geometry.Offset(w * 0.62f, h * 0.36f),
            style = stroke
        )

        // Avatar shoulders inside front card
        val shoulderPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.44f, h * 0.68f)
            quadraticTo(w * 0.62f, h * 0.50f, w * 0.80f, h * 0.68f)
        }
        drawPath(shoulderPath, color = tint, style = stroke)
    }
}

/**
 * Call icon with a plus sign at the top right, as shown in user's reference image.
 */
@Composable
fun ChatCallWithPlusIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color(0xFF111827)
) {
    Box(
        modifier = modifier.size(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Call,
            contentDescription = "Call",
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 1.dp, y = (-2).dp)
                .size(11.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(11.dp)
            )
        }
    }
}

/**
 * Select Call Type Bottom Sheet matching user's reference image:
 * Features a top drag handle, "Select call type" title in Official Blue,
 * and two clean options: "Voice call" and "Video call".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectCallTypeBottomSheet(
    onDismissRequest: () -> Unit,
    onVoiceCallClick: () -> Unit,
    onVideoCallClick: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 38.dp, height = 4.dp),
                shape = RoundedCornerShape(2.dp),
                color = Color(0xFFD1D5DB)
            ) {}
        },
        modifier = Modifier
            .navigationBarsPadding()
            .testTag("select_call_type_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp)
        ) {
            Text(
                text = "Select call type",
                fontSize = 14.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0160E3), // Official Blue as requested
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Voice Call
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onDismissRequest()
                        onVoiceCallClick()
                    }
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Voice call",
                    tint = Color(0xFF111827),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(20.dp))
                Text(
                    text = "Voice call",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF111827)
                )
            }

            // Video Call
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onDismissRequest()
                        onVideoCallClick()
                    }
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = "Video call",
                    tint = Color(0xFF111827),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(20.dp))
                Text(
                    text = "Video call",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF111827)
                )
            }
        }
    }
}

/**
 * Chat Lists Bottom Sheet: allows user to manage / assign this conversation
 * to custom lists (e.g., Favorites, Friends, Family, Work, VIP).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListsBottomSheet(
    conversation: Conversation,
    labels: List<ChatLabel> = emptyList(),
    onDismissRequest: () -> Unit,
    onCreateNewLabel: (String, String) -> Unit = { _, _ -> },
    onSaveLists: (List<String>) -> Unit
) {
    var selectedLists by remember {
        mutableStateOf(conversation.labelIds.toSet())
    }
    var showAddCustomList by remember { mutableStateOf(false) }
    var newListName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(WhatsAppListColors.first()) }
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 38.dp, height = 4.dp),
                shape = RoundedCornerShape(2.dp),
                color = Color(0xFFD1D5DB)
            ) {}
        },
        modifier = Modifier
            .navigationBarsPadding()
            .testTag("chat_lists_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Lists",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = "Add ${conversation.title} to custom lists",
                        fontSize = 12.5.sp,
                        color = Color(0xFF64748B)
                    )
                }
                TextButton(
                    onClick = {
                        onSaveLists(selectedLists.toList())
                        onDismissRequest()
                        Toast.makeText(context, "Lists updated", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("chat_lists_done_button")
                ) {
                    Text(
                        text = "Done",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0160E3)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Display all available labels from labels (both defaults and custom)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp)
            ) {
                items(labels) { label ->
                    val isSelected = selectedLists.contains(label.id)
                    val labelColor = parseHexColorSafely(label.colorHex, Color(0xFF0160E3))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                selectedLists = if (isSelected) {
                                    selectedLists - label.id
                                } else {
                                    selectedLists + label.id
                                }
                            }
                            .padding(vertical = 10.dp, horizontal = 8.dp)
                            .testTag("chat_list_item_${label.id}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(
                                    width = 1.8.dp,
                                    color = if (isSelected) Color(0xFF0160E3) else Color(0xFFCBD5E1),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .background(if (isSelected) Color(0xFF0160E3) else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        // Color indicator dot
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(labelColor)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = label.name,
                            fontSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = Color(0xFF111827)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!showAddCustomList) {
                TextButton(
                    onClick = { showAddCustomList = true },
                    modifier = Modifier
                        .align(Alignment.Start)
                        .testTag("chat_new_list_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color(0xFF0160E3),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "New List",
                        color = Color(0xFF0160E3),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Create New List",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newListName,
                        onValueChange = { newListName = it },
                        placeholder = { Text("List name (e.g. Work, Family)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0160E3),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_list_name_input")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Choose colour",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    // Palette row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(WhatsAppListColors.take(10)) { colorHex ->
                            val c = parseHexColorSafely(colorHex)
                            val isColorChosen = selectedColor.equals(colorHex, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .clickable { selectedColor = colorHex }
                                    .border(
                                        width = if (isColorChosen) 2.5.dp else 0.dp,
                                        color = if (isColorChosen) Color(0xFF1E293B) else Color.Transparent,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isColorChosen) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddCustomList = false }) {
                            Text("Cancel", color = Color(0xFF64748B))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newListName.isNotBlank()) {
                                    val trimmedName = newListName.trim()
                                    onCreateNewLabel(trimmedName, selectedColor)
                                    Toast.makeText(context, "List \"$trimmedName\" created", Toast.LENGTH_SHORT).show()
                                    newListName = ""
                                    showAddCustomList = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0160E3)),
                            modifier = Modifier.testTag("create_new_list_confirm_button")
                        ) {
                            Text("Add List")
                        }
                    }
                }
            }
        }
    }
}
