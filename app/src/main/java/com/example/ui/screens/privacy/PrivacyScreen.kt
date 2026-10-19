package com.example.ui.screens.privacy

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ChatViewModel
import com.example.ui.theme.OlinamPrimary

enum class PrivacySubScreen {
    MAIN,
    CHECKUP,
    LAST_SEEN,
    PROFILE_PHOTO,
    ABOUT,
    STATUS,
    DEFAULT_TIMER,
    GROUPS,
    LIVE_LOCATION,
    CALLS,
    BLOCKED_CONTACTS,
    CHAT_LOCK,
    ADVANCED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(
    viewModel: ChatViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val privacyPrefs = remember { PrivacyPreferences(context) }
    val state by privacyPrefs.state.collectAsState()

    var currentScreen by remember { mutableStateOf(PrivacySubScreen.MAIN) }

    when (currentScreen) {
        PrivacySubScreen.CHECKUP -> {
            PrivacyCheckupScreen(
                onNavigateToWhoCanContact = { currentScreen = PrivacySubScreen.CALLS },
                onNavigateToPersonalInfo = { currentScreen = PrivacySubScreen.PROFILE_PHOTO },
                onNavigateToChatPrivacy = { currentScreen = PrivacySubScreen.DEFAULT_TIMER },
                onNavigateToAccountProtection = { currentScreen = PrivacySubScreen.ADVANCED },
                onBackClick = { currentScreen = PrivacySubScreen.MAIN }
            )
            return
        }
        PrivacySubScreen.LAST_SEEN -> {
            LastSeenPrivacyScreen(privacyPrefs = privacyPrefs, onBackClick = { currentScreen = PrivacySubScreen.MAIN })
            return
        }
        PrivacySubScreen.PROFILE_PHOTO -> {
            ProfilePicturePrivacyScreen(privacyPrefs = privacyPrefs, onBackClick = { currentScreen = PrivacySubScreen.MAIN })
            return
        }
        PrivacySubScreen.ABOUT -> {
            AboutPrivacyScreen(privacyPrefs = privacyPrefs, onBackClick = { currentScreen = PrivacySubScreen.MAIN })
            return
        }
        PrivacySubScreen.STATUS -> {
            StatusPrivacyScreen(privacyPrefs = privacyPrefs, onBackClick = { currentScreen = PrivacySubScreen.MAIN })
            return
        }
        PrivacySubScreen.DEFAULT_TIMER -> {
            DefaultMessageTimerScreen(privacyPrefs = privacyPrefs, onBackClick = { currentScreen = PrivacySubScreen.MAIN })
            return
        }
        PrivacySubScreen.GROUPS -> {
            GroupsPrivacyScreen(privacyPrefs = privacyPrefs, onBackClick = { currentScreen = PrivacySubScreen.MAIN })
            return
        }
        PrivacySubScreen.LIVE_LOCATION -> {
            LiveLocationPrivacyScreen(onBackClick = { currentScreen = PrivacySubScreen.MAIN })
            return
        }
        PrivacySubScreen.CALLS -> {
            CallsPrivacyScreen(privacyPrefs = privacyPrefs, onBackClick = { currentScreen = PrivacySubScreen.MAIN })
            return
        }
        PrivacySubScreen.BLOCKED_CONTACTS -> {
            BlockedContactsScreen(privacyPrefs = privacyPrefs, onBackClick = { currentScreen = PrivacySubScreen.MAIN })
            return
        }
        PrivacySubScreen.CHAT_LOCK -> {
            ChatLockPrivacyScreen(privacyPrefs = privacyPrefs, onBackClick = { currentScreen = PrivacySubScreen.MAIN })
            return
        }
        PrivacySubScreen.ADVANCED -> {
            AdvancedPrivacyScreen(privacyPrefs = privacyPrefs, onBackClick = { currentScreen = PrivacySubScreen.MAIN })
            return
        }
        PrivacySubScreen.MAIN -> {}
    }

    BackHandler { onBackClick() }

    val primaryBlue = OlinamPrimary

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("privacy_main_screen"),
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Privacy",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF101828)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("privacy_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF101828)
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
            // Privacy Checkup Top Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { currentScreen = PrivacySubScreen.CHECKUP }
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEBF3FE)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = primaryBlue,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Privacy checkup",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF101828)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Control your privacy settings and review protection tools.",
                            fontSize = 13.sp,
                            color = Color(0xFF475467)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = primaryBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Section 1: Who can see my personal info
            PrivacySectionHeader(title = "Who can see my personal info")

            PrivacyNavigationRow(
                title = "Last seen and online",
                subtitle = state.lastSeenAudience.title,
                onClick = { currentScreen = PrivacySubScreen.LAST_SEEN }
            )

            PrivacyNavigationRow(
                title = "Profile picture",
                subtitle = state.profilePhotoAudience.title,
                onClick = { currentScreen = PrivacySubScreen.PROFILE_PHOTO }
            )

            PrivacyNavigationRow(
                title = "About",
                subtitle = state.aboutAudience.title,
                onClick = { currentScreen = PrivacySubScreen.ABOUT }
            )

            PrivacyNavigationRow(
                title = "Status",
                subtitle = state.statusAudience.title,
                onClick = { currentScreen = PrivacySubScreen.STATUS }
            )

            // Read receipts Switch Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Read receipts",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF101828)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "If turned off, you won't send or receive Read receipts. Read receipts are always sent for group chats.",
                        fontSize = 13.sp,
                        color = Color(0xFF667085),
                        lineHeight = 18.sp
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Switch(
                    checked = state.readReceiptsEnabled,
                    onCheckedChange = { privacyPrefs.setReadReceipts(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = primaryBlue
                    )
                )
            }

            HorizontalDivider(color = Color(0xFFF2F4F7), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

            // Section 2: Disappearing messages
            PrivacySectionHeader(title = "Disappearing messages")

            PrivacyNavigationRow(
                title = "Default message timer",
                subtitle = if (state.defaultMessageTimer == DisappearingTimer.OFF) "Off" else "Start new chats with timer set to ${state.defaultMessageTimer.label}",
                onClick = { currentScreen = PrivacySubScreen.DEFAULT_TIMER }
            )

            HorizontalDivider(color = Color(0xFFF2F4F7), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

            // Section 3: Groups & calls
            PrivacySectionHeader(title = "Groups & calls")

            PrivacyNavigationRow(
                title = "Groups",
                subtitle = state.groupsAudience.title,
                onClick = { currentScreen = PrivacySubScreen.GROUPS }
            )

            PrivacyNavigationRow(
                title = "Live location",
                subtitle = "None",
                onClick = { currentScreen = PrivacySubScreen.LIVE_LOCATION }
            )

            PrivacyNavigationRow(
                title = "Calls",
                subtitle = if (state.silenceUnknownCallers) "Silence unknown callers: On" else "Silence unknown callers",
                onClick = { currentScreen = PrivacySubScreen.CALLS }
            )

            PrivacyNavigationRow(
                title = "Blocked accounts",
                subtitle = "${state.blockedContacts.size} accounts",
                onClick = { currentScreen = PrivacySubScreen.BLOCKED_CONTACTS }
            )

            HorizontalDivider(color = Color(0xFFF2F4F7), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

            // Section 4: Security & locks
            PrivacySectionHeader(title = "Security & locks")

            PrivacyNavigationRow(
                title = "Chat lock",
                subtitle = if (state.isChatLockBiometricEnabled) "Biometric lock: On" else "Fingerprint / Face unlock",
                onClick = { currentScreen = PrivacySubScreen.CHAT_LOCK }
            )

            PrivacyNavigationRow(
                title = "Advanced",
                subtitle = "Protect IP in calls, disable link previews",
                onClick = { currentScreen = PrivacySubScreen.ADVANCED }
            )

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
private fun PrivacySectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF5F6368),
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 14.dp, bottom = 4.dp)
    )
}

@Composable
private fun PrivacyNavigationRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 13.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF101828)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            fontSize = 13.sp,
            color = Color(0xFF667085)
        )
    }
}
