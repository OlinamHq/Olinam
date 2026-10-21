package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserProfile
import com.example.ui.screens.qr.MyQrCodeTab
import com.example.ui.screens.qr.QrCodeGenerator
import com.example.ui.screens.qr.ScanQrCodeTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortLinkQrScreen(
    currentUser: UserProfile,
    onBackClick: () -> Unit,
    onOpenScannedChat: ((name: String, phoneNumber: String) -> Unit)? = null
) {
    BackHandler { onBackClick() }

    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: MY CODE, 1: SCAN CODE

    val isScanTab = selectedTab == 1
    val barColor = if (isScanTab) Color(0xFF1E2022) else Color.White
    val contentColor = if (isScanTab) Color.White else Color(0xFF111827)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("short_link_qr_screen"),
        containerColor = if (isScanTab) Color(0xFF121212) else Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Short link QR",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = contentColor
                        )
                    }
                },
                actions = {
                    if (selectedTab == 0) {
                        IconButton(
                            onClick = {
                                QrCodeGenerator.shareQrCodeImage(context, currentUser, null)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share QR code",
                                tint = contentColor
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = barColor
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tabs: MY CODE and SCAN CODE (Matching Screenshots)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = barColor,
                contentColor = contentColor,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = if (isScanTab) Color.White else Color(0xFF0160E3)
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "MY CODE",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.5.sp,
                            color = if (selectedTab == 0) (if (isScanTab) Color.White else Color(0xFF111827)) else Color(0xFF6B7280)
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "SCAN CODE",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.5.sp,
                            color = if (selectedTab == 1) Color.White else Color(0xFF6B7280)
                        )
                    }
                )
            }

            if (selectedTab == 0) {
                MyQrCodeTab(
                    currentUser = currentUser,
                    modifier = Modifier.weight(1f)
                )
            } else {
                ScanQrCodeTab(
                    onOpenChat = { name, phone ->
                        if (onOpenScannedChat != null) {
                            onOpenScannedChat(name, phone)
                        } else {
                            // Default back if no specific handler provided
                            onBackClick()
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
