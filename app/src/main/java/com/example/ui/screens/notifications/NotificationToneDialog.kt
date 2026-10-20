package com.example.ui.screens.notifications

import android.media.RingtoneManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OlinamPrimary

@Composable
fun NotificationToneDialog(
    title: String,
    currentToneTitle: String,
    ringtoneType: Int = RingtoneManager.TYPE_NOTIFICATION,
    notificationPrefs: NotificationPreferences,
    onToneSelected: (title: String, uriString: String) -> Unit,
    onDismiss: () -> Unit
) {
    val tones = remember(ringtoneType) {
        notificationPrefs.getAvailableTones(ringtoneType)
    }

    var selectedItem by remember {
        val initial = tones.find { it.title == currentToneTitle } ?: tones.firstOrNull() ?: ToneItem("Default (System)", "")
        mutableStateOf(initial)
    }

    var currentlyPlayingUri by remember { mutableStateOf<String?>(null) }

    // Make sure preview stops when dialog dismisses
    DisposableEffect(Unit) {
        onDispose {
            notificationPrefs.stopTonePreview()
        }
    }

    AlertDialog(
        onDismissRequest = {
            notificationPrefs.stopTonePreview()
            onDismiss()
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = OlinamPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF101828)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 340.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(tones, key = { it.title + it.uriString }) { tone ->
                        val isSelected = (tone.title == selectedItem.title)
                        val isPlaying = (currentlyPlayingUri == tone.uriString && tone.uriString.isNotBlank() && tone.uriString != "silent")

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedItem = tone
                                    currentlyPlayingUri = tone.uriString
                                    notificationPrefs.playTonePreview(tone.uriString, ringtoneType)
                                }
                                .padding(vertical = 6.dp, horizontal = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    selectedItem = tone
                                    currentlyPlayingUri = tone.uriString
                                    notificationPrefs.playTonePreview(tone.uriString, ringtoneType)
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = OlinamPrimary
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tone.title,
                                fontSize = 15.sp,
                                color = if (isSelected) OlinamPrimary else Color(0xFF1E293B),
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                            if (tone.uriString.isNotBlank() && tone.uriString != "silent") {
                                IconButton(
                                    onClick = {
                                        if (isPlaying) {
                                            notificationPrefs.stopTonePreview()
                                            currentlyPlayingUri = null
                                        } else {
                                            selectedItem = tone
                                            currentlyPlayingUri = tone.uriString
                                            notificationPrefs.playTonePreview(tone.uriString, ringtoneType)
                                        }
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                        contentDescription = if (isPlaying) "Stop preview" else "Play tone preview",
                                        tint = OlinamPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    notificationPrefs.stopTonePreview()
                    onToneSelected(selectedItem.title, selectedItem.uriString)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = OlinamPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    notificationPrefs.stopTonePreview()
                    onDismiss()
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White
    )
}
