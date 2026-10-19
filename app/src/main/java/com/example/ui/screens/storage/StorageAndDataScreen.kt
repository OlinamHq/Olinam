package com.example.ui.screens.storage

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OlinamPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageAndDataScreen(
    onBackClick: () -> Unit
) {
    BackHandler { onBackClick() }

    val context = LocalContext.current
    val storagePrefs = remember { StoragePreferences(context) }
    val state by storagePrefs.state.collectAsState()

    var showManageStorageScreen by remember { mutableStateOf(false) }
    var showNetworkUsageDialog by remember { mutableStateOf(false) }
    var showProxyDialog by remember { mutableStateOf(false) }

    if (showManageStorageScreen) {
        ManageStorageScreen(
            storagePrefs = storagePrefs,
            onBackClick = { showManageStorageScreen = false }
        )
        return
    }

    if (showNetworkUsageDialog) {
        NetworkUsageDialog(
            storagePrefs = storagePrefs,
            onDismiss = { showNetworkUsageDialog = false }
        )
    }

    if (showProxyDialog) {
        ProxySettingsDialog(
            storagePrefs = storagePrefs,
            onDismiss = { showProxyDialog = false }
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("storage_and_data_screen"),
        containerColor = Color(0xFFF8F9FB),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Data & storage",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        color = Color(0xFF101828)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("storage_back_button")
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Manage Storage Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showManageStorageScreen = true }
                    .testTag("manage_storage_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F3F5))
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFEBF3FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = OlinamPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Manage storage",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF101828)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Review and free up space by deleting photos, videos, and files stored in Olinam.",
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Open",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // 2. Auto-download media while using mobile data
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F3F5))
            ) {
                Column(modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFEBF3FE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                tint = OlinamPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Auto-download media while using mobile data.",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF101828),
                            lineHeight = 20.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFFF8F9FA), thickness = 1.dp)

                    StorageCheckboxRow(
                        title = "Image",
                        checked = state.mobileDataImages,
                        onCheckedChange = { storagePrefs.setMobileDataImages(it) }
                    )

                    StorageCheckboxRow(
                        title = "Audio/Video",
                        checked = state.mobileDataAudioVideo,
                        onCheckedChange = { storagePrefs.setMobileDataAudioVideo(it) }
                    )

                    StorageCheckboxRow(
                        title = "Others",
                        checked = state.mobileDataOthers,
                        onCheckedChange = { storagePrefs.setMobileDataOthers(it) }
                    )
                }
            }

            // 3. Auto-download media while connected to Wifi
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F3F5))
            ) {
                Column(modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFEBF3FE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Wifi,
                                contentDescription = null,
                                tint = OlinamPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Auto-download media while connected to Wifi.",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF101828),
                            lineHeight = 20.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFFF8F9FA), thickness = 1.dp)

                    StorageCheckboxRow(
                        title = "Image",
                        checked = state.wifiImages,
                        onCheckedChange = { storagePrefs.setWifiImages(it) }
                    )

                    StorageCheckboxRow(
                        title = "Audio/Video",
                        checked = state.wifiAudioVideo,
                        onCheckedChange = { storagePrefs.setWifiAudioVideo(it) }
                    )

                    StorageCheckboxRow(
                        title = "Others",
                        checked = state.wifiOthers,
                        onCheckedChange = { storagePrefs.setWifiOthers(it) }
                    )
                }
            }

            // 4. Media upload quality Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F3F5))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFEBF3FE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileUpload,
                                contentDescription = null,
                                tint = OlinamPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Media upload quality",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF101828)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Enable this option to compress your images while uploading.",
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFF2F4F7), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { storagePrefs.setCompressMedia(!state.compressMediaToOptimize) }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Compress to optimize",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF101828)
                        )
                        Switch(
                            checked = state.compressMediaToOptimize,
                            onCheckedChange = { storagePrefs.setCompressMedia(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = OlinamPrimary
                            )
                        )
                    }
                }
            }

            // 5. Media visibility Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F3F5))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { storagePrefs.setMediaVisibility(!state.mediaVisibilityInGallery) },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFEBF3FE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PermMedia,
                                    contentDescription = null,
                                    tint = OlinamPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Media visibility",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF101828)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Show newly downloaded media in your device's gallery.",
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Switch(
                            checked = state.mediaVisibilityInGallery,
                            onCheckedChange = { storagePrefs.setMediaVisibility(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = OlinamPrimary
                            )
                        )
                    }
                }
            }

            // 6. Network & Enterprise Calls
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F3F5))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Low data for calls
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { storagePrefs.setUseLessDataForCalls(!state.useLessDataForCalls) },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFEBF3FE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhoneInTalk,
                                    contentDescription = null,
                                    tint = OlinamPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Use less data for calls",
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF101828)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Reduces data consumed during high-definition voice and video calls.",
                                    fontSize = 12.5.sp,
                                    lineHeight = 17.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Switch(
                            checked = state.useLessDataForCalls,
                            onCheckedChange = { storagePrefs.setUseLessDataForCalls(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = OlinamPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFFF2F4F7), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Network Usage
                    StorageActionRow(
                        icon = Icons.Default.DataUsage,
                        title = "Network usage",
                        subtitle = "Review sent and received bytes across Wi-Fi and mobile networks",
                        onClick = { showNetworkUsageDialog = true }
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFFF2F4F7), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Proxy Settings
                    StorageActionRow(
                        icon = Icons.Default.VpnKey,
                        title = "Proxy settings",
                        subtitle = if (state.proxyEnabled) "Active: ${state.proxyHost}:${state.proxyPort}" else "Configure custom proxy for restricted networks",
                        onClick = { showProxyDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StorageCheckboxRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 18.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 15.sp,
            color = Color(0xFF101828),
            fontWeight = FontWeight.Normal
        )
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = OlinamPrimary,
                checkmarkColor = Color.White
            )
        )
    }
}

@Composable
private fun StorageActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFEBF3FE)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = OlinamPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF101828)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.5.sp,
                lineHeight = 17.sp,
                color = Color(0xFF64748B)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFF94A3B8),
            modifier = Modifier.size(20.dp)
        )
    }
}
