package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ContactHelper
import com.example.data.DeviceContact
import com.example.model.UserProfile

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.TextButton
import com.example.model.GroupPermissions

private val OlinamBlue = Color(0xFF0160E3)
private val OlinamBlueLight = Color(0xFFE2EDFC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewGroupScreen(
    registeredPhoneNumbers: Set<String> = emptySet(),
    currentUser: UserProfile? = null,
    onBackClick: () -> Unit,
    onGroupCreated: (
        groupName: String,
        participants: List<DeviceContact>,
        disappearingDuration: String,
        permissions: GroupPermissions,
        avatarUrl: String?
    ) -> Unit
) {
    val context = LocalContext.current
    val rawContacts = remember { ContactHelper.getDeviceContacts(context) }
    val selectedContacts = remember { mutableStateListOf<DeviceContact>() }

    var isStep2GroupDetails by remember { mutableStateOf(false) }
    var groupName by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    var disappearingDuration by remember { mutableStateOf("Off") }
    var showDisappearingDialog by remember { mutableStateOf(false) }
    var showGroupPermissionsScreen by remember { mutableStateOf(false) }
    var groupPermissions by remember { mutableStateOf(GroupPermissions()) }
    var selectedAvatarEmoji by remember { mutableStateOf("👥") }
    var showAvatarPickerDialog by remember { mutableStateOf(false) }

    if (showGroupPermissionsScreen) {
        GroupPermissionsScreen(
            initialPermissions = groupPermissions,
            onBackClick = { showGroupPermissionsScreen = false },
            onPermissionsChanged = { groupPermissions = it }
        )
        return
    }

    fun isContactOnOlinam(contact: DeviceContact): Boolean {
        if (contact.hasOlinam) return true
        val clean = contact.phoneNumber.trim()
        val digits = clean.filter { it.isDigit() }
        val last10 = digits.takeLast(10)
        return registeredPhoneNumbers.contains(clean) ||
                registeredPhoneNumbers.contains(digits) ||
                (last10.length >= 7 && registeredPhoneNumbers.contains(last10))
    }

    fun sendInvite(contact: DeviceContact) {
        try {
            val sendIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:${contact.phoneNumber}")
                putExtra("sms_body", "Let's chat on Olinam! It's a fast, simple, and secure messaging app. Download: https://olinamhq.github.io")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(sendIntent)
            Toast.makeText(context, "Inviting ${contact.name} via SMS...", Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "Let's chat on Olinam! It's a fast, simple, and secure messaging app. Download: https://olinamhq.github.io")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(Intent.createChooser(shareIntent, "Invite ${contact.name} to Olinam"))
        }
    }

    val evaluatedContacts = remember(rawContacts, registeredPhoneNumbers) {
        rawContacts.map { contact ->
            val hasApp = isContactOnOlinam(contact)
            contact.copy(
                hasOlinam = hasApp,
                status = if (hasApp) "Available on Olinam 🔒" else "Invite to Olinam"
            )
        }
    }

    val filteredContacts = remember(evaluatedContacts, searchQuery) {
        if (searchQuery.isBlank()) evaluatedContacts
        else evaluatedContacts.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.phoneNumber.contains(searchQuery)
        }
    }

    val appContacts = remember(filteredContacts) {
        filteredContacts.filter { it.hasOlinam }
    }

    val nonAppContacts = remember(filteredContacts) {
        filteredContacts.filter { !it.hasOlinam }
    }

    BackHandler {
        if (isStep2GroupDetails) {
            isStep2GroupDetails = false
        } else {
            onBackClick()
        }
    }

    if (isStep2GroupDetails) {
        // Step 2: Group subject & confirmation (WhatsApp style matching Screenshot 6)
        Scaffold(
            containerColor = Color.White,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "New group",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF111827)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { isStep2GroupDetails = false }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        val finalName = if (groupName.isBlank()) "Group" else groupName.trim()
                        Toast.makeText(context, "Group \"$finalName\" created!", Toast.LENGTH_SHORT).show()
                        onGroupCreated(
                            finalName,
                            selectedContacts.toList(),
                            disappearingDuration,
                            groupPermissions,
                            selectedAvatarEmoji
                        )
                    },
                    shape = CircleShape,
                    containerColor = Color(0xFF111827),
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(4.dp),
                    modifier = Modifier
                        .size(56.dp)
                        .testTag("create_group_confirm_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Create Group",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Group Icon & Name Row (Screenshot 6)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE2EDFC))
                            .clickable { showAvatarPickerDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedAvatarEmoji.isNotBlank() && selectedAvatarEmoji != "👥") {
                            Text(
                                text = selectedAvatarEmoji,
                                fontSize = 26.sp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Group icon",
                                tint = OlinamBlue,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { groupName = it.take(25) },
                        placeholder = {
                            Text(
                                "Group name (optional)",
                                color = Color(0xFF94A3B8),
                                fontSize = 15.sp
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.SentimentSatisfiedAlt,
                                contentDescription = "Emoji",
                                tint = Color(0xFF64748B),
                                modifier = Modifier
                                    .size(22.dp)
                                    .clickable {
                                        groupName += " 😊"
                                    }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("group_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF111827),
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            cursorColor = Color(0xFF111827)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Option 1: Disappearing messages (Screenshot 6)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showDisappearingDialog = true }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Disappearing messages",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF111827)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = disappearingDuration,
                            fontSize = 13.5.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Timer",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Option 2: Group permissions (Screenshot 6)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showGroupPermissionsScreen = true }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Group permissions",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF111827)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Send messages: ${if (groupPermissions.canMembersSendMessages) "All members" else "Only admins"}",
                            fontSize = 13.5.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Group permissions",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFF1F5F9))
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Members count & list (Screenshot 6)
                Text(
                    text = "Members: ${if (selectedContacts.isEmpty()) "1 (You)" else "${selectedContacts.size}"}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF54656F)
                )

                Spacer(modifier = Modifier.height(14.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (selectedContacts.isEmpty()) {
                        item {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(60.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE2EDFC)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "You",
                                        color = OlinamBlue,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "You",
                                    fontSize = 12.sp,
                                    color = Color(0xFF334155),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    } else {
                        items(selectedContacts, key = { it.id }) { contact ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(62.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE2EDFC)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = contact.name.take(1).uppercase(),
                                        color = OlinamBlue,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = contact.name,
                                    fontSize = 12.sp,
                                    color = Color(0xFF334155),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showDisappearingDialog) {
            AlertDialog(
                onDismissRequest = { showDisappearingDialog = false },
                title = { Text("Disappearing messages timer", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        listOf("24 hours", "7 days", "90 days", "Off").forEach { option ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        disappearingDuration = option
                                        showDisappearingDialog = false
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = disappearingDuration == option,
                                    onClick = {
                                        disappearingDuration = option
                                        showDisappearingDialog = false
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF007AFF))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(option, fontSize = 15.sp)
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showDisappearingDialog = false }) {
                        Text("Cancel", color = Color(0xFF64748B))
                    }
                }
            )
        }

        return
    }

    // Step 1: Select participants (WhatsApp style)
    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    if (isSearching) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search name or number...") },
                            singleLine = true,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("group_search_input")
                        )
                    } else {
                        Column {
                            Text(
                                text = "New group",
                                fontSize = 18.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                            Text(
                                text = if (selectedContacts.isEmpty()) "Add participants" else "${selectedContacts.size} selected",
                                fontSize = 12.5.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            isSearching = !isSearching
                            if (!isSearching) searchQuery = ""
                        }
                    ) {
                        Icon(
                            imageVector = if (isSearching) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (isSearching) "Close Search" else "Search"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            // Next arrow button in Olinam Blue
            if (selectedContacts.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { isStep2GroupDetails = true },
                    shape = CircleShape,
                    containerColor = OlinamBlue,
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(4.dp),
                    modifier = Modifier
                        .size(56.dp)
                        .testTag("group_next_fab")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next",
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {
            // Selected Participants Horizontal Chips Row
            if (selectedContacts.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(selectedContacts, key = { it.id }) { contact ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { selectedContacts.remove(contact) }
                        ) {
                            Box(contentAlignment = Alignment.BottomEnd) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(OlinamBlueLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = contact.name.take(1).uppercase(),
                                        color = OlinamBlue,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF6B7280)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = Color.White,
                                        modifier = Modifier.size(11.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = contact.name.take(8),
                                fontSize = 11.5.sp,
                                color = Color(0xFF374151),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                // Section 1 Header: Contacts on Olinam (App users only can be added)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Contacts on Olinam",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = OlinamBlue
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Encrypted",
                            tint = OlinamBlue,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "${appContacts.size}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF6B7280)
                        )
                    }
                }

                // If no app contacts found
                if (appContacts.isEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFEFF6FF)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No contacts on Olinam yet",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1D4ED8)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Only contacts using Olinam can be added to groups. Non-app contacts can be invited below via SMS.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF2563EB),
                                    lineHeight = 16.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { isStep2GroupDetails = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = OlinamBlue),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Create group with You as Admin", fontSize = 12.5.sp)
                                }
                            }
                        }
                    }
                } else {
                    // List of selectable Olinam contacts
                    items(appContacts, key = { "app_" + it.id + it.phoneNumber }) { contact ->
                        val isSelected = selectedContacts.any { it.id == contact.id }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isSelected) {
                                        selectedContacts.removeAll { it.id == contact.id }
                                    } else {
                                        selectedContacts.add(contact)
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                .testTag("group_contact_item_${contact.id}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(OlinamBlueLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = contact.name.take(1).uppercase(),
                                    color = OlinamBlue,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = contact.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF111827)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = contact.phoneNumber,
                                    fontSize = 13.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }

                            // Circular selection checkbox on the right in Olinam Blue
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = 1.5.dp,
                                        color = if (isSelected) OlinamBlue else Color(0xFF9CA3AF),
                                        shape = CircleShape
                                    )
                                    .background(if (isSelected) OlinamBlue else Color.Transparent),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 2 Header: Invite to Olinam (Non-app contacts with Invite button)
                if (nonAppContacts.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp, bottom = 4.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFFFBEB)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = null,
                                        tint = Color(0xFFB45309),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Contacts not using Olinam cannot be added to group chats. Tap Invite to send them an SMS invite link.",
                                        fontSize = 12.sp,
                                        color = Color(0xFFB45309),
                                        lineHeight = 16.sp
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Invite to Olinam",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4B5563)
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "${nonAppContacts.size}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }
                    }

                    items(nonAppContacts, key = { "nonapp_" + it.id + it.phoneNumber }) { contact ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    Toast.makeText(
                                        context,
                                        "${contact.name} is not on Olinam yet. Tap Invite to send them an invitation.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .padding(horizontal = 16.dp, vertical = 9.dp)
                                .testTag("group_nonapp_contact_${contact.id}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF3F4F6)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = contact.name.take(1).uppercase(),
                                    color = Color(0xFF6B7280),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = contact.name,
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF111827)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = contact.phoneNumber,
                                    fontSize = 12.5.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                            }

                            // Dedicated Invite Button (Cannot add to group, must invite!)
                            OutlinedButton(
                                onClick = { sendInvite(contact) },
                                shape = RoundedCornerShape(20.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, OlinamBlue),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = OlinamBlue
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                    horizontal = 14.dp,
                                    vertical = 4.dp
                                ),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("invite_contact_button_${contact.id}")
                            ) {
                                Text(
                                    text = "Invite",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = OlinamBlue
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
