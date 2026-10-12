package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserProfile
import com.example.ui.theme.OlinamPrimary

data class LinkedDeviceItem(
    val id: String,
    val name: String,
    val os: String,
    val lastActive: String,
    val isCurrent: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkedDevicesScreen(
    currentUser: UserProfile,
    onBackClick: () -> Unit,
    onLinkDeviceClick: () -> Unit
) {
    BackHandler { onBackClick() }

    val context = LocalContext.current
    val linkedDevices = remember {
        mutableStateListOf(
            LinkedDeviceItem("1", "Google Chrome (Windows)", "Windows 11", "Active now", isCurrent = true),
            LinkedDeviceItem("2", "Olinam Web (Mac OS)", "macOS Sequoia", "Last active yesterday at 4:32 PM")
        )
    }

    var deviceToLogout by remember { mutableStateOf<LinkedDeviceItem?>(null) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("linked_devices_screen"),
        containerColor = Color(0xFFF7F8FA),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Linked devices",
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = Color(0xFF111B21)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("linked_devices_back")
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero card matching WhatsApp
            Surface(
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Devices,
                            contentDescription = "Linked devices illustration",
                            tint = OlinamPrimary,
                            modifier = Modifier.size(54.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Use Olinam on other devices",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111B21),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Link up to 4 devices to your account without keeping your phone online.",
                        fontSize = 13.5.sp,
                        color = Color(0xFF667781),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onLinkDeviceClick,
                        colors = ButtonDefaults.buttonColors(containerColor = OlinamPrimary),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("link_device_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Link a device",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Device status section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    text = "DEVICE STATUS",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF667781),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )

                Text(
                    text = "Tap a device to log out.",
                    fontSize = 13.sp,
                    color = Color(0xFF8696A0),
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 10.dp)
                )

                if (linkedDevices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No linked devices active",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    linkedDevices.forEachIndexed { index, device ->
                        if (index > 0) {
                            HorizontalDivider(
                                color = Color(0xFFF0F2F5),
                                modifier = Modifier.padding(start = 68.dp)
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { deviceToLogout = device }
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF0F2F5)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (device.os.contains("Mac")) Icons.Default.Laptop else Icons.Default.Computer,
                                    contentDescription = null,
                                    tint = Color(0xFF54656F),
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = device.name,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.5.sp,
                                    color = Color(0xFF111B21)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (device.isCurrent) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF25D366))
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = device.lastActive,
                                        fontSize = 13.sp,
                                        color = if (device.isCurrent) Color(0xFF25D366) else Color(0xFF667781)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Security note matching WhatsApp
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFF8696A0),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Your personal messages are end-to-end encrypted on all your devices.",
                    fontSize = 12.sp,
                    color = Color(0xFF8696A0),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Logout confirmation dialog
    if (deviceToLogout != null) {
        val dev = deviceToLogout!!
        AlertDialog(
            onDismissRequest = { deviceToLogout = null },
            title = {
                Text(text = "Log out of ${dev.name}?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("This will disconnect this browser/computer session from your Olinam account.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        linkedDevices.remove(dev)
                        deviceToLogout = null
                        Toast.makeText(context, "Logged out from ${dev.name}", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Log out")
                }
            },
            dismissButton = {
                TextButton(onClick = { deviceToLogout = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
