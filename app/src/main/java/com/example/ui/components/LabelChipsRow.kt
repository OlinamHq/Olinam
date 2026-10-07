package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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

@Composable
fun LabelChipsRow(
    labels: List<ChatLabel>,
    selectedLabelId: String,
    onLabelClick: (ChatLabel) -> Unit,
    onAddLabelClick: () -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(labels, key = { it.id }) { label ->
            val isSelected = selectedLabelId == label.id
            val labelColor = try {
                Color(android.graphics.Color.parseColor(label.colorHex))
            } catch (_: Exception) {
                Color(0xFF0160E3)
            }

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onLabelClick(label) }
                    .testTag("label_chip_${label.id}"),
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) Color(0xFFE2EDFC) else Color.White,
                border = if (isSelected) BorderStroke(1.dp, Color(0xFFBFDBFE)) else BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (label.id != "all" && label.id != "direct" && label.id != "groups" && label.id != "sms") {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(labelColor)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                    }
                    Text(
                        text = if (label.id == "all") "All" else label.name,
                        fontSize = 13.5.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isSelected) Color(0xFF0160E3) else Color(0xFF4B5563)
                    )
                }
            }
        }

        // The "+" Button for creating a new custom label / folder
        item {
            Surface(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onAddLabelClick)
                    .testTag("add_custom_label_button"),
                shape = CircleShape,
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFD1D5DB))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Label / Folder",
                        tint = Color(0xFF4B5563),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
