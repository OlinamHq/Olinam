package com.example.model

import androidx.compose.runtime.Immutable
import java.util.UUID

/**
 * Authenticated user session for Omkun Morph Cloud & Oj AI integration.
 * Ensures every read, create, update, and delete operation is strictly scoped
 * to a verified user account rather than anonymous/global storage.
 */
@Immutable
data class MorphAccount(
    val accountId: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val authProvider: String = "OMKUN_ID", // "GOOGLE", "OLINAM_SSO", "OMKUN_ID"
    val sessionToken: String = "",
    val authorizedScopes: List<String> = DEFAULT_SCOPES,
    val connectedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val SCOPE_READ = "morph.notes.read"
        const val SCOPE_WRITE = "morph.notes.write"
        const val SCOPE_DELETE = "morph.notes.delete"

        val DEFAULT_SCOPES = listOf(SCOPE_READ, SCOPE_WRITE, SCOPE_DELETE)
    }
}

/**
 * Data model for Omkun Morph Notes (scoped per authenticated MorphAccount).
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
    val isArchived: Boolean = false
)

@Immutable
data class MorphChecklistItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val isDone: Boolean = false
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
