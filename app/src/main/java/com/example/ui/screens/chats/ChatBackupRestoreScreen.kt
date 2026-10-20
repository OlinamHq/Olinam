package com.example.ui.screens.chats

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ChatViewModel
import kotlinx.coroutines.launch

@Composable
fun GoogleLogoIcon(modifier: Modifier = Modifier.size(28.dp)) {
    Canvas(modifier = modifier) {
        val strokeWidth = size.width * 0.18f
        val radius = (size.width - strokeWidth) / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // Blue arc (top-right & right)
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
            size = Size(radius * 2f, radius * 2f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        // Green arc (bottom & bottom-right)
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
            size = Size(radius * 2f, radius * 2f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        // Yellow arc (bottom-left)
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 135f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
            size = Size(radius * 2f, radius * 2f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        // Red arc (top-left & top)
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 225f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
            size = Size(radius * 2f, radius * 2f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        // Horizontal blue bar
        drawLine(
            color = Color(0xFF4285F4),
            start = Offset(center.x, center.y),
            end = Offset(size.width - strokeWidth / 2f, center.y),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun ChatBackupRestoreScreen(
    viewModel: ChatViewModel,
    onBackClick: () -> Unit
) {
    BackHandler { onBackClick() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { ChatSettingsPreferences(context) }
    val state by prefs.state.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showFrequencyDialog by remember { mutableStateOf(false) }
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var showLearnMoreDialog by remember { mutableStateOf(false) }
    var showChangeAccountDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F7F9))
            .statusBarsPadding()
    ) {
        // Top App Bar
        Surface(
            color = Color(0xFFF6F7F9),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("backup_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF1E293B),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Chat Backup & Restore",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Main Google Backup Card (Matching screenshot Screenshot_20261020-054416_92ccbd0c.png)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 1.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = Color.Black.copy(alpha = 0.05f),
                        spotColor = Color.Black.copy(alpha = 0.05f)
                    )
                    .testTag("google_backup_card"),
                shape = RoundedCornerShape(20.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    // Header row with Google logo
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GoogleLogoIcon(modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Google backup",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Description text with "Learn more" clickable link
                    val annotatedDescription = buildAnnotatedString {
                        append("Back up your chats and media to your Google Account's storage. You can restore them when you set up Olinam on a new phone. ")
                        pushStringAnnotation(tag = "LEARN_MORE", annotation = "learn_more")
                        withStyle(style = SpanStyle(color = Color(0xFF2563EB), fontWeight = FontWeight.Medium)) {
                            append("Learn more")
                        }
                        pop()
                    }

                    androidx.compose.foundation.text.ClickableText(
                        text = annotatedDescription,
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 14.sp,
                            color = Color(0xFF4B5563),
                            lineHeight = 21.sp
                        ),
                        onClick = { offset ->
                            annotatedDescription.getStringAnnotations(tag = "LEARN_MORE", start = offset, end = offset)
                                .firstOrNull()?.let {
                                    showLearnMoreDialog = true
                                }
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (!state.googleBackupEnabled) {
                        // "Enable backup" button matching reference image
                        Button(
                            onClick = {
                                prefs.setGoogleBackupEnabled(true)
                                val userEmail = currentUser?.email?.ifBlank { null } ?: "help@opendesh.com"
                                prefs.setBackupAccount(userEmail)
                                Toast.makeText(context, "Google backup enabled for $userEmail", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3855B3),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(24.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                horizontal = 24.dp,
                                vertical = 12.dp
                            ),
                            modifier = Modifier.testTag("enable_backup_button")
                        ) {
                            Text(
                                text = "Enable backup",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        // Enabled state - full controls
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFECFDF5))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF059669),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Backup status: Active",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF059669)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Backup Action Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Last backup: ${prefs.formatBackupDate(state.lastBackupTimestamp)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = "Size: ${state.lastBackupSizeFormatted}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }

                            Button(
                                onClick = {
                                    scope.launch {
                                        val ok = prefs.performBackup(viewModel.repository)
                                        if (ok) {
                                            Toast.makeText(context, "Backup completed successfully!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Backup failed. Please try again.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                enabled = !state.isBackingUp && !state.isRestoring,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF3855B3),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.testTag("backup_now_button")
                            ) {
                                if (state.isBackingUp) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Backing up...", fontSize = 13.sp)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.CloudUpload,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Back up now", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Account
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showChangeAccountDialog = true }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Google Account",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = state.backupAccount,
                                    fontSize = 13.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            Text(
                                text = "Change",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF3855B3)
                            )
                        }

                        // Frequency
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showFrequencyDialog = true }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Auto-backup frequency",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = state.backupFrequency,
                                    fontSize = 13.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            Text(
                                text = "Edit",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF3855B3)
                            )
                        }

                        // Back up over cellular
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Back up using cellular",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = if (state.backupOverCellular) "Cellular and Wi-Fi" else "Wi-Fi only",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            Switch(
                                checked = state.backupOverCellular,
                                onCheckedChange = { prefs.setBackupOverCellular(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF3855B3)
                                )
                            )
                        }

                        // Include videos
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Include videos",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = "Include video messages and media in backup archive",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            Switch(
                                checked = state.includeVideos,
                                onCheckedChange = { prefs.setIncludeVideos(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF3855B3)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        Spacer(modifier = Modifier.height(14.dp))

                        // Restore action
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Restore conversations",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = "Restore messages and media from your latest backup",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }

                            OutlinedButton(
                                onClick = { showRestoreConfirmDialog = true },
                                enabled = !state.isBackingUp && !state.isRestoring,
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.testTag("restore_chats_button")
                            ) {
                                if (state.isRestoring) {
                                    CircularProgressIndicator(
                                        color = Color(0xFF3855B3),
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Restoring...", fontSize = 12.sp)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.CloudDownload,
                                        contentDescription = null,
                                        tint = Color(0xFF3855B3),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Restore", fontSize = 12.sp, color = Color(0xFF3855B3))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Encryption info note
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEFF6FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "End-to-End Encrypted Backups",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "Your messages and media are protected by end-to-end encryption before uploading to Google Drive storage.",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }
    }

    // Frequency Dialog
    if (showFrequencyDialog) {
        val frequencies = listOf("Daily", "Weekly", "Monthly", "Only when I tap Back up")
        AlertDialog(
            onDismissRequest = { showFrequencyDialog = false },
            title = {
                Text("Auto-backup Frequency", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column {
                    frequencies.forEach { freq ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    prefs.setBackupFrequency(freq)
                                    showFrequencyDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = state.backupFrequency == freq,
                                onClick = {
                                    prefs.setBackupFrequency(freq)
                                    showFrequencyDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF3855B3))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = freq, fontSize = 15.sp, color = Color(0xFF1E293B))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFrequencyDialog = false }) {
                    Text("Close", color = Color(0xFF3855B3))
                }
            }
        )
    }

    // Change Account Dialog
    if (showChangeAccountDialog) {
        var newAccount by remember { mutableStateOf(state.backupAccount) }
        AlertDialog(
            onDismissRequest = { showChangeAccountDialog = false },
            title = {
                Text("Google Account for Backup", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column {
                    Text("Enter the Google email account to store your encrypted backups:", fontSize = 13.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(12.dp))
                    androidx.compose.material3.OutlinedTextField(
                        value = newAccount,
                        onValueChange = { newAccount = it },
                        label = { Text("Google Account") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newAccount.isNotBlank()) {
                            prefs.setBackupAccount(newAccount.trim())
                        }
                        showChangeAccountDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3855B3))
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangeAccountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Restore Confirm Dialog
    if (showRestoreConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirmDialog = false },
            title = {
                Text("Restore Chat History?", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Text(
                    text = "This will restore your conversations and messages from the latest backup (${state.lastBackupSizeFormatted}). Any newer unsaved chats may be merged.",
                    fontSize = 14.sp,
                    color = Color(0xFF4B5563)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRestoreConfirmDialog = false
                        scope.launch {
                            val success = prefs.performRestore(viewModel.repository)
                            if (success) {
                                Toast.makeText(context, "Chats restored successfully!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "No backup file found to restore.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3855B3))
                ) {
                    Text("Restore Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Learn More Dialog
    if (showLearnMoreDialog) {
        AlertDialog(
            onDismissRequest = { showLearnMoreDialog = false },
            title = {
                Text("About Google Backup", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Olinam encrypts your chat database and media files with military-grade AES-256 before uploading to Google Drive storage.",
                        fontSize = 14.sp,
                        color = Color(0xFF374151)
                    )
                    Text(
                        text = "When switching to a new Android device or reinstalling Olinam, log in with the same phone number or Google account to automatically restore your message history.",
                        fontSize = 14.sp,
                        color = Color(0xFF374151)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showLearnMoreDialog = false }) {
                    Text("Got it", color = Color(0xFF3855B3))
                }
            }
        )
    }
}
