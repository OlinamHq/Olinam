package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
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
    onCameraClick: () -> Unit,
    onMenuNewGroup: () -> Unit,
    onMenuProfile: () -> Unit,
    onMenuSecurity: () -> Unit,
    onMenuSettings: () -> Unit,
    onSyncSms: () -> Unit = {},
    onOpenSms: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 16.dp, end = 8.dp, top = 6.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // WhatsApp-style compact branding title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.testTag("app_title_olinam")
        ) {
            Text(
                text = "Olinam",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0160E3))
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onCameraClick,
                modifier = Modifier
                    .size(42.dp)
                    .testTag("camera_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Camera",
                    tint = Color(0xFF1E293B),
                    modifier = Modifier.size(23.dp)
                )
            }

            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("more_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = Color(0xFF1E293B),
                        modifier = Modifier.size(23.dp)
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    DropdownMenuItem(
                        text = { Text("New group", fontWeight = FontWeight.Medium) },
                        onClick = {
                            menuExpanded = false
                            onMenuNewGroup()
                        },
                        modifier = Modifier.testTag("menu_new_group")
                    )
                    DropdownMenuItem(
                        text = { Text("Sync Phone SMS", fontWeight = FontWeight.Medium) },
                        onClick = {
                            menuExpanded = false
                            onSyncSms()
                        },
                        modifier = Modifier.testTag("menu_sync_sms")
                    )
                    DropdownMenuItem(
                        text = { Text("Text Messages (SMS)", fontWeight = FontWeight.Medium) },
                        onClick = {
                            menuExpanded = false
                            onOpenSms()
                        },
                        modifier = Modifier.testTag("menu_sms_messages")
                    )
                    DropdownMenuItem(
                        text = { Text("End-to-End Encryption", fontWeight = FontWeight.Medium) },
                        onClick = {
                            menuExpanded = false
                            onMenuSecurity()
                        },
                        modifier = Modifier.testTag("menu_security")
                    )
                    DropdownMenuItem(
                        text = { Text("My Profile", fontWeight = FontWeight.Medium) },
                        onClick = {
                            menuExpanded = false
                            onMenuProfile()
                        },
                        modifier = Modifier.testTag("menu_profile")
                    )
                    DropdownMenuItem(
                        text = { Text("Settings", fontWeight = FontWeight.Medium) },
                        onClick = {
                            menuExpanded = false
                            onMenuSettings()
                        },
                        modifier = Modifier.testTag("menu_settings")
                    )
                    DropdownMenuItem(
                        text = { Text("Log out", color = Color(0xFFDC2626), fontWeight = FontWeight.Medium) },
                        onClick = {
                            menuExpanded = false
                            onLogout()
                        },
                        modifier = Modifier.testTag("menu_logout")
                    )
                }
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
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .height(40.dp)
            .testTag("search_bar_container"),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF1F3F5),
        border = androidx.compose.foundation.BorderStroke(0.6.dp, Color(0xFFE2E8F0))
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
                tint = Color(0xFF64748B),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = "Search...",
                        color = Color(0xFF64748B),
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color(0xFF0F172A),
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    cursorBrush = SolidColor(OlinamPrimary),
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

@Composable
fun ConversationListItem(
    conversation: Conversation,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp)
            .testTag("conversation_item_${conversation.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // WhatsApp compact circular avatar (48dp)
        Box(contentAlignment = Alignment.BottomEnd) {
            val avatarColor = remember(conversation.id) {
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
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(avatarColor),
                contentAlignment = Alignment.Center
            ) {
                if (conversation.isGroup) {
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

            if (conversation.onlineStatus == "online") {
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
                            tint = Color(0xFF00A884),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = formatMessageTime(conversation.lastMessageTimestamp),
                    fontSize = 12.sp,
                    color = if (conversation.unreadCount > 0) Color(0xFF00A884) else Color(0xFF8696A0),
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
                        text = conversation.lastMessageText,
                        fontSize = 13.5.sp,
                        color = Color(0xFF64748B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (conversation.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF25D366)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = conversation.unreadCount.toString(),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
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
    onTabSelected: (AppTab) -> Unit
) {
    Surface(
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(0.6.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .testTag("bottom_nav_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Chats
            BottomNavItem(
                selected = currentTab == AppTab.CHATS,
                label = stringResource(R.string.chats),
                icon = { tint ->
                    Icon(
                        imageVector = Icons.Rounded.ChatBubble,
                        contentDescription = stringResource(R.string.chats),
                        tint = tint,
                        modifier = Modifier.size(23.dp)
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
                        imageVector = Icons.Rounded.DonutLarge,
                        contentDescription = "Status",
                        tint = tint,
                        modifier = Modifier.size(23.dp)
                    )
                },
                onClick = { onTabSelected(AppTab.STORIES) },
                testTag = "nav_item_stories"
            )

            // 3. Oj Ai
            BottomNavItem(
                selected = currentTab == AppTab.OJ_AI,
                label = stringResource(R.string.oj_ai),
                icon = { tint ->
                    Icon(
                        painter = painterResource(id = R.drawable.ic_oj_ai),
                        contentDescription = stringResource(R.string.oj_ai),
                        tint = tint,
                        modifier = Modifier.size(23.dp)
                    )
                },
                onClick = { onTabSelected(AppTab.OJ_AI) },
                testTag = "nav_item_oj_ai"
            )

            // 4. Calls
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
    icon: @Composable (Color) -> Unit,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .height(56.dp)
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (selected) {
            // Royal Blue capsule pill with White icon (requested by user)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(OlinamPrimary)
                    .padding(horizontal = 18.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                icon(Color.White)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = OlinamPrimary
            )
        } else {
            // Unselected: slate icon and text
            Box(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                icon(Color(0xFF64748B))
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF64748B)
            )
        }
    }
}

private val cachedTimeFormat = SimpleDateFormat("h:mm a", Locale.US)
private val cachedDateFormat = SimpleDateFormat("MMM d", Locale.US)

fun formatMessageTime(timestamp: Long): String {
    if (timestamp <= 0L) return ""
    val isToday = android.text.format.DateUtils.isToday(timestamp)
    val date = Date(timestamp)
    return if (isToday) {
        synchronized(cachedTimeFormat) { cachedTimeFormat.format(date).lowercase(Locale.US) }
    } else {
        synchronized(cachedDateFormat) { cachedDateFormat.format(date) }
    }
}
