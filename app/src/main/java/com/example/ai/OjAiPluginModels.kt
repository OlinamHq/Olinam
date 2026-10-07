package com.example.ai

data class AiPlugin(
    val id: String,
    val name: String,
    val description: String,
    val iconType: String, // "GMAIL", "DRIVE", "CALENDAR", "SMS", "KNOWLEDGE"
    val isConnected: Boolean = true,
    val syncSummary: String = "",
    val sampleItems: List<String> = emptyList()
)

data class DelegatedContact(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val isAiAutoReplyEnabled: Boolean = true,
    val allowReadEmailContext: Boolean = true,
    val allowDirectSend: Boolean = true, // Send directly vs ask 1-tap confirmation
    val customInstruction: String = "Respond politely and professionally on my behalf",
    val lastActionSummary: String = "Ready to respond",
    val lastActionTimestamp: Long = System.currentTimeMillis()
)

data class AiAgentSettings(
    val isAgentMasterEnabled: Boolean = true,
    val autonomousMode: Boolean = true, // true = direct send, false = draft with confirmation
    val personalityTone: String = "Professional Executive", // "Professional Executive", "Friendly & Warm", "Concise & Fast"
    val userTitle: String = "Manager",
    val customSystemBio: String = "I am currently focused on project operations. Handle client inquiries smoothly."
)

data class AiReplyResult(
    val generatedText: String,
    val contextSourcesUsed: List<String>,
    val timestamp: Long = System.currentTimeMillis(),
    val isDirectlySent: Boolean = false
)
