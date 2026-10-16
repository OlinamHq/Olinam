package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Conversation
import com.example.model.GroupPermissions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupInfoScreen(
    conversation: Conversation,
    currentUserId: String,
    onBackClick: () -> Unit,
    onSaveGroupName: (String) -> Unit,
    onSaveGroupDescription: (String) -> Unit = {},
    onUpdateMembers: (List<String>) -> Unit,
    onToggleAdmin: (String) -> Unit = {},
    onToggleLock: (Boolean) -> Unit = {},
    onToggleFavourite: (Boolean) -> Unit = {},
    onClearChat: () -> Unit = {},
    onExitGroup: () -> Unit = {},
    onUpdatePermissions: (GroupPermissions) -> Unit = {},
    onVerifyParticipant: suspend (String) -> Boolean = { true }
) {
    BackHandler { onBackClick() }

    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Members, 1: Media, 2: Settings

    var showEditNameDialog by remember { mutableStateOf(false) }
    var editNameInput by remember { mutableStateOf(conversation.title) }

    var showEditDescriptionDialog by remember { mutableStateOf(false) }
    var editDescriptionInput by remember { mutableStateOf(conversation.groupDescription ?: "") }

    var showAddMemberDialog by remember { mutableStateOf(false) }
    var newMemberPhoneInput by remember { mutableStateOf("") }

    var selectedMemberForAction by remember { mutableStateOf<String?>(null) }
    var showRemoveMemberConfirm by remember { mutableStateOf<String?>(null) }

    var showClearChatConfirm by remember { mutableStateOf(false) }
    var showExitGroupConfirm by remember { mutableStateOf(false) }
    var showReportConfirm by remember { mutableStateOf(false) }

    var isChatLocked by remember(conversation.isChatLocked) { mutableStateOf(conversation.isChatLocked) }
    var isFavourite by remember(conversation.isFavourite) { mutableStateOf(conversation.isFavourite) }

    var topMenuExpanded by remember { mutableStateOf(false) }
    var showGroupLinkScreen by remember { mutableStateOf(false) }
    var showGroupPermissionsScreen by remember { mutableStateOf(false) }

    var memberSearchQuery by remember { mutableStateOf("") }
    var isSearchingMembers by remember { mutableStateOf(false) }

    val members = remember(conversation.participantIds) {
        conversation.participantIds.filter { it.isNotBlank() }.distinct()
    }
    val adminIds = remember(conversation.groupAdminIds) {
        conversation.groupAdminIds.toSet()
    }

    val filteredMembers = remember(members, memberSearchQuery) {
        if (memberSearchQuery.isBlank()) members
        else members.filter { it.contains(memberSearchQuery, ignoreCase = true) }
    }

    if (showGroupLinkScreen) {
        GroupLinkScreen(
            conversation = conversation,
            onBackClick = { showGroupLinkScreen = false }
        )
        return
    }

    if (showGroupPermissionsScreen) {
        GroupPermissionsScreen(
            initialPermissions = GroupPermissions(
                canMembersEditSettings = conversation.canMembersEditSettings,
                canMembersSendMessages = conversation.canMembersSendMessages,
                canMembersAddOthers = conversation.canMembersAddOthers,
                canMembersInviteViaLink = conversation.canMembersInviteViaLink,
                requireAdminApprovalToJoin = conversation.requireAdminApprovalToJoin
            ),
            onBackClick = { showGroupPermissionsScreen = false },
            onPermissionsChanged = { perms ->
                onUpdatePermissions(perms)
            }
        )
        return
    }

    fun exportChat() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Chat export: ${conversation.title}")
            putExtra(
                Intent.EXTRA_TEXT,
                "Chat history for group \"${conversation.title}\" on Olinam.\nMessages are end-to-end encrypted."
            )
        }
        context.startActivity(Intent.createChooser(intent, "Export chat"))
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE2EDFC)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = conversation.title.take(1).uppercase(),
                                color = Color(0xFF0160E3),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = conversation.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF111827),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF111827)
                        )
                    }
                },
                actions = {
                    // Gallery / Media icon (Screenshot 1)
                    IconButton(onClick = { selectedTab = 1 }) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Media",
                            tint = Color(0xFF111827)
                        )
                    }
                    // QR Code icon (Screenshot 1)
                    IconButton(onClick = { showGroupLinkScreen = true }) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "Group link & QR",
                            tint = Color(0xFF111827)
                        )
                    }
                    // 3-dot overflow menu (Screenshot 1)
                    Box {
                        IconButton(onClick = { topMenuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options",
                                tint = Color(0xFF111827)
                            )
                        }
                        DropdownMenu(
                            expanded = topMenuExpanded,
                            onDismissRequest = { topMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Add members", fontSize = 15.sp) },
                                onClick = {
                                    topMenuExpanded = false
                                    showAddMemberDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Edit name", fontSize = 15.sp) },
                                onClick = {
                                    topMenuExpanded = false
                                    editNameInput = conversation.title
                                    showEditNameDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Export chat", fontSize = 15.sp) },
                                onClick = {
                                    topMenuExpanded = false
                                    exportChat()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Group Hero Details (Center avatar, title, subtitle)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE2EDFC)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = conversation.title.take(1).uppercase(),
                        color = Color(0xFF0160E3),
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = conversation.title,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Group · ${members.size} members",
                    fontSize = 13.5.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Action Pills (Voice chat, Share, Search) matching Screenshot 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GroupActionPill(
                        icon = Icons.Default.VolumeUp,
                        label = "Voice chat",
                        onClick = {
                            Toast.makeText(context, "Starting voice chat...", Toast.LENGTH_SHORT).show()
                        }
                    )
                    Spacer(modifier = Modifier.width(28.dp))
                    GroupActionPill(
                        icon = Icons.Default.Share,
                        label = "Share",
                        onClick = { showGroupLinkScreen = true }
                    )
                    Spacer(modifier = Modifier.width(28.dp))
                    GroupActionPill(
                        icon = Icons.Default.Search,
                        label = "Search",
                        onClick = {
                            selectedTab = 0
                            isSearchingMembers = true
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // "Add to lists" Row matching Screenshot 1
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            Toast.makeText(context, "Added to chat lists", Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Lists",
                        tint = Color(0xFF54656F),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Add to lists",
                        fontSize = 15.5.sp,
                        color = Color(0xFF111827)
                    )
                }
            }

            // WhatsApp 3-Tab Row matching Screenshot 1, 2, 4
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color(0xFF111827),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFF111827),
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "Members",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 15.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "Media",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 15.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            text = "Settings",
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 15.sp
                        )
                    }
                )
            }

            when (selectedTab) {
                0 -> {
                    // Members Tab (Screenshots 1 & 5)
                    MembersTabContent(
                        members = filteredMembers,
                        totalMembersCount = members.size,
                        adminIds = adminIds,
                        currentUserId = currentUserId,
                        isSearching = isSearchingMembers,
                        searchQuery = memberSearchQuery,
                        onSearchQueryChange = { memberSearchQuery = it },
                        onToggleSearch = {
                            isSearchingMembers = !isSearchingMembers
                            if (!isSearchingMembers) memberSearchQuery = ""
                        },
                        onAddMemberClick = { showAddMemberDialog = true },
                        onMemberClick = { member ->
                            selectedMemberForAction = member
                        }
                    )
                }
                1 -> {
                    // Media Tab (Screenshot 2)
                    MediaTabContent(
                        conversation = conversation,
                        onExportChat = { exportChat() },
                        onClearChat = { showClearChatConfirm = true }
                    )
                }
                2 -> {
                    // Settings Tab (Screenshot 4)
                    SettingsTabContent(
                        conversation = conversation,
                        isChatLocked = isChatLocked,
                        onChatLockedChange = {
                            isChatLocked = it
                            onToggleLock(it)
                        },
                        isFavourite = isFavourite,
                        onFavouriteChange = {
                            isFavourite = it
                            onToggleFavourite(it)
                        },
                        onOpenPermissions = { showGroupPermissionsScreen = true },
                        onExportChat = { exportChat() },
                        onClearChat = { showClearChatConfirm = true },
                        onExitGroup = { showExitGroupConfirm = true },
                        onReportGroup = { showReportConfirm = true }
                    )
                }
            }
        }
    }

    // Edit Name Dialog
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Edit group name", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = editNameInput,
                    onValueChange = { editNameInput = it.take(30) },
                    singleLine = true,
                    placeholder = { Text("Group name") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clean = editNameInput.trim()
                        if (clean.isNotBlank()) {
                            onSaveGroupName(clean)
                            showEditNameDialog = false
                            Toast.makeText(context, "Group name updated", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            }
        )
    }

    // Edit Description Dialog
    if (showEditDescriptionDialog) {
        AlertDialog(
            onDismissRequest = { showEditDescriptionDialog = false },
            title = { Text("Group description", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = editDescriptionInput,
                    onValueChange = { editDescriptionInput = it },
                    placeholder = { Text("Add group description...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveGroupDescription(editDescriptionInput.trim())
                        showEditDescriptionDialog = false
                        Toast.makeText(context, "Description updated", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDescriptionDialog = false }) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            }
        )
    }

    // Add Member Dialog
    if (showAddMemberDialog) {
        AlertDialog(
            onDismissRequest = { showAddMemberDialog = false },
            title = { Text("Add Member", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Enter phone number or contact ID to add to this group:",
                        fontSize = 13.5.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newMemberPhoneInput,
                        onValueChange = { newMemberPhoneInput = it },
                        singleLine = true,
                        placeholder = { Text("+91 98765 43210") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clean = newMemberPhoneInput.trim()
                        if (clean.isNotBlank()) {
                            val updated = (members + clean).distinct()
                            onUpdateMembers(updated)
                            newMemberPhoneInput = ""
                            showAddMemberDialog = false
                            Toast.makeText(context, "Member added", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMemberDialog = false }) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            }
        )
    }

    // Member Action Bottom Sheet / Dialog
    val targetMember = selectedMemberForAction
    if (targetMember != null) {
        val isTargetAdmin = adminIds.contains(targetMember)
        AlertDialog(
            onDismissRequest = { selectedMemberForAction = null },
            title = { Text(targetMember, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedMemberForAction = null
                                onToggleAdmin(targetMember)
                                Toast.makeText(
                                    context,
                                    if (isTargetAdmin) "Dismissed as admin" else "Made group admin",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Color(0xFF007AFF)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(if (isTargetAdmin) "Dismiss as admin" else "Make group admin")
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedMemberForAction = null
                                showRemoveMemberConfirm = targetMember
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.RemoveCircleOutline,
                            contentDescription = null,
                            tint = Color(0xFFDC2626)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Remove from group", color = Color(0xFFDC2626))
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedMemberForAction = null }) {
                    Text("Close", color = Color(0xFF64748B))
                }
            }
        )
    }

    // Remove Member Confirmation
    val removeTarget = showRemoveMemberConfirm
    if (removeTarget != null) {
        AlertDialog(
            onDismissRequest = { showRemoveMemberConfirm = null },
            title = { Text("Remove from group?") },
            text = { Text("Are you sure you want to remove $removeTarget from ${conversation.title}?") },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = members.filter { it != removeTarget }
                        onUpdateMembers(updated)
                        showRemoveMemberConfirm = null
                        Toast.makeText(context, "$removeTarget removed", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveMemberConfirm = null }) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            }
        )
    }

    // Clear Chat Confirm
    if (showClearChatConfirm) {
        AlertDialog(
            onDismissRequest = { showClearChatConfirm = false },
            title = { Text("Clear this chat?") },
            text = { Text("Messages will only be cleared from this device.") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearChat()
                        showClearChatConfirm = false
                        Toast.makeText(context, "Chat cleared", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Clear chat")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearChatConfirm = false }) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            }
        )
    }

    // Exit Group Confirm
    if (showExitGroupConfirm) {
        AlertDialog(
            onDismissRequest = { showExitGroupConfirm = false },
            title = { Text("Exit \"${conversation.title}\" group?") },
            text = { Text("Only group admins will be notified that you left the group.") },
            confirmButton = {
                Button(
                    onClick = {
                        showExitGroupConfirm = false
                        onExitGroup()
                        Toast.makeText(context, "You left the group", Toast.LENGTH_SHORT).show()
                        onBackClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Exit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitGroupConfirm = false }) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            }
        )
    }

    // Report Group Confirm
    if (showReportConfirm) {
        AlertDialog(
            onDismissRequest = { showReportConfirm = false },
            title = { Text("Report this group?") },
            text = { Text("The last 5 messages from this group will be forwarded to Olinam security team.") },
            confirmButton = {
                Button(
                    onClick = {
                        showReportConfirm = false
                        Toast.makeText(context, "Report submitted. Thank you.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Report")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportConfirm = false }) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            }
        )
    }
}

@Composable
private fun GroupActionPill(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFF111827),
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF54656F),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun MembersTabContent(
    members: List<String>,
    totalMembersCount: Int,
    adminIds: Set<String>,
    currentUserId: String,
    isSearching: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onToggleSearch: () -> Unit,
    onAddMemberClick: () -> Unit,
    onMemberClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        // Members header with search icon (Screenshot 1 & 5)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSearching) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Search members...") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Text(
                        text = "$totalMembersCount members",
                        fontSize = 15.sp,
                        color = Color(0xFF54656F),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                }

                IconButton(onClick = onToggleSearch) {
                    Icon(
                        imageVector = if (isSearching) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = "Search members",
                        tint = Color(0xFF54656F)
                    )
                }
            }
        }

        // "+ Add members" row (Screenshot 1 & 5)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onAddMemberClick)
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF111827)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "Add members",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Add members",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF111827)
                )
            }
        }

        // "You" row (Screenshot 1 & 5)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE2EDFC)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "You",
                        color = Color(0xFF0160E3),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "You",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF111827)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Add member tag",
                        fontSize = 13.sp,
                        color = Color(0xFF007AFF),
                        fontWeight = FontWeight.Medium
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF1F5F9)
                ) {
                    Text(
                        text = "Group Admin",
                        fontSize = 12.sp,
                        color = Color(0xFF475569),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Other members list (Screenshot 5)
        items(members.filter { it != currentUserId && it != "You" }) { member ->
            val isAdmin = adminIds.contains(member)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onMemberClick(member) }
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEDE9FE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF7C3AED),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = member,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF111827),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (isAdmin) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = "Group Admin",
                            fontSize = 12.sp,
                            color = Color(0xFF475569),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaTabContent(
    conversation: Conversation,
    onExportChat: () -> Unit,
    onClearChat: () -> Unit
) {
    val context = LocalContext.current
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text(
                text = "Settings",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF54656F),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
            )
        }

        item {
            GroupSettingRow(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                subtitle = "Highlights",
                onClick = {
                    Toast.makeText(context, "Notification preferences: Highlights", Toast.LENGTH_SHORT).show()
                }
            )
        }

        item {
            GroupSettingRow(
                icon = Icons.Default.PhotoLibrary,
                title = "Media visibility",
                subtitle = null,
                onClick = {
                    Toast.makeText(context, "Media saved to device gallery", Toast.LENGTH_SHORT).show()
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            GroupSettingRow(
                icon = Icons.Default.Lock,
                title = "Encryption",
                subtitle = "Messages and calls are end-to-end encrypted. Learn more",
                onClick = {
                    Toast.makeText(context, "Verified end-to-end encrypted chat", Toast.LENGTH_SHORT).show()
                }
            )
        }

        item {
            GroupSettingRow(
                icon = Icons.Default.Security,
                title = "Advanced chat privacy",
                subtitle = "Off",
                onClick = {}
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            GroupSettingRow(
                icon = Icons.Default.Upload,
                title = "Export chat",
                subtitle = null,
                onClick = onExportChat
            )
        }

        item {
            GroupSettingRow(
                icon = Icons.Default.RemoveCircleOutline,
                title = "Clear chat",
                subtitle = null,
                textColor = Color(0xFFDC2626),
                onClick = onClearChat
            )
        }
    }
}

@Composable
private fun SettingsTabContent(
    conversation: Conversation,
    isChatLocked: Boolean,
    onChatLockedChange: (Boolean) -> Unit,
    isFavourite: Boolean,
    onFavouriteChange: (Boolean) -> Unit,
    onOpenPermissions: () -> Unit,
    onExportChat: () -> Unit,
    onClearChat: () -> Unit,
    onExitGroup: () -> Unit,
    onReportGroup: () -> Unit
) {
    val context = LocalContext.current
    val createdDateFormatted = remember(conversation.groupCreatedAt) {
        val sdf = SimpleDateFormat("dd/MM/yy, h:mm a", Locale.getDefault())
        sdf.format(Date(conversation.groupCreatedAt))
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        // Group permissions row
        item {
            GroupSettingRow(
                icon = Icons.Default.Settings,
                title = "Group permissions",
                subtitle = "Control what members and admins can do in this group",
                onClick = onOpenPermissions
            )
        }

        // Encryption row (Screenshot 4)
        item {
            GroupSettingRow(
                icon = Icons.Default.Lock,
                title = "Encryption",
                subtitle = "Messages and calls are end-to-end encrypted. Learn more",
                onClick = {
                    Toast.makeText(context, "End-to-end encryption active", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Chat lock row with switch toggle (Screenshot 4)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFF54656F),
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(18.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Chat lock",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF111827)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Lock and hide this chat on this device.",
                        fontSize = 13.5.sp,
                        color = Color(0xFF64748B)
                    )
                }

                Switch(
                    checked = isChatLocked,
                    onCheckedChange = onChatLockedChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF111827),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFCBD5E1)
                    )
                )
            }
        }

        // Advanced chat privacy (Screenshot 4)
        item {
            GroupSettingRow(
                icon = Icons.Default.Security,
                title = "Advanced chat privacy",
                subtitle = "Off",
                onClick = {}
            )
        }

        // Add to Favourites (Screenshot 4)
        item {
            GroupSettingRow(
                icon = if (isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                title = if (isFavourite) "Added to Favourites" else "Add to Favourites",
                subtitle = null,
                iconTint = if (isFavourite) Color(0xFFDC2626) else Color(0xFF54656F),
                onClick = {
                    val newVal = !isFavourite
                    onFavouriteChange(newVal)
                    Toast.makeText(context, if (newVal) "Added to Favourites" else "Removed from Favourites", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Export chat (Screenshot 4)
        item {
            GroupSettingRow(
                icon = Icons.Default.Upload,
                title = "Export chat",
                subtitle = null,
                onClick = onExportChat
            )
        }

        // Clear chat (Screenshot 4)
        item {
            GroupSettingRow(
                icon = Icons.Default.RemoveCircleOutline,
                title = "Clear chat",
                subtitle = null,
                textColor = Color(0xFFDC2626),
                iconTint = Color(0xFFDC2626),
                onClick = onClearChat
            )
        }

        // Exit group (Screenshot 4)
        item {
            GroupSettingRow(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                title = "Exit group",
                subtitle = null,
                textColor = Color(0xFFDC2626),
                iconTint = Color(0xFFDC2626),
                onClick = onExitGroup
            )
        }

        // Report group (Screenshot 4)
        item {
            GroupSettingRow(
                icon = Icons.Default.ThumbDown,
                title = "Report group",
                subtitle = null,
                textColor = Color(0xFFDC2626),
                iconTint = Color(0xFFDC2626),
                onClick = onReportGroup
            )
        }

        // Footer timestamp (Screenshot 4)
        item {
            val createdByText = conversation.groupCreatedBy ?: "Admin"
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Created by $createdByText, $createdDateFormatted",
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun GroupSettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    textColor: Color = Color(0xFF111827),
    iconTint: Color = Color(0xFF54656F),
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(18.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = textColor
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 13.5.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}
