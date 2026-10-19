package com.example.ui.screens.privacy

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OlinamPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockedContactsScreen(
    privacyPrefs: PrivacyPreferences,
    onBackClick: () -> Unit
) {
    BackHandler { onBackClick() }

    val context = LocalContext.current
    val state by privacyPrefs.state.collectAsState()
    val primaryBlue = OlinamPrimary

    var showAddBlockDialog by remember { mutableStateOf(false) }
    var newBlockInput by remember { mutableStateOf("") }
    var contactToUnblock by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("blocked_contacts_screen"),
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Blocked accounts",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF101828)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF101828)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showAddBlockDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Block new contact",
                            tint = primaryBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        if (state.blockedContacts.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(60.dp))
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(Color(0xFFEBF3FE), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = null,
                        tint = primaryBlue,
                        modifier = Modifier.size(46.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "No blocked accounts",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF101828)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Blocked contacts will no longer be able to call you or send you messages on Olinam. Tap + to add a number to your block list.",
                    fontSize = 14.sp,
                    color = Color(0xFF667085),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                item {
                    Text(
                        text = "Blocked contacts can't call or message you. Tap to unblock.",
                        fontSize = 13.5.sp,
                        color = Color(0xFF667085),
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp)
                    )
                }

                items(state.blockedContacts.toList()) { contact ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { contactToUnblock = contact }
                            .padding(horizontal = 24.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(Color(0xFFF2F4F7), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = null,
                                tint = Color(0xFF98A2B3),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = contact,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF101828)
                            )
                            Text(
                                text = "Blocked • Tap to unblock",
                                fontSize = 13.sp,
                                color = Color(0xFF98A2B3)
                            )
                        }
                    }

                    HorizontalDivider(
                        color = Color(0xFFF2F4F7),
                        thickness = 1.dp,
                        modifier = Modifier.padding(start = 82.dp)
                    )
                }
            }
        }
    }

    if (showAddBlockDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddBlockDialog = false
                newBlockInput = ""
            },
            title = { Text("Block a contact or number", fontWeight = FontWeight.SemiBold) },
            text = {
                OutlinedTextField(
                    value = newBlockInput,
                    onValueChange = { newBlockInput = it },
                    label = { Text("Phone number or name") },
                    placeholder = { Text("+91 98765 43210") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newBlockInput.isNotBlank()) {
                            privacyPrefs.blockContact(newBlockInput.trim())
                            Toast.makeText(context, "${newBlockInput.trim()} blocked", Toast.LENGTH_SHORT).show()
                            showAddBlockDialog = false
                            newBlockInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryBlue)
                ) {
                    Text("Block")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddBlockDialog = false
                    newBlockInput = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (contactToUnblock != null) {
        AlertDialog(
            onDismissRequest = { contactToUnblock = null },
            title = { Text("Unblock $contactToUnblock?", fontWeight = FontWeight.SemiBold) },
            text = { Text("This contact will now be able to call you and send you messages.") },
            confirmButton = {
                Button(
                    onClick = {
                        contactToUnblock?.let {
                            privacyPrefs.unblockContact(it)
                            Toast.makeText(context, "$it unblocked", Toast.LENGTH_SHORT).show()
                        }
                        contactToUnblock = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryBlue)
                ) {
                    Text("Unblock")
                }
            },
            dismissButton = {
                TextButton(onClick = { contactToUnblock = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
