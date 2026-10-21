package com.example.ui.screens.group

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.group.GroupColorHelper

/**
 * Enterprise Member Item row for Group Info screen with Admin badge, role actions and options menu.
 */
@Composable
fun GroupMemberItem(
    memberId: String,
    displayName: String,
    phoneNumber: String,
    isAdmin: Boolean,
    isCurrentUser: Boolean,
    canManageMembers: Boolean,
    onMakeAdmin: () -> Unit,
    onDismissAdmin: () -> Unit,
    onRemoveMember: () -> Unit,
    onMessageMember: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val avatarBg = remember(memberId) { GroupColorHelper.getColorForSender(memberId, displayName).copy(alpha = 0.15f) }
    val avatarColor = remember(memberId) { GroupColorHelper.getColorForSender(memberId, displayName) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !isCurrentUser) { menuExpanded = true }
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .testTag("group_member_$memberId"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar circle with initials
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(avatarBg),
            contentAlignment = Alignment.Center
        ) {
            val initial = displayName.trim().take(1).uppercase().ifBlank { "?" }
            Text(
                text = initial,
                color = avatarColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isCurrentUser) "You" else displayName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF111827),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (isAdmin) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFE2EDFC)
                    ) {
                        Text(
                            text = "Group Admin",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0160E3),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (phoneNumber.isNotBlank() && !isCurrentUser) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = phoneNumber,
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Action menu for non-current user
        if (!isCurrentUser) {
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Member options",
                        tint = Color(0xFF64748B)
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Message $displayName") },
                        onClick = {
                            menuExpanded = false
                            onMessageMember()
                        }
                    )

                    if (canManageMembers) {
                        if (isAdmin) {
                            DropdownMenuItem(
                                text = { Text("Dismiss as admin") },
                                onClick = {
                                    menuExpanded = false
                                    onDismissAdmin()
                                }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("Make group admin") },
                                onClick = {
                                    menuExpanded = false
                                    onMakeAdmin()
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = { Text("Remove $displayName", color = Color(0xFFDC2626)) },
                            onClick = {
                                menuExpanded = false
                                onRemoveMember()
                            }
                        )
                    }
                }
            }
        }
    }
}
