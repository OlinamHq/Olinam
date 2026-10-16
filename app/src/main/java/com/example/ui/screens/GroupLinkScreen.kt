package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Conversation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupLinkScreen(
    conversation: Conversation,
    onBackClick: () -> Unit,
    onForwardToChats: (String) -> Unit = {}
) {
    BackHandler { onBackClick() }

    val context = LocalContext.current
    var showQrDialog by remember { mutableStateOf(false) }

    val inviteCode = remember(conversation.id, conversation.groupInviteCode) {
        if (conversation.groupInviteCode.isNotBlank()) conversation.groupInviteCode
        else conversation.id.filter { it.isLetterOrDigit() }.takeLast(10)
    }
    val inviteUrl = "https://chat.olinam.com/invite/$inviteCode"

    fun copyToClipboard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Group Invite Link", inviteUrl)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Link copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    fun shareLink() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Join \"${conversation.title}\" on Olinam")
            putExtra(
                Intent.EXTRA_TEXT,
                "Follow this link to join my Olinam group \"${conversation.title}\":\n$inviteUrl"
            )
        }
        context.startActivity(Intent.createChooser(intent, "Share group link"))
    }

    fun sendViaSms() {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("sms:")
                putExtra(
                    "sms_body",
                    "Join my Olinam group \"${conversation.title}\": $inviteUrl"
                )
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            shareLink()
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Group link",
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
        ) {
            // Group link header preview
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE2EDFC)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = conversation.title.take(1).uppercase(),
                        color = Color(0xFF0160E3),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = conversation.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF111827),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = inviteUrl,
                        fontSize = 13.5.sp,
                        color = Color(0xFF007AFF),
                        lineHeight = 18.sp,
                        modifier = Modifier.clickable { copyToClipboard() }
                    )
                }
            }

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFF1F5F9))
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action Items matching Screenshot 3
            GroupLinkActionItem(
                icon = Icons.Default.ContentCopy,
                title = "Copy link",
                onClick = { copyToClipboard() },
                testTag = "group_link_copy"
            )

            GroupLinkActionItem(
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                title = "Forward link via Olinam",
                onClick = {
                    onForwardToChats(inviteUrl)
                    shareLink()
                },
                testTag = "group_link_forward"
            )

            GroupLinkActionItem(
                icon = Icons.Default.Sms,
                title = "Send link via SMS",
                onClick = { sendViaSms() },
                testTag = "group_link_sms"
            )

            GroupLinkActionItem(
                icon = Icons.Default.Share,
                title = "Share link",
                onClick = { shareLink() },
                testTag = "group_link_share"
            )

            GroupLinkActionItem(
                icon = Icons.Default.QrCode,
                title = "QR code",
                onClick = { showQrDialog = true },
                testTag = "group_link_qr"
            )
        }
    }

    if (showQrDialog) {
        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            title = {
                Text(
                    text = "Group QR Code",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = conversation.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .size(200.dp)
                            .padding(12.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = "QR Code",
                                tint = Color(0xFF111827),
                                modifier = Modifier.size(160.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "People can scan this QR code with their phone camera to join ${conversation.title}.",
                        fontSize = 12.5.sp,
                        color = Color(0xFF64748B),
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showQrDialog = false
                        shareLink()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
                ) {
                    Text("Share QR Code", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showQrDialog = false }) {
                    Text("Close", color = Color(0xFF64748B))
                }
            }
        )
    }
}

@Composable
private fun GroupLinkActionItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 15.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF54656F),
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(24.dp))

        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF111827)
        )
    }
}
