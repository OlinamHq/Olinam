package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.ContactHelper
import com.example.data.DeviceContact
import com.example.model.CallType
import com.example.ui.ChatViewModel
import com.example.ui.theme.OlinamPrimary
import com.example.ui.theme.OlinamPrimaryContainer

@Composable
fun SelectCallContactScreen(
    viewModel: ChatViewModel,
    onBackClick: () -> Unit,
    onNewContactClick: () -> Unit,
    onKeypadClick: () -> Unit
) {
    BackHandler { onBackClick() }

    val context = LocalContext.current
    var contacts by remember { mutableStateOf<List<DeviceContact>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    val selectedContacts = remember { mutableStateListOf<DeviceContact>() }

    var showScheduleDialog by remember { mutableStateOf(false) }
    var showCallLinkDialog by remember { mutableStateOf(false) }

    fun loadContacts() {
        contacts = ContactHelper.getDeviceContacts(context)
    }

    LaunchedEffect(Unit) {
        loadContacts()
    }

    val filteredContacts = remember(contacts, searchQuery) {
        if (searchQuery.isBlank()) contacts
        else contacts.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.phoneNumber.contains(searchQuery)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .testTag("select_call_contact_screen")
    ) {
        // TOP SEARCH BAR (matching Screenshot 2: Back arrow, "Name, number, @username", Keypad icon)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.testTag("back_button_call_contacts")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF1E293B)
                )
            }

            // Search input field
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFFF1F5F9)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Name, number, @username",
                            color = Color(0xFF64748B),
                            fontSize = 15.sp
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = Color(0xFF0F172A),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        cursorBrush = SolidColor(OlinamPrimary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Keypad icon on the right (matching Screenshot 2)
            IconButton(
                onClick = onKeypadClick,
                modifier = Modifier.testTag("keypad_quick_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Dialpad,
                    contentDescription = "Keypad",
                    tint = Color(0xFF1E293B),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Subtitle: "Add up to 31 people" (Screenshot 2)
        Text(
            text = "Add up to 31 people",
            fontSize = 13.5.sp,
            color = Color(0xFF64748B),
            modifier = Modifier.padding(start = 16.dp, top = 6.dp, bottom = 10.dp)
        )

        // SELECTED CONTACTS CHIP ROW WITH CALL ACTION BUTTONS (Screenshot 1)
        AnimatedVisibility(visible = selectedContacts.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC))
                    .padding(vertical = 10.dp, horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Selected contacts horizontal list
                    LazyRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(selectedContacts, key = { it.id }) { contact ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(62.dp)
                            ) {
                                Box(contentAlignment = Alignment.BottomEnd) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(OlinamPrimaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = contact.name.take(1).uppercase(),
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = OlinamPrimary
                                        )
                                    }

                                    // Small X cross badge on avatar
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF64748B))
                                            .clickable { selectedContacts.remove(contact) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = contact.name,
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF0F172A),
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Action buttons on the right: Video Call and Audio Call (Screenshot 1)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Video Call button
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9))
                                .border(1.dp, Color(0xFFCBD5E1), CircleShape)
                                .clickable {
                                    val firstContact = selectedContacts.firstOrNull()
                                    if (firstContact != null) {
                                        viewModel.startCall(
                                            contactName = firstContact.name,
                                            contactPhone = firstContact.phoneNumber,
                                            callType = CallType.VIDEO
                                        )
                                        onBackClick()
                                    }
                                }
                                .testTag("start_video_call_selected_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Video Call",
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Audio Call button
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9))
                                .border(1.dp, Color(0xFFCBD5E1), CircleShape)
                                .clickable {
                                    val firstContact = selectedContacts.firstOrNull()
                                    if (firstContact != null) {
                                        viewModel.startCall(
                                            contactName = firstContact.name,
                                            contactPhone = firstContact.phoneNumber,
                                            callType = CallType.VOICE
                                        )
                                        onBackClick()
                                    }
                                }
                                .testTag("start_audio_call_selected_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Audio Call",
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }

        // MAIN LIST: 4 Quick Actions + Contacts List (Screenshot 2)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Action 1: New call link
            item {
                CallActionItem(
                    icon = Icons.Default.Link,
                    title = "New call link",
                    onClick = { showCallLinkDialog = true }
                )
            }

            // Action 2: Call a number
            item {
                CallActionItem(
                    icon = Icons.Default.Dialpad,
                    title = "Call a number",
                    onClick = onKeypadClick
                )
            }

            // Action 3: New contact with QR icon
            item {
                CallActionItem(
                    icon = Icons.Default.PersonAdd,
                    title = "New contact",
                    trailingIcon = Icons.Default.QrCode,
                    onClick = onNewContactClick
                )
            }

            // Action 4: Schedule call
            item {
                CallActionItem(
                    icon = Icons.Default.CalendarMonth,
                    title = "Schedule call",
                    onClick = { showScheduleDialog = true }
                )
            }

            // Section Header: "Contacts on Olinam" (Screenshot 2)
            item {
                Text(
                    text = "Contacts on Olinam",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(top = 18.dp, bottom = 10.dp)
                )
            }

            if (filteredContacts.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No contacts matching \"$searchQuery\"" else "No contacts found yet",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap 'New contact' above to add someone or invite contacts to Olinam.",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            // Contacts List with Avatar and Radio Selection Circle (Screenshots 1 & 2)
            items(filteredContacts, key = { it.id }) { contact ->
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
                        .padding(vertical = 10.dp)
                        .testTag("call_contact_item_${contact.id}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Contact Avatar
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                when (contact.name.take(1).uppercase()) {
                                    "A" -> Color(0xFFFDE8E8)
                                    "S" -> Color(0xFFE0F2FE)
                                    else -> Color(0xFFF3E8FF)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = contact.name.take(2).uppercase(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (contact.name.take(1).uppercase()) {
                                "A" -> Color(0xFF991B1B)
                                "S" -> Color(0xFF0369A1)
                                else -> Color(0xFF6B21A8)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Text(
                        text = contact.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF0F172A),
                        modifier = Modifier.weight(1f)
                    )

                    // Radio Selection Circle (matching Screenshot 2: unselected hollow circle, Screenshot 1: checked black circle)
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Color(0xFF0F172A) else Color.Transparent)
                            .border(
                                width = if (isSelected) 0.dp else 1.5.dp,
                                color = if (isSelected) Color.Transparent else Color(0xFF94A3B8),
                                shape = CircleShape
                            ),
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
    }

    // Call Link Modal
    if (showCallLinkDialog) {
        val linkUrl = "https://olinam.call/room-${(1000..9999).random()}"
        AlertDialog(
            onDismissRequest = { showCallLinkDialog = false },
            title = { Text("New Call Link") },
            text = {
                Column {
                    Text("Anyone with this link can join your call:")
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = linkUrl,
                        color = OlinamPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCallLinkDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = OlinamPrimary)
                ) {
                    Text("Copy Link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCallLinkDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Schedule Call Modal
    if (showScheduleDialog) {
        var callTitle by remember { mutableStateOf("Meeting Call") }
        AlertDialog(
            onDismissRequest = { showScheduleDialog = false },
            title = { Text("Schedule a Call") },
            text = {
                Column {
                    OutlinedTextField(
                        value = callTitle,
                        onValueChange = { callTitle = it },
                        label = { Text("Call Subject") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Scheduled for today at 6:00 PM (IST)",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showScheduleDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = OlinamPrimary)
                ) {
                    Text("Schedule")
                }
            },
            dismissButton = {
                TextButton(onClick = { showScheduleDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun CallActionItem(
    icon: ImageVector,
    title: String,
    trailingIcon: ImageVector? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Black Circular Icon Container (Screenshot 2)
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E293B)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF0F172A),
            modifier = Modifier.weight(1f)
        )

        if (trailingIcon != null) {
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = Color(0xFF64748B),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
