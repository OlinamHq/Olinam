package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import com.example.model.ChatLabel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.DonutLarge
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AppTab
import com.example.model.Conversation
import com.example.model.FilterCategory
import com.example.model.MessageStatus
import com.example.ui.theme.OlinamAlertYellow
import com.example.ui.theme.OlinamCheckBlue
import com.example.ui.theme.OlinamInviteBlue
import com.example.ui.theme.OlinamOnlineGreen
import com.example.ui.theme.OlinamPrimary
import com.example.ui.theme.OlinamPrimaryContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OlinamTopBar(
    currentUserAvatarUrl: String? = null,
    currentUserName: String = "",
    onCameraClick: () -> Unit,
    onMenuNewGroup: () -> Unit,
    onMenuLists: () -> Unit,
    onMenuLinkedDevices: () -> Unit,
    onMenuStarred: () -> Unit,
    onMenuSettings: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 18.dp, end = 10.dp, top = 8.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Olinam branding logo image (clean without any trailing dot)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.testTag("app_title_olinam")
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_olinam_logo_text),
                contentDescription = "Olinam",
                modifier = Modifier
                    .height(28.dp)
                    .widthIn(max = 130.dp),
                contentScale = ContentScale.Fit
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onCameraClick,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("camera_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Camera",
                    tint = Color(0xFF111B21),
                    modifier = Modifier.size(24.dp)
                )
            }

            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("more_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = Color(0xFF111B21),
                        modifier = Modifier.size(24.dp)
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    // 1. New group
                    DropdownMenuItem(
                        text = { Text("New group", fontWeight = FontWeight.Medium, fontSize = 15.sp) },
                        onClick = {
                            menuExpanded = false
                            onMenuNewGroup()
                        },
                        modifier = Modifier.testTag("menu_new_group")
                    )
                    // 2. Lists
                    DropdownMenuItem(
                        text = { Text("Lists", fontWeight = FontWeight.Medium, fontSize = 15.sp) },
                        onClick = {
                            menuExpanded = false
                            onMenuLists()
                        },
                        modifier = Modifier.testTag("menu_lists")
                    )
                    // 3. Linked device
                    DropdownMenuItem(
                        text = { Text("Linked device", fontWeight = FontWeight.Medium, fontSize = 15.sp) },
                        onClick = {
                            menuExpanded = false
                            onMenuLinkedDevices()
                        },
                        modifier = Modifier.testTag("menu_linked_device")
                    )
                    // 4. Starred messages (Started)
                    DropdownMenuItem(
                        text = { Text("Starred messages", fontWeight = FontWeight.Medium, fontSize = 15.sp) },
                        onClick = {
                            menuExpanded = false
                            onMenuStarred()
                        },
                        modifier = Modifier.testTag("menu_started").testTag("menu_starred")
                    )
                    // 5. Settings
                    DropdownMenuItem(
                        text = { Text("Settings", fontWeight = FontWeight.Medium, fontSize = 15.sp) },
                        onClick = {
                            menuExpanded = false
                            onMenuSettings()
                        },
                        modifier = Modifier.testTag("menu_setting").testTag("menu_settings")
                    )
                }
            }
        }
    }
}

/**
 * Contextual Action Bar shown when 1 or more conversations are selected.
 * Matches WhatsApp style selection bar (Image 1):
 * Back Arrow, Selection Count, Archive, Pin, Delete, Mute, 3-dots Menu.
 */
@Composable
fun ConversationSelectionTopBar(
    selectedCount: Int,
    onClearSelection: () -> Unit,
    onArchiveClick: () -> Unit = {},
    onPinClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onMuteClick: () -> Unit = {},
    onSelectAllClick: () -> Unit = {},
    onMarkUnreadClick: () -> Unit = {},
    onLockChatClick: () -> Unit = {}
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(56.dp)
            .background(Color.White)
            .padding(horizontal = 4.dp)
            .testTag("conversation_selection_top_bar"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onClearSelection,
            modifier = Modifier.testTag("selection_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Cancel Selection",
                tint = Color(0xFF1E293B)
            )
        }

        Text(
            text = selectedCount.toString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A),
            modifier = Modifier
                .padding(start = 12.dp)
                .testTag("selection_count_text")
        )

        Spacer(modifier = Modifier.weight(1f))

        IconButton(
            onClick = onArchiveClick,
            modifier = Modifier.testTag("selection_archive_button")
        ) {
            Icon(
                imageVector = Icons.Default.Archive,
                contentDescription = "Archive",
                tint = Color(0xFF1E293B)
            )
        }

        IconButton(
            onClick = onPinClick,
            modifier = Modifier.testTag("selection_pin_button")
        ) {
            Icon(
                imageVector = Icons.Default.PushPin,
                contentDescription = "Pin",
                tint = Color(0xFF1E293B)
            )
        }

        IconButton(
            onClick = onDeleteClick,
            modifier = Modifier.testTag("selection_delete_button")
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = Color(0xFF1E293B)
            )
        }

        IconButton(
            onClick = onMuteClick,
            modifier = Modifier.testTag("selection_mute_button")
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsOff,
                contentDescription = "Mute",
                tint = Color(0xFF1E293B)
            )
        }

        Box {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.testTag("selection_more_button")
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = Color(0xFF1E293B)
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                DropdownMenuItem(
                    text = { Text("Select all", fontWeight = FontWeight.Medium, fontSize = 15.sp) },
                    onClick = {
                        menuExpanded = false
                        onSelectAllClick()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Mark as unread", fontWeight = FontWeight.Medium, fontSize = 15.sp) },
                    onClick = {
                        menuExpanded = false
                        onMarkUnreadClick()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Lock chat", fontWeight = FontWeight.Medium, fontSize = 15.sp) },
                    onClick = {
                        menuExpanded = false
                        onLockChatClick()
                    }
                )
            }
        }
    }
}

@Composable
fun OlinamSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .height(44.dp)
            .testTag("search_bar_container"),
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFFF0F2F5)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.search_hint),
                tint = Color(0xFF667781),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = "Search...",
                        color = Color(0xFF667781),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color(0xFF111B21),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    cursorBrush = SolidColor(Color(0xFF007AFF)),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_text_input")
                )
            }
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(22.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun OlinamFilterChips(
    activeFilter: FilterCategory,
    onFilterSelected: (FilterCategory) -> Unit,
    onAddClick: () -> Unit
) {
    val items = listOf(
        FilterCategory.CHATS to "Chats",
        FilterCategory.DIRECT to "Direct",
        FilterCategory.GROUPS to "Groups"
    )

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(items) { (category, label) ->
            val isSelected = activeFilter == category
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onFilterSelected(category) }
                    .testTag("filter_chip_${label.lowercase()}"),
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) OlinamPrimaryContainer else Color.Transparent,
                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Text(
                    text = label,
                    fontSize = 15.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isSelected) OlinamPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 9.dp)
                )
            }
        }

        item {
            Surface(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onAddClick)
                    .testTag("add_chip_button"),
                shape = CircleShape,
                color = Color.Transparent,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add chat or group",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ArattaiAlertsItem(
    conversation: Conversation,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("arattai_alerts_item"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Yellow alert circular logo
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(OlinamAlertYellow),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arattai_alerts),
                contentDescription = "Alerts",
                tint = Color(0xFF1B1B1B),
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Arattai Alerts",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified badge",
                        tint = OlinamAlertYellow,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = "4:56 pm",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = conversation.lastMessageText,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun InviteFriendsItem(
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("invite_friends_item"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Blue circle with heart
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(OlinamInviteBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Invite friends",
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Invite friends",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Go",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Connect with your friends on Arattai",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private val AVATAR_PALETTE = listOf(
    Color(0xFF0160E3), // Olinam Royal Blue
    Color(0xFF0284C7), // Sky
    Color(0xFF6366F1), // Indigo
    Color(0xFF8B5CF6), // Purple
    Color(0xFFEC4899), // Pink
    Color(0xFFF59E0B)  // Amber
)

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ConversationListItem(
    conversation: Conversation,
    labels: List<ChatLabel> = emptyList(),
    isLocked: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onAvatarClick: () -> Unit = onClick
) {
    val isItemLocked = isLocked
    val attachedLabels = remember(conversation.labelIds, labels) {
        if (conversation.isSmsContact || conversation.labelIds.isEmpty()) {
            emptyList()
        } else {
            val nonSystemIds = conversation.labelIds.filter { it !in listOf("all", "direct", "groups", "sms") }
            if (nonSystemIds.isEmpty()) {
                emptyList()
            } else {
                labels.filter { it.id in nonSystemIds }
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) Color(0xFFEBF2FA) else Color.Transparent)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 14.dp, vertical = 9.dp)
            .testTag("conversation_item_${conversation.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // WhatsApp compact circular avatar (48dp)
        Box(
            contentAlignment = Alignment.BottomEnd,
            modifier = Modifier
                .clickable { onAvatarClick() }
                .testTag("avatar_${conversation.id}")
        ) {
            val avatarColor = remember(conversation.id, conversation.title) {
                if (conversation.title.startsWith("A", ignoreCase = true)) {
                    Color(0xFFE91E63) // Vibrant pink/magenta
                } else {
                    AVATAR_PALETTE[Math.abs(conversation.id.hashCode()) % AVATAR_PALETTE.size]
                }
            }

            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(avatarColor),
                contentAlignment = Alignment.Center
            ) {
                if (!conversation.avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = conversation.avatarUrl,
                        contentDescription = conversation.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (conversation.isGroup) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = "Group",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                } else {
                    Text(
                        text = conversation.title.take(1).uppercase(Locale.getDefault()),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            } else if (conversation.onlineStatus == "online") {
                Box(
                    modifier = Modifier
                        .size(13.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(OlinamOnlineGreen)
                )
            }
        }

        Spacer(modifier = Modifier.width(13.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF111827),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (conversation.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified",
                            tint = Color(0xFF0160E3),
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    if (isItemLocked) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked chat",
                            tint = Color(0xFF0160E3),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = formatMessageTime(conversation.lastMessageTimestamp),
                    fontSize = 12.sp,
                    color = if (conversation.unreadCount > 0) Color(0xFF0160E3) else Color(0xFF8696A0),
                    fontWeight = if (conversation.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (conversation.isSmsContact) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.padding(end = 5.dp)
                        ) {
                            Text(
                                text = "SMS",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    } else {
                        // WhatsApp double checkmarks for sent read receipts
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Read",
                            tint = Color(0xFF53BDEB),
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 3.dp)
                        )
                    }

                    Text(
                        text = if (isItemLocked) "🔒 Locked chat" else conversation.lastMessageText,
                        fontSize = 13.5.sp,
                        color = if (isItemLocked) Color(0xFF0160E3) else Color(0xFF64748B),
                        fontStyle = if (isItemLocked) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (conversation.unreadCount > 0) {
                    val badgeText = if (conversation.unreadCount > 99) "99+" else conversation.unreadCount.toString()
                    Box(
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0160E3))
                            .padding(horizontal = if (conversation.unreadCount > 9) 6.dp else 2.dp, vertical = 2.dp)
                            .testTag("unread_badge_${conversation.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = badgeText,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 13.sp
                        )
                    }
                }
            }

            // Attached label badges shown below last message (satisfies "home page me jo lable laga hua uska niche ki traf dekhi bhi de")
            if (attachedLabels.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    attachedLabels.take(3).forEach { label ->
                        val labelColor = try {
                            Color(android.graphics.Color.parseColor(label.colorHex))
                        } catch (_: Exception) {
                            Color(0xFF007AFF)
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = labelColor.copy(alpha = 0.12f),
                            border = BorderStroke(0.7.dp, labelColor.copy(alpha = 0.35f)),
                            modifier = Modifier.testTag("chat_label_badge_${conversation.id}_${label.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(labelColor)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = label.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = labelColor
                                )
                            }
                        }
                    }
                    if (attachedLabels.size > 3) {
                        Text(
                            text = "+${attachedLabels.size - 3}",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OlinamBottomNavBar(
    currentTab: AppTab,
    unreadChatsCount: Int = 0,
    onTabSelected: (AppTab) -> Unit
) {
    Surface(
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .testTag("bottom_nav_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Chats
            val chatBadgeText = if (unreadChatsCount > 0) {
                if (unreadChatsCount > 99) "99+" else unreadChatsCount.toString()
            } else null

            BottomNavItem(
                selected = currentTab == AppTab.CHATS,
                label = stringResource(R.string.chats),
                badgeText = chatBadgeText,
                icon = { tint ->
                    Icon(
                        painter = painterResource(id = R.drawable.ic_nav_chats),
                        contentDescription = stringResource(R.string.chats),
                        tint = tint,
                        modifier = Modifier.size(24.dp)
                    )
                },
                onClick = { onTabSelected(AppTab.CHATS) },
                testTag = "nav_item_chats"
            )

            // 2. Status
            BottomNavItem(
                selected = currentTab == AppTab.STORIES,
                label = "Status",
                icon = { tint ->
                    Icon(
                        painter = painterResource(id = R.drawable.ic_status_ring),
                        contentDescription = "Status",
                        tint = tint,
                        modifier = Modifier.size(23.dp)
                    )
                },
                onClick = { onTabSelected(AppTab.STORIES) },
                testTag = "nav_item_stories"
            )

            // 3. Oj AI (In Center)
            BottomNavItem(
                selected = currentTab == AppTab.OJ_AI,
                label = stringResource(R.string.oj_ai),
                icon = { tint ->
                    Icon(
                        painter = painterResource(id = R.drawable.ic_oj_ai),
                        contentDescription = stringResource(R.string.oj_ai),
                        tint = tint,
                        modifier = Modifier.size(24.dp)
                    )
                },
                onClick = { onTabSelected(AppTab.OJ_AI) },
                testTag = "nav_item_oj_ai"
            )

            // 4. Morph (Omkun Morph notes)
            BottomNavItem(
                selected = currentTab == AppTab.MORPH,
                label = stringResource(R.string.morph),
                icon = { tint ->
                    Icon(
                        painter = painterResource(id = R.drawable.ic_morph_notes),
                        contentDescription = stringResource(R.string.morph),
                        tint = tint,
                        modifier = Modifier.size(23.dp)
                    )
                },
                onClick = { onTabSelected(AppTab.MORPH) },
                testTag = "nav_item_morph"
            )

            // 5. Calls
            BottomNavItem(
                selected = currentTab == AppTab.CALLS,
                label = stringResource(R.string.calls),
                icon = { tint ->
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = stringResource(R.string.calls),
                        tint = tint,
                        modifier = Modifier.size(23.dp)
                    )
                },
                onClick = { onTabSelected(AppTab.CALLS) },
                testTag = "nav_item_calls"
            )
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.BottomNavItem(
    selected: Boolean,
    label: String,
    badgeText: String? = null,
    icon: @Composable (Color) -> Unit,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .height(58.dp)
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .height(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (selected) Color(0xFFE9EBEA) else Color.Transparent)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                val iconTint = if (selected) Color(0xFF111B21) else Color(0xFF54656F)
                icon(iconTint)
            }

            if (!badgeText.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-3).dp)
                        .defaultMinSize(minWidth = 18.dp, minHeight = 18.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF007AFF))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                        .testTag("bottom_nav_badge_$label"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeText,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 12.sp,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color(0xFF111B21) else Color(0xFF54656F)
        )
    }
}

private val timeFormat = ThreadLocal.withInitial { SimpleDateFormat("h:mm a", Locale.US) }
private val dateFormat = ThreadLocal.withInitial { SimpleDateFormat("MMM d", Locale.US) }
private val formattedTimeCache = java.util.concurrent.ConcurrentHashMap<Long, String>(1024)
private var lastTodayCheckTime = 0L
private var startOfTodayMillis = 0L
private var endOfTodayMillis = 0L

private fun isTimestampTodayFast(timestamp: Long): Boolean {
    val now = System.currentTimeMillis()
    if (now - lastTodayCheckTime > 60_000L || startOfTodayMillis == 0L) {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        startOfTodayMillis = cal.timeInMillis
        endOfTodayMillis = startOfTodayMillis + 86400000L
        lastTodayCheckTime = now
    }
    return timestamp in startOfTodayMillis until endOfTodayMillis
}

fun formatMessageTime(timestamp: Long): String {
    if (timestamp <= 0L) return ""
    val minuteBucket = timestamp / 60000L
    val cached = formattedTimeCache[minuteBucket]
    if (cached != null) return cached

    val date = Date(timestamp)
    val formatted = if (isTimestampTodayFast(timestamp)) {
        val fmt = timeFormat.get() ?: SimpleDateFormat("h:mm a", Locale.US)
        fmt.format(date).lowercase(Locale.US)
    } else {
        val fmt = dateFormat.get() ?: SimpleDateFormat("MMM d", Locale.US)
        fmt.format(date)
    }
    formattedTimeCache[minuteBucket] = formatted
    return formatted
}
