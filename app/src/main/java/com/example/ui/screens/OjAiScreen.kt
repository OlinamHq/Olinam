@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.AiPlugin
import com.example.ai.AiReplyResult
import com.example.ai.DelegatedContact
import com.example.data.ContactHelper
import com.example.ui.ChatViewModel
import com.example.ui.theme.OlinamPrimary
import com.example.ui.theme.OlinamPrimaryContainer
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Oj AI Agent Control Hub & Plugin Management:
 * Not a simple chatbot; an autonomous agent management center with
 * Gmail, Google Drive, Calendar, SIM SMS plugins and contact delegation.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OjAiScreen(viewModel: ChatViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val agentManager = remember { viewModel.getOrCreateAiAgentManager(context) }

    val plugins by agentManager.plugins.collectAsState()
    val delegatedContacts by agentManager.delegatedContacts.collectAsState()
    val settings by agentManager.settings.collectAsState()
    val recentActions by agentManager.recentActions.collectAsState()

    var showAddContactDialog by remember { mutableStateOf(false) }
    var showEditContactDialog by remember { mutableStateOf<DelegatedContact?>(null) }
    var selectedPluginForDetails by remember { mutableStateOf<AiPlugin?>(null) }

    // Simulator testing states
    var testSelectedContact by remember { mutableStateOf<DelegatedContact?>(null) }
    var testQueryInput by remember { mutableStateOf("When is the project meeting scheduled for today?") }
    var isSimulating by remember { mutableStateOf(false) }
    var simulationResult by remember { mutableStateOf<AiReplyResult?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("oj_ai_screen")
    ) {
        // 1. Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFEFF6FF),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = "Oj AI Hub",
                                    tint = OlinamPrimary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Oj AI Agent Hub",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFEEF2FF)
                                ) {
                                    Text(
                                        text = "Gemini 2.5",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OlinamPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Autonomous agent & daily-use plugin manager",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    // Master Toggle
                    Switch(
                        checked = settings.isAgentMasterEnabled,
                        onCheckedChange = {
                            agentManager.updateSettings(settings.copy(isAgentMasterEnabled = it))
                            val msg = if (it) "Oj AI Agent is now Active" else "Oj AI Agent Paused"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF00A884)
                        ),
                        modifier = Modifier.testTag("ai_master_switch")
                    )
                }
            }
        }

        // 2. Master Status & Mode Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (settings.isAgentMasterEnabled) "Agent Status: Active on Your Behalf" else "Agent Status: Paused",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (settings.isAgentMasterEnabled) Color(0xFF00A884) else Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (settings.autonomousMode) "Mode: Direct Autonomous Dispatch (SMS & Chats)" else "Mode: Draft with 1-Tap Approval",
                                fontSize = 12.5.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.clickable {
                                val newMode = !settings.autonomousMode
                                agentManager.updateSettings(settings.copy(autonomousMode = newMode))
                            }
                        ) {
                            Text(
                                text = if (settings.autonomousMode) "Auto-Send" else "Approval Mode",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF334155),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    Spacer(modifier = Modifier.height(10.dp))

                    // Persona / Tone Selector
                    Text(
                        text = "AI Communication Tone",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF475569)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val tones = listOf("Professional Executive", "Friendly & Warm", "Concise & Fast")
                        tones.forEach { tone ->
                            val isSelected = settings.personalityTone == tone
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) OlinamPrimaryContainer else Color(0xFFF8FAFC),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, OlinamPrimary) else null,
                                modifier = Modifier.clickable {
                                    agentManager.updateSettings(settings.copy(personalityTone = tone))
                                }
                            ) {
                                Text(
                                    text = tone,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) OlinamPrimary else Color(0xFF475569),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Daily Connected Plugins & Add-ons Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Daily Plugins & Add-ons",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Oj AI reads from connected plugins to answer on your behalf",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }

        items(plugins, key = { it.id }) { plugin ->
            PluginItemCard(
                plugin = plugin,
                onToggle = { isChecked ->
                    agentManager.togglePlugin(plugin.id, isChecked)
                },
                onClick = { selectedPluginForDetails = plugin }
            )
        }

        // 4. Delegated Contacts Section (Who AI can message directly)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Authorized Contacts for AI",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Contacts where Oj AI can reply directly using email/calendar context",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Button(
                        onClick = { showAddContactDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = OlinamPrimary),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("authorize_contact_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add", fontSize = 13.sp)
                    }
                }
            }
        }

        if (delegatedContacts.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No Contacts Authorized Yet",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF334155)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap '+ Add' above to select contacts that Oj AI has permission to auto-reply to on your behalf.",
                            fontSize = 12.5.sp,
                            color = Color(0xFF64748B),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(delegatedContacts, key = { it.id }) { contact ->
                DelegatedContactCard(
                    contact = contact,
                    onEdit = { showEditContactDialog = contact },
                    onDelete = { agentManager.removeDelegatedContact(contact.id) },
                    onToggleAutoReply = { isChecked ->
                        agentManager.updateDelegatedContact(contact.copy(isAiAutoReplyEnabled = isChecked))
                    }
                )
            }
        }

        // 5. Live Agent Simulator & Test Console
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 22.dp, bottom = 8.dp)
            ) {
                Text(
                    text = "Live Agent Simulator",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Test how Oj AI reads Gmail/Calendar data to formulate smart replies",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Sample Incoming Message Query:",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = testQueryInput,
                        onValueChange = { testQueryInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        placeholder = { Text("e.g. When is our project meeting?") }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Preset Quick Questions
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val presets = listOf(
                            "When is our meeting?",
                            "Did you get the proposal?",
                            "What is your flight time?"
                        )
                        presets.forEach { preset ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF1F5F9),
                                modifier = Modifier.clickable { testQueryInput = preset }
                            ) {
                                Text(
                                    text = preset,
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF334155),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            isSimulating = true
                            simulationResult = null
                            scope.launch {
                                val targetContact = testSelectedContact ?: delegatedContacts.firstOrNull() ?: DelegatedContact(
                                    id = "sample_test",
                                    name = "Alex Client",
                                    phoneNumber = "+91 98765 43210",
                                    isAiAutoReplyEnabled = true,
                                    allowReadEmailContext = true,
                                    allowDirectSend = true
                                )
                                val res = agentManager.generateAutonomousReply(targetContact, testQueryInput)
                                simulationResult = res
                                isSimulating = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A884)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("run_ai_simulation_button")
                    ) {
                        if (isSimulating) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analyzing Plugins & Generating...", fontSize = 13.5.sp)
                        } else {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simulate Oj AI Autonomous Reply", fontSize = 13.5.sp)
                        }
                    }

                    // Simulation Result Display
                    AnimatedVisibility(visible = simulationResult != null) {
                        simulationResult?.let { res ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 14.dp)
                            ) {
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF00A884),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Sources Context Analyzed:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF334155)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = res.contextSourcesUsed.joinToString(", ").ifEmpty { "Personal Agent Knowledge" },
                                        fontSize = 12.sp,
                                        color = OlinamPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF8FAFC),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "Oj AI Formulated Response (Sent on Your Behalf):",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF64748B)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "\"${res.generatedText}\"",
                                            fontSize = 14.sp,
                                            color = Color(0xFF0F172A),
                                            lineHeight = 20.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Dialog: Authorize New Contact
    if (showAddContactDialog) {
        AuthorizeContactDialog(
            context = context,
            onDismiss = { showAddContactDialog = false },
            onAdd = { newContact ->
                agentManager.addDelegatedContact(newContact)
                showAddContactDialog = false
                Toast.makeText(context, "${newContact.name} authorized for Oj AI", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Dialog: Edit Contact Authorization
    showEditContactDialog?.let { target ->
        EditDelegatedContactDialog(
            contact = target,
            onDismiss = { showEditContactDialog = null },
            onSave = { updated ->
                agentManager.updateDelegatedContact(updated)
                showEditContactDialog = null
                Toast.makeText(context, "Permissions updated", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Dialog: Plugin Details & Scope Inspection
    selectedPluginForDetails?.let { plugin ->
        AlertDialog(
            onDismissRequest = { selectedPluginForDetails = null },
            icon = {
                Icon(
                    imageVector = getPluginIcon(plugin.iconType),
                    contentDescription = null,
                    tint = OlinamPrimary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(plugin.name, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(plugin.description, fontSize = 13.5.sp, color = Color(0xFF475569))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Synced Data Feeds for Gemini Context:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    plugin.sampleItems.forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(item, fontSize = 12.sp, modifier = Modifier.padding(8.dp))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedPluginForDetails = null },
                    colors = ButtonDefaults.buttonColors(containerColor = OlinamPrimary)
                ) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
private fun PluginItemCard(
    plugin: AiPlugin,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = when (plugin.iconType) {
                    "GMAIL" -> Color(0xFFFEF2F2)
                    "DRIVE" -> Color(0xFFEFF6FF)
                    "CALENDAR" -> Color(0xFFF0FDF4)
                    "SMS" -> Color(0xFFFFFBEB)
                    else -> Color(0xFFF8FAFC)
                },
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = getPluginIcon(plugin.iconType),
                        contentDescription = null,
                        tint = when (plugin.iconType) {
                            "GMAIL" -> Color(0xFFDC2626)
                            "DRIVE" -> Color(0xFF2563EB)
                            "CALENDAR" -> Color(0xFF16A34A)
                            "SMS" -> Color(0xFFD97706)
                            else -> Color(0xFF64748B)
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = plugin.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (plugin.isConnected) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00A884))
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = plugin.syncSummary,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Switch(
                checked = plugin.isConnected,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF00A884)
                )
            )
        }
    }
}

@Composable
private fun DelegatedContactCard(
    contact: DelegatedContact,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleAutoReply: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFEEF2FF),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = contact.name.take(1).uppercase(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = OlinamPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = contact.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = contact.phoneNumber,
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = contact.isAiAutoReplyEnabled,
                        onCheckedChange = onToggleAutoReply,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF00A884)
                        )
                    )
                    IconButton(onClick = onEdit) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Badges
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (contact.allowReadEmailContext) {
                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFFEF2F2)) {
                        Text("Gmail Context: ON", fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFDC2626), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
                if (contact.allowDirectSend) {
                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFF0FDF4)) {
                        Text("Direct Send: ON", fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF16A34A), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Last Action: ${contact.lastActionSummary}",
                fontSize = 11.5.sp,
                color = Color(0xFF64748B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AuthorizeContactDialog(
    context: Context,
    onDismiss: () -> Unit,
    onAdd: (DelegatedContact) -> Unit
) {
    val deviceContacts = remember { ContactHelper.getDeviceContacts(context) }
    var selectedName by remember { mutableStateOf("") }
    var selectedPhone by remember { mutableStateOf("") }
    var allowEmail by remember { mutableStateOf(true) }
    var allowDirectSend by remember { mutableStateOf(true) }
    var customNotes by remember { mutableStateOf("Respond politely and professionally") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Authorize Contact for Oj AI", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Select a contact or enter phone number to allow Oj AI to message them on your behalf:",
                    fontSize = 13.sp,
                    color = Color(0xFF475569)
                )

                OutlinedTextField(
                    value = selectedName,
                    onValueChange = { selectedName = it },
                    label = { Text("Contact Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = selectedPhone,
                    onValueChange = { selectedPhone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                // Pick from real device contacts quickly
                if (deviceContacts.isNotEmpty()) {
                    Text("Quick pick from contacts:", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        deviceContacts.take(5).forEach { c ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF1F5F9),
                                modifier = Modifier.clickable {
                                    selectedName = c.name
                                    selectedPhone = c.phoneNumber
                                }
                            ) {
                                Text(c.name, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Allow reading Gmail context", fontSize = 13.sp)
                    Switch(checked = allowEmail, onCheckedChange = { allowEmail = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Allow direct send via SIM/Chat", fontSize = 13.sp)
                    Switch(checked = allowDirectSend, onCheckedChange = { allowDirectSend = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedName.isNotBlank()) {
                        onAdd(
                            DelegatedContact(
                                id = UUID.randomUUID().toString(),
                                name = selectedName.trim(),
                                phoneNumber = selectedPhone.trim(),
                                isAiAutoReplyEnabled = true,
                                allowReadEmailContext = allowEmail,
                                allowDirectSend = allowDirectSend,
                                customInstruction = customNotes.trim()
                            )
                        )
                    }
                },
                enabled = selectedName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = OlinamPrimary)
            ) {
                Text("Authorize")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun EditDelegatedContactDialog(
    contact: DelegatedContact,
    onDismiss: () -> Unit,
    onSave: (DelegatedContact) -> Unit
) {
    var allowEmail by remember { mutableStateOf(contact.allowReadEmailContext) }
    var allowDirectSend by remember { mutableStateOf(contact.allowDirectSend) }
    var customInstruction by remember { mutableStateOf(contact.customInstruction) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permissions: ${contact.name}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Read Gmail & Drive Context", fontSize = 13.sp)
                    Switch(checked = allowEmail, onCheckedChange = { allowEmail = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Send directly without confirmation", fontSize = 13.sp)
                    Switch(checked = allowDirectSend, onCheckedChange = { allowDirectSend = it })
                }

                OutlinedTextField(
                    value = customInstruction,
                    onValueChange = { customInstruction = it },
                    label = { Text("Custom instructions for this contact") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        contact.copy(
                            allowReadEmailContext = allowEmail,
                            allowDirectSend = allowDirectSend,
                            customInstruction = customInstruction
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = OlinamPrimary)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private fun getPluginIcon(type: String): ImageVector {
    return when (type) {
        "GMAIL" -> Icons.Default.Email
        "DRIVE" -> Icons.Default.Description
        "CALENDAR" -> Icons.Default.CalendarMonth
        "SMS" -> Icons.Default.SimCard
        else -> Icons.Default.Lightbulb
    }
}
