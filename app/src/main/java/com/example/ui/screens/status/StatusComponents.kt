package com.example.ui.screens.status

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Story
import com.example.ui.components.formatMessageTime
import com.example.ui.theme.OlinamPrimary

/**
 * Segmented status ring drawing one arc per status item (WhatsApp signature ring).
 */
@Composable
fun SegmentedStatusRing(
    items: List<Story>,
    modifier: Modifier = Modifier,
    size: Dp = 58.dp,
    strokeWidth: Dp = 2.8.dp,
    activeColor: Color = Color(0xFF25D366),
    viewedColor: Color = Color(0xFFB0B0B0)
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        val totalCount = items.size.coerceAtLeast(1)

        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val canvasSize = this.size.minDimension
            val radius = (canvasSize - strokePx) / 2f
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)
            val arcSize = Size(canvasSize - strokePx, canvasSize - strokePx)

            if (totalCount == 1) {
                val isViewed = items.firstOrNull()?.viewed == true
                drawArc(
                    color = if (isViewed) viewedColor else activeColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx)
                )
            } else {
                val gapDegrees = if (totalCount <= 3) 14f else if (totalCount <= 6) 10f else 6f
                val sweepAngle = (360f - (gapDegrees * totalCount)) / totalCount
                var currentAngle = -90f + (gapDegrees / 2f)

                items.forEach { story ->
                    val color = if (story.viewed) viewedColor else activeColor
                    drawArc(
                        color = color,
                        startAngle = currentAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )
                    currentAngle += sweepAngle + gapDegrees
                }
            }
        }
    }
}

/**
 * Contact Status List Item row in WhatsApp Updates list.
 */
@Composable
fun StatusListItem(
    group: UserStatusGroup,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp)
            .testTag("status_item_${group.userId}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar inside segmented status ring
        Box(
            modifier = Modifier.size(58.dp),
            contentAlignment = Alignment.Center
        ) {
            SegmentedStatusRing(
                items = group.items,
                size = 58.dp,
                activeColor = Color(0xFF25D366),
                viewedColor = Color(0xFFC4C4C4)
            )

            // Contact Avatar or First Letter
            val latestItem = group.items.firstOrNull()
            if (!latestItem?.mediaUrl.isNullOrBlank()) {
                AsyncImage(
                    model = latestItem?.mediaUrl,
                    contentDescription = group.userName,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (latestItem?.backgroundColor != null)
                                TextStatusPalettes.parseColor(latestItem.backgroundColor)
                            else OlinamPrimary.copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = group.userName.take(1).uppercase(),
                        color = if (latestItem?.backgroundColor != null) Color.White else OlinamPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = group.userName,
                fontSize = 16.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            val timeText = if (group.latestTimestamp > 0) formatMessageTime(group.latestTimestamp) else "Today"
            Text(
                text = timeText,
                fontSize = 13.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * WhatsApp "My Status" Row at the top of Updates tab.
 */
@Composable
fun MyStatusHeaderRow(
    myGroup: UserStatusGroup?,
    onViewMyStatus: () -> Unit,
    onAddCameraStatus: () -> Unit,
    onAddTextStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasMyStatus = myGroup != null && myGroup.items.isNotEmpty()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                if (hasMyStatus) {
                    onViewMyStatus()
                } else {
                    onAddCameraStatus()
                }
            }
            .padding(vertical = 8.dp)
            .testTag("my_status_row"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar with Add (+) badge or Segmented Ring
        Box(
            modifier = Modifier.size(58.dp),
            contentAlignment = Alignment.Center
        ) {
            if (hasMyStatus) {
                SegmentedStatusRing(
                    items = myGroup!!.items,
                    size = 58.dp,
                    activeColor = Color(0xFF25D366),
                    viewedColor = Color(0xFFC4C4C4)
                )
                val latest = myGroup.items.first()
                if (!latest.mediaUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = latest.mediaUrl,
                        contentDescription = "My status",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (latest.backgroundColor != null)
                                    TextStatusPalettes.parseColor(latest.backgroundColor)
                                else OlinamPrimary
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "You",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "You",
                        fontWeight = FontWeight.Bold,
                        color = OlinamPrimary,
                        fontSize = 16.sp
                    )
                }

                // Green/Blue Plus badge at bottom-end
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(Color(0xFF25D366))
                        .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add status",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "My Status",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (hasMyStatus) {
                    val count = myGroup!!.items.size
                    val timeStr = formatMessageTime(myGroup.latestTimestamp)
                    "$timeStr • $count update${if (count > 1) "s" else ""}"
                } else {
                    "Tap to add status update"
                },
                fontSize = 13.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (hasMyStatus) {
            IconButton(
                onClick = onAddCameraStatus,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Add photo/video status",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(
                onClick = onAddTextStatus,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Add text status",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
