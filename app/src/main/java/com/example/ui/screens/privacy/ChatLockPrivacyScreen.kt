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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
fun ChatLockPrivacyScreen(
    privacyPrefs: PrivacyPreferences,
    onBackClick: () -> Unit
) {
    BackHandler { onBackClick() }

    val context = LocalContext.current
    val state by privacyPrefs.state.collectAsState()
    val primaryBlue = OlinamPrimary

    var showClearConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("chat_lock_privacy_screen"),
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Chat lock",
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
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .background(Color(0xFFEBF3FE), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = primaryBlue,
                        modifier = Modifier.size(50.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Chat lock keeps your chats locked and hidden",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF101828),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "If you have locked chats, pull down on your chat list or type your secret code in the search bar to find them.",
                    fontSize = 14.sp,
                    color = Color(0xFF667085),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }

            HorizontalDivider(color = Color(0xFFF2F4F7), thickness = 1.dp)

            // Biometric lock toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Unlock with fingerprint or face",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF101828)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Use device biometrics or screen lock to open locked chats.",
                        fontSize = 13.5.sp,
                        color = Color(0xFF667085)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Switch(
                    checked = state.isChatLockBiometricEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled && !privacyPrefs.canUseBiometrics(context)) {
                            Toast.makeText(context, "Please set up a screen lock (fingerprint, face, or PIN) in Android Settings first", Toast.LENGTH_LONG).show()
                            privacyPrefs.setChatLockBiometric(false)
                        } else {
                            privacyPrefs.setChatLockBiometric(enabled)
                            if (enabled) {
                                Toast.makeText(context, "Biometric unlock enabled for locked chats", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = primaryBlue
                    )
                )
            }

            HorizontalDivider(color = Color(0xFFF2F4F7), thickness = 1.dp)

            // Unlock and clear locked chats
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showClearConfirmDialog = true }
                    .padding(horizontal = 24.dp, vertical = 18.dp)
            ) {
                Text(
                    text = "Unlock and clear locked chats",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF101828)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "If you forgot your secret code, you can clear it. This will also unlock and clear messages, photos, and videos in locked chats.",
                    fontSize = 13.5.sp,
                    color = Color(0xFF667085),
                    lineHeight = 19.sp
                )
            }
        }
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Unlock and clear all locked chats?", fontWeight = FontWeight.SemiBold) },
            text = { Text("All messages and media stored inside locked chats will be removed, and chat lock will be reset.") },
            confirmButton = {
                Button(
                    onClick = {
                        val chatPrefs = context.getSharedPreferences("olinam_chat_prefs", android.content.Context.MODE_PRIVATE)
                        chatPrefs.edit().clear().apply()
                        privacyPrefs.setChatLockBiometric(false)
                        Toast.makeText(context, "Locked chats cleared and unlocked", Toast.LENGTH_SHORT).show()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryBlue)
                ) {
                    Text("Clear and Unlock")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
