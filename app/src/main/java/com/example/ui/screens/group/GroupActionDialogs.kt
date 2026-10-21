package com.example.ui.screens.group

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EditGroupNameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var nameInput by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter group subject", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { if (it.length <= 100) nameInput = it },
                    singleLine = true,
                    label = { Text("Group Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clean = nameInput.trim()
                    if (clean.isNotBlank()) onConfirm(clean)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0160E3))
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF64748B))
            }
        }
    )
}

@Composable
fun EditGroupDescriptionDialog(
    currentDescription: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var descInput by remember { mutableStateOf(currentDescription) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit group description", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column {
                OutlinedTextField(
                    value = descInput,
                    onValueChange = { descInput = it },
                    maxLines = 5,
                    label = { Text("Description") },
                    placeholder = { Text("Add a group description to let members know what this group is about.") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(descInput.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0160E3))
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF64748B))
            }
        }
    )
}

@Composable
fun AddGroupMemberDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var phoneInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add group participant", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column {
                Text(
                    text = "Enter the phone number or name of the participant to add to this group.",
                    fontSize = 13.5.sp,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it },
                    singleLine = true,
                    label = { Text("Phone number or Contact name") },
                    placeholder = { Text("+1 (555) 000-0000") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clean = phoneInput.trim()
                    if (clean.isNotBlank()) onConfirm(clean)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0160E3))
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF64748B))
            }
        }
    )
}

@Composable
fun ExitGroupConfirmDialog(
    groupTitle: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Exit \"$groupTitle\" group?", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Text(
                text = "Only group admins will be notified that you left the group. You will no longer be able to send or receive messages in this group.",
                fontSize = 14.sp,
                color = Color(0xFF64748B),
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
            ) {
                Text("Exit Group", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF64748B))
            }
        }
    )
}

@Composable
fun RemoveMemberConfirmDialog(
    memberName: String,
    groupTitle: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Remove $memberName?", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Text(
                text = "Are you sure you want to remove $memberName from \"$groupTitle\"? They will no longer be able to send or receive messages in this group.",
                fontSize = 14.sp,
                color = Color(0xFF64748B),
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
            ) {
                Text("Remove", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF64748B))
            }
        }
    )
}
