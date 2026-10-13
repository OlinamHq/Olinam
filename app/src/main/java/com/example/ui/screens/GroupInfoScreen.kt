package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Conversation

private val GroupBlue = Color(0xFF0160E3)

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun GroupInfoScreen(
    conversation: Conversation,
    currentUserId: String,
    onBackClick: () -> Unit,
    onSaveGroupName: (String) -> Unit,
    onUpdateMembers: (List<String>) -> Unit
) {
    BackHandler { onBackClick() }
    var groupName by remember(conversation.id, conversation.title) { mutableStateOf(conversation.title) }
    var newMember by remember(conversation.id) { mutableStateOf("") }
    var showRemoveConfirm by remember { mutableStateOf<String?>(null) }
    val members = remember(conversation.participantIds) { conversation.participantIds.distinct() }

    Scaffold(
        containerColor = Color(0xFFF5F7FB),
        topBar = {
            TopAppBar(
                title = { Text("Group info", fontWeight = FontWeight.Bold, color = Color(0xFF111827)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF111827))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF5F7FB))
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier.size(76.dp).clip(CircleShape).background(Color(0xFFE5EEFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Group, contentDescription = null, tint = GroupBlue, modifier = Modifier.size(38.dp))
                        }
                        Text(groupName.ifBlank { "Group" }, fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Color(0xFF101828))
                        Text("${members.size} participants", fontSize = 13.sp, color = Color(0xFF667085))
                        OutlinedTextField(
                            value = groupName,
                            onValueChange = { groupName = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Group name") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )
                        Button(
                            onClick = { if (groupName.isNotBlank()) onSaveGroupName(groupName.trim()) },
                            enabled = groupName.isNotBlank() && groupName.trim() != conversation.title,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = GroupBlue)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Save group name")
                        }
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Add participant", fontWeight = FontWeight.Bold, color = Color(0xFF101828))
                        Text("Enter the participant's Olinam user ID or phone number.", fontSize = 12.sp, color = Color(0xFF667085))
                        OutlinedTextField(
                            value = newMember,
                            onValueChange = { newMember = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("User ID / phone number") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )
                        Button(
                            onClick = {
                                val value = newMember.trim()
                                if (value.isNotBlank() && value !in members) {
                                    onUpdateMembers((members + value).distinct())
                                    newMember = ""
                                }
                            },
                            enabled = newMember.isNotBlank() && newMember.trim() !in members,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = GroupBlue)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Add participant")
                        }
                    }
                }
            }
            item {
                Text("PARTICIPANTS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF667085), letterSpacing = 1.sp)
            }
            items(members, key = { it }) { member ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFE5EEFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(member.take(1).uppercase(), color = GroupBlue, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = if (member == currentUserId || member == "user_me" || member == "me") "$member (You)" else member,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF101828),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text("Group participant", fontSize = 12.sp, color = Color(0xFF667085))
                        }
                        if (member != currentUserId && member != "user_me" && member != "me") {
                            IconButton(onClick = { showRemoveConfirm = member }) {
                                Icon(Icons.Default.Close, contentDescription = "Remove participant", tint = Color(0xFFB42318))
                            }
                        }
                    }
                }
            }
            item {
                Text(
                    "Group admin permissions, secure invite links, polls and multi-person calling still require separate implementation and testing.",
                    fontSize = 12.sp,
                    color = Color(0xFF667085),
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
        }
    }

    showRemoveConfirm?.let { member ->
        AlertDialog(
            onDismissRequest = { showRemoveConfirm = null },
            title = { Text("Remove participant?") },
            text = { Text("Remove $member from this group?") },
            confirmButton = {
                TextButton(onClick = {
                    onUpdateMembers(members.filterNot { it == member })
                    showRemoveConfirm = null
                }) { Text("Remove", color = Color(0xFFB42318)) }
            },
            dismissButton = { TextButton(onClick = { showRemoveConfirm = null }) { Text("Cancel") } }
        )
    }
}
