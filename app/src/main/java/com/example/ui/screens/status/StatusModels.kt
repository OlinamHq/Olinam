package com.example.ui.screens.status

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import com.example.model.Story

/**
 * Group of stories belonging to a single user (matching WhatsApp status grouping).
 */
data class UserStatusGroup(
    val userId: String,
    val userName: String,
    val userAvatar: String? = null,
    val items: List<Story>,
    val hasUnread: Boolean = false,
    val latestTimestamp: Long = 0L
)

/**
 * Color palette for WhatsApp-style text status updates.
 */
object TextStatusPalettes {
    val colors = listOf(
        Color(0xFF005C4B), // WhatsApp Dark Emerald
        Color(0xFF1E293B), // Midnight Slate
        Color(0xFF7C3AED), // Royal Purple
        Color(0xFFE11D48), // Vivid Rose
        Color(0xFFEA580C), // Sunset Amber/Orange
        Color(0xFF1D4ED8), // Sapphire Blue
        Color(0xFF0D9488), // Ocean Teal
        Color(0xFF831843)  // Deep Berry
    )

    val colorHexStrings = listOf(
        "#005C4B",
        "#1E293B",
        "#7C3AED",
        "#E11D48",
        "#EA580C",
        "#1D4ED8",
        "#0D9488",
        "#831843"
    )

    val fonts = listOf(
        FontFamily.Default to "Default",
        FontFamily.Serif to "Serif",
        FontFamily.Cursive to "Cursive",
        FontFamily.Monospace to "Monospace",
        FontFamily.SansSerif to "Sans"
    )

    fun parseColor(hex: String?): Color {
        if (hex.isNullOrBlank()) return Color(0xFF005C4B)
        return try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (_: Exception) {
            Color(0xFF005C4B)
        }
    }
}
