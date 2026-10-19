package com.example.model

import java.util.UUID

/**
 * Data model for Omkun Morph Notes (Google Keep style inside Olinam)
 */
data class MorphNote(
    val id: String = UUID.randomUUID().toString(),
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

data class MorphChecklistItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val isDone: Boolean = false
)

object MorphPalette {
    val Default = "#FFFFFF"
    val Peach = "#FCE8E6"
    val Orange = "#FFE0B2"
    val Lemon = "#FFF9C4"
    val Mint = "#E8F5E9"
    val Teal = "#E0F2F1"
    val Sky = "#E3F2FD"
    val Indigo = "#E8EAF6"
    val Lavender = "#F3E5F5"
    val Pink = "#FCE4EC"

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
