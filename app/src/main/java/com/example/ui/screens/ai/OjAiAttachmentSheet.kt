package com.example.ui.screens.ai

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.screens.camera.RecentMediaItem

/**
 * WhatsApp-style Attachment Sheet for Oj AI, docked smoothly below the input bar.
 */
@Composable
fun OjAiAttachmentSheet(
    onDocumentClick: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onMorphNotesClick: () -> Unit,
    onAudioClick: () -> Unit,
    onLocationClick: () -> Unit,
    recentMedia: List<RecentMediaItem> = emptyList(),
    onRecentMediaClick: (RecentMediaItem) -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
        color = Color(0xFFF8F9FA),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 12.dp)
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF8696A0).copy(alpha = 0.45f))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action row 1: Document, Camera, Gallery
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OjAttachmentItem(
                    label = "Document",
                    icon = Icons.Default.Description,
                    color = Color(0xFF7F66FF),
                    onClick = onDocumentClick,
                    testTag = "oj_attach_doc"
                )
                OjAttachmentItem(
                    label = "Camera",
                    icon = Icons.Default.CameraAlt,
                    color = Color(0xFFE91E63),
                    onClick = onCameraClick,
                    testTag = "oj_attach_camera"
                )
                OjAttachmentItem(
                    label = "Gallery",
                    icon = Icons.Default.PhotoLibrary,
                    color = Color(0xFF9C27B0),
                    onClick = onGalleryClick,
                    testTag = "oj_attach_gallery"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action row 2: Morph Notes, Audio, Location
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OjAttachmentItem(
                    label = "Morph Note",
                    icon = Icons.AutoMirrored.Filled.Notes,
                    color = Color(0xFF0288D1),
                    onClick = onMorphNotesClick,
                    testTag = "oj_attach_morph"
                )
                OjAttachmentItem(
                    label = "Audio File",
                    icon = Icons.Default.Headphones,
                    color = Color(0xFFFF9800),
                    onClick = onAudioClick,
                    testTag = "oj_attach_audio"
                )
                OjAttachmentItem(
                    label = "Location",
                    icon = Icons.Default.LocationOn,
                    color = Color(0xFF00A884),
                    onClick = onLocationClick,
                    testTag = "oj_attach_location"
                )
            }

            // Recent media thumbnails if available
            if (recentMedia.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Recent Photos & Media",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF54656F),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recentMedia.take(8), key = { it.id }) { item ->
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onRecentMediaClick(item) }
                        ) {
                            AsyncImage(
                                model = item.uri,
                                contentDescription = "Recent item",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OjAttachmentItem(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF374151),
            fontWeight = FontWeight.Medium
        )
    }
}
