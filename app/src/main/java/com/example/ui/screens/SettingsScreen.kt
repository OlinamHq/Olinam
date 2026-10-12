package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.data.SmsHelper
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.ChatViewModel
import com.example.ui.theme.OlinamPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ChatViewModel,
    onBackClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onQrClick: () -> Unit,
    onSecurityClick: () -> Unit
) {
    BackHandler { onBackClick() }

    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var activeInfoDialogTitle by remember { mutableStateOf<String?>(null) }
    var activeInfoDialogContent by remember { mutableStateOf<String?>(null) }

    var isDefaultSms by remember { mutableStateOf(SmsHelper.isDefaultSmsApp(context)) }
    val defaultSmsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        isDefaultSms = SmsHelper.isDefaultSmsApp(context)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = Color(0xFF111B21)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF111B21)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // WhatsApp User Profile Header Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEditProfileClick() }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFCBD5E1)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!currentUser.avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = currentUser.avatarUrl,
                            contentDescription = "Profile Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentUser.name.ifBlank { "Olinam User" },
                        fontSize = 17.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF111B21),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = currentUser.statusMessage.ifBlank { "Hello! I'm using Olinam" },
                        fontSize = 13.5.sp,
                        color = Color(0xFF667781),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onQrClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = "QR Code",
                        tint = OlinamPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            HorizontalDivider(
                color = Color(0xFFF0F2F5),
                thickness = 1.dp
            )

            // Settings Items List
            SettingsMenuItem(
                icon = Icons.Default.Key,
                title = "Account",
                subtitle = "Security notifications, change number",
                onClick = {
                    activeInfoDialogTitle = "Account Settings"
                    activeInfoDialogContent = "Manage your registered phone number (${currentUser.phoneNumber.ifBlank { "Not set" }}), passkeys, and account security notifications."
                }
            )

            SettingsMenuItem(
                icon = Icons.Default.Lock,
                title = "Privacy",
                subtitle = "Block contacts, disappearing messages, E2EE",
                onClick = onSecurityClick
            )

            SettingsMenuItem(
                icon = Icons.Default.Chat,
                title = "Chats",
                subtitle = "Theme, wallpapers, chat history, backup",
                onClick = {
                    activeInfoDialogTitle = "Chats Settings"
                    activeInfoDialogContent = "Chat themes, light/dark mode, cloud backups, and export options are configured."
                }
            )

            SettingsMenuItem(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                subtitle = "Message, group & call tones, vibration",
                onClick = {
                    activeInfoDialogTitle = "Notification Preferences"
                    activeInfoDialogContent = "High-priority push notifications and sound alerts are active for all conversations."
                }
            )

            SettingsMenuItem(
                icon = Icons.Default.Chat,
                title = "Default SMS App",
                subtitle = if (isDefaultSms) "Olinam is active as your default SMS app" else "Tap to set Olinam as default SMS app",
                onClick = {
                    val intent = SmsHelper.createDefaultSmsIntent(context)
                    if (intent != null) {
                        try {
                            defaultSmsLauncher.launch(intent)
                        } catch (_: Exception) {
                            try { context.startActivity(intent) } catch (_: Exception) {}
                        }
                    }
                }
            )

            SettingsMenuItem(
                icon = Icons.Default.Storage,
                title = "Storage and data",
                subtitle = "Network usage, auto-download",
                onClick = {
                    activeInfoDialogTitle = "Storage & Data"
                    activeInfoDialogContent = "Manage your network usage and media auto-download settings."
                }
            )

            SettingsMenuItem(
                icon = Icons.Default.Language,
                title = "App language",
                subtitle = "English (phone's language)",
                onClick = {
                    activeInfoDialogTitle = "App Language"
                    activeInfoDialogContent = "Olinam supports English and Hindi. The app uses your default system language."
                }
            )

            SettingsMenuItem(
                icon = Icons.Default.Help,
                title = "Help",
                subtitle = "Help center, contact us, privacy policy",
                onClick = {
                    activeInfoDialogTitle = "Help & Support"
                    activeInfoDialogContent = "Olinam Messaging App v1.0. End-to-end encrypted chats with live WebRTC calling and Oj AI assistant."
                }
            )

            SettingsMenuItem(
                icon = Icons.Default.Share,
                title = "Invite a friend",
                subtitle = "Share Olinam link with your contacts",
                onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Join me on Olinam")
                        putExtra(Intent.EXTRA_TEXT, "Hey! Chat with me on Olinam - fast, private messaging: https://olinam.im/download")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Invite friends"))
                }
            )

            HorizontalDivider(
                color = Color(0xFFF0F2F5),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Log out Item (Red)
            SettingsMenuItem(
                icon = Icons.Default.Logout,
                title = "Log out",
                subtitle = "Sign out from this device",
                titleColor = Color(0xFFDC2626),
                onClick = { showLogoutDialog = true }
            )

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log out of Olinam?", fontWeight = FontWeight.Bold) },
            text = { Text("You will need to sign in with your phone number again to access your messages.") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Log out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Info modal dialog
    if (activeInfoDialogTitle != null) {
        AlertDialog(
            onDismissRequest = {
                activeInfoDialogTitle = null
                activeInfoDialogContent = null
            },
            title = { Text(activeInfoDialogTitle ?: "", fontWeight = FontWeight.Bold) },
            text = { Text(activeInfoDialogContent ?: "") },
            confirmButton = {
                TextButton(onClick = {
                    activeInfoDialogTitle = null
                    activeInfoDialogContent = null
                }) {
                    Text("OK", color = OlinamPrimary)
                }
            }
        )
    }
}

@Composable
fun SettingsMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    titleColor: Color = Color(0xFF111B21),
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (titleColor != Color(0xFF111B21)) titleColor else Color(0xFF54656F),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(20.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = titleColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = Color(0xFF667781),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
