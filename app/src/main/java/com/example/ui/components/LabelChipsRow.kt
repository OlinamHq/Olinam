package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatLabel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LabelChipsRow(
    labels: List<ChatLabel>,
    selectedLabelId: String,
    unreadCounts: Map<String, Int> = emptyMap(),
    onLabelClick: (ChatLabel) -> Unit,
    onLabelLongClick: (ChatLabel) -> Unit = {},
    onAddLabelClick: () -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(labels, key = { it.id }) { label ->
            val isSelected = selectedLabelId == label.id
            val unreadCount = unreadCounts[label.id] ?: 0
            val isCustomLabel = label.id !in listOf("all", "direct", "groups", "sms")
            val labelColor = try {
                Color(android.graphics.Color.parseColor(label.colorHex))
            } catch (_: Exception) {
                Color(0xFF007AFF)
            }

            val chipBgColor = if (isSelected) {
                if (isCustomLabel) labelColor.copy(alpha = 0.15f) else Color(0xFFDCE8FE)
            } else {
                Color.White
            }

            val chipBorder = if (isSelected) {
                if (isCustomLabel) BorderStroke(1.2.dp, labelColor.copy(alpha = 0.7f)) else null
            } else {
                BorderStroke(1.dp, Color(0xFFE2E8F0))
            }

            val chipTextColor = if (isSelected) {
                if (isCustomLabel) labelColor else Color(0xFF007AFF)
            } else {
                Color(0xFF54656F)
            }

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .combinedClickable(
                        onClick = { onLabelClick(label) },
                        onLongClick = { onLabelLongClick(label) }
                    )
                    .testTag("label_chip_${label.id}"),
                shape = RoundedCornerShape(16.dp),
                color = chipBgColor,
                border = chipBorder
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Show color dot on the Home page for custom labels
                    if (isCustomLabel) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(labelColor)
                                .testTag("label_dot_${label.id}")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    val displayName = when (label.id) {
                        "all", "direct" -> "Chats"
                        "groups" -> "Groups"
                        "sms" -> "SMS"
                        else -> label.name
                    }

                    Text(
                        text = displayName,
                        fontSize = 13.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = chipTextColor
                    )

                    if (unreadCount > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        val badgeText = if (unreadCount > 99) "99+" else unreadCount.toString()
                        val badgeBg = if (isSelected) {
                            if (isCustomLabel) labelColor else Color(0xFF007AFF)
                        } else {
                            if (isCustomLabel) labelColor.copy(alpha = 0.2f) else Color(0xFF007AFF).copy(alpha = 0.15f)
                        }
                        val badgeTextColor = if (isSelected) {
                            Color.White
                        } else {
                            if (isCustomLabel) labelColor else Color(0xFF007AFF)
                        }

                        Box(
                            modifier = Modifier
                                .defaultMinSize(minWidth = 18.dp, minHeight = 18.dp)
                                .clip(CircleShape)
                                .background(badgeBg)
                                .padding(horizontal = if (unreadCount > 9) 5.dp else 2.dp, vertical = 1.dp)
                                .testTag("label_chip_badge_${label.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = badgeText,
                                color = badgeTextColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // The "+" Button for creating a new custom list / label
        item {
            Surface(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onAddLabelClick)
                    .testTag("add_custom_label_button"),
                shape = CircleShape,
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New List",
                        tint = Color(0xFF54656F),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
