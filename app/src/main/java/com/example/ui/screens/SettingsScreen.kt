package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.ui.screens.notifications.NotificationsScreen
import com.example.ui.screens.storage.StorageAndDataScreen
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.SmsHelper
import com.example.ui.ChatViewModel
import com.example.ui.theme.OlinamPrimary

import com.example.ui.screens.privacy.PrivacyScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ChatViewModel,
    onBackClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onQrClick: () -> Unit,
    onSecurityClick: () -> Unit,
    onConnectedServicesClick: () -> Unit
) {
    BackHandler { onBackClick() }

    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var activeInfoDialogTitle by remember { mutableStateOf<String?>(null) }
    var activeInfoDialogContent by remember { mutableStateOf<String?>(null) }
    var showAccountScreen by remember { mutableStateOf(false) }
    var showPrivacyScreen by remember { mutableStateOf(false) }
    var showLanguageScreen by remember { mutableStateOf(false) }
    var showStorageScreen by remember { mutableStateOf(false) }
    var showNotificationsScreen by remember { mutableStateOf(false) }

    val appPrefs = remember { context.getSharedPreferences("olinam_app_prefs", android.content.Context.MODE_PRIVATE) }
    var currentLanguageName by remember {
        mutableStateOf(appPrefs.getString("app_language_name", "English (Device's language)") ?: "English (Device's language)")
    }

    if (showAccountScreen) {
        AccountScreen(
            viewModel = viewModel,
            onBackClick = { showAccountScreen = false }
        )
        return
    }

    if (showPrivacyScreen) {
        PrivacyScreen(
            viewModel = viewModel,
            onBackClick = { showPrivacyScreen = false }
        )
        return
    }

    if (showLanguageScreen) {
        AppLanguageScreen(
            onBackClick = { showLanguageScreen = false },
            onLanguageSelected = { lang ->
                currentLanguageName = if (lang.isDeviceDefault) {
                    "English (Device's language)"
                } else {
                    "${lang.nativeName} (${lang.englishName})"
                }
            }
        )
        return
    }

    if (showStorageScreen) {
        StorageAndDataScreen(
            onBackClick = { showStorageScreen = false }
        )
        return
    }

    if (showNotificationsScreen) {
        NotificationsScreen(
            onBackClick = { showNotificationsScreen = false }
        )
        return
    }

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
        containerColor = Color(0xFFF8F9FB),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        color = Color(0xFF101828)
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
                            tint = Color(0xFF101828)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8F9FB))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Profile Card (Modern soft blue pastel container with camera badge and QR icon)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEditProfileClick() },
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEBF3FF)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Decorative circular highlight in the top-right corner
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 24.dp, y = (-20).dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDCEBFE).copy(alpha = 0.85f))
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User Avatar with circular camera overlay
                        Box(
                            modifier = Modifier.size(66.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(66.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF7FAFF5)),
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
                                        modifier = Modifier.size(42.dp)
                                    )
                                }
                            }

                            // Camera badge on bottom right
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E293B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Change photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // User Name, Phone, and Encryption Status
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = currentUser.name.ifBlank { "Om Kunwar" },
                                fontSize = 17.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currentUser.phoneNumber.ifBlank { "+91 98765 43210" },
                                fontSize = 13.5.sp,
                                color = Color(0xFF475467),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.5.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF22C55E))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (currentUser.statusMessage.isNotBlank()) currentUser.statusMessage else "Using Olinam with End-to-End Encryption",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF475467),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // QR Code and Arrow
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onQrClick,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCode2,
                                    contentDescription = "QR Code",
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Card 1: Account & Connected apps
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F3F5))
            ) {
                Column {
                    ModernSettingsItem(
                        icon = Icons.Default.Person,
                        iconBgColor = Color(0xFFEBF3FE),
                        iconTint = Color(0xFF2563EB),
                        title = "Account",
                        subtitle = "Security, notifications, change number",
                        onClick = {
                            showAccountScreen = true
                        }
                    )
                    HorizontalDivider(
                        color = Color(0xFFF2F4F7),
                        thickness = 1.dp,
                        modifier = Modifier.padding(start = 74.dp, end = 16.dp)
                    )
                    ModernSettingsItem(
                        icon = Icons.Default.Link,
                        iconBgColor = Color(0xFFE8F8EE),
                        iconTint = Color(0xFF16A34A),
                        title = "Connected apps",
                        subtitle = "Gmail, Calendar, Contacts, Drive and more",
                        onClick = onConnectedServicesClick
                    )
                }
            }

            // Card 2: Privacy, Chats, Notifications
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F3F5))
            ) {
                Column {
                    ModernSettingsItem(
                        icon = Icons.Default.Shield,
                        iconBgColor = Color(0xFFF1EEFD),
                        iconTint = Color(0xFF7C3AED),
                        title = "Privacy",
                        subtitle = "Block contacts, disappearing messages",
                        onClick = { showPrivacyScreen = true }
                    )
                    HorizontalDivider(
                        color = Color(0xFFF2F4F7),
                        thickness = 1.dp,
                        modifier = Modifier.padding(start = 74.dp, end = 16.dp)
                    )
                    ModernSettingsItem(
                        icon = Icons.Default.Chat,
                        iconBgColor = Color(0xFFFFECEF),
                        iconTint = Color(0xFFE11D48),
                        title = "Chats",
                        subtitle = "Theme, wallpapers, chat history, backup",
                        onClick = {
                            activeInfoDialogTitle = "Chats"
                            activeInfoDialogContent = "Customize chat themes, wallpapers, chat font size, cloud backup, and conversation history."
                        }
                    )
                    HorizontalDivider(
                        color = Color(0xFFF2F4F7),
                        thickness = 1.dp,
                        modifier = Modifier.padding(start = 74.dp, end = 16.dp)
                    )
                    ModernSettingsItem(
                        icon = Icons.Default.Notifications,
                        iconBgColor = Color(0xFFFEF6E6),
                        iconTint = Color(0xFFD97706),
                        title = "Notifications",
                        subtitle = "Message, group & call tones, vibration",
                        onClick = {
                            showNotificationsScreen = true
                        }
                    )
                }
            }

            // Card 3: Default SMS App & Storage and data
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F3F5))
            ) {
                Column {
                    ModernSettingsItem(
                        icon = Icons.Default.Forum,
                        iconBgColor = Color(0xFFEEEDFD),
                        iconTint = Color(0xFF6366F1),
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
                    HorizontalDivider(
                        color = Color(0xFFF2F4F7),
                        thickness = 1.dp,
                        modifier = Modifier.padding(start = 74.dp, end = 16.dp)
                    )
                    ModernSettingsItem(
                        icon = Icons.Default.BarChart,
                        iconBgColor = Color(0xFFE0F7F6),
                        iconTint = Color(0xFF0D9488),
                        title = "Storage and data",
                        subtitle = "Network usage, auto-download",
                        onClick = {
                            showStorageScreen = true
                        }
                    )
                }
            }

            // Card 4: App language & Help & support
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F3F5))
            ) {
                Column {
                    ModernSettingsItem(
                        icon = Icons.Default.Language,
                        iconBgColor = Color(0xFFFDECEE),
                        iconTint = Color(0xFFEF4444),
                        title = "App language",
                        subtitle = currentLanguageName,
                        onClick = { showLanguageScreen = true }
                    )
                    HorizontalDivider(
                        color = Color(0xFFF2F4F7),
                        thickness = 1.dp,
                        modifier = Modifier.padding(start = 74.dp, end = 16.dp)
                    )
                    ModernSettingsItem(
                        icon = Icons.Default.Info,
                        iconBgColor = Color(0xFFE0F2FE),
                        iconTint = Color(0xFF0284C7),
                        title = "Help & support",
                        subtitle = "Get help, contact us, privacy policy",
                        onClick = {
                            activeInfoDialogTitle = "Help & support"
                            activeInfoDialogContent = "Olinam Messenger v1.0. End-to-end encrypted chats with live WebRTC calling, group messaging, and Oj AI assistant."
                        }
                    )
                }
            }

            // Card 5: Invite friend & Log out
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F3F5))
            ) {
                Column {
                    ModernSettingsItem(
                        icon = Icons.Default.Share,
                        iconBgColor = Color(0xFFECFDF5),
                        iconTint = Color(0xFF059669),
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
                        color = Color(0xFFF2F4F7),
                        thickness = 1.dp,
                        modifier = Modifier.padding(start = 74.dp, end = 16.dp)
                    )
                    ModernSettingsItem(
                        icon = Icons.Default.Logout,
                        iconBgColor = Color(0xFFFEE2E2),
                        iconTint = Color(0xFFDC2626),
                        title = "Log out",
                        subtitle = "Sign out from this device",
                        titleColor = Color(0xFFDC2626),
                        onClick = { showLogoutDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
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

    // Information dialog
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
fun ModernSettingsItem(
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    titleColor: Color = Color(0xFF101828),
    subtitleColor: Color = Color(0xFF667085),
    showChevron: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(23.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 15.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.8.sp,
                color = subtitleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (showChevron) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF98A2B3),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
