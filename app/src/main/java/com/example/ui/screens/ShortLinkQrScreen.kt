package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortLinkQrScreen(
    currentUser: UserProfile,
    onBackClick: () -> Unit
) {
    BackHandler { onBackClick() }

    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: MY CODE, 1: SCAN CODE
    var isFlashOn by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = if (selectedTab == 1) Color(0xFF1E2022) else Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Short link QR",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == 1) Color.White else Color(0xFF111827)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = if (selectedTab == 1) Color.White else Color(0xFF111827)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Connect with me on Olinam")
                                putExtra(Intent.EXTRA_TEXT, "Hey! Add me on Olinam: ${currentUser.name} (${currentUser.phoneNumber})")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share QR code"))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = if (selectedTab == 1) Color.White else Color(0xFF111827)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (selectedTab == 1) Color(0xFF1E2022) else Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tabs: MY CODE and SCAN CODE (Matching Screenshot 5)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = if (selectedTab == 1) Color(0xFF1E2022) else Color.White,
                contentColor = if (selectedTab == 1) Color.White else Color(0xFF111827),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = if (selectedTab == 1) Color.White else Color(0xFF00A884)
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
                            color = if (selectedTab == 0) (if (selectedTab == 1) Color.White else Color(0xFF111827)) else Color(0xFF6B7280)
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
                // MY CODE View
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFF9FAFB),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // User Avatar
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00A884)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentUser.name.take(1).uppercase().ifBlank { "Y" },
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = currentUser.name.ifBlank { "Olinam User" },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )

                            Text(
                                text = currentUser.phoneNumber.ifBlank { "Olinam Contact" },
                                fontSize = 13.5.sp,
                                color = Color(0xFF6B7280)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Large QR Code Display
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCode,
                                    contentDescription = "My QR Code",
                                    tint = Color(0xFF1F2937),
                                    modifier = Modifier.size(160.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "Your QR code is private. If you share it with someone, they can scan it with their Olinam camera to message you.",
                                fontSize = 12.5.sp,
                                color = Color(0xFF6B7280),
                                textAlign = TextAlign.Center,
                                lineHeight = 17.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "Connect with me on Olinam: ${currentUser.name} (${currentUser.phoneNumber})")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share QR code"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A884)),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Text("Share code", fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                    }
                }
            } else {
                // SCAN CODE View (Matching Screenshot 5)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF121212)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Spacer(modifier = Modifier.height(40.dp))

                    // Viewfinder frame
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0x33FFFFFF))
                            .border(2.dp, Color(0xFF00A884), RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Align QR code within frame",
                            color = Color(0xCCFFFFFF),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Text(
                        text = "Scan an Olinam QR code",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 16.dp)
                    )

                    // Bottom Camera Controls: Gallery on left, Flash on right (Matching Screenshot 5)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 36.dp, vertical = 32.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {},
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0x44FFFFFF))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Gallery",
                                tint = Color.White
                            )
                        }

                        IconButton(
                            onClick = { isFlashOn = !isFlashOn },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (isFlashOn) Color(0xFF00A884) else Color(0x44FFFFFF))
                        ) {
                            Icon(
                                imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = "Flashlight",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
