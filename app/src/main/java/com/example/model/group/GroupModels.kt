package com.example.model.group

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

enum class GroupRole {
    OWNER,
    ADMIN,
    MEMBER
}

@Immutable
data class GroupMember(
    val id: String,
    val name: String,
    val phoneNumber: String = "",
    val avatarUrl: String? = null,
    val role: GroupRole = GroupRole.MEMBER,
    val joinedAt: Long = System.currentTimeMillis()
)

enum class GroupSystemEventType {
    GROUP_CREATED,
    MEMBER_ADDED,
    MEMBER_REMOVED,
    MEMBER_LEFT,
    ADMIN_PROMOTED,
    ADMIN_DEMOTED,
    NAME_CHANGED,
    DESCRIPTION_CHANGED,
    PERMISSIONS_CHANGED
}

/**
 * Deterministic sender color helper for group chats, matching WhatsApp's distinct
 * color assignment per participant ID.
 */
object GroupColorHelper {
    private val memberColors = listOf(
        Color(0xFFE542A3), // Magenta / Pink
        Color(0xFF00A884), // Teal / Green
        Color(0xFF1E88E5), // Blue
        Color(0xFFF57C00), // Deep Orange
        Color(0xFF8E24AA), // Purple
        Color(0xFF00ACC1), // Cyan
        Color(0xFF43A047), // Green
        Color(0xFFD81B60), // Ruby Red
        Color(0xFF3949AB), // Indigo
        Color(0xFF00897B)  // Dark Teal
    )

    fun getColorForSender(senderId: String, senderName: String = ""): Color {
        val key = senderId.ifBlank { senderName }
        if (key.isBlank()) return memberColors[0]
        val hash = kotlin.math.abs(key.hashCode())
        return memberColors[hash % memberColors.size]
    }
}
