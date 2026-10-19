package com.example.ui.screens.notifications

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import com.example.ui.theme.OlinamPrimary

enum class ActiveToneTarget {
    MESSAGE, GROUP, CALL, STATUS
}

enum class ActiveVibrateTarget {
    MESSAGE, GROUP, CALL, STATUS
}

enum class ActiveLightTarget {
    MESSAGE, GROUP
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBackClick: () -> Unit
) {
    BackHandler { onBackClick() }

    val context = LocalContext.current
    val notificationPrefs = remember { NotificationPreferences(context) }
    val state by notificationPrefs.state.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var activeToneTarget by remember { mutableStateOf<ActiveToneTarget?>(null) }
    var activeVibrateTarget by remember { mutableStateOf<ActiveVibrateTarget?>(null) }
    var activeLightTarget by remember { mutableStateOf<ActiveLightTarget?>(null) }
    var showPopupInfoDialog by remember { mutableStateOf(false) }

    // Active Tone Picker Dialog
    activeToneTarget?.let { target ->
        val dialogTitle = when (target) {
            ActiveToneTarget.MESSAGE -> "Notification tone"
            ActiveToneTarget.GROUP -> "Group notification tone"
            ActiveToneTarget.CALL -> "Ringtone"
            ActiveToneTarget.STATUS -> "Status notification tone"
        }
        val currentTone = when (target) {
            ActiveToneTarget.MESSAGE -> state.messageTone
            ActiveToneTarget.GROUP -> state.groupTone
            ActiveToneTarget.CALL -> state.callRingtone
            ActiveToneTarget.STATUS -> state.statusTone
        }
        NotificationToneDialog(
            title = dialogTitle,
            currentTone = currentTone,
            notificationPrefs = notificationPrefs,
            onToneSelected = { newTone ->
                when (target) {
                    ActiveToneTarget.MESSAGE -> notificationPrefs.setMessageTone(newTone)
                    ActiveToneTarget.GROUP -> notificationPrefs.setGroupTone(newTone)
                    ActiveToneTarget.CALL -> notificationPrefs.setCallRingtone(newTone)
                    ActiveToneTarget.STATUS -> notificationPrefs.setStatusTone(newTone)
                }
            },
            onDismiss = { activeToneTarget = null }
        )
    }

    // Active Vibrate Dialog
    activeVibrateTarget?.let { target ->
        val currentVibrate = when (target) {
            ActiveVibrateTarget.MESSAGE -> state.messageVibrate
            ActiveVibrateTarget.GROUP -> state.groupVibrate
            ActiveVibrateTarget.CALL -> state.callVibrate
            ActiveVibrateTarget.STATUS -> state.statusVibrate
        }
        VibrationDialog(
            title = "Vibrate",
            currentVibrate = currentVibrate,
            notificationPrefs = notificationPrefs,
            onVibrateSelected = { newVibrate ->
                when (target) {
                    ActiveVibrateTarget.MESSAGE -> notificationPrefs.setMessageVibrate(newVibrate)
                    ActiveVibrateTarget.GROUP -> notificationPrefs.setGroupVibrate(newVibrate)
                    ActiveVibrateTarget.CALL -> notificationPrefs.setCallVibrate(newVibrate)
                    ActiveVibrateTarget.STATUS -> notificationPrefs.setStatusVibrate(newVibrate)
                }
            },
            onDismiss = { activeVibrateTarget = null }
        )
    }

    // Active Light Dialog
    activeLightTarget?.let { target ->
        val currentLight = when (target) {
            ActiveLightTarget.MESSAGE -> state.messageLight
            ActiveLightTarget.GROUP -> state.groupLight
        }
        NotificationLightDialog(
            title = "Light",
            currentLight = currentLight,
            onLightSelected = { newLight ->
                when (target) {
                    ActiveLightTarget.MESSAGE -> notificationPrefs.setMessageLight(newLight)
                    ActiveLightTarget.GROUP -> notificationPrefs.setGroupLight(newLight)
                }
            },
            onDismiss = { activeLightTarget = null }
        )
    }

    // Popup Info Dialog
    if (showPopupInfoDialog) {
        AlertDialog(
            onDismissRequest = { showPopupInfoDialog = false },
            title = { Text("Popup notification", fontWeight = FontWeight.SemiBold) },
            text = {
                Text(
                    "Popup notifications are no longer supported in Android 10 and above. Modern Android versions automatically display high-priority previews (heads-up notifications) at the top of your screen.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = { showPopupInfoDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = OlinamPrimary)
                ) {
                    Text("Understood")
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }

    // Reset Confirm Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Reset notification settings?", fontWeight = FontWeight.SemiBold) },
            text = {
                Text(
                    "Reset all notification settings, including custom notification settings for chats?",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        notificationPrefs.resetToDefaults()
                        showResetConfirmDialog = false
                        Toast.makeText(context, "Notification settings reset", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OlinamPrimary)
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("notifications_screen"),
        containerColor = Color(0xFFF8F9FB),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Notifications",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF101828)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("notifications_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF101828)
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options",
                                tint = Color(0xFF101828)
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Reset notification settings") },
                                onClick = {
                                    showMenu = false
                                    showResetConfirmDialog = true
                                }
                            )
                        }
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
            // General Settings Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F3F5))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Conversation tones
                    NotificationSwitchRow(
                        title = "Conversation tones",
                        subtitle = "Play sounds for incoming and outgoing messages.",
                        checked = state.conversationTones,
                        onCheckedChange = { notificationPrefs.setConversationTones(it) }
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFFF2F4F7), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Reminders
                    NotificationSwitchRow(
                        title = "Reminders",
                        subtitle = "Get occasional reminders about messages or status updates you haven't seen",
                        checked = state.reminders,
                        onCheckedChange = { notificationPrefs.setReminders(it) }
                    )
                }
            }

            // Section 1: Messages
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F3F5))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Messages",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = OlinamPrimary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    NotificationItemRow(
                        title = "Notification tone",
                        subtitle = state.messageTone,
                        onClick = { activeToneTarget = ActiveToneTarget.MESSAGE }
                    )

                    NotificationDivider()

                    NotificationItemRow(
                        title = "Vibrate",
                        subtitle = state.messageVibrate,
                        onClick = { activeVibrateTarget = ActiveVibrateTarget.MESSAGE }
                    )

                    NotificationDivider()

                    NotificationItemRow(
                        title = "Popup notification",
                        subtitle = "Not available",
                        subtitleColor = Color(0xFF94A3B8),
                        onClick = { showPopupInfoDialog = true }
                    )

                    NotificationDivider()

                    NotificationItemRow(
                        title = "Light",
                        subtitle = state.messageLight,
                        onClick = { activeLightTarget = ActiveLightTarget.MESSAGE }
                    )

                    NotificationDivider()

                    NotificationSwitchRow(
                        title = "Use high priority notifications",
                        subtitle = "Show previews of notifications at the top of the screen",
                        checked = state.messageHighPriority,
                        onCheckedChange = { notificationPrefs.setMessageHighPriority(it) }
                    )

                    NotificationDivider()

                    NotificationSwitchRow(
                        title = "Reaction notifications",
                        subtitle = "Show notifications for reactions to messages you send",
                        checked = state.messageReactions,
                        onCheckedChange = { notificationPrefs.setMessageReactions(it) }
                    )
                }
            }

            // Section 2: Groups
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F3F5))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Groups",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = OlinamPrimary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    NotificationItemRow(
                        title = "Notification tone",
                        subtitle = state.groupTone,
                        onClick = { activeToneTarget = ActiveToneTarget.GROUP }
                    )

                    NotificationDivider()

                    NotificationItemRow(
                        title = "Vibrate",
                        subtitle = state.groupVibrate,
                        onClick = { activeVibrateTarget = ActiveVibrateTarget.GROUP }
                    )

                    NotificationDivider()

                    NotificationItemRow(
                        title = "Light",
                        subtitle = state.groupLight,
                        onClick = { activeLightTarget = ActiveLightTarget.GROUP }
                    )

                    NotificationDivider()

                    NotificationSwitchRow(
                        title = "Use high priority notifications",
                        subtitle = "Show previews of notifications at the top of the screen",
                        checked = state.groupHighPriority,
                        onCheckedChange = { notificationPrefs.setGroupHighPriority(it) }
                    )

                    NotificationDivider()

                    NotificationSwitchRow(
                        title = "Reaction notifications",
                        subtitle = "Show notifications for reactions to messages you send",
                        checked = state.groupReactions,
                        onCheckedChange = { notificationPrefs.setGroupReactions(it) }
                    )
                }
            }

            // Section 3: Calls
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F3F5))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Calls",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = OlinamPrimary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    NotificationItemRow(
                        title = "Ringtone",
                        subtitle = state.callRingtone,
                        onClick = { activeToneTarget = ActiveToneTarget.CALL }
                    )

                    NotificationDivider()

                    NotificationItemRow(
                        title = "Vibrate",
                        subtitle = state.callVibrate,
                        onClick = { activeVibrateTarget = ActiveVibrateTarget.CALL }
                    )
                }
            }

            // Section 4: Status
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F3F5))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Status",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = OlinamPrimary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    NotificationItemRow(
                        title = "Notification tone",
                        subtitle = state.statusTone,
                        onClick = { activeToneTarget = ActiveToneTarget.STATUS }
                    )

                    NotificationDivider()

                    NotificationItemRow(
                        title = "Vibrate",
                        subtitle = state.statusVibrate,
                        onClick = { activeVibrateTarget = ActiveVibrateTarget.STATUS }
                    )

                    NotificationDivider()

                    NotificationSwitchRow(
                        title = "Use high priority notifications",
                        subtitle = "Show previews of notifications at the top of the screen",
                        checked = state.statusHighPriority,
                        onCheckedChange = { notificationPrefs.setStatusHighPriority(it) }
                    )

                    NotificationDivider()

                    NotificationSwitchRow(
                        title = "Reactions",
                        subtitle = "Show notifications when you get likes on a status",
                        checked = state.statusReactions,
                        onCheckedChange = { notificationPrefs.setStatusReactions(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun NotificationDivider() {
    Spacer(modifier = Modifier.height(12.dp))
    HorizontalDivider(color = Color(0xFFF2F4F7), thickness = 1.dp)
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
private fun NotificationItemRow(
    title: String,
    subtitle: String,
    subtitleColor: Color = Color(0xFF64748B),
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF101828)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            fontSize = 13.sp,
            color = subtitleColor
        )
    }
}

@Composable
private fun NotificationSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF101828)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = Color(0xFF64748B)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = OlinamPrimary
            )
        )
    }
}
