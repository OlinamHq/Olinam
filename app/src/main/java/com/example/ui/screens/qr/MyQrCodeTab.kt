package com.example.ui.screens.qr

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun MyQrCodeTab(
    currentUser: UserProfile,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val shortUrl = remember(currentUser) {
        QrCodeGenerator.buildOlinamChatUrl(currentUser)
    }

    LaunchedEffect(currentUser) {
        isLoading = true
        val generated = withContext(Dispatchers.IO) {
            QrCodeGenerator.generateQrBitmap(
                content = shortUrl,
                width = 600,
                height = 600
            )
        }
        qrBitmap = generated
        isLoading = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Main QR Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("my_qr_code_card"),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFF9FAFB),
            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // User Avatar Circle
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0160E3)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentUser.name.take(1).uppercase().ifBlank { "Y" },
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = currentUser.name.ifBlank { "Olinam User" },
                    fontSize = 18.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )

                Text(
                    text = currentUser.phoneNumber.ifBlank { "Olinam Contact" },
                    fontSize = 13.5.sp,
                    color = Color(0xFF6B7280)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Real Scannable QR Code Box
                Box(
                    modifier = Modifier
                        .size(210.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color(0xFF0160E3),
                            modifier = Modifier.size(36.dp)
                        )
                    } else if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap!!.asImageBitmap(),
                            contentDescription = "My Scannable QR Code",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = "Error generating QR",
                            fontSize = 12.sp,
                            color = Color.Red
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Your QR code is private. If you share it with someone, they can scan it with their Olinam camera to message you.",
                    fontSize = 12.5.sp,
                    color = Color(0xFF6B7280),
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action 1: Share Code Button (Real image + link share)
        Button(
            onClick = {
                QrCodeGenerator.shareQrCodeImage(context, currentUser, qrBitmap)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0160E3)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("share_qr_code_button")
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Share code",
                fontWeight = FontWeight.Bold,
                fontSize = 14.5.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action 2: Copy link button
        OutlinedButton(
            onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Olinam Contact Link", shortUrl)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Short link copied to clipboard!", Toast.LENGTH_SHORT).show()
            },
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = null,
                tint = Color(0xFF374151),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Copy link",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.5.sp,
                color = Color(0xFF374151)
            )
        }
    }
}
