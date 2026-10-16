package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ContactHelper
import com.example.model.CallDirection
import com.example.model.CallLog
import com.example.model.CallType
import com.example.model.Conversation
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
    val context = LocalContext.current
    val callLogs by viewModel.callLogs.collectAsState()
    val conversations by viewModel.conversations.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    var showKeypadSheet by remember { mutableStateOf(false) }
    var showFavouritesSheet by remember { mutableStateOf(false) }
    var showAddFavouriteDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var selectedCallDetail by remember { mutableStateOf<CallLog?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    val keypadSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val favouritesSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val detailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
            // TOP BAR: "Calls", Search, 3-dots
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
                                    text = { Text("Add to favourites") },
                                    onClick = {
                                        showMenu = false
                                        showAddFavouriteDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Clear call history") },
                                    onClick = {
                                        showMenu = false
                                        showClearHistoryDialog = true
                                    }
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
                // 3 PRODUCTION QUICK ACTION BUTTONS ROW: Call, Keypad, Favourites (Schedule Removed)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        QuickCallButton(
                            icon = Icons.Default.Call,
                            label = "Call",
                            onClick = onOpenSelectContact
                        )
                        QuickCallButton(
                            icon = Icons.Default.Dialpad,
                            label = "Keypad",
                            onClick = { showKeypadSheet = true }
                        )
                        QuickCallButton(
                            icon = Icons.Default.Favorite,
                            label = "Favourites",
                            onClick = { showFavouritesSheet = true }
                        )
                    }
                }

                // "Recent" Section Header
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

                // Call Logs List
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
                            .clickable { selectedCallDetail = call }
                            .padding(vertical = 10.dp)
                            .testTag("call_item_${call.id}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isMissed) Color(0xFFFDE8E8) else Color(0xFFE0F2FE)
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

                        // Middle column: Name/number, direction arrow & timestamp
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = titleText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isMissed) Color(0xFFDC2626) else Color(0xFF0F172A)
                            )

                            if (!call.subtitleNote.isNullOrBlank()) {
                                Text(
                                    text = call.subtitleNote,
                                    fontSize = 13.sp,
                                    color = Color(0xFF64748B)
                                )
                            }

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

                        // Right: Call Icon Button (Instantly redials)
                        IconButton(
                            onClick = {
                                viewModel.startCall(
                                    contactName = call.contactName,
                                    contactPhone = call.phoneNumber.ifBlank { call.contactName },
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
    }

    // 1. Production Keypad Bottom Sheet (Real-time dialing, cellular SIM, Olinam HD voice & video)
    if (showKeypadSheet) {
        ModalBottomSheet(
            onDismissRequest = { showKeypadSheet = false },
            sheetState = keypadSheetState,
            containerColor = Color.White
        ) {
            KeypadSheetContent(
                conversations = conversations,
                onStartCall = { number, isVideo, matchedName ->
                    showKeypadSheet = false
                    viewModel.startCall(
                        contactName = matchedName ?: number,
                        contactPhone = number,
                        callType = if (isVideo) CallType.VIDEO else CallType.VOICE
                    )
                },
                onDirectPhoneCall = { number ->
                    showKeypadSheet = false
                    viewModel.makeDirectPhoneCall(context, number)
                }
            )
        }
    }

    // 2. Production Favourites Bottom Sheet
    if (showFavouritesSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFavouritesSheet = false },
            sheetState = favouritesSheetState,
            containerColor = Color.White
        ) {
            FavouritesSheetContent(
                conversations = conversations,
                onAddClick = { showAddFavouriteDialog = true },
                onVoiceCall = { conv ->
                    showFavouritesSheet = false
                    viewModel.startCall(
                        contactName = conv.title,
                        contactPhone = conv.phoneNumber ?: conv.title,
                        callType = CallType.VOICE
                    )
                },
                onVideoCall = { conv ->
                    showFavouritesSheet = false
                    viewModel.startCall(
                        contactName = conv.title,
                        contactPhone = conv.phoneNumber ?: conv.title,
                        callType = CallType.VIDEO
                    )
                },
                onCellularCall = { phone ->
                    showFavouritesSheet = false
                    viewModel.makeDirectPhoneCall(context, phone)
                },
                onRemoveFavourite = { convId ->
                    viewModel.toggleFavourite(convId, false)
                }
            )
        }
    }

    // 3. Add Contact to Favourites Dialog
    if (showAddFavouriteDialog) {
        AddFavouriteDialog(
            conversations = conversations,
            onDismiss = { showAddFavouriteDialog = false },
            onSelectConversation = { conv ->
                viewModel.toggleFavourite(conv.id, true)
                showAddFavouriteDialog = false
                android.widget.Toast.makeText(context, "${conv.title} added to favourites", android.widget.Toast.LENGTH_SHORT).show()
            }
        )
    }

    // 4. Call Details Bottom Sheet (when tapping a recent call item)
    if (selectedCallDetail != null) {
        val target = selectedCallDetail!!
        ModalBottomSheet(
            onDismissRequest = { selectedCallDetail = null },
            sheetState = detailSheetState,
            containerColor = Color.White
        ) {
            CallDetailSheetContent(
                call = target,
                onVoiceCall = {
                    selectedCallDetail = null
                    viewModel.startCall(
                        contactName = target.contactName,
                        contactPhone = target.phoneNumber.ifBlank { target.contactName },
                        callType = CallType.VOICE
                    )
                },
                onVideoCall = {
                    selectedCallDetail = null
                    viewModel.startCall(
                        contactName = target.contactName,
                        contactPhone = target.phoneNumber.ifBlank { target.contactName },
                        callType = CallType.VIDEO
                    )
                },
                onCellularCall = {
                    selectedCallDetail = null
                    viewModel.makeDirectPhoneCall(context, target.phoneNumber.ifBlank { target.contactName })
                },
                onSendMessage = {
                    selectedCallDetail = null
                    val convId = viewModel.getOrCreateConversationForContact(
                        name = target.contactName,
                        phoneNumber = target.phoneNumber.ifBlank { target.contactName },
                        isSms = false
                    )
                    viewModel.openConversation(convId, context)
                },
                onDeleteLog = {
                    viewModel.deleteCallLog(target.id)
                    selectedCallDetail = null
                    android.widget.Toast.makeText(context, "Call log removed", android.widget.Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    // 5. Clear Call History Confirmation Dialog
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = {
                Text("Clear call log?", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            },
            text = {
                Text("Do you want to clear your entire call history? This cannot be undone.", color = Color(0xFF475569))
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearCallHistory()
                        showClearHistoryDialog = false
                        android.widget.Toast.makeText(context, "Call history cleared", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Clear", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            }
        )
    }
}

// Circular Quick Action Button Component (Call, Keypad, Favourites)
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
            .padding(horizontal = 12.dp)
            .testTag("quick_action_$label")
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFF0F172A),
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF334155)
        )
    }
}

// Dialpad Sheet with Real-time Contact Suggestions
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KeypadSheetContent(
    conversations: List<Conversation> = emptyList(),
    onStartCall: (number: String, isVideo: Boolean, matchedName: String?) -> Unit,
    onDirectPhoneCall: (number: String) -> Unit = {}
) {
    var dialedNumber by remember { mutableStateOf("") }
    var selectedContactName by remember { mutableStateOf<String?>(null) }

    // Live search matching contacts based on entered digits
    val matchingContacts = remember(dialedNumber, conversations) {
        if (dialedNumber.length < 2) emptyList()
        else {
            val cleanDial = dialedNumber.filter { it.isDigit() }
            conversations.filter { conv ->
                val phoneClean = (conv.phoneNumber ?: "").filter { it.isDigit() }
                phoneClean.contains(cleanDial) || conv.title.contains(dialedNumber, ignoreCase = true)
            }.take(4)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Matched contact suggestions strip
        if (matchingContacts.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(matchingContacts) { contact ->
                    Surface(
                        modifier = Modifier.clickable {
                            val targetPhone = contact.phoneNumber ?: contact.title
                            dialedNumber = targetPhone
                            selectedContactName = contact.title
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFEFF6FF),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = contact.title,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = OlinamPrimary
                            )
                            if (!contact.phoneNumber.isNullOrBlank()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = contact.phoneNumber,
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Display contact name if identified
        if (!selectedContactName.isNullOrBlank()) {
            Text(
                text = selectedContactName!!,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = OlinamPrimary
            )
        }

        // Number display with click & long-press backspace
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
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
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .combinedClickable(
                            onClick = { dialedNumber = dialedNumber.dropLast(1) },
                            onLongClick = {
                                dialedNumber = ""
                                selectedContactName = null
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Backspace,
                        contentDescription = "Backspace",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Keypad grid (1-9, *, 0, #) with letters
        val keys = listOf(
            listOf("1" to "", "2" to "ABC", "3" to "DEF"),
            listOf("4" to "GHI", "5" to "JKL", "6" to "MNO"),
            listOf("7" to "PQRS", "8" to "TUV", "9" to "WXYZ"),
            listOf("*" to "", "0" to "+", "#" to "")
        )

        for (row in keys) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for ((digit, sub) in row) {
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                            .combinedClickable(
                                onClick = {
                                    dialedNumber += digit
                                    selectedContactName = null
                                },
                                onLongClick = {
                                    if (digit == "0") {
                                        dialedNumber += "+"
                                        selectedContactName = null
                                    }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = digit,
                                fontSize = 23.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            if (sub.isNotEmpty()) {
                                Text(
                                    text = sub,
                                    fontSize = 9.sp,
                                    color = Color(0xFF64748B),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 3 Real Action Buttons: Cellular SIM, Olinam HD Voice, Olinam HD Video
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Direct Cellular Phone Call (SIM)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5F9))
                        .clickable {
                            if (dialedNumber.isNotBlank()) {
                                onDirectPhoneCall(dialedNumber)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Cellular SIM Call",
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Cellular", fontSize = 11.5.sp, color = Color(0xFF64748B))
            }

            // 2. Olinam HD Voice Call (Royal Blue)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(OlinamPrimary)
                        .clickable {
                            if (dialedNumber.isNotBlank()) {
                                onStartCall(dialedNumber, false, selectedContactName)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Olinam HD Voice Call",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Olinam HD", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = OlinamPrimary)
            }

            // 3. Olinam HD Video Call
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5F9))
                        .clickable {
                            if (dialedNumber.isNotBlank()) {
                                onStartCall(dialedNumber, true, selectedContactName)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Olinam Video Call",
                        tint = OlinamPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Video", fontSize = 11.5.sp, color = Color(0xFF64748B))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// Production Favourites Bottom Sheet
@Composable
fun FavouritesSheetContent(
    conversations: List<Conversation>,
    onAddClick: () -> Unit,
    onVoiceCall: (Conversation) -> Unit,
    onVideoCall: (Conversation) -> Unit,
    onCellularCall: (String) -> Unit,
    onRemoveFavourite: (String) -> Unit
) {
    val favourites = remember(conversations) {
        conversations.filter { it.isFavourite }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Favourites",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = OlinamPrimary),
                shape = RoundedCornerShape(20.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (favourites.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEE2E2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Favourites",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No favourite contacts yet",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Add contacts here for fast, one-tap calling.",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(containerColor = OlinamPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("+ Add to Favourites")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(favourites, key = { it.id }) { conv ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(OlinamPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = conv.title.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = conv.title,
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F172A)
                            )
                            if (!conv.phoneNumber.isNullOrBlank()) {
                                Text(
                                    text = conv.phoneNumber,
                                    fontSize = 12.5.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        // Voice Call
                        IconButton(onClick = { onVoiceCall(conv) }) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Voice Call",
                                tint = OlinamPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Video Call
                        IconButton(onClick = { onVideoCall(conv) }) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Video Call",
                                tint = OlinamPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Remove from Favourites
                        IconButton(onClick = { onRemoveFavourite(conv.id) }) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Remove Favourite",
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// Add to Favourites Selection Dialog
@Composable
fun AddFavouriteDialog(
    conversations: List<Conversation>,
    onDismiss: () -> Unit,
    onSelectConversation: (Conversation) -> Unit
) {
    var search by remember { mutableStateOf("") }
    val candidates = remember(conversations, search) {
        conversations.filter { conv ->
            !conv.isFavourite &&
            (search.isBlank() || conv.title.contains(search, ignoreCase = true) || (conv.phoneNumber ?: "").contains(search))
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Favourites", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text("Search contacts...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (candidates.isEmpty()) {
                    Text(
                        text = "No contacts available to add.",
                        fontSize = 13.5.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    ) {
                        items(candidates) { conv ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectConversation(conv) }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE2E8F0)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = conv.title.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = conv.title, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                    if (!conv.phoneNumber.isNullOrBlank()) {
                                        Text(text = conv.phoneNumber, fontSize = 12.sp, color = Color(0xFF64748B))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Call Detail Sheet Content (Tapping a call item in Recent)
@Composable
fun CallDetailSheetContent(
    call: CallLog,
    onVoiceCall: () -> Unit,
    onVideoCall: () -> Unit,
    onCellularCall: () -> Unit,
    onSendMessage: () -> Unit,
    onDeleteLog: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Large Avatar
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0F2FE)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = call.contactName.take(1).uppercase(),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = OlinamPrimary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = call.contactName,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        if (call.phoneNumber.isNotBlank()) {
            Text(
                text = call.phoneNumber,
                fontSize = 14.sp,
                color = Color(0xFF64748B)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Direction & Duration info
        Row(verticalAlignment = Alignment.CenterVertically) {
            when (call.direction) {
                CallDirection.MISSED -> {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.CallMissed,
                        contentDescription = "Missed",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Missed call", color = Color(0xFFDC2626), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
                CallDirection.INCOMING -> {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.CallReceived,
                        contentDescription = "Incoming",
                        tint = OlinamOnlineGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Incoming call", color = OlinamOnlineGreen, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
                CallDirection.OUTGOING -> {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.CallMade,
                        contentDescription = "Outgoing",
                        tint = OlinamOnlineGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Outgoing call", color = OlinamOnlineGreen, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "•  ${formatMessageTime(call.timestamp)}",
                color = Color(0xFF64748B),
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4 Action Circular Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Voice Call
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEFF6FF))
                        .clickable(onClick = onVoiceCall),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Call, contentDescription = "Voice Call", tint = OlinamPrimary, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Voice", fontSize = 12.sp, color = Color(0xFF475569))
            }

            // Video Call
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEFF6FF))
                        .clickable(onClick = onVideoCall),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = OlinamPrimary, modifier = Modifier.size(26.dp))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Video", fontSize = 12.sp, color = Color(0xFF475569))
            }

            // Cellular Call
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5F9))
                        .clickable(onClick = onCellularCall),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Phone, contentDescription = "Cellular Call", tint = Color(0xFF0F172A), modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Cellular", fontSize = 12.sp, color = Color(0xFF475569))
            }

            // Message Chat
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5F9))
                        .clickable(onClick = onSendMessage),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Message, contentDescription = "Message", tint = Color(0xFF0F172A), modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Message", fontSize = 12.sp, color = Color(0xFF475569))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(color = Color(0xFFF1F5F9))
        Spacer(modifier = Modifier.height(10.dp))

        // Delete from call history button
        TextButton(
            onClick = onDeleteLog,
            colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = Color(0xFFDC2626))
        ) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Remove from call log", fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
