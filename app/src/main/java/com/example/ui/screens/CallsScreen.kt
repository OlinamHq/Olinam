package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CallDirection
import com.example.model.CallLog
import com.example.model.CallType
import com.example.ui.ChatViewModel
import com.example.ui.components.formatMessageTime
import com.example.ui.theme.OlinamOnlineGreen
import com.example.ui.theme.OlinamPrimary
import com.example.ui.theme.OlinamPrimaryContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallsScreen(
    viewModel: ChatViewModel,
    onOpenSelectContact: () -> Unit = {}
) {
    val callLogs by viewModel.callLogs.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    var showKeypadSheet by remember { mutableStateOf(false) }
    var showScheduleDialog by remember { mutableStateOf(false) }
    var showFavouritesDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    val keypadSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val filteredLogs = remember(callLogs, searchQuery) {
        if (searchQuery.isBlank()) callLogs
        else callLogs.filter {
            it.contactName.contains(searchQuery, ignoreCase = true) ||
                    it.phoneNumber.contains(searchQuery) ||
                    (it.subtitleNote?.contains(searchQuery, ignoreCase = true) == true)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .testTag("calls_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // TOP BAR: "Calls", Search, 3-dots (Screenshot 4)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSearching) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search calls...") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        trailingIcon = {
                            IconButton(onClick = {
                                isSearching = false
                                searchQuery = ""
                            }) {
                                Text("✕", color = Color(0xFF64748B))
                            }
                        }
                    )
                } else {
                    Text(
                        text = "Calls",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        letterSpacing = (-0.5).sp
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { isSearching = true },
                            modifier = Modifier.testTag("calls_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search calls",
                                tint = Color(0xFF1E293B),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier.testTag("calls_menu_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More",
                                    tint = Color(0xFF1E293B),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Simulate Incoming Call") },
                                    onClick = {
                                        showMenu = false
                                        viewModel.simulateIncomingCall(
                                            contactName = "Incoming Call",
                                            contactPhone = "+91 98765 43210",
                                            callType = CallType.VOICE
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Clear call log") },
                                    onClick = { showMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Settings") },
                                    onClick = { showMenu = false }
                                )
                            }
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // 4 QUICK ACTION BUTTONS ROW (Screenshot 4)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        QuickCallButton(
                            icon = Icons.Default.Call,
                            label = "Call",
                            onClick = onOpenSelectContact
                        )
                        QuickCallButton(
                            icon = Icons.Default.CalendarMonth,
                            label = "Schedule",
                            onClick = { showScheduleDialog = true }
                        )
                        QuickCallButton(
                            icon = Icons.Default.Dialpad,
                            label = "Keypad",
                            onClick = { showKeypadSheet = true }
                        )
                        QuickCallButton(
                            icon = Icons.Default.Favorite,
                            label = "Favourites",
                            onClick = { showFavouritesDialog = true }
                        )
                    }
                }

                // "Recent" Section Header (Screenshot 4)
                item {
                    Text(
                        text = "Recent",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        modifier = Modifier.padding(top = 10.dp, bottom = 8.dp)
                    )
                }

                if (filteredLogs.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 42.dp, horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                                    .background(OlinamPrimaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Calls",
                                    tint = OlinamPrimary,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "No recent calls",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "To make a call, tap the Call button or dial a number using the Keypad.",
                                fontSize = 13.5.sp,
                                color = Color(0xFF64748B),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Button(
                                onClick = onOpenSelectContact,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = OlinamPrimary,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Start a call")
                            }
                        }
                    }
                }

                // Call Logs List (Screenshot 4)
                items(filteredLogs, key = { it.id }) { call ->
                    val isMissed = call.direction == CallDirection.MISSED
                    val titleText = if (call.count > 1) {
                        "${call.contactName} (${call.count})"
                    } else {
                        call.contactName
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.startCall(
                                    contactName = call.contactName,
                                    contactPhone = call.phoneNumber.ifBlank { "+91 97738 62847" },
                                    callType = call.callType
                                )
                            }
                            .padding(vertical = 10.dp)
                            .testTag("call_item_${call.id}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar (Screenshot 4)
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(
                                    if (call.contactName.contains("sanju", ignoreCase = true) || call.contactName.contains("89207")) {
                                        Color(0xFFFDE8E8)
                                    } else {
                                        Color(0xFFE0F2FE)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (call.subtitleNote?.replace("~", "")?.trim()?.take(1)
                                    ?: call.contactName.take(1)).uppercase(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isMissed) Color(0xFFDC2626) else OlinamPrimary
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Middle column: Phone number / Name, ~ subtitle, Arrow & Timestamp
                        Column(modifier = Modifier.weight(1f)) {
                            // Title: Red if missed call, Black if answered/outgoing
                            Text(
                                text = titleText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isMissed) Color(0xFFDC2626) else Color(0xFF0F172A)
                            )

                            // Subtitle 1: ~ sanju / ~ sonu
                            if (!call.subtitleNote.isNullOrBlank()) {
                                Text(
                                    text = call.subtitleNote,
                                    fontSize = 13.sp,
                                    color = Color(0xFF64748B)
                                )
                            }

                            // Subtitle 2: Direction arrow & timestamp (e.g. ↙ Yesterday, 8:05 pm)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                when (call.direction) {
                                    CallDirection.MISSED -> {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.CallMissed,
                                            contentDescription = "Missed call",
                                            tint = Color(0xFFDC2626),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                    CallDirection.INCOMING -> {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.CallReceived,
                                            contentDescription = "Incoming call",
                                            tint = OlinamOnlineGreen,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                    CallDirection.OUTGOING -> {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.CallMade,
                                            contentDescription = "Outgoing call",
                                            tint = OlinamOnlineGreen,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                Text(
                                    text = formatMessageTime(call.timestamp),
                                    fontSize = 12.5.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        // Right: Call Icon Button (Phone / Video)
                        IconButton(
                            onClick = {
                                viewModel.startCall(
                                    contactName = call.contactName,
                                    contactPhone = call.phoneNumber.ifBlank { "+91 97738 62847" },
                                    callType = call.callType
                                )
                            },
                            modifier = Modifier.testTag("call_action_button_${call.id}")
                        ) {
                            Icon(
                                imageVector = if (call.callType == CallType.VIDEO) Icons.Default.Videocam else Icons.Default.Call,
                                contentDescription = "Call",
                                tint = Color(0xFF1E293B),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }

        // FLOATING ACTION BUTTON (Royal Blue FAB at bottom right with Phone+ icon)
        FloatingActionButton(
            onClick = onOpenSelectContact,
            shape = CircleShape,
            containerColor = OlinamPrimary,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(4.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 18.dp, end = 18.dp)
                .size(56.dp)
                .testTag("calls_fab_new_call")
        ) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "New Call",
                modifier = Modifier.size(24.dp)
            )
        }
    }

    // Keypad Bottom Sheet (allows dialing any number and starting audio/video call)
    if (showKeypadSheet) {
        ModalBottomSheet(
            onDismissRequest = { showKeypadSheet = false },
            sheetState = keypadSheetState,
            containerColor = Color.White
        ) {
            KeypadSheetContent(
                onStartCall = { number, isVideo ->
                    showKeypadSheet = false
                    viewModel.startCall(
                        contactName = number,
                        contactPhone = number,
                        callType = if (isVideo) CallType.VIDEO else CallType.VOICE
                    )
                }
            )
        }
    }

    // Schedule Call Dialog
    if (showScheduleDialog) {
        var scheduleTitle by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showScheduleDialog = false },
            title = { Text("Schedule a Call") },
            text = {
                Column {
                    OutlinedTextField(
                        value = scheduleTitle,
                        onValueChange = { scheduleTitle = it },
                        label = { Text("Call Subject / Participant") },
                        placeholder = { Text("e.g. Project Sync") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Meeting will be scheduled for tomorrow at 10:00 AM.",
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

    // Favourites Dialog
    if (showFavouritesDialog) {
        AlertDialog(
            onDismissRequest = { showFavouritesDialog = false },
            title = { Text("Favourite Contacts") },
            text = {
                Column {
                    Text("No favourite contacts added yet.", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Add contacts to favourites for one-tap calling.",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showFavouritesDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = OlinamPrimary)
                ) {
                    Text("Done")
                }
            }
        )
    }
}

// Circular Quick Action Button Component (matching Screenshot 4: Call, Schedule, Keypad, Favourites)
@Composable
fun QuickCallButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp)
            .testTag("quick_action_$label")
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFF0F172A),
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF334155)
        )
    }
}

// Dialpad Sheet for dialing numbers
@Composable
fun KeypadSheetContent(
    onStartCall: (number: String, isVideo: Boolean) -> Unit
) {
    var dialedNumber by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Number display
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dialedNumber.ifEmpty { "Enter phone number" },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (dialedNumber.isEmpty()) Color(0xFF94A3B8) else Color(0xFF0F172A)
            )
            if (dialedNumber.isNotEmpty()) {
                Spacer(modifier = Modifier.width(16.dp))
                IconButton(onClick = { dialedNumber = dialedNumber.dropLast(1) }) {
                    Icon(
                        imageVector = Icons.Default.Backspace,
                        contentDescription = "Backspace",
                        tint = Color(0xFF64748B)
                    )
                }
            }
        }

        // Keypad grid
        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("*", "0", "#")
        )

        for (row in keys) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (key in row) {
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                            .clickable { dialedNumber += key },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = key,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Call Action Buttons (Audio & Video)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Video Call button
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F5F9))
                    .clickable {
                        if (dialedNumber.isNotBlank()) {
                            onStartCall(dialedNumber, true)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = "Video Call",
                    tint = OlinamPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Audio Call button (Royal Blue)
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(OlinamPrimary)
                    .clickable {
                        if (dialedNumber.isNotBlank()) {
                            onStartCall(dialedNumber, false)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Audio Call",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
