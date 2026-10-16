package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GroupPermissions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupPermissionsScreen(
    initialPermissions: GroupPermissions = GroupPermissions(),
    onBackClick: () -> Unit,
    onPermissionsChanged: (GroupPermissions) -> Unit
) {
    BackHandler { onBackClick() }

    var canEditSettings by remember { mutableStateOf(initialPermissions.canMembersEditSettings) }
    var canSendMessages by remember { mutableStateOf(initialPermissions.canMembersSendMessages) }
    var canAddOthers by remember { mutableStateOf(initialPermissions.canMembersAddOthers) }
    var canInviteViaLink by remember { mutableStateOf(initialPermissions.canMembersInviteViaLink) }
    var requireAdminApproval by remember { mutableStateOf(initialPermissions.requireAdminApprovalToJoin) }

    fun notifyChange() {
        onPermissionsChanged(
            GroupPermissions(
                canMembersEditSettings = canEditSettings,
                canMembersSendMessages = canSendMessages,
                canMembersAddOthers = canAddOthers,
                canMembersInviteViaLink = canInviteViaLink,
                requireAdminApprovalToJoin = requireAdminApproval
            )
        )
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Group permissions",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF111827)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF111827)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Members can:",
                fontSize = 14.sp,
                color = Color(0xFF54656F),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // 1. Edit group settings
            PermissionToggleItem(
                icon = Icons.Default.Edit,
                title = "Edit group settings",
                subtitle = "This includes the name, icon, description, disappearing message timer, and the ability to pin, keep or unkeep messages.",
                checked = canEditSettings,
                onCheckedChange = {
                    canEditSettings = it
                    notifyChange()
                },
                testTag = "perm_edit_settings"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Send new messages
            PermissionToggleItem(
                icon = Icons.Default.ChatBubbleOutline,
                title = "Send new messages",
                subtitle = null,
                checked = canSendMessages,
                onCheckedChange = {
                    canSendMessages = it
                    notifyChange()
                },
                testTag = "perm_send_messages"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Add other members
            PermissionToggleItem(
                icon = Icons.Default.PersonAdd,
                title = "Add other members",
                subtitle = null,
                checked = canAddOthers,
                onCheckedChange = {
                    canAddOthers = it
                    notifyChange()
                },
                testTag = "perm_add_members"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Invite via link or QR code
            PermissionToggleItem(
                icon = Icons.Default.Link,
                title = "Invite via link or QR code",
                subtitle = null,
                checked = canInviteViaLink,
                onCheckedChange = {
                    canInviteViaLink = it
                    notifyChange()
                },
                testTag = "perm_invite_link"
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Admins can:",
                fontSize = 14.sp,
                color = Color(0xFF54656F),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // 5. Approve new members
            PermissionToggleItem(
                icon = Icons.Default.ManageAccounts,
                title = "Approve new members",
                subtitle = "When turned on, admins must approve anyone who wants to join the group.",
                hasLearnMore = true,
                checked = requireAdminApproval,
                onCheckedChange = {
                    requireAdminApproval = it
                    notifyChange()
                },
                testTag = "perm_approve_members"
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PermissionToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    hasLearnMore: Boolean = false,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF54656F),
            modifier = Modifier
                .padding(top = 4.dp)
                .size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF111827)
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 13.5.sp,
                    lineHeight = 18.sp,
                    color = Color(0xFF667781)
                )
            }
            if (hasLearnMore) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Learn more",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF007AFF)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF111827),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFCBD5E1)
            )
        )
    }
}
