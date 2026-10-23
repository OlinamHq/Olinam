package com.example.ai

import androidx.compose.runtime.Immutable

@Immutable
data class AiPlugin(
    val id: String,
    val name: String,
    val description: String,
    val iconType: String, // "GMAIL", "DRIVE", "CALENDAR", "PHOTOS", "MORPH", "SMS", "KNOWLEDGE"
    val isConnected: Boolean = false,
    val connectedAccountEmail: String = "",
    val syncSummary: String = "",
    val sampleItems: List<String> = emptyList()
) {
    val type: String get() = iconType
    val isEnabled: Boolean get() = isConnected
}

@Immutable
data class DelegatedContact(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val isAiAutoReplyEnabled: Boolean = true,
    val allowReadEmailContext: Boolean = true,
    val allowDirectSend: Boolean = true,
    val customInstruction: String = "Respond politely and professionally on my behalf",
    val lastActionSummary: String = "Ready to respond",
    val lastActionTimestamp: Long = System.currentTimeMillis()
)

@Immutable
data class AiAgentSettings(
    val isAgentMasterEnabled: Boolean = true,
    val autonomousMode: Boolean = true,
    val personalityTone: String = "Professional Executive",
    val userTitle: String = "Manager",
    val customSystemBio: String = "I am currently focused on project operations. Handle client inquiries smoothly."
) {
    val isMasterEnabled: Boolean get() = isAgentMasterEnabled
}

@Immutable
data class AiReplyResult(
    val generatedText: String,
    val contextSourcesUsed: List<String>,
    val timestamp: Long = System.currentTimeMillis(),
    val isDirectlySent: Boolean = false
)
