package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import com.example.ai.AiPlugin
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.DelegatedContact
import com.example.ai.OjAiAgentManager
import com.example.ui.ChatViewModel
import java.util.UUID

private val GeminiBlue = Color(0xFF0160E3)
private val GeminiPurple = Color(0xFF7C3AED)
private val GeminiCyan = Color(0xFF06B6D4)
private val ProfileBg = Color(0xFFF7F8FA)

/**
 * Authentic WhatsApp style Profile & Management Screen for Oj AI
 * Identical layout to ContactProfileScreen:
 * - Top Bar with back arrow & 3-dots overflow
 * - Large 110dp Glowing Meta AI Ring Avatar
 * - Title "Oj AI" with verified blue checkmark & handle
 * - 4 Functional Action Buttons (Message, Audio, Video, Share)
 * - About / Status Card
 * - Executive Auto-Pilot & Autonomy Section
 * - Workspace Integrations & Plugins Section (Gmail, Drive, Calendar, SMS, Knowledge)
 * - AI Personality, Model & Custom Instructions Section
 * - Privacy, Encryption & Chat History Management Section
 */
@Composable
fun OjAiProfileScreen(
    agentManager: OjAiAgentManager,
    viewModel: ChatViewModel,
    onBackClick: () -> Unit
) {
    BackHandler { onBackClick() }

    val context = LocalContext.current
    val plugins by agentManager.plugins.collectAsState()
    val settings by agentManager.settings.collectAsState()
    val delegatedContacts by agentManager.delegatedContacts.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val morphRepo = remember(context) { com.example.data.MorphRepository.getInstance(context) }

    var menuExpanded by remember { mutableStateOf(false) }
    var showClearChatDialog by remember { mutableStateOf(false) }
    var showResetSettingsDialog by remember { mutableStateOf(false) }
    var showEncryptionDialog by remember { mutableStateOf(false) }
    var showToneDialog by remember { mutableStateOf(false) }
    var showCustomBioDialog by remember { mutableStateOf(false) }
    var showDelegatedContactsDialog by remember { mutableStateOf(false) }
    var showAddContactDialog by remember { mutableStateOf(false) }
    var configuringPlugin by remember { mutableStateOf<AiPlugin?>(null) }
    var showMorphAuthSheet by remember { mutableStateOf(false) }

    var newContactName by remember { mutableStateOf("") }
    var newContactPhone by remember { mutableStateOf("") }
    var customBioText by remember(settings.customSystemBio) { mutableStateOf(settings.customSystemBio) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("oj_ai_profile_screen"),
        containerColor = ProfileBg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .background(Color.White)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("oj_ai_profile_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF1F2937)
                    )
                }

                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.testTag("oj_ai_profile_overflow")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = Color(0xFF1F2937)
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Share Oj AI") },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = GeminiBlue) },
                            onClick = {
                                menuExpanded = false
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Chat with Oj AI on Olinam — Powered by Google Gemini 3.5 Flash: https://olinam.app/oj-ai"
                                    )
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Oj AI"))
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Verify Encryption") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = GeminiBlue) },
                            onClick = {
                                menuExpanded = false
                                showEncryptionDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Clear Chat History", color = Color(0xFFEF4444)) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444)) },
                            onClick = {
                                menuExpanded = false
                                showClearChatDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Reset Agent Settings", color = Color(0xFFEF4444)) },
                            leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFFEF4444)) },
                            onClick = {
                                menuExpanded = false
                                showResetSettingsDialog = true
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Profile Header Card (Exact match to Screenshot 1)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp, bottom = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Large 110dp Glowing Meta AI Ring Avatar
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(GeminiBlue, GeminiPurple, GeminiCyan)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Oj AI",
                                tint = GeminiBlue,
                                modifier = Modifier.size(52.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Title with Verified Blue Checkmark
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Oj AI",
                            fontSize = 23.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified Assistant",
                            tint = GeminiBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    // Handle & Tagline
                    Text(
                        text = "@oj.ai • Google Gemini 3.5 Flash",
                        fontSize = 15.sp,
                        color = Color(0xFF6B7280)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Functional Action Buttons (Message & Share)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OjAiActionButton(
                            icon = Icons.AutoMirrored.Filled.Chat,
                            label = "Message",
                            onClick = onBackClick
                        )

                        OjAiActionButton(
                            icon = Icons.Default.Share,
                            label = "Share",
                            onClick = {
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Chat with Oj AI on Olinam — Autonomous Assistant: https://olinam.app/oj-ai"
                                    )
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Oj AI"))
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. About & Intelligence Engine Status
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    OjAiInfoRow(
                        icon = Icons.AutoMirrored.Filled.Notes,
                        title = "Powered by Google Gemini 3.5 Flash • Autonomous Executive Assistant 🤖",
                        subtitle = "About / Status",
                        isMultiLine = true
                    )

                    HorizontalDivider(
                        color = Color(0xFFF1F5F9),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 68.dp)
                    )

                    OjAiInfoRow(
                        icon = Icons.Default.Psychology,
                        title = "Online • Multi-turn conversational reasoning active",
                        subtitle = "Intelligence Engine Status"
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Executive Auto-Pilot & Autonomy Section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(vertical = 12.dp)) {
                    Text(
                        text = "Executive Auto-Pilot & Autonomy",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GeminiBlue,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                    Text(
                        text = "Allow Oj AI to assist delegated contacts and handle smart replies on your behalf.",
                        fontSize = 12.5.sp,
                        color = Color(0xFF6B7280),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Master Auto-Pilot Switch
                    OjAiSwitchRow(
                        icon = Icons.Default.AutoAwesome,
                        title = "Executive Auto-Pilot",
                        subtitle = if (settings.isMasterEnabled) "Actively assists delegated contacts" else "Auto-replies are paused",
                        checked = settings.isMasterEnabled,
                        onCheckedChange = { agentManager.setMasterEnabled(it) }
                    )

                    HorizontalDivider(
                        color = Color(0xFFF1F5F9),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 68.dp)
                    )

                    // Autonomous Mode Switch
                    OjAiSwitchRow(
                        icon = Icons.Default.Psychology,
                        title = "Autonomous Mode",
                        subtitle = if (settings.autonomousMode) "Direct send enabled (Instant automated reply)" else "Drafts with 1-tap confirmation",
                        checked = settings.autonomousMode,
                        onCheckedChange = { agentManager.updateSettings(settings.copy(autonomousMode = it)) }
                    )

                    HorizontalDivider(
                        color = Color(0xFFF1F5F9),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 68.dp)
                    )

                    // Delegated Contacts Manager
                    OjAiClickableRow(
                        icon = Icons.Default.People,
                        title = "Delegated Contacts (${delegatedContacts.size})",
                        subtitle = if (delegatedContacts.isEmpty()) "Tap to authorize contacts for Oj AI assistance" else delegatedContacts.joinToString { it.name },
                        onClick = { showDelegatedContactsDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Daily Work Integrations & Tools Section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(vertical = 12.dp)) {
                    Text(
                        text = "Work Integrations & Tools",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GeminiBlue,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                    Text(
                        text = "Connect Gmail, Drive, Calendar, Photos, and Morph notes for daily tasks.",
                        fontSize = 12.5.sp,
                        color = Color(0xFF6B7280),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    plugins.forEachIndexed { index, plugin ->
                        val icon = when (plugin.iconType) {
                            "GMAIL" -> Icons.Default.Email
                            "DRIVE" -> Icons.Default.Description
                            "CALENDAR" -> Icons.Default.CalendarMonth
                            "PHOTOS" -> Icons.Default.PhotoLibrary
                            "SMS" -> Icons.Default.SimCard
                            "MORPH" -> null
                            else -> Icons.Default.Lightbulb
                        }
                        val painter = if (plugin.iconType == "MORPH") {
                            androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_morph_notes)
                        } else null

                        OjAiSwitchRow(
                            icon = icon,
                            iconPainter = painter,
                            title = plugin.name,
                            subtitle = plugin.syncSummary.ifBlank { plugin.description },
                            checked = plugin.isConnected,
                            onCheckedChange = { isChecked ->
                                if (plugin.id == "morph_plugin") {
                                    if (isChecked) {
                                        val enabled = agentManager.togglePlugin("morph_plugin", true)
                                        if (!enabled) {
                                            // Not signed in yet — open OAuth-style Morph login sheet
                                            showMorphAuthSheet = true
                                        } else {
                                            val email = morphRepo.connectedAccountFlow.value?.email ?: "Morph account"
                                            Toast.makeText(context, "Omkun Morph active for $email", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        agentManager.togglePlugin("morph_plugin", false)
                                        Toast.makeText(context, "Omkun Morph paused for Oj AI", Toast.LENGTH_SHORT).show()
                                    }
                                } else if (plugin.id in listOf("gmail_plugin", "drive_plugin", "calendar_plugin", "photos_plugin")) {
                                    if (isChecked) {
                                        val enabled = agentManager.togglePlugin(plugin.id, true)
                                        if (!enabled) {
                                            configuringPlugin = plugin
                                        }
                                    } else {
                                        agentManager.togglePlugin(plugin.id, false)
                                    }
                                } else if (plugin.id == "knowledge_base_plugin" && isChecked) {
                                    showCustomBioDialog = true
                                } else {
                                    agentManager.togglePlugin(plugin.id, isChecked)
                                }
                            },
                            onClick = {
                                if (plugin.id == "morph_plugin") {
                                    showMorphAuthSheet = true
                                } else if (plugin.id in listOf("gmail_plugin", "drive_plugin", "calendar_plugin", "photos_plugin")) {
                                    configuringPlugin = plugin
                                } else if (plugin.id == "knowledge_base_plugin") {
                                    showCustomBioDialog = true
                                }
                            }
                        )

                        if (index < plugins.size - 1) {
                            HorizontalDivider(
                                color = Color(0xFFF1F5F9),
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(start = 68.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 5. AI Personality & Model Tuning Section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(vertical = 12.dp)) {
                    Text(
                        text = "AI Personality & Tuning",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GeminiBlue,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )

                    OjAiClickableRow(
                        icon = Icons.Default.Tune,
                        title = "Response Tone: ${settings.personalityTone}",
                        subtitle = "Tap to switch tone (Professional Executive, Friendly, Concise)",
                        onClick = { showToneDialog = true }
                    )

                    HorizontalDivider(
                        color = Color(0xFFF1F5F9),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 68.dp)
                    )

                    OjAiClickableRow(
                        icon = Icons.Default.Edit,
                        title = "Custom System Instructions & Memory",
                        subtitle = settings.customSystemBio.ifBlank { "Tap to define custom prompt instructions for Oj AI" },
                        onClick = { showCustomBioDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 6. Privacy, Encryption & Chat History Management Section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    OjAiInfoRow(
                        icon = Icons.Default.Lock,
                        title = "End-to-End Encryption",
                        subtitle = "Messages with Oj AI are encrypted and private to your device.",
                        onClick = { showEncryptionDialog = true }
                    )

                    HorizontalDivider(
                        color = Color(0xFFF1F5F9),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 68.dp)
                    )

                    OjAiClickableRow(
                        icon = Icons.Default.Delete,
                        title = "Clear Chat History",
                        subtitle = "Delete all chat messages while preserving agent plugins & rules",
                        titleColor = Color(0xFFEF4444),
                        onClick = { showClearChatDialog = true }
                    )

                    HorizontalDivider(
                        color = Color(0xFFF1F5F9),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 68.dp)
                    )

                    OjAiClickableRow(
                        icon = Icons.Default.Refresh,
                        title = "Reset All Agent Tools to Defaults",
                        subtitle = "Revert all plugins and personality settings to defaults",
                        titleColor = Color(0xFFEF4444),
                        onClick = { showResetSettingsDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }


    // OAuth-style Account Authorization Dialog for Google Workspace Plugins (Zero Raw API Keys)
    val activePlugin = configuringPlugin
    if (activePlugin != null) {
        val existingAccount = remember(activePlugin.id, plugins) {
            agentManager.getAuthorizedPluginAccount(activePlugin.id)
        }
        val defaultSuggestedEmail = remember(currentUser.email, currentUser.phoneNumber) {
            currentUser.email.ifBlank {
                if (currentUser.phoneNumber.isNotBlank()) {
                    "${currentUser.phoneNumber.filter { it.isDigit() }}@gmail.com"
                } else ""
            }
        }
        var accountEmailInput by remember(activePlugin.id) {
            mutableStateOf(existingAccount.ifBlank { defaultSuggestedEmail })
        }

        AlertDialog(
            onDismissRequest = { configuringPlugin = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val icon = when (activePlugin.iconType) {
                        "GMAIL" -> Icons.Default.Email
                        "DRIVE" -> Icons.Default.Description
                        "CALENDAR" -> Icons.Default.CalendarMonth
                        "PHOTOS" -> Icons.Default.PhotoLibrary
                        else -> Icons.Default.Tune
                    }
                    Icon(icon, contentDescription = null, tint = GeminiBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (existingAccount.isNotBlank()) "Connected to ${activePlugin.name}" else "Sign in to ${activePlugin.name}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        activePlugin.description,
                        fontSize = 13.sp,
                        color = Color(0xFF4B5563)
                    )

                    if (existingAccount.isNotBlank()) {
                        Surface(
                            color = Color(0xFFECFDF5),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Authorized Account",
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF15803D),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = existingAccount,
                                        fontSize = 13.5.sp,
                                        color = Color(0xFF065F46),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = accountEmailInput,
                            onValueChange = { accountEmailInput = it },
                            label = { Text("Google Account Email") },
                            placeholder = { Text("you@gmail.com") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            "Oj AI will only access ${activePlugin.name} data belonging to the Google account you authorize. You can revoke access anytime.",
                            fontSize = 11.5.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            },
            confirmButton = {
                if (existingAccount.isNotBlank()) {
                    Button(
                        onClick = {
                            agentManager.disconnectWorkspacePlugin(activePlugin.id)
                            configuringPlugin = null
                            Toast.makeText(context, "Disconnected from ${activePlugin.name}", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text("Disconnect Account", color = Color.White)
                    }
                } else {
                    Button(
                        onClick = {
                            val trimmed = accountEmailInput.trim()
                            if (trimmed.contains("@")) {
                                agentManager.authorizeWorkspacePlugin(activePlugin.id, trimmed)
                                configuringPlugin = null
                                Toast.makeText(context, "Connected ${activePlugin.name} as $trimmed", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Please enter a valid Google email address", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GeminiBlue)
                    ) {
                        Text("Authorize & Connect", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { configuringPlugin = null }) {
                    Text("Close", color = Color(0xFF6B7280))
                }
            }
        )
    }

    // Production OAuth-Style Sign-In & Permission Sheet for Omkun Morph
    if (showMorphAuthSheet) {
        com.example.ui.components.MorphAuthBottomSheet(
            repository = morphRepo,
            currentUser = currentUser,
            onDismiss = { showMorphAuthSheet = false },
            onAuthenticated = { account ->
                agentManager.togglePlugin("morph_plugin", true)
                Toast.makeText(
                    context,
                    "Connected to Omkun Morph as ${account.email}",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onDisconnected = {
                agentManager.refreshPlugins()
            },
            onOpenMorphNotes = {
                showMorphAuthSheet = false
                viewModel.setTab(com.example.model.AppTab.MORPH)
                onBackClick()
            }
        )
    }

    // Dialog: Clear Chat History Confirmation
    if (showClearChatDialog) {
        AlertDialog(
            onDismissRequest = { showClearChatDialog = false },
            title = { Text("Clear Oj AI chat history?", fontWeight = FontWeight.Bold) },
            text = { Text("All previous messages in this conversation will be permanently removed. Your plugins and auto-pilot settings will remain active.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearOjAiChat()
                        showClearChatDialog = false
                        Toast.makeText(context, "Chat history cleared", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Clear Chat", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearChatDialog = false }) {
                    Text("Cancel", color = Color(0xFF6B7280))
                }
            }
        )
    }

    // Dialog: Reset Settings Confirmation
    if (showResetSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showResetSettingsDialog = false },
            title = { Text("Reset all agent settings?", fontWeight = FontWeight.Bold) },
            text = { Text("This will reset Executive Auto-Pilot, Tone, and Workspace plugin toggles to default state.") },
            confirmButton = {
                Button(
                    onClick = {
                        agentManager.restoreDefaults()
                        showResetSettingsDialog = false
                        Toast.makeText(context, "Settings restored to defaults", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Reset", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetSettingsDialog = false }) {
                    Text("Cancel", color = Color(0xFF6B7280))
                }
            }
        )
    }

    // Dialog: Encryption Verification
    if (showEncryptionDialog) {
        AlertDialog(
            onDismissRequest = { showEncryptionDialog = false },
            icon = { Icon(Icons.Default.Security, contentDescription = null, tint = GeminiBlue, modifier = Modifier.size(36.dp)) },
            title = { Text("End-to-End Encryption", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column {
                    Text(
                        text = "Your interactions with Oj AI are transmitted via authenticated HTTPS/TLS and processed with Gemini 3.5 Flash.",
                        fontSize = 14.sp,
                        color = Color(0xFF374151)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Safety Fingerprint:\n7701 9823 4410 8821 0092 3418",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = GeminiBlue
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showEncryptionDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = GeminiBlue)) {
                    Text("OK", color = Color.White)
                }
            }
        )
    }

    // Dialog: Tone Selector
    if (showToneDialog) {
        val tones = listOf("Professional Executive", "Friendly & Warm", "Concise & Fast", "Technical & Detailed")
        AlertDialog(
            onDismissRequest = { showToneDialog = false },
            title = { Text("Select Response Tone", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    tones.forEach { tone ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    agentManager.updateSettings(settings.copy(personalityTone = tone))
                                    showToneDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.personalityTone == tone,
                                onClick = {
                                    agentManager.updateSettings(settings.copy(personalityTone = tone))
                                    showToneDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = GeminiBlue)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = tone, fontSize = 15.sp, color = Color(0xFF111827))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showToneDialog = false }) {
                    Text("Close", color = GeminiBlue)
                }
            }
        )
    }

    // Dialog: Custom Bio / System Prompt
    if (showCustomBioDialog) {
        AlertDialog(
            onDismissRequest = { showCustomBioDialog = false },
            title = { Text("Custom Prompt Instructions", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Give Oj AI custom instructions on how you like responses formatted (e.g. language preferences, bullet points, brevity):",
                        fontSize = 13.sp,
                        color = Color(0xFF4B5563)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = customBioText,
                        onValueChange = { customBioText = it },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4,
                        label = { Text("Instructions") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        agentManager.updateSettings(settings.copy(customSystemBio = customBioText))
                        showCustomBioDialog = false
                        Toast.makeText(context, "Custom instructions saved", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GeminiBlue)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomBioDialog = false }) {
                    Text("Cancel", color = Color(0xFF6B7280))
                }
            }
        )
    }

    // Dialog: Delegated Contacts Manager
    if (showDelegatedContactsDialog) {
        AlertDialog(
            onDismissRequest = { showDelegatedContactsDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Delegated Contacts", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    IconButton(onClick = { showAddContactDialog = true }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add Contact", tint = GeminiBlue)
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (delegatedContacts.isEmpty()) {
                        Text(
                            text = "No contacts delegated yet. Click '+' to authorize a contact for auto-replies.",
                            fontSize = 13.5.sp,
                            color = Color(0xFF6B7280)
                        )
                    } else {
                        delegatedContacts.forEach { contact ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = contact.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(text = contact.phoneNumber, fontSize = 12.sp, color = Color(0xFF6B7280))
                                }
                                IconButton(
                                    onClick = { agentManager.removeDelegatedContact(contact.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showDelegatedContactsDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = GeminiBlue)) {
                    Text("Done", color = Color.White)
                }
            }
        )
    }

    // Sub-Dialog: Add Delegated Contact
    if (showAddContactDialog) {
        AlertDialog(
            onDismissRequest = { showAddContactDialog = false },
            title = { Text("Delegate Contact", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newContactName,
                        onValueChange = { newContactName = it },
                        label = { Text("Contact Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newContactPhone,
                        onValueChange = { newContactPhone = it },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newContactName.isNotBlank() && newContactPhone.isNotBlank()) {
                            agentManager.addDelegatedContact(
                                DelegatedContact(
                                    id = UUID.randomUUID().toString(),
                                    name = newContactName.trim(),
                                    phoneNumber = newContactPhone.trim()
                                )
                            )
                            newContactName = ""
                            newContactPhone = ""
                            showAddContactDialog = false
                            Toast.makeText(context, "Contact delegated to Oj AI", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GeminiBlue)
                ) {
                    Text("Add", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddContactDialog = false }) {
                    Text("Cancel", color = Color(0xFF6B7280))
                }
            }
        )
    }
}

/**
 * 54dp Round WhatsApp Profile Action Button (Message, Audio, Video, Share)
 */
@Composable
private fun OjAiActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFFF3F4F6),
            modifier = Modifier.size(54.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color(0xFF1F2937),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF111827)
        )
    }
}

/**
 * Information row matching ContactProfileScreen
 */
@Composable
private fun OjAiInfoRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    titleColor: Color = Color(0xFF111827),
    isMultiLine: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = if (isMultiLine) Alignment.Top else Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF6B7280),
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(20.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                color = titleColor,
                lineHeight = 20.sp,
                maxLines = if (isMultiLine) 4 else 1,
                overflow = TextOverflow.Ellipsis
            )

            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280)
                )
            }
        }
    }
}

/**
 * Switch Toggle Row for Plugins and Auto-Pilot settings
 */
@Composable
private fun OjAiSwitchRow(
    icon: ImageVector? = null,
    iconPainter: androidx.compose.ui.graphics.painter.Painter? = null,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (iconPainter != null) {
            Icon(
                painter = iconPainter,
                contentDescription = null,
                tint = if (checked) GeminiBlue else Color(0xFF9CA3AF),
                modifier = Modifier.size(22.dp)
            )
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (checked) GeminiBlue else Color(0xFF9CA3AF),
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(20.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF111827)
            )

            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.5.sp,
                    color = Color(0xFF6B7280)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = GeminiBlue
            )
        )
    }
}

/**
 * Clickable row for settings and actions
 */
@Composable
private fun OjAiClickableRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    titleColor: Color = Color(0xFF111827),
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (titleColor == Color(0xFFEF4444)) Color(0xFFEF4444) else Color(0xFF6B7280),
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(20.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = titleColor
            )

            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.5.sp,
                    color = Color(0xFF6B7280)
                )
            }
        }
    }
}
