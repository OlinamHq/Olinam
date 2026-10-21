package com.example.ui.screens.contacts

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactSettingsBottomSheet(
    totalContactsCount: Int,
    onDismiss: () -> Unit,
    onSettingsChanged: () -> Unit,
    onTriggerSync: suspend () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { ContactSettingsPreferences(context) }

    var sortOrder by remember { mutableStateOf(prefs.sortOrder) }
    var nameFormat by remember { mutableStateOf(prefs.nameFormat) }
    var isAutoSync by remember { mutableStateOf(prefs.isAutoSyncEnabled) }
    var lastSyncTs by remember { mutableLongStateOf(prefs.lastSyncTimestamp) }
    var isSyncing by remember { mutableStateOf(false) }

    val hasReadContactsPermission = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val infiniteTransition = rememberInfiniteTransition(label = "sync_spin")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    fun formatLastSync(ts: Long): String {
        if (ts <= 0L) return "Never synced"
        val diff = System.currentTimeMillis() - ts
        return when {
            diff < 60_000 -> "Just now"
            diff < 3600_000 -> "${diff / 60_000} mins ago"
            diff < 86400_000 -> "${diff / 3600_000} hours ago"
            else -> SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(ts))
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE2EDFC)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContactPage,
                            contentDescription = null,
                            tint = Color(0xFF0160E3),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Contact settings",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF6B7280)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 1: Auto Sync Toggle
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE0E7FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                tint = Color(0xFF4338CA),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-sync contacts",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF111827)
                            )
                            Text(
                                text = "Keep phone contacts up-to-date with Olinam directory",
                                fontSize = 12.sp,
                                color = Color(0xFF6B7280)
                            )
                        }

                        Switch(
                            checked = isAutoSync,
                            onCheckedChange = { checked ->
                                isAutoSync = checked
                                prefs.isAutoSyncEnabled = checked
                                onSettingsChanged()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF0160E3)
                            )
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = Color(0xFFE5E7EB)
                    )

                    // Sync Status Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total contacts: $totalContactsCount",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF374151)
                            )
                            Text(
                                text = "Last synced: ${formatLastSync(lastSyncTs)}",
                                fontSize = 11.5.sp,
                                color = Color(0xFF9CA3AF)
                            )
                        }

                        Button(
                            onClick = {
                                if (!isSyncing) {
                                    scope.launch {
                                        isSyncing = true
                                        try {
                                            onTriggerSync()
                                            delay(500)
                                            val now = System.currentTimeMillis()
                                            prefs.lastSyncTimestamp = now
                                            lastSyncTs = now
                                            onSettingsChanged()
                                        } finally {
                                            isSyncing = false
                                        }
                                    }
                                }
                            },
                            enabled = !isSyncing,
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0160E3))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sync Now",
                                modifier = Modifier
                                    .size(16.dp)
                                    .rotate(if (isSyncing) rotationAngle else 0f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSyncing) "Syncing..." else "Sync now",
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 2: Sort order
            Text(
                text = "Sort contacts by",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4B5563),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                sortOrder = ContactSortOrder.FIRST_NAME
                                prefs.sortOrder = ContactSortOrder.FIRST_NAME
                                onSettingsChanged()
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = sortOrder == ContactSortOrder.FIRST_NAME,
                            onClick = {
                                sortOrder = ContactSortOrder.FIRST_NAME
                                prefs.sortOrder = ContactSortOrder.FIRST_NAME
                                onSettingsChanged()
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF0160E3))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "First name (A to Z)",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF111827)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                sortOrder = ContactSortOrder.LAST_NAME
                                prefs.sortOrder = ContactSortOrder.LAST_NAME
                                onSettingsChanged()
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = sortOrder == ContactSortOrder.LAST_NAME,
                            onClick = {
                                sortOrder = ContactSortOrder.LAST_NAME
                                prefs.sortOrder = ContactSortOrder.LAST_NAME
                                onSettingsChanged()
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF0160E3))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Last name (A to Z)",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF111827)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 3: Name format
            Text(
                text = "Display name format",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4B5563),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                nameFormat = ContactNameFormat.FIRST_NAME_FIRST
                                prefs.nameFormat = ContactNameFormat.FIRST_NAME_FIRST
                                onSettingsChanged()
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = nameFormat == ContactNameFormat.FIRST_NAME_FIRST,
                            onClick = {
                                nameFormat = ContactNameFormat.FIRST_NAME_FIRST
                                prefs.nameFormat = ContactNameFormat.FIRST_NAME_FIRST
                                onSettingsChanged()
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF0160E3))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "First name first (John Doe)",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF111827)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                nameFormat = ContactNameFormat.LAST_NAME_FIRST
                                prefs.nameFormat = ContactNameFormat.LAST_NAME_FIRST
                                onSettingsChanged()
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = nameFormat == ContactNameFormat.LAST_NAME_FIRST,
                            onClick = {
                                nameFormat = ContactNameFormat.LAST_NAME_FIRST
                                prefs.nameFormat = ContactNameFormat.LAST_NAME_FIRST
                                onSettingsChanged()
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF0160E3))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Last name first (Doe, John)",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF111827)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 4: Device Permissions Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (hasReadContactsPermission) Icons.Default.CheckCircle else Icons.Default.Security,
                        contentDescription = null,
                        tint = if (hasReadContactsPermission) Color(0xFF10B981) else Color(0xFFF59E0B),
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (hasReadContactsPermission) "Contacts permission granted" else "Contacts permission restricted",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF111827)
                        )
                        Text(
                            text = "Managed in Android system permissions",
                            fontSize = 11.5.sp,
                            color = Color(0xFF6B7280)
                        )
                    }

                    IconButton(onClick = { ContactActions.openAppSettings(context) }) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Open App Settings",
                            tint = Color(0xFF0160E3),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
