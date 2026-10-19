package com.example.ui.screens.privacy

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OlinamPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedPrivacyScreen(
    privacyPrefs: PrivacyPreferences,
    onBackClick: () -> Unit
) {
    BackHandler { onBackClick() }

    val state by privacyPrefs.state.collectAsState()
    val primaryBlue = OlinamPrimary

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("advanced_privacy_screen"),
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Advanced",
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            // 1. Block unknown account messages
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Block unknown account messages",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF101828)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "To protect your account and improve device performance, Olinam will block messages from unknown accounts if they exceed a certain volume.",
                        fontSize = 13.5.sp,
                        color = Color(0xFF667085),
                        lineHeight = 19.sp
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Switch(
                    checked = state.blockUnknownMessages,
                    onCheckedChange = { privacyPrefs.setBlockUnknownMessages(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = primaryBlue)
                )
            }

            HorizontalDivider(color = Color(0xFFF2F4F7), thickness = 1.dp)

            // 2. Protect IP address in calls
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Protect IP address in calls",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF101828)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "To make it harder for people to infer your location, calls on this device will be securely relayed through Olinam servers.",
                        fontSize = 13.5.sp,
                        color = Color(0xFF667085),
                        lineHeight = 19.sp
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Switch(
                    checked = state.protectIpInCalls,
                    onCheckedChange = { privacyPrefs.setProtectIpInCalls(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = primaryBlue)
                )
            }

            HorizontalDivider(color = Color(0xFFF2F4F7), thickness = 1.dp)

            // 3. Disable link previews
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Disable link previews",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF101828)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "To help protect your IP address from being inferred by third-party websites, previews for the links you share in chats will no longer be generated.",
                        fontSize = 13.5.sp,
                        color = Color(0xFF667085),
                        lineHeight = 19.sp
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Switch(
                    checked = state.disableLinkPreviews,
                    onCheckedChange = { privacyPrefs.setDisableLinkPreviews(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = primaryBlue)
                )
            }

            HorizontalDivider(color = Color(0xFFF2F4F7), thickness = 1.dp)

            // 4. Strict account settings
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Strict account settings",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF101828)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "To help protect your account if you are at high-risk of targeted cyber attacks, certain settings will be locked.",
                        fontSize = 13.5.sp,
                        color = Color(0xFF667085),
                        lineHeight = 19.sp
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Switch(
                    checked = state.strictAccountSettings,
                    onCheckedChange = { privacyPrefs.setStrictAccountSettings(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = primaryBlue)
                )
            }
        }
    }
}
