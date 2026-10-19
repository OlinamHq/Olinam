package com.example.ui.screens.storage

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OlinamPrimary

@Composable
fun NetworkUsageDialog(
    storagePrefs: StoragePreferences,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var metrics by remember { mutableStateOf(storagePrefs.getNetworkUsage()) }
    var showConfirmReset by remember { mutableStateOf(false) }

    val totalSent = metrics.bytesSent
    val totalReceived = metrics.bytesReceived
    val totalBytes = totalSent + totalReceived

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFEBF3FE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DataUsage,
                        contentDescription = null,
                        tint = OlinamPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Network usage",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF101828)
                    )
                    Text(
                        text = "Total: ${StoragePreferences.formatFileSize(totalBytes)}",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Sent vs Received Summary
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8F9FA))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = OlinamPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("Sent", fontSize = 12.sp, color = Color(0xFF64748B))
                            Text(
                                StoragePreferences.formatFileSize(totalSent),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF101828)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("Received", fontSize = 12.sp, color = Color(0xFF64748B))
                            Text(
                                StoragePreferences.formatFileSize(totalReceived),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF101828)
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFF1F3F5), thickness = 1.dp)

                // Category Breakdowns
                NetworkMetricRow(
                    icon = Icons.Default.Chat,
                    title = "Messages",
                    detail = "${metrics.messagesSent} sent • ${metrics.messagesReceived} received"
                )

                NetworkMetricRow(
                    icon = Icons.Default.Image,
                    title = "Media",
                    detail = "${StoragePreferences.formatFileSize(metrics.mediaBytesSent)} sent • ${StoragePreferences.formatFileSize(metrics.mediaBytesReceived)} received"
                )

                NetworkMetricRow(
                    icon = Icons.Default.Call,
                    title = "Calls",
                    detail = "${metrics.callMinutes} mins (${StoragePreferences.formatFileSize(metrics.callBytes)})"
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = OlinamPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Close")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { showConfirmReset = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFFEF4444)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset stats", color = Color(0xFFEF4444), fontSize = 13.sp)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White
    )

    if (showConfirmReset) {
        AlertDialog(
            onDismissRequest = { showConfirmReset = false },
            title = { Text("Reset network statistics?") },
            text = { Text("This will reset all transferred data counters back to zero. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        storagePrefs.resetNetworkStats()
                        metrics = storagePrefs.getNetworkUsage()
                        showConfirmReset = false
                        Toast.makeText(context, "Network statistics reset", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Reset", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmReset = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun NetworkMetricRow(
    icon: ImageVector,
    title: String,
    detail: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF475569),
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B)
            )
            Text(
                text = detail,
                fontSize = 12.5.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}
