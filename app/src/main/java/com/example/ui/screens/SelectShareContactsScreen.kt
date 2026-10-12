package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.DeviceContact
import com.example.model.Conversation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectShareContactsScreen(
    caption: String,
    mediaUri: String?,
    conversations: List<Conversation>,
    deviceContacts: List<DeviceContact> = emptyList(),
    onDismiss: () -> Unit,
    onSend: (sendToStatus: Boolean, selectedConversations: List<Conversation>, selectedContacts: List<DeviceContact>) -> Unit
) {
    BackHandler { onDismiss() }

    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    var isStatusSelected by remember { mutableStateOf(false) }
    val selectedConvIds = remember { mutableStateListOf<String>() }
    val selectedContactIds = remember { mutableStateListOf<String>() }

    val totalSelected = (if (isStatusSelected) 1 else 0) + selectedConvIds.size + selectedContactIds.size

    // Filtered lists based on search
    val filteredConversations = remember(conversations, searchQuery) {
        if (searchQuery.isBlank()) conversations else {
            conversations.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.lastMessageText.contains(searchQuery, ignoreCase = true) ||
                        (it.phoneNumber ?: "").contains(searchQuery)
            }
        }
    }

    val filteredContacts = remember(deviceContacts, searchQuery) {
        if (searchQuery.isBlank()) deviceContacts else {
            deviceContacts.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.phoneNumber.contains(searchQuery)
            }
        }
    }

    // Split conversations/contacts into "Frequently contacted" (top items) and "Recent chats"
    val frequentlyContacted = remember(filteredConversations, filteredContacts) {
        if (filteredConversations.size >= 3) {
            filteredConversations.take(3)
        } else {
            filteredConversations
        }
    }

    val recentChats = remember(filteredConversations, frequentlyContacted) {
        filteredConversations.drop(frequentlyContacted.size)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("select_contacts_screen"),
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search name or number...", fontSize = 15.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_contacts_input")
                        )
                    } else {
                        Text(
                            text = "Select contacts",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("select_contacts_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF111827)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            isSearchActive = !isSearchActive
                            if (!isSearchActive) searchQuery = ""
                        },
                        modifier = Modifier.testTag("select_contacts_search_toggle")
                    ) {
                        Icon(
                            imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (isSearchActive) "Close Search" else "Search",
                            tint = Color(0xFF111827)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            // WhatsApp-style Floating Send Button
            AnimatedVisibility(
                visible = totalSelected > 0,
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                BadgedBox(
                    badge = {
                        if (totalSelected > 1) {
                            Badge(
                                containerColor = Color(0xFF0160E3),
                                contentColor = Color.White,
                                modifier = Modifier.size(20.dp)
                            ) {
                                Text(totalSelected.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                ) {
                    FloatingActionButton(
                        onClick = {
                            val selectedConvs = conversations.filter { selectedConvIds.contains(it.id) }
                            val selectedConts = deviceContacts.filter { selectedContactIds.contains(it.id) }
                            onSend(isStatusSelected, selectedConvs, selectedConts)
                        },
                        containerColor = Color(0xFF0160E3),
                        contentColor = Color.White,
                        shape = CircleShape,
                        elevation = FloatingActionButtonDefaults.elevation(6.dp),
                        modifier = Modifier
                            .navigationBarsPadding()
                            .size(58.dp)
                            .testTag("send_selected_contacts_fab")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. "My status" item (Matching Screenshot 1 exactly)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isStatusSelected = !isStatusSelected }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .testTag("share_target_my_status"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dark circle with camera icon
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "My status",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )

                        if (isStatusSelected) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .align(Alignment.BottomEnd)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0160E3))
                                    .border(1.5.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "My status",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF111827)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "My contacts",
                            fontSize = 13.5.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    IconButton(
                        onClick = {
                            Toast.makeText(context, "Status privacy: My contacts", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreHoriz,
                            contentDescription = "Status options",
                            tint = Color(0xFF64748B)
                        )
                    }
                }
            }

            // 2. Section: "Frequently contacted"
            if (frequentlyContacted.isNotEmpty()) {
                item {
                    Text(
                        text = "Frequently contacted",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                    )
                }

                items(frequentlyContacted, key = { "freq_${it.id}" }) { conv ->
                    val isSelected = selectedConvIds.contains(conv.id)
                    ContactRecipientRow(
                        title = conv.phoneNumber?.ifBlank { conv.title } ?: conv.title,
                        subtitle = if (conv.phoneNumber?.isNotBlank() == true) "~ ${conv.title}" else conv.lastMessageText.ifBlank { "Recent chat" },
                        avatarUrl = conv.avatarUrl,
                        isVerified = conv.isVerified,
                        isBusiness = conv.isVerified || conv.title.contains("business", ignoreCase = true) || conv.title.contains("Minutes", ignoreCase = true),
                        isSelected = isSelected,
                        onClick = {
                            if (isSelected) selectedConvIds.remove(conv.id)
                            else selectedConvIds.add(conv.id)
                        }
                    )
                }
            }

            // Fallback device contacts in Frequently contacted if chats are few
            if (frequentlyContacted.isEmpty() && filteredContacts.isNotEmpty()) {
                item {
                    Text(
                        text = "Frequently contacted",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                    )
                }

                items(filteredContacts.take(3), key = { "freq_contact_${it.id}" }) { contact ->
                    val isSelected = selectedContactIds.contains(contact.id)
                    ContactRecipientRow(
                        title = contact.phoneNumber.ifBlank { contact.name },
                        subtitle = "~ ${contact.name}",
                        avatarUrl = null,
                        isVerified = false,
                        isBusiness = contact.status.contains("business", ignoreCase = true),
                        isSelected = isSelected,
                        onClick = {
                            if (isSelected) selectedContactIds.remove(contact.id)
                            else selectedContactIds.add(contact.id)
                        }
                    )
                }
            }

            // 3. Section: "Recent chats"
            item {
                Text(
                    text = "Recent chats",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                )
            }

            if (recentChats.isEmpty() && frequentlyContacted.isEmpty()) {
                item {
                    Text(
                        text = "No recent chats found",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            } else {
                val chatsToShow = if (recentChats.isNotEmpty()) recentChats else filteredConversations
                items(chatsToShow, key = { "recent_${it.id}" }) { conv ->
                    val isSelected = selectedConvIds.contains(conv.id)
                    ContactRecipientRow(
                        title = conv.title,
                        subtitle = if (conv.phoneNumber?.isNotBlank() == true) "~ ${conv.phoneNumber}" else conv.lastMessageText.ifBlank { "Tap to send" },
                        avatarUrl = conv.avatarUrl,
                        isVerified = conv.isVerified,
                        isBusiness = conv.isVerified || conv.title.contains("Minutes", ignoreCase = true) || conv.title.contains("basket", ignoreCase = true) || conv.title.contains("INDmoney", ignoreCase = true),
                        isSelected = isSelected,
                        onClick = {
                            if (isSelected) selectedConvIds.remove(conv.id)
                            else selectedConvIds.add(conv.id)
                        }
                    )
                }
            }

            // Extra bottom spacing for FloatingActionButton
            item {
                Spacer(modifier = Modifier.height(84.dp))
            }
        }
    }
}

@Composable
private fun ContactRecipientRow(
    title: String,
    subtitle: String,
    avatarUrl: String?,
    isVerified: Boolean = false,
    isBusiness: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar with optional green selection badge
        Box(
            modifier = Modifier.size(50.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
            ) {
                if (!avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = title.take(1).uppercase().ifBlank { "?" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = Color(0xFF475569)
                    )
                }
            }

            // Selected checkmark overlay badge matching WhatsApp
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(Color(0xFF0160E3))
                        .border(1.5.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF111827),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (isVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified Business",
                        tint = Color(0xFF007AFF),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                fontSize = 13.5.sp,
                color = Color(0xFF64748B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (isBusiness) {
                Text(
                    text = "BUSINESS ACCOUNT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 0.5.sp
                )
            }
        }

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Selected",
                tint = Color(0xFF0160E3),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
