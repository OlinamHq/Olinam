package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.core.content.ContextCompat
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppTab
import com.example.model.Conversation
import com.example.model.FilterCategory
import com.example.ui.ChatViewModel
import com.example.ui.components.ConversationListItem
import com.example.ui.components.CreateLabelBottomSheet
import com.example.ui.components.LabelChipsRow
import com.example.ui.components.OlinamBottomNavBar
import com.example.ui.components.OlinamSearchBar
import com.example.ui.components.OlinamTopBar
import com.example.ui.theme.OlinamPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ChatViewModel
) {
    val context = LocalContext.current
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val conversations by viewModel.conversations.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val labels by viewModel.labels.collectAsState()
    val selectedLabelId by viewModel.selectedLabelId.collectAsState()
    val activeConversationId by viewModel.activeConversationId.collectAsState()
    val activeMessages by viewModel.activeConversationMessages.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val firebaseConnected by viewModel.firebaseConnected.collectAsState()
    val activeCallState by viewModel.activeCallState.collectAsState()
    val registeredPhoneNumbers by viewModel.registeredPhoneNumbers.collectAsState()

    var showNewChatDialog by remember { mutableStateOf(false) }
    var showSelectContactScreen by remember { mutableStateOf(false) }
    var showSelectCallContactScreen by remember { mutableStateOf(false) }
    var showNewGroupScreen by remember { mutableStateOf(false) }
    var showNewContactScreen by remember { mutableStateOf(false) }
    var showQrScreen by remember { mutableStateOf(false) }
    var showInviteDialog by remember { mutableStateOf(false) }
    var showCameraScreen by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showSecurityDialog by remember { mutableStateOf(false) }
    var showCreateLabelSheet by remember { mutableStateOf(false) }
    var showContactProfileScreen by remember { mutableStateOf(false) }
    val labelSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val webrtcCallState by viewModel.webRtcCallState.collectAsState()

    // SMS & Contacts Permission handling for auto-syncing real phone messages & spam filtering
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val smsGranted = perms[Manifest.permission.READ_SMS] == true
        if (smsGranted) {
            viewModel.syncDeviceSms(context)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.restoreSavedSession(context)
    }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            val hasSmsPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_SMS
            ) == PackageManager.PERMISSION_GRANTED

            if (hasSmsPermission) {
                viewModel.syncDeviceSms(context)
            } else {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.READ_SMS,
                        Manifest.permission.READ_CONTACTS
                    )
                )
            }
        }
    }

    // 0. WebRTC Audio & Video Call Screen (only when active)
    if (webrtcCallState != com.example.webrtc.model.WebRtcCallState.IDLE && viewModel.webRtcCallManager != null) {
        com.example.webrtc.ui.WebRtcActiveCallScreen(callManager = viewModel.webRtcCallManager!!)
        return
    }

    // Active Audio or Video Call Screen (real live call)
    if (activeCallState != null) {
        ActiveCallScreen(viewModel = viewModel)
        return
    }

    // 1. Initial Login / Signup screen check
    if (!isLoggedIn) {
        LoginScreen(viewModel = viewModel)
        return
    }

    // 2. Fullscreen Camera Screen (matching Screenshots 6 and 3)
    if (showCameraScreen) {
        CameraScreen(
            onDismiss = { showCameraScreen = false },
            onSendStatus = { caption, mediaUri ->
                showCameraScreen = false
                viewModel.addStory(caption, mediaUri, isImage = true)
                viewModel.setTab(AppTab.STORIES)
            }
        )
        return
    }

    // 3. Select Call Contact Screen (matching Screenshots 1 and 2)
    if (showSelectCallContactScreen) {
        SelectCallContactScreen(
            viewModel = viewModel,
            onBackClick = { showSelectCallContactScreen = false },
            onNewContactClick = {
                showSelectCallContactScreen = false
                showNewContactScreen = true
            },
            onKeypadClick = {
                showSelectCallContactScreen = false
                viewModel.setTab(AppTab.CALLS)
            }
        )
        return
    }

    // Short link QR Screen (Screenshots 3 & 5)
    if (showQrScreen) {
        ShortLinkQrScreen(
            currentUser = currentUser,
            onBackClick = { showQrScreen = false }
        )
        return
    }

    // New Contact Screen (Screenshot 6)
    if (showNewContactScreen) {
        NewContactScreen(
            onBackClick = { showNewContactScreen = false },
            onQrClick = {
                showNewContactScreen = false
                showQrScreen = true
            },
            onContactSaved = { name, phone ->
                showNewContactScreen = false
                val convId = viewModel.getOrCreateConversationForContact(name, phone, isSms = false)
                viewModel.openConversation(convId, context)
            }
        )
        return
    }

    // New Group Screen (Screenshots 2 & 4)
    if (showNewGroupScreen) {
        NewGroupScreen(
            onBackClick = { showNewGroupScreen = false },
            onGroupCreated = { name, participants ->
                showNewGroupScreen = false
                viewModel.createNewChat(name, isGroup = true)
            }
        )
        return
    }

    // If contact profile is open
    val selectedConv = conversations.find { it.id == activeConversationId }
    if (selectedConv != null && showContactProfileScreen) {
        ContactProfileScreen(
            conversation = selectedConv,
            currentUser = currentUser,
            safetyNumber = viewModel.getSafetyNumber(selectedConv.id),
            onBackClick = { showContactProfileScreen = false },
            onCallClick = { type ->
                showContactProfileScreen = false
                viewModel.startWebRtcCall(
                    context = context,
                    contactName = selectedConv.title,
                    contactPhone = selectedConv.phoneNumber ?: selectedConv.title,
                    contactAvatar = null,
                    callType = type
                )
            }
        )
        return
    }

    // If a conversation is opened, show ChatDetailScreen
    if (selectedConv != null) {
        ChatDetailScreen(
            conversation = selectedConv,
            messages = activeMessages,
            currentUserId = currentUser.id,
            safetyNumber = viewModel.getSafetyNumber(selectedConv.id),
            onBackClick = {
                showContactProfileScreen = false
                viewModel.closeConversation()
            },
            onSendMessage = { text -> viewModel.sendMessage(selectedConv.id, text) },
            onSendDirectSms = { text ->
                val destPhone = selectedConv.phoneNumber ?: selectedConv.title.filter { it.isDigit() || it == '+' }
                viewModel.sendDirectSms(context, selectedConv.id, destPhone, text)
            },
            onCallClick = { type ->
                viewModel.startWebRtcCall(
                    context = context,
                    contactName = selectedConv.title,
                    contactPhone = selectedConv.phoneNumber ?: selectedConv.title,
                    contactAvatar = null,
                    callType = type
                )
            },
            onVerifyAppStatus = { viewModel.verifyAndUpdateConversationAppStatus(selectedConv.id) },
            onLoadMessages = { viewModel.loadMessagesForConversation(selectedConv.id, context) },
            onOpenProfile = { showContactProfileScreen = true }
        )
        return
    }

    // WhatsApp-style Select Contact Screen
    if (showSelectContactScreen) {
        SelectContactScreen(
            userPhoneNumber = currentUser.phoneNumber,
            registeredPhoneNumbers = registeredPhoneNumbers,
            onBackClick = { showSelectContactScreen = false },
            onNewGroupClick = {
                showSelectContactScreen = false
                showNewGroupScreen = true
            },
            onNewContactClick = {
                showSelectContactScreen = false
                showNewContactScreen = true
            },
            onQrClick = {
                showSelectContactScreen = false
                showQrScreen = true
            },
            onContactSelected = { name, phone, hasOlinam ->
                showSelectContactScreen = false
                val convId = viewModel.getOrCreateConversationForContact(name, phone, isSms = !hasOlinam)
                viewModel.openConversation(convId, context)
            },
            onInviteContactClick = {
                showSelectContactScreen = false
                showInviteDialog = true
            }
        )
        return
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen_root"),
        containerColor = Color.White,
        topBar = {
            if (currentTab == AppTab.CHATS) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                ) {
                    OlinamTopBar(
                        onCameraClick = { showCameraScreen = true },
                        onMenuNewGroup = { showNewGroupScreen = true },
                        onMenuProfile = { showProfileDialog = true },
                        onMenuSecurity = { showSecurityDialog = true },
                        onMenuSettings = { showProfileDialog = true },
                        onSyncSms = { viewModel.syncDeviceSms(context) },
                        onOpenSms = { viewModel.selectLabel("sms") },
                        onLogout = { viewModel.logout(context) }
                    )
                    OlinamSearchBar(
                        query = searchQuery,
                        onQueryChange = { viewModel.setSearchQuery(it) }
                    )
                    // WhatsApp-style Compact Custom Labels / Folders Chips Row
                    LabelChipsRow(
                        labels = labels,
                        selectedLabelId = selectedLabelId,
                        onLabelClick = { label ->
                            if (label.id == "groups") {
                                val hasGroups = conversations.any { it.isGroup }
                                if (!hasGroups) {
                                    showNewGroupScreen = true
                                } else {
                                    viewModel.selectLabel(label.id)
                                }
                            } else {
                                viewModel.selectLabel(label.id)
                            }
                        },
                        onAddLabelClick = { showCreateLabelSheet = true }
                    )
                }
            }
        },
        bottomBar = {
            OlinamBottomNavBar(
                currentTab = currentTab,
                onTabSelected = { viewModel.setTab(it) }
            )
        },
        floatingActionButton = {
            if (currentTab == AppTab.CHATS) {
                // Large '+' FAB in Royal Blue (OlinamPrimary) requested by user
                FloatingActionButton(
                    onClick = { showSelectContactScreen = true },
                    shape = RoundedCornerShape(18.dp),
                    containerColor = OlinamPrimary,
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(4.dp),
                    modifier = Modifier
                        .size(56.dp)
                        .testTag("new_chat_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Chat",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {
            when (currentTab) {
                AppTab.CHATS -> {
                    ChatsTabContent(
                        conversations = conversations,
                        selectedLabelId = selectedLabelId,
                        onConversationClick = { conv -> viewModel.openConversation(conv.id, context) },
                        onStartChatClick = { showSelectContactScreen = true },
                        onNewGroupClick = { showNewGroupScreen = true }
                    )
                }
                AppTab.STORIES -> {
                    StoriesScreen(
                        viewModel = viewModel,
                        onCameraClick = { showCameraScreen = true }
                    )
                }
                AppTab.OJ_AI -> {
                    OjAiScreen(viewModel = viewModel)
                }
                AppTab.CALLS -> {
                    CallsScreen(
                        viewModel = viewModel,
                        onOpenSelectContact = { showSelectCallContactScreen = true }
                    )
                }
            }
        }
    }

    // Swipeable Bottom Sheet for Creating New Label / Chat Folder
    if (showCreateLabelSheet) {
        CreateLabelBottomSheet(
            sheetState = labelSheetState,
            conversations = conversations,
            onDismissRequest = { showCreateLabelSheet = false },
            onSaveLabel = { name, colorHex, chatIds ->
                viewModel.createLabel(name, colorHex, chatIds)
                showCreateLabelSheet = false
            }
        )
    }

    // New Chat / Group Creation Dialog
    if (showNewChatDialog) {
        NewChatModal(
            onDismiss = { showNewChatDialog = false },
            onCreate = { name, isGroup, msg ->
                viewModel.createNewChat(name, isGroup, msg)
                showNewChatDialog = false
            }
        )
    }

    // Invite Friends Dialog
    if (showInviteDialog) {
        InviteFriendsModal(
            currentUser = currentUser,
            onDismiss = { showInviteDialog = false }
        )
    }

    // Profile Modal
    if (showProfileDialog) {
        ProfileModal(
            currentUser = currentUser,
            firebaseConnected = firebaseConnected,
            onDismiss = { showProfileDialog = false },
            onSave = { name, status, phone ->
                viewModel.updateProfile(name, status, phone)
                showProfileDialog = false
            }
        )
    }

    // End-to-End Encryption Security Overview Modal
    if (showSecurityDialog) {
        SecurityOverviewModal(
            currentUser = currentUser,
            onDismiss = { showSecurityDialog = false }
        )
    }
}

@Composable
fun ChatsTabContent(
    conversations: List<Conversation>,
    selectedLabelId: String = "all",
    onConversationClick: (Conversation) -> Unit,
    onStartChatClick: () -> Unit,
    onNewGroupClick: () -> Unit = {}
) {
    if (conversations.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = when (selectedLabelId) {
                        "sms" -> Color(0xFFDCFCE7)
                        "groups" -> Color(0xFFDCFCE7)
                        else -> Color(0xFFE2EDFC)
                    },
                    modifier = Modifier.size(76.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when (selectedLabelId) {
                                "sms" -> Icons.Default.Chat
                                "groups" -> Icons.Default.Groups
                                else -> Icons.Default.Chat
                            },
                            contentDescription = null,
                            tint = when (selectedLabelId) {
                                "sms" -> Color(0xFF00A884)
                                "groups" -> Color(0xFF00A884)
                                else -> Color(0xFF0160E3)
                            },
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = when (selectedLabelId) {
                        "sms" -> "No SMS Messages"
                        "groups" -> "No Groups Yet"
                        "direct" -> "No Direct Chats"
                        else -> "No Chats Yet"
                    },
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = when (selectedLabelId) {
                        "sms" -> "All your phone text messages (SMS) will appear here in this dedicated label."
                        "groups" -> "Create a group to chat with family, friends, or teams."
                        "direct" -> "Only app-to-app chats appear here."
                        else -> "Start a conversation or invite friends on Olinam."
                    },
                    fontSize = 13.5.sp,
                    color = Color(0xFF6B7280),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 19.sp
                )

                if (selectedLabelId == "groups") {
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = onNewGroupClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A884)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("empty_state_new_group_button")
                    ) {
                        Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("New Group", fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = onStartChatClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("empty_state_start_chat_button")
                    ) {
                        Icon(Icons.Default.AddComment, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Chatting", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            if (selectedLabelId == "groups") {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onNewGroupClick)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .testTag("action_new_group_row"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00A884)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GroupAdd,
                                contentDescription = "New group",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "New group",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                            Text(
                                text = "Create a new group chat",
                                fontSize = 12.5.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }
                }
            }
            items(conversations, key = { it.id }) { conv ->
                ConversationListItem(
                    conversation = conv,
                    onClick = { onConversationClick(conv) }
                )
            }
        }
    }
}

@Composable
fun NewChatModal(
    onDismiss: () -> Unit,
    onCreate: (name: String, isGroup: Boolean, initialMsg: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var isGroup by remember { mutableStateOf(false) }
    var initialMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isGroup) "Create New Group" else "Start New Chat",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Group Chat", fontWeight = FontWeight.Medium)
                    Switch(
                        checked = isGroup,
                        onCheckedChange = { isGroup = it }
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (isGroup) "Group Name" else "Contact Name or Mobile (+91...)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_chat_name_input")
                )

                OutlinedTextField(
                    value = initialMessage,
                    onValueChange = { initialMessage = it },
                    label = { Text("Initial message (optional)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_chat_msg_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onCreate(name.trim(), isGroup, initialMessage.trim())
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = OlinamPrimary),
                modifier = Modifier.testTag("confirm_create_chat_button")
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

@Composable
fun InviteFriendsModal(
    currentUser: com.example.model.UserProfile,
    onDismiss: () -> Unit
) {
    var copied by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Invite friends to Olinam", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Share the Olinam download link with your contacts to enjoy private, end-to-end encrypted messaging.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "https://olinam.app/join?ref=${currentUser.phoneNumber.ifBlank { currentUser.id }}",
                        modifier = Modifier.padding(12.dp),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        color = OlinamPrimary
                    )
                }
                if (copied) {
                    Text(
                        text = "Invite link copied to clipboard!",
                        fontSize = 12.sp,
                        color = OlinamPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { copied = true },
                colors = ButtonDefaults.buttonColors(containerColor = OlinamPrimary)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share Link")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
fun CameraPreviewModal(
    onDismiss: () -> Unit,
    onCapture: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Olinam Camera", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Camera viewfinder",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Live HD Viewfinder",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onCapture,
                colors = ButtonDefaults.buttonColors(containerColor = OlinamPrimary)
            ) {
                Text("Capture & Post Story")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ProfileModal(
    currentUser: com.example.model.UserProfile,
    firebaseConnected: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, status: String, phone: String) -> Unit
) {
    var name by remember { mutableStateOf(currentUser.name) }
    var status by remember { mutableStateOf(currentUser.statusMessage) }
    var phone by remember { mutableStateOf(currentUser.phoneNumber) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("My Profile", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Firebase backend status badge
                Surface(
                    color = if (firebaseConnected) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (firebaseConnected) Color(0xFF2E7D32) else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (firebaseConnected) "Firebase Connected: olinam-90d42" else "Local Sync Active",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (firebaseConnected) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Your Name") },
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

                OutlinedTextField(
                    value = status,
                    onValueChange = { status = it },
                    label = { Text("Status") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, status, phone) },
                colors = ButtonDefaults.buttonColors(containerColor = OlinamPrimary)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun SecurityOverviewModal(
    currentUser: com.example.model.UserProfile,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = OlinamPrimary,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text("End-to-End Encryption", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Olinam secures all chats and calls with genuine client-side AES-256-GCM encryption. Keys are never sent to external servers in plaintext.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Your Identity Key Fingerprint:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = currentUser.safetyNumber,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.5.sp,
                        modifier = Modifier.padding(10.dp),
                        lineHeight = 16.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = OlinamPrimary)
            ) {
                Text("Got It")
            }
        }
    )
}
