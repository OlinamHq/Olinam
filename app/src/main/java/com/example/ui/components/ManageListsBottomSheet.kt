package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatLabel
import com.example.ui.theme.OlinamPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageListsBottomSheet(
    sheetState: SheetState,
    labels: List<ChatLabel>,
    selectedLabelId: String,
    onDismissRequest: () -> Unit,
    onSelectList: (String) -> Unit,
    onCreateNewList: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 40.dp, height = 4.dp),
                shape = RoundedCornerShape(2.dp),
                color = Color(0xFFD1D5DB)
            ) {}
        },
        modifier = Modifier
            .navigationBarsPadding()
            .testTag("manage_lists_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Label,
                            contentDescription = null,
                            tint = OlinamPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Lists",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111B21)
                    )
                }

                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF667781)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Use lists to organize your chats and quickly filter conversations.",
                fontSize = 13.5.sp,
                color = Color(0xFF667781),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // "+ New list" action button
            Button(
                onClick = {
                    onDismissRequest()
                    onCreateNewList()
                },
                colors = ButtonDefaults.buttonColors(containerColor = OlinamPrimary),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("new_list_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "New list",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFF0F2F5))
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "YOUR CHAT LISTS",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF8696A0),
                modifier = Modifier.padding(vertical = 6.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                // System List: Chats
                item {
                    ListItemRow(
                        title = "Chats",
                        icon = Icons.Default.Chat,
                        iconTint = Color(0xFF0160E3),
                        isSelected = selectedLabelId == "direct" || selectedLabelId == "all",
                        onClick = {
                            onSelectList("direct")
                            onDismissRequest()
                        }
                    )
                }

                // System List: Groups
                item {
                    ListItemRow(
                        title = "Groups",
                        icon = Icons.Default.Groups,
                        iconTint = Color(0xFF007AFF),
                        isSelected = selectedLabelId == "groups",
                        onClick = {
                            onSelectList("groups")
                            onDismissRequest()
                        }
                    )
                }

                // System List: SMS
                item {
                    ListItemRow(
                        title = "SMS",
                        icon = Icons.Default.Sms,
                        iconTint = Color(0xFF8B5CF6),
                        isSelected = selectedLabelId == "sms",
                        onClick = {
                            onSelectList("sms")
                            onDismissRequest()
                        }
                    )
                }

                // Custom user-defined labels (created by the user)
                items(labels.filter { it.id !in listOf("all", "direct", "groups", "sms") }) { label ->
                    val color = try {
                        Color(android.graphics.Color.parseColor(label.colorHex))
                    } catch (_: Exception) {
                        OlinamPrimary
                    }
                    ListItemRow(
                        title = label.name,
                        icon = Icons.Default.Label,
                        iconTint = color,
                        isSelected = selectedLabelId == label.id,
                        onClick = {
                            onSelectList(label.id)
                            onDismissRequest()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ListItemRow(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) OlinamPrimary else Color(0xFF111B21),
            modifier = Modifier.weight(1f)
        )

        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(OlinamPrimary)
            )
        }
    }
}
