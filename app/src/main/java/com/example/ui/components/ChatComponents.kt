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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
    onOpenSpam: () -> Unit = {}
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ultra-Premium Olinam Title & Emblem
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.testTag("app_title_olinam")
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(Color(0xFF0160E3), Color(0xFF1E3A8A))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.ChatBubble,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Olinam",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        style = TextStyle(
                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(Color(0xFF0F172A), Color(0xFF0160E3))
                            )
                        ),
                        letterSpacing = (-0.8).sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0160E3))
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onCameraClick,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("camera_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Camera",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(26.dp)
                )
            }

            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("more_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(26.dp)
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
                        text = { Text("Spam & Blocked", fontWeight = FontWeight.Medium) },
                        onClick = {
                            menuExpanded = false
                            onOpenSpam()
                        },
                        modifier = Modifier.testTag("menu_spam_sms")
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
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .height(50.dp)
            .testTag("search_bar_container"),
        shape = RoundedCornerShape(25.dp),
        color = Color(0xFFE9EDF2), // Visible contrasting grey matching WhatsApp
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD6DBE1))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.search_hint),
                tint = Color(0xFF6B7280),
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = "Search...",
                        color = Color(0xFF6B7280),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color(0xFF111827),
                        fontSize = 16.sp,
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
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(18.dp)
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
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("conversation_item_${conversation.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar with online status
        Box(contentAlignment = Alignment.BottomEnd) {
            val avatarColor = remember(conversation.id) {
                val colors = listOf(
                    Color(0xFF3B82F6),
                    Color(0xFF8B5CF6),
                    Color(0xFFEC4899),
                    Color(0xFF10B981),
                    Color(0xFFF59E0B)
                )
                colors[Math.abs(conversation.id.hashCode()) % colors.size]
            }

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(avatarColor),
                contentAlignment = Alignment.Center
            ) {
                if (conversation.isGroup) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = "Group",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                } else {
                    Text(
                        text = conversation.title.take(1).uppercase(Locale.getDefault()),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                }
            }

            if (conversation.onlineStatus == "online") {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(OlinamOnlineGreen)
                )
            }
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
                        text = conversation.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (conversation.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified",
                            tint = OlinamPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    text = formatMessageTime(conversation.lastMessageTimestamp),
                    fontSize = 12.sp,
                    color = if (conversation.unreadCount > 0) OlinamPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (conversation.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (conversation.isSpam) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFFEE2E2),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = "SPAM",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    } else if (conversation.isSmsContact) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFFEF3C7),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = "SMS",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    } else if (conversation.isE2EE) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Encrypted",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = conversation.lastMessageText,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (conversation.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(OlinamPrimary),
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
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 4.dp,
        modifier = Modifier.testTag("bottom_nav_bar")
    ) {
        val selectedColor = OlinamPrimary
        val unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant

        // 1. Chats
        NavigationBarItem(
            selected = currentTab == AppTab.CHATS,
            onClick = { onTabSelected(AppTab.CHATS) },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.ChatBubble,
                    contentDescription = stringResource(R.string.chats),
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.chats),
                    fontWeight = if (currentTab == AppTab.CHATS) FontWeight.SemiBold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = selectedColor,
                selectedTextColor = selectedColor,
                indicatorColor = OlinamPrimaryContainer,
                unselectedIconColor = unselectedColor,
                unselectedTextColor = unselectedColor
            ),
            modifier = Modifier.testTag("nav_item_chats")
        )

        // 2. Stories
        NavigationBarItem(
            selected = currentTab == AppTab.STORIES,
            onClick = { onTabSelected(AppTab.STORIES) },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.DonutLarge,
                    contentDescription = stringResource(R.string.stories),
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.stories),
                    fontWeight = if (currentTab == AppTab.STORIES) FontWeight.SemiBold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = selectedColor,
                selectedTextColor = selectedColor,
                indicatorColor = OlinamPrimaryContainer,
                unselectedIconColor = unselectedColor,
                unselectedTextColor = unselectedColor
            ),
            modifier = Modifier.testTag("nav_item_stories")
        )

        // 3. Oj Ai
        NavigationBarItem(
            selected = currentTab == AppTab.OJ_AI,
            onClick = { onTabSelected(AppTab.OJ_AI) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_oj_ai),
                    contentDescription = stringResource(R.string.oj_ai),
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.oj_ai),
                    fontWeight = if (currentTab == AppTab.OJ_AI) FontWeight.SemiBold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = selectedColor,
                selectedTextColor = selectedColor,
                indicatorColor = OlinamPrimaryContainer,
                unselectedIconColor = unselectedColor,
                unselectedTextColor = unselectedColor
            ),
            modifier = Modifier.testTag("nav_item_oj_ai")
        )

        // 4. Calls
        NavigationBarItem(
            selected = currentTab == AppTab.CALLS,
            onClick = { onTabSelected(AppTab.CALLS) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = stringResource(R.string.calls),
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.calls),
                    fontWeight = if (currentTab == AppTab.CALLS) FontWeight.SemiBold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = selectedColor,
                selectedTextColor = selectedColor,
                indicatorColor = OlinamPrimaryContainer,
                unselectedIconColor = unselectedColor,
                unselectedTextColor = unselectedColor
            ),
            modifier = Modifier.testTag("nav_item_calls")
        )
    }
}

fun formatMessageTime(timestamp: Long): String {
    val date = Date(timestamp)
    val now = Date()
    val sameDay = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(date) ==
            SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(now)

    return if (sameDay) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(date).lowercase(Locale.getDefault())
    } else {
        SimpleDateFormat("MMM d", Locale.getDefault()).format(date)
    }
}
