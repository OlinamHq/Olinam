package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CallDirection
import com.example.model.CallType
import com.example.ui.ChatViewModel
import com.example.ui.components.formatMessageTime
import com.example.ui.theme.OlinamOnlineGreen
import com.example.ui.theme.OlinamPrimary

@Composable
fun CallsScreen(viewModel: ChatViewModel) {
    val callLogs by viewModel.callLogs.collectAsState()
    var showNewCallDialog by remember { mutableStateOf(false) }
    var activeOngoingCall by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("calls_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Calls",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            // Create Call Link Card
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showNewCallDialog = true }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(OlinamPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "New call",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Start a Call",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "End-to-End encrypted HD voice & video",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Recent",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(callLogs, key = { it.id }) { call ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = call.contactName.take(1).uppercase(),
                                color = OlinamPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = call.contactName,
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val directionIcon = when (call.direction) {
                                    CallDirection.INCOMING -> Icons.AutoMirrored.Filled.CallReceived
                                    CallDirection.OUTGOING -> Icons.AutoMirrored.Filled.CallMade
                                    CallDirection.MISSED -> Icons.AutoMirrored.Filled.CallMissed
                                }
                                val directionColor = when (call.direction) {
                                    CallDirection.MISSED -> Color(0xFFEF4444)
                                    else -> OlinamOnlineGreen
                                }
                                Icon(
                                    imageVector = directionIcon,
                                    contentDescription = null,
                                    tint = directionColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = formatMessageTime(call.timestamp),
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = {
                            activeOngoingCall = call.contactName
                            viewModel.initiateCall(call.contactName, call.callType)
                        }
                    ) {
                        Icon(
                            imageVector = if (call.callType == CallType.VIDEO) Icons.Default.Videocam else Icons.Default.Call,
                            contentDescription = "Call",
                            tint = OlinamPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Your personal calls are end-to-end encrypted",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { showNewCallDialog = true },
            containerColor = OlinamPrimary,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .size(56.dp)
        ) {
            Icon(Icons.Default.Call, contentDescription = "New Call", modifier = Modifier.size(24.dp))
        }
    }

    if (showNewCallDialog) {
        var contactName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewCallDialog = false },
            title = { Text("Start New Call", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = contactName,
                    onValueChange = { contactName = it },
                    label = { Text("Contact Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Row {
                    Button(
                        onClick = {
                            if (contactName.isNotBlank()) {
                                viewModel.initiateCall(contactName.trim(), CallType.VOICE)
                                activeOngoingCall = contactName.trim()
                                showNewCallDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OlinamPrimary)
                    ) {
                        Text("Voice")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (contactName.isNotBlank()) {
                                viewModel.initiateCall(contactName.trim(), CallType.VIDEO)
                                activeOngoingCall = contactName.trim()
                                showNewCallDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OlinamPrimary)
                    ) {
                        Text("Video")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewCallDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    activeOngoingCall?.let { name ->
        AlertDialog(
            onDismissRequest = { activeOngoingCall = null },
            icon = {
                Icon(Icons.Default.Call, contentDescription = null, tint = OlinamOnlineGreen, modifier = Modifier.size(36.dp))
            },
            title = { Text("Calling $name...", fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("End-to-End Encrypted Call (AES-256)", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("00:08", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = OlinamPrimary)
                }
            },
            confirmButton = {
                Button(
                    onClick = { activeOngoingCall = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("End Call")
                }
            }
        )
    }
}
