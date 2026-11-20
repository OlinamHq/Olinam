package com.example.model

import androidx.compose.runtime.Immutable
import java.util.UUID

/**
 * Authenticated user session for Omkun Morph Cloud & Oj AI integration.
 * Supports both Scoped Developer API Keys (`omk_live_...`) via REST API v1.2
 * (`https://api.omkunmorph.workers.dev/api/v1` / `https://morph.omkun.com/api/v1`)
 * and Omkun Account credentials.
 */
@Immutable
data class MorphAccount(
    val accountId: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val authProvider: String = "OMKUN_ID", // "API_KEY", "GOOGLE", "OLINAM_SSO", "OMKUN_ID"
    val sessionToken: String = "",
    val refreshToken: String = "",
    val apiKey: String = "",
    val endpointEnvironment: String = ENV_EDGE, // "EDGE" or "CANONICAL"
    val workspaceId: String = "",
    val authorizedScopes: List<String> = DEFAULT_SCOPES,
    val connectedAt: Long = System.currentTimeMillis()
) {
    fun hasFullAccess(): Boolean = authorizedScopes.contains(SCOPE_ALL)

    fun canReadDocs(): Boolean =
        hasFullAccess() || authorizedScopes.any { it == SCOPE_READ_DOCS || it == SCOPE_READ }

    fun canWriteDocs(): Boolean =
        hasFullAccess() || authorizedScopes.any { it == SCOPE_WRITE_DOCS || it == SCOPE_WRITE }

    fun canReadDatabases(): Boolean =
        hasFullAccess() || authorizedScopes.any { it == SCOPE_READ_DATABASES || it == SCOPE_READ }

    fun canReadTasks(): Boolean =
        hasFullAccess() || authorizedScopes.any { it == SCOPE_READ_TASKS || it == SCOPE_READ }

    fun canWriteTasks(): Boolean =
        hasFullAccess() || authorizedScopes.any { it == SCOPE_WRITE_TASKS || it == SCOPE_WRITE }

    fun canDelete(): Boolean =
        hasFullAccess() || authorizedScopes.any { it == SCOPE_DELETE || it == SCOPE_WRITE_DOCS }

    fun maskedApiKey(): String {
        val trimmed = apiKey.trim()
        if (trimmed.isBlank()) return ""
        return if (trimmed.length > 12) {
            "${trimmed.take(9)}••••${trimmed.takeLast(4)}"
        } else {
            "••••${trimmed.takeLast(4)}"
        }
    }

    companion object {
        const val ENV_EDGE = "EDGE"
        const val ENV_CANONICAL = "CANONICAL"

        // Omkun Morph REST API v1.2 Scopes (morph.omkun.com/developer-api)
        const val SCOPE_ALL = "*"
        const val SCOPE_READ_DOCS = "read:docs"
        const val SCOPE_WRITE_DOCS = "write:docs"
        const val SCOPE_READ_DATABASES = "read:databases"
        const val SCOPE_READ_TASKS = "read:tasks"
        const val SCOPE_WRITE_TASKS = "write:tasks"

        // Legacy internal aliases retained for compatibility
        const val SCOPE_READ = "morph.notes.read"
        const val SCOPE_WRITE = "morph.notes.write"
        const val SCOPE_DELETE = "morph.notes.delete"

        val DEFAULT_SCOPES = listOf(
            SCOPE_READ_DOCS,
            SCOPE_WRITE_DOCS,
            SCOPE_READ_DATABASES,
            SCOPE_READ_TASKS,
            SCOPE_WRITE_TASKS,
            SCOPE_READ,
            SCOPE_WRITE,
            SCOPE_DELETE
        )

        val READ_ONLY_SCOPES = listOf(
            SCOPE_READ_DOCS,
            SCOPE_READ_DATABASES,
            SCOPE_READ_TASKS,
            SCOPE_READ
        )
    }
}

/**
 * Data model for Omkun Morph Documents, Tasks, and Notes (scoped per authenticated MorphAccount).
 */
@Immutable
data class MorphNote(
    val id: String = UUID.randomUUID().toString(),
    val accountId: String = "",
    val title: String = "",
    val content: String = "",
    val checklist: List<MorphChecklistItem> = emptyList(),
    val isChecklist: Boolean = false,
    val colorHex: String = "#FFFFFF",
    val isPinned: Boolean = false,
    val labels: List<String> = emptyList(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false,
    val sourceType: String = SOURCE_DOC, // "DOC", "TASK", "NOTE"
    val status: String = "",
    val remoteSynced: Boolean = false
) {
    companion object {
        const val SOURCE_DOC = "DOC"
        const val SOURCE_TASK = "TASK"
        const val SOURCE_NOTE = "NOTE"
    }
}

@Immutable
data class MorphChecklistItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val isDone: Boolean = false
)

@Immutable
data class MorphDatabaseInfo(
    val id: String,
    val title: String,
    val description: String = "",
    val recordCount: Int = 0
)

object MorphPalette {
    const val Default = "#FFFFFF"
    const val Peach = "#FCE8E6"
    const val Orange = "#FFE0B2"
    const val Lemon = "#FFF9C4"
    const val Mint = "#E8F5E9"
    const val Teal = "#E0F2F1"
    const val Sky = "#E3F2FD"
    const val Indigo = "#E8EAF6"
    const val Lavender = "#F3E5F5"
    const val Pink = "#FCE4EC"

    val Colors = listOf(
        Default,
        Peach,
        Orange,
        Lemon,
        Mint,
        Teal,
        Sky,
        Indigo,
        Lavender,
        Pink
    )
}
