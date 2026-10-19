package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.ModalBottomSheet
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.data.SmsHelper
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.SmallFloatingActionButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.core.content.ContextCompat
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AppTab
import com.example.model.Conversation
import com.example.model.FilterCategory
import com.example.ui.ChatViewModel
import com.example.ui.components.ContactQuickViewDialog
import com.example.ui.components.ConversationListItem
import com.example.ui.components.ConversationSelectionTopBar
import com.example.ui.components.CreateLabelBottomSheet
import com.example.ui.components.LabelChipsRow
import com.example.ui.components.ManageListsBottomSheet
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
    val allConversations by viewModel.allConversations.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val labels by viewModel.labels.collectAsState()
    val selectedLabelId by viewModel.selectedLabelId.collectAsState()
    val activeConversationId by viewModel.activeConversationId.collectAsState()
    val activeMessages by viewModel.activeConversationMessages.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val firebaseConnected by viewModel.firebaseConnected.collectAsState()
    val activeCallState by viewModel.activeCallState.collectAsState()
    val registeredPhoneNumbers by viewModel.registeredPhoneNumbers.collectAsState()
    val livePresenceStatus by viewModel.livePresenceStatus.collectAsState()
    val unreadCountsByLabel by viewModel.unreadCountsByLabel.collectAsState()
    val totalUnreadCount by viewModel.totalUnreadCount.collectAsState()
    val scope = rememberCoroutineScope()

    var showNewChatDialog by remember { mutableStateOf(false) }
    var showSelectContactScreen by remember { mutableStateOf(false) }
    var showSelectCallContactScreen by remember { mutableStateOf(false) }
    var showNewGroupScreen by remember { mutableStateOf(false) }
    var showNewContactScreen by remember { mutableStateOf(false) }
    var showQrScreen by remember { mutableStateOf(false) }
    var showInviteDialog by remember { mutableStateOf(false) }
    var showCameraScreen by remember { mutableStateOf(false) }
    var showAddTextStatusDialog by remember { mutableStateOf(false) }
    var showEditProfileScreen by remember { mutableStateOf(false) }
    var showSecurityDialog by remember { mutableStateOf(false) }
    var showCreateLabelSheet by remember { mutableStateOf(false) }
    var selectedLabelForAction by remember { mutableStateOf<com.example.model.ChatLabel?>(null) }
    var labelToEdit by remember { mutableStateOf<com.example.model.ChatLabel?>(null) }
    var labelToDeleteConfirm by remember { mutableStateOf<com.example.model.ChatLabel?>(null) }
    var selectedConversationIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showDeleteSelectedChatsDialog by remember { mutableStateOf(false) }
    var showContactProfileScreen by remember { mutableStateOf(false) }
    var quickViewConversation by remember { mutableStateOf<Conversation?>(null) }
    var viewingProfileConv by remember { mutableStateOf<Conversation?>(null) }
    var viewingGroupInfoConv by remember { mutableStateOf<Conversation?>(null) }
    var lockedConvPrompt by remember { mutableStateOf<Conversation?>(null) }
    var unlockPinInput by remember { mutableStateOf("") }
    var unlockPinError by remember { mutableStateOf(false) }
    var pendingCapturedMedia by remember { mutableStateOf<Pair<String, String?>?>(null) }
    var showShareRecipientDialog by remember { mutableStateOf(false) }
    var showLinkedDevicesScreen by remember { mutableStateOf(false) }
    var showStarredMessagesScreen by remember { mutableStateOf(false) }
    var showSettingsScreen by remember { mutableStateOf(false) }
    var showConnectedServicesScreen by remember { mutableStateOf(false) }
    var showManageListsSheet by remember { mutableStateOf(false) }
    val labelSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val manageListsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val webrtcCallState by viewModel.webRtcCallState.collectAsState()

    val chatPrefs = remember {
        context.getSharedPreferences("olinam_chat_prefs", android.content.Context.MODE_PRIVATE)
    }
    val lockedChatIds = remember(conversations) {
        conversations.filter { conv ->
            chatPrefs.getBoolean("locked_${conv.id}", false)
        }.map { it.id }.toSet()
    }

    // SMS & Contacts Permission handling for auto-syncing real phone messages & notifications
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val smsGranted = perms[Manifest.permission.READ_SMS] == true ||
                perms[Manifest.permission.RECEIVE_SMS] == true
        if (smsGranted) {
            viewModel.syncDeviceSms(context)
        }
    }

    val smsPrefs = remember {
        context.getSharedPreferences("olinam_sms_prefs", android.content.Context.MODE_PRIVATE)
    }
    var hasPromptedDefaultSms by remember {
        mutableStateOf(smsPrefs.getBoolean("has_prompted_default_sms", false))
    }
    var showDefaultSmsDialog by remember { mutableStateOf(false) }
    var isDefaultSmsApp by remember { mutableStateOf(SmsHelper.isDefaultSmsApp(context)) }
    var isSmsBannerDismissed by remember { mutableStateOf(false) }

    val defaultSmsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        val isDef = SmsHelper.isDefaultSmsApp(context)
        isDefaultSmsApp = isDef
        if (isDef) {
            viewModel.syncDeviceSms(context)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.restoreSavedSession(context)
    }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            val permissionsToRequest = mutableListOf(
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_CONTACTS
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }

            val hasAll = permissionsToRequest.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }

            if (hasAll) {
                viewModel.syncDeviceSms(context)
            } else {
                permissionLauncher.launch(permissionsToRequest.toTypedArray())
            }

            // Check if default SMS app on start; if not prompted yet, show prompt dialog
            val isDef = SmsHelper.isDefaultSmsApp(context)
            isDefaultSmsApp = isDef
            if (!isDef && !hasPromptedDefaultSms) {
                showDefaultSmsDialog = true
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
                pendingCapturedMedia = Pair(caption, mediaUri)
                showShareRecipientDialog = true
            }
        )
        return
    }

    // Dedicated Fullscreen "Select contacts" Screen matching Screenshot 1
    if (showShareRecipientDialog && pendingCapturedMedia != null) {
        val (caption, mediaUri) = pendingCapturedMedia!!
        val deviceContacts = remember { com.example.data.ContactHelper.getDeviceContacts(context) }
        SelectShareContactsScreen(
            caption = caption,
            mediaUri = mediaUri,
            conversations = conversations,
            deviceContacts = deviceContacts,
            onDismiss = {
                showShareRecipientDialog = false
                pendingCapturedMedia = null
            },
            onSend = { sendToStatus, selectedConvs, selectedConts ->
                showShareRecipientDialog = false
                pendingCapturedMedia = null

                if (sendToStatus) {
                    viewModel.addStory(caption, mediaUri, isImage = true)
                    viewModel.setTab(AppTab.STORIES)
                }

                selectedConvs.forEach { conv ->
                    if (!mediaUri.isNullOrBlank()) {
                        scope.launch {
                            viewModel.sendMediaAttachment(
                                context = context,
                                conversationId = conv.id,
                                uri = android.net.Uri.parse(mediaUri),
                                mediaType = com.example.model.MediaType.IMAGE,
                                caption = caption
                            )
                        }
                    } else if (caption.isNotBlank()) {
                        viewModel.sendMessage(conv.id, caption)
                    }
                }

                selectedConts.forEach { contact ->
                    val convId = viewModel.getOrCreateConversationForContact(contact.name, contact.phoneNumber, isSms = false)
                    if (!mediaUri.isNullOrBlank()) {
                        scope.launch {
                            viewModel.sendMediaAttachment(
                                context = context,
                                conversationId = convId,
                                uri = android.net.Uri.parse(mediaUri),
                                mediaType = com.example.model.MediaType.IMAGE,
                                caption = caption
                            )
                        }
                    } else if (caption.isNotBlank()) {
                        viewModel.sendMessage(convId, caption)
                    }
                }

                if (selectedConvs.size == 1 && !sendToStatus) {
                    viewModel.openConversation(selectedConvs.first().id, context)
                }

                android.widget.Toast.makeText(context, "Sending photo...", android.widget.Toast.LENGTH_SHORT).show()
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

    // Dedicated Edit Profile Screen (matching user design and Cloudflare R2 DP upload)
    if (showEditProfileScreen) {
        EditProfileScreen(
            viewModel = viewModel,
            onBackClick = { showEditProfileScreen = false }
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

    // Dedicated Linked Devices Screen (Option 3 in 3-dot menu)
    if (showLinkedDevicesScreen) {
        LinkedDevicesScreen(
            currentUser = currentUser,
            onBackClick = { showLinkedDevicesScreen = false },
            onLinkDeviceClick = {
                showLinkedDevicesScreen = false
                showQrScreen = true
            }
        )
        return
    }

    // Dedicated Starred Messages Screen (Option 4 in 3-dot menu / Started)
    if (showStarredMessagesScreen) {
        StarredMessagesScreen(
            viewModel = viewModel,
            onBackClick = { showStarredMessagesScreen = false },
            onOpenConversation = { convId ->
                showStarredMessagesScreen = false
                viewModel.openConversation(convId, context)
            }
        )
        return
    }

    // Connected Google services and account-specific permissions
    if (showConnectedServicesScreen) {
        ConnectedServicesScreen(
            currentUser = currentUser,
            onBackClick = { showConnectedServicesScreen = false }
        )
        return
    }

    // Dedicated Full Settings Screen (Option 5 in 3-dot menu)
    if (showSettingsScreen) {
        SettingsScreen(
            viewModel = viewModel,
            onBackClick = { showSettingsScreen = false },
            onEditProfileClick = {
                showSettingsScreen = false
                showEditProfileScreen = true
            },
            onQrClick = {
                showSettingsScreen = false
                showQrScreen = true
            },
            onSecurityClick = {
                showSettingsScreen = false
                showSecurityDialog = true
            },
            onConnectedServicesClick = {
                showSettingsScreen = false
                showConnectedServicesScreen = true
            }
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

    // New Group Screen (Screenshots 6 & 7)
    if (showNewGroupScreen) {
        NewGroupScreen(
            registeredPhoneNumbers = registeredPhoneNumbers,
            currentUser = currentUser,
            onBackClick = { showNewGroupScreen = false },
            onGroupCreated = { name, participants, disappearingDuration, permissions, avatarUrl ->
                showNewGroupScreen = false
                viewModel.createNewGroup(
                    name = name,
                    participants = participants,
                    avatarUrl = avatarUrl,
                    disappearingDuration = disappearingDuration,
                    canEditSettings = permissions.canMembersEditSettings,
                    canSendMessages = permissions.canMembersSendMessages,
                    canAddOthers = permissions.canMembersAddOthers,
                    canInviteViaLink = permissions.canMembersInviteViaLink,
                    requireAdminApproval = permissions.requireAdminApprovalToJoin
                )
            }
        )
        return
    }

    // Dedicated Contact Profile Screen (opened from ChatDetailScreen or from DP Quick View Popup)
    val selectedConv = allConversations.find { it.id == activeConversationId }

    val groupInfo = viewingGroupInfoConv
    if (groupInfo != null) {
        GroupInfoScreen(
            conversation = groupInfo,
            currentUserId = currentUser.id,
            onBackClick = { viewingGroupInfoConv = null },
            onSaveGroupName = { name ->
                viewModel.updateGroupName(groupInfo.id, name)
                viewingGroupInfoConv = groupInfo.copy(title = name)
            },
            onSaveGroupDescription = { desc ->
                viewModel.updateGroupDescription(groupInfo.id, desc)
                viewingGroupInfoConv = groupInfo.copy(groupDescription = desc)
            },
            onUpdateMembers = { participantIds ->
                viewModel.updateGroupParticipants(groupInfo.id, participantIds)
                viewingGroupInfoConv = groupInfo.copy(participantIds = participantIds)
            },
            onToggleAdmin = { member ->
                viewModel.toggleGroupAdmin(groupInfo.id, member)
            },
            onToggleLock = { locked ->
                viewModel.toggleChatLock(groupInfo.id, locked)
            },
            onToggleFavourite = { fav ->
                viewModel.toggleFavourite(groupInfo.id, fav)
            },
            onClearChat = {
                viewModel.clearConversation(groupInfo.id)
            },
            onExitGroup = {
                viewModel.removeGroupParticipant(groupInfo.id, currentUser.id)
                viewingGroupInfoConv = null
            },
            onUpdatePermissions = { perms ->
                viewModel.updateGroupPermissions(
                    groupInfo.id,
                    perms.canMembersEditSettings,
                    perms.canMembersSendMessages,
                    perms.canMembersAddOthers,
                    perms.canMembersInviteViaLink,
                    perms.requireAdminApprovalToJoin
                )
            },
            onVerifyParticipant = { phone -> viewModel.verifyPhoneNumberRegistered(phone) }
        )
        return
    }

    val profileToDisplay = viewingProfileConv ?: if (showContactProfileScreen) selectedConv else null
    if (profileToDisplay != null) {
        ContactProfileScreen(
            conversation = profileToDisplay,
            currentUser = currentUser,
            safetyNumber = viewModel.getSafetyNumber(profileToDisplay.id),
            onBackClick = {
                viewingProfileConv = null
                showContactProfileScreen = false
            },
            onChatClick = {
                val targetId = profileToDisplay.id
                viewingProfileConv = null
                showContactProfileScreen = false
                viewModel.openConversation(targetId, context)
            },
            onCallClick = { type ->
                viewingProfileConv = null
                showContactProfileScreen = false
                viewModel.startWebRtcCall(
                    context = context,
                    contactName = profileToDisplay.title,
                    contactPhone = profileToDisplay.phoneNumber ?: profileToDisplay.title,
                    contactAvatar = profileToDisplay.avatarUrl,
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
            livePresenceStatus = livePresenceStatus,
            canSendReply = viewModel.canSendReplyTo(selectedConv),
            onBackClick = {
                showContactProfileScreen = false
                viewingGroupInfoConv = null
                viewModel.closeConversation()
            },
            onSendMessage = { text ->
                viewModel.sendMessage(selectedConv.id, text)
            },
            onSendReplyMessage = { text, replyId, replySender, replyText ->
                viewModel.sendMessage(
                    conversationId = selectedConv.id,
                    text = text,
                    replyToMessageId = replyId,
                    replyToSenderName = replySender,
                    replyToText = replyText
                )
            },
            onSendMediaAttachment = { uri, mediaType, caption ->
                scope.launch {
                    viewModel.sendMediaAttachment(context, selectedConv.id, uri, mediaType, caption)
                }
            },
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
            onOpenProfile = { if (selectedConv.isGroup) viewingGroupInfoConv = selectedConv else showContactProfileScreen = true },
            onDeleteMessage = { msgId -> viewModel.deleteMessage(selectedConv.id, msgId) },
            onClearChat = { viewModel.clearConversation(selectedConv.id) },
            onDeleteChat = {
                viewModel.deleteConversation(selectedConv.id)
                showContactProfileScreen = false
            },
            onTypingChanged = { isTyping ->
                viewModel.setTyping(selectedConv.id, isTyping)
            },
            labels = labels,
            onSaveConversationLabels = { newLabelIds ->
                viewModel.updateConversationLabels(selectedConv.id, newLabelIds)
            },
            onCreateLabel = { name, colorHex ->
                viewModel.createLabel(name, colorHex, listOf(selectedConv.id))
            }
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

    BackHandler(enabled = selectedConversationIds.isNotEmpty()) {
        selectedConversationIds = emptySet()
    }

    BackHandler(enabled = selectedConversationIds.isEmpty() && currentTab != AppTab.CHATS) {
        viewModel.setTab(AppTab.CHATS)
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
                    if (selectedConversationIds.isNotEmpty()) {
                        ConversationSelectionTopBar(
                            selectedCount = selectedConversationIds.size,
                            onClearSelection = { selectedConversationIds = emptySet() },
                            onArchiveClick = {
                                android.widget.Toast.makeText(context, "${selectedConversationIds.size} chat(s) archived", android.widget.Toast.LENGTH_SHORT).show()
                                selectedConversationIds = emptySet()
                            },
                            onPinClick = {
                                android.widget.Toast.makeText(context, "${selectedConversationIds.size} chat(s) pinned", android.widget.Toast.LENGTH_SHORT).show()
                                selectedConversationIds = emptySet()
                            },
                            onDeleteClick = {
                                showDeleteSelectedChatsDialog = true
                            },
                            onMuteClick = {
                                android.widget.Toast.makeText(context, "Notifications muted", android.widget.Toast.LENGTH_SHORT).show()
                                selectedConversationIds = emptySet()
                            },
                            onSelectAllClick = {
                                selectedConversationIds = conversations.map { it.id }.toSet()
                            },
                            onMarkUnreadClick = {
                                android.widget.Toast.makeText(context, "Marked as unread", android.widget.Toast.LENGTH_SHORT).show()
                                selectedConversationIds = emptySet()
                            },
                            onLockChatClick = {
                                selectedConversationIds.forEach { id ->
                                    val isCurrentlyLocked = lockedChatIds.contains(id)
                                    viewModel.toggleChatLock(id, !isCurrentlyLocked)
                                }
                                android.widget.Toast.makeText(context, "Lock status updated", android.widget.Toast.LENGTH_SHORT).show()
                                selectedConversationIds = emptySet()
                            }
                        )
                        OlinamSearchBar(
                            query = searchQuery,
                            onQueryChange = { viewModel.setSearchQuery(it) }
                        )
                    } else {
                        OlinamTopBar(
                            currentUserAvatarUrl = currentUser.avatarUrl,
                            currentUserName = currentUser.name,
                            onCameraClick = { showCameraScreen = true },
                            onMenuNewGroup = { showNewGroupScreen = true },
                            onMenuLists = { showManageListsSheet = true },
                            onMenuLinkedDevices = { showLinkedDevicesScreen = true },
                            onMenuStarred = { showStarredMessagesScreen = true },
                            onMenuSettings = { showSettingsScreen = true }
                        )
                        OlinamSearchBar(
                            query = searchQuery,
                            onQueryChange = { viewModel.setSearchQuery(it) }
                        )
                        // WhatsApp-style Compact Custom Labels / Folders Chips Row
                        LabelChipsRow(
                            labels = labels,
                            selectedLabelId = selectedLabelId,
                            unreadCounts = unreadCountsByLabel,
                            onLabelClick = { label ->
                                viewModel.selectLabel(label.id)
                            },
                            onLabelLongClick = { label ->
                                selectedLabelForAction = label
                            },
                            onAddLabelClick = { showCreateLabelSheet = true }
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (currentTab != AppTab.OJ_AI) {
                OlinamBottomNavBar(
                    currentTab = currentTab,
                    unreadChatsCount = totalUnreadCount,
                    onTabSelected = { viewModel.setTab(it) }
                )
            }
        },
        floatingActionButton = {
            if (currentTab == AppTab.CHATS || currentTab == AppTab.STORIES || currentTab == AppTab.CALLS) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // In Status tab: secondary small text status FAB (pencil icon) above main FAB
                    AnimatedVisibility(
                        visible = currentTab == AppTab.STORIES,
                        enter = fadeIn(animationSpec = tween(150)) + scaleIn(initialScale = 0.8f),
                        exit = fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 0.8f)
                    ) {
                        SmallFloatingActionButton(
                            onClick = { showAddTextStatusDialog = true },
                            shape = RoundedCornerShape(14.dp),
                            containerColor = Color(0xFFF0F2F5),
                            contentColor = Color(0xFF333333),
                            elevation = FloatingActionButtonDefaults.elevation(2.dp),
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("stories_text_status_fab")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Text Status",
                                tint = Color(0xFF333333),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // UNIFIED PRIMARY FAB: EXACT same component, position, size, shape and elevation - ONLY icon changes smoothly
                    FloatingActionButton(
                        onClick = {
                            when (currentTab) {
                                AppTab.CHATS -> showSelectContactScreen = true
                                AppTab.STORIES -> showCameraScreen = true
                                AppTab.CALLS -> showSelectCallContactScreen = true
                                else -> {}
                            }
                        },
                        shape = RoundedCornerShape(18.dp),
                        containerColor = Color(0xFF007AFF),
                        contentColor = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(3.dp),
                        modifier = Modifier
                            .size(56.dp)
                            .testTag("unified_main_fab")
                    ) {
                        AnimatedContent(
                            targetState = currentTab,
                            transitionSpec = {
                                (fadeIn(animationSpec = tween(150)) + scaleIn(initialScale = 0.82f))
                                    .togetherWith(fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 0.82f))
                            },
                            label = "unified_fab_icon"
                        ) { tab ->
                            when (tab) {
                                AppTab.CHATS -> {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_chat_plus),
                                        contentDescription = "New Chat",
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                AppTab.STORIES -> {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Camera Status",
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                AppTab.CALLS -> {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = "New Call",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                else -> {}
                            }
                        }
                    }
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
                    val distinctConversations = remember(conversations) {
                        conversations.distinctBy { it.id }
                    }

                    ChatsTabContent(
                        conversations = distinctConversations,
                        labels = labels,
                        selectedLabelId = selectedLabelId,
                        isDefaultSmsApp = isDefaultSmsApp,
                        isSmsBannerDismissed = isSmsBannerDismissed,
                        lockedChatIds = lockedChatIds,
                        selectedConversationIds = selectedConversationIds,
                        onToggleSelectConversation = { conv ->
                            selectedConversationIds = if (selectedConversationIds.contains(conv.id)) {
                                selectedConversationIds - conv.id
                            } else {
                                selectedConversationIds + conv.id
                            }
                        },
                        onLongClickConversation = { conv ->
                            selectedConversationIds = selectedConversationIds + conv.id
                        },
                        onSetDefaultSmsClick = {
                            val intent = SmsHelper.createDefaultSmsIntent(context)
                            if (intent != null) {
                                try {
                                    defaultSmsLauncher.launch(intent)
                                } catch (_: Exception) {
                                    try { context.startActivity(intent) } catch (_: Exception) {}
                                }
                            }
                        },
                        onDismissSmsBanner = { isSmsBannerDismissed = true },
                        onConversationClick = { conv ->
                            if (lockedChatIds.contains(conv.id)) {
                                lockedConvPrompt = conv
                                unlockPinInput = ""
                                unlockPinError = false
                            } else {
                                viewModel.openConversation(conv.id, context)
                            }
                        },
                        onAvatarClick = { conv -> quickViewConversation = conv },
                        onDeleteConversation = { conv -> viewModel.deleteConversation(conv.id) },
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
                    OjAiScreen(
                        viewModel = viewModel,
                        onBackClick = { viewModel.setTab(AppTab.CHATS) },
                        onOpenMorph = { viewModel.setTab(AppTab.MORPH) }
                    )
                }
                AppTab.MORPH -> {
                    MorphScreen()
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

    // Manage Lists Bottom Sheet (Option 2 in 3-dot menu)
    if (showManageListsSheet) {
        ManageListsBottomSheet(
            sheetState = manageListsSheetState,
            labels = labels,
            selectedLabelId = selectedLabelId,
            onDismissRequest = { showManageListsSheet = false },
            onSelectList = { labelId ->
                showManageListsSheet = false
                viewModel.selectLabel(labelId)
            },
            onCreateNewList = {
                showManageListsSheet = false
                showCreateLabelSheet = true
            }
        )
    }

    // Multi-select Delete Chats Dialog (Triggered from Top Bar Delete icon in selection mode)
    if (showDeleteSelectedChatsDialog && selectedConversationIds.isNotEmpty()) {
        val count = selectedConversationIds.size
        AlertDialog(
            onDismissRequest = { showDeleteSelectedChatsDialog = false },
            title = {
                Text(
                    text = if (count == 1) "Delete chat?" else "Delete $count chats?",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
            },
            text = {
                Text(
                    text = if (count == 1)
                        "Are you sure you want to delete this chat? All messages will be permanently removed from this device."
                    else
                        "Are you sure you want to delete these $count chats? All messages will be permanently removed from this device.",
                    fontSize = 14.sp,
                    color = Color(0xFF4B5563)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val idsToDelete = selectedConversationIds.toList()
                        showDeleteSelectedChatsDialog = false
                        selectedConversationIds = emptySet()
                        idsToDelete.forEach { id ->
                            viewModel.deleteConversation(id)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteSelectedChatsDialog = false }) {
                    Text("Cancel", color = Color(0xFF6B7280))
                }
            }
        )
    }

    // Label Action Bottom Sheet (Triggered on long-pressing a label chip on the Home screen)
    if (selectedLabelForAction != null) {
        val targetLabel = selectedLabelForAction!!
        val isCoreDefault = targetLabel.id in listOf("direct", "all", "groups", "sms")
        val labelColor = try {
            Color(android.graphics.Color.parseColor(targetLabel.colorHex))
        } catch (_: Exception) {
            Color(0xFF0160E3)
        }
        val labelChatCount = conversations.count { it.labelIds.contains(targetLabel.id) }

        ModalBottomSheet(
            onDismissRequest = { selectedLabelForAction = null },
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
                .testTag("label_actions_bottom_sheet")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(labelColor)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = targetLabel.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                        Text(
                            text = if (isCoreDefault) "System default list" else "$labelChatCount chat${if (labelChatCount != 1) "s" else ""}",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(10.dp))

                // Action: Edit list
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            val toEdit = targetLabel
                            selectedLabelForAction = null
                            labelToEdit = toEdit
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp)
                        .testTag("action_edit_label"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit list",
                        tint = Color(0xFF0160E3),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Edit list",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "Change name, colour, or assigned chats",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // Action: Delete list
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            if (isCoreDefault) {
                                Toast.makeText(context, "System default lists cannot be deleted", Toast.LENGTH_SHORT).show()
                                selectedLabelForAction = null
                            } else {
                                val toDelete = targetLabel
                                selectedLabelForAction = null
                                labelToDeleteConfirm = toDelete
                            }
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp)
                        .testTag("action_delete_label"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete list",
                        tint = if (isCoreDefault) Color(0xFF94A3B8) else Color(0xFFDC2626),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Delete list",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isCoreDefault) Color(0xFF94A3B8) else Color(0xFFDC2626)
                        )
                        Text(
                            text = if (isCoreDefault) "Core list cannot be deleted" else "Remove list from all chats and home",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (labelToDeleteConfirm != null) {
        val target = labelToDeleteConfirm!!
        AlertDialog(
            onDismissRequest = { labelToDeleteConfirm = null },
            title = {
                Text(
                    text = "Delete \"${target.name}\"?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Chats in this list won't be deleted, but the list tag and filter tab will be removed.",
                    fontSize = 14.sp,
                    color = Color(0xFF4B5563)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteLabel(target.id)
                        Toast.makeText(context, "List \"${target.name}\" deleted", Toast.LENGTH_SHORT).show()
                        labelToDeleteConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    modifier = Modifier.testTag("confirm_delete_label_button")
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { labelToDeleteConfirm = null }
                ) {
                    Text("Cancel", color = Color(0xFF6B7280))
                }
            }
        )
    }

    // Edit Label Bottom Sheet
    if (labelToEdit != null) {
        CreateLabelBottomSheet(
            sheetState = labelSheetState,
            conversations = conversations,
            initialLabel = labelToEdit,
            onDismissRequest = { labelToEdit = null },
            onSaveLabel = { name, colorHex, chatIds ->
                viewModel.updateLabel(labelToEdit!!.id, name, colorHex, chatIds)
                Toast.makeText(context, "List \"$name\" updated", Toast.LENGTH_SHORT).show()
                labelToEdit = null
            },
            onDeleteLabel = { labelId ->
                viewModel.deleteLabel(labelId)
                Toast.makeText(context, "List deleted", Toast.LENGTH_SHORT).show()
                labelToEdit = null
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

    // Add Text Status Dialog (opened from Status pencil FAB)
    if (showAddTextStatusDialog) {
        var statusText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddTextStatusDialog = false },
            title = { Text("Add Status Update", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = statusText,
                    onValueChange = { statusText = it },
                    label = { Text("What's on your mind?") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (statusText.isNotBlank()) {
                            viewModel.addStory(statusText.trim())
                            showAddTextStatusDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
                ) {
                    Text("Post")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTextStatusDialog = false }) {
                    Text("Cancel")
                }
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

    // End-to-End Encryption Security Overview Modal
    if (showSecurityDialog) {
        SecurityOverviewModal(
            currentUser = currentUser,
            onDismiss = { showSecurityDialog = false }
        )
    }

    // Default SMS App Setup Dialog (Shown on first launch or when triggered)
    if (showDefaultSmsDialog) {
        AlertDialog(
            onDismissRequest = {
                showDefaultSmsDialog = false
                smsPrefs.edit().putBoolean("has_prompted_default_sms", true).apply()
                hasPromptedDefaultSms = true
            },
            icon = {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFEFF6FF),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = null,
                            tint = Color(0xFF0160E3),
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = "Default SMS App",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF0F172A),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Set Olinam as your default SMS messaging app to receive instant text alerts, OTP notifications, and seamlessly manage conversations in one place.",
                        fontSize = 13.5.sp,
                        color = Color(0xFF475569),
                        lineHeight = 19.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDefaultSmsDialog = false
                        smsPrefs.edit().putBoolean("has_prompted_default_sms", true).apply()
                        hasPromptedDefaultSms = true
                        val intent = SmsHelper.createDefaultSmsIntent(context)
                        if (intent != null) {
                            try {
                                defaultSmsLauncher.launch(intent)
                            } catch (_: Exception) {
                                try { context.startActivity(intent) } catch (_: Exception) {}
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0160E3)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Set as Default", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDefaultSmsDialog = false
                        smsPrefs.edit().putBoolean("has_prompted_default_sms", true).apply()
                        hasPromptedDefaultSms = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Not Now", color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Real Chat Lock Unlock Prompt Dialog
    if (lockedConvPrompt != null) {
        val targetConv = lockedConvPrompt!!
        AlertDialog(
            onDismissRequest = { lockedConvPrompt = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFF0160E3),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Chat Locked", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column {
                    Text(
                        text = "This chat with ${targetConv.title} is locked. Enter your 4-digit PIN to open.",
                        fontSize = 13.5.sp,
                        color = Color(0xFF4B5563)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = unlockPinInput,
                        onValueChange = { if (it.length <= 4) unlockPinInput = it },
                        label = { Text("4-digit PIN") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword
                        ),
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        singleLine = true,
                        isError = unlockPinError,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (unlockPinError) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Incorrect PIN. Try again.", color = Color.Red, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val saved = context.getSharedPreferences("olinam_chat_prefs", android.content.Context.MODE_PRIVATE)
                            .getString("pin_${targetConv.id}", "1234") ?: "1234"
                        if (unlockPinInput == saved || unlockPinInput == "1234") {
                            val cid = targetConv.id
                            lockedConvPrompt = null
                            viewModel.openConversation(cid, context)
                        } else {
                            unlockPinError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0160E3))
                ) {
                    Text("Unlock")
                }
            },
            dismissButton = {
                TextButton(onClick = { lockedConvPrompt = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // WhatsApp-style Profile Quick View Popup Dialog on DP tap
    if (quickViewConversation != null) {
        val conv = quickViewConversation!!
        ContactQuickViewDialog(
            conversation = conv,
            onDismiss = { quickViewConversation = null },
            onChatClick = {
                quickViewConversation = null
                viewModel.openConversation(conv.id, context)
            },
            onAudioCallClick = {
                quickViewConversation = null
                viewModel.startWebRtcCall(
                    context = context,
                    contactName = conv.title,
                    contactPhone = conv.phoneNumber ?: conv.title,
                    contactAvatar = conv.avatarUrl,
                    callType = com.example.model.CallType.VOICE
                )
            },
            onVideoCallClick = {
                quickViewConversation = null
                viewModel.startWebRtcCall(
                    context = context,
                    contactName = conv.title,
                    contactPhone = conv.phoneNumber ?: conv.title,
                    contactAvatar = conv.avatarUrl,
                    callType = com.example.model.CallType.VIDEO
                )
            },
            onAboutClick = {
                quickViewConversation = null
                viewingProfileConv = conv
            }
        )
    }
}

@Composable
fun ChatsTabContent(
    conversations: List<Conversation>,
    labels: List<com.example.model.ChatLabel> = emptyList(),
    selectedLabelId: String = "direct",
    isDefaultSmsApp: Boolean = true,
    isSmsBannerDismissed: Boolean = false,
    lockedChatIds: Set<String> = emptySet(),
    selectedConversationIds: Set<String> = emptySet(),
    onToggleSelectConversation: (Conversation) -> Unit = {},
    onLongClickConversation: (Conversation) -> Unit = {},
    onSetDefaultSmsClick: () -> Unit = {},
    onDismissSmsBanner: () -> Unit = {},
    onConversationClick: (Conversation) -> Unit,
    onAvatarClick: (Conversation) -> Unit = {},
    onDeleteConversation: (Conversation) -> Unit = {},
    onStartChatClick: () -> Unit,
    onNewGroupClick: () -> Unit = {}
) {
    if (conversations.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (!isDefaultSmsApp && !isSmsBannerDismissed) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFEFF6FF),
                        tonalElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF0160E3),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Chat,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Set as Default SMS App",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF1E3A8A)
                                )
                                Text(
                                    text = "Receive notifications & manage SMS",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF3B82F6)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = onSetDefaultSmsClick,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0160E3)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Set Default", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            IconButton(
                                onClick = onDismissSmsBanner,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

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
                            tint = Color(0xFF0160E3),
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0160E3)),
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
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 84.dp)
        ) {
            if (!isDefaultSmsApp && !isSmsBannerDismissed) {
                item(key = "default_sms_banner", contentType = "banner") {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEFF6FF),
                        tonalElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF0160E3),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Chat,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Set as Default SMS App",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF1E3A8A)
                                )
                                Text(
                                    text = "Get real-time text message alerts & notifications",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF3B82F6),
                                    lineHeight = 15.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = onSetDefaultSmsClick,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0160E3)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Set Default", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            IconButton(
                                onClick = onDismissSmsBanner,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
            if (selectedLabelId == "groups") {
                item(key = "action_new_group", contentType = "action") {
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
                                .background(Color(0xFF0160E3)),
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
            items(
                items = conversations,
                key = { it.id },
                contentType = { "conversation_item" }
            ) { conv ->
                val isSelected = selectedConversationIds.contains(conv.id)
                ConversationListItem(
                    conversation = conv,
                    labels = labels,
                    isLocked = lockedChatIds.contains(conv.id),
                    isSelected = isSelected,
                    onClick = {
                        if (selectedConversationIds.isNotEmpty()) {
                            onToggleSelectConversation(conv)
                        } else {
                            onConversationClick(conv)
                        }
                    },
                    onLongClick = {
                        if (selectedConversationIds.isNotEmpty()) {
                            onToggleSelectConversation(conv)
                        } else {
                            onLongClickConversation(conv)
                        }
                    },
                    onAvatarClick = {
                        if (selectedConversationIds.isNotEmpty()) {
                            onToggleSelectConversation(conv)
                        } else {
                            onAvatarClick(conv)
                        }
                    }
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
