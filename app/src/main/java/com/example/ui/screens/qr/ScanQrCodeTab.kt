package com.example.ui.screens.qr

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors

@Composable
fun ScanQrCodeTab(
    onOpenChat: (name: String, phoneNumber: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var camera by remember { mutableStateOf<Camera?>(null) }
    var isFlashOn by remember { mutableStateOf(false) }
    var detectedContact by remember { mutableStateOf<ScannedContactInfo?>(null) }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val frameAnalyzer = remember {
        QrCodeAnalyzer.CameraFrameAnalyzer { scannedText ->
            val info = QrCodeAnalyzer.parseQrPayload(scannedText)
            detectedContact = info
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) {
            Toast.makeText(context, "Camera permission needed to scan QR codes", Toast.LENGTH_SHORT).show()
        }
    }

    // Photo picker for scanning QR code from gallery
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    if (bitmap != null) {
                        val decodedText = QrCodeAnalyzer.decodeBitmap(bitmap)
                        if (!decodedText.isNullOrBlank()) {
                            val info = QrCodeAnalyzer.parseQrPayload(decodedText)
                            detectedContact = info
                        } else {
                            Toast.makeText(context, "No QR code found in selected photo", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Could not load selected photo", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to read image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    // Scan line animation
    val infiniteTransition = rememberInfiniteTransition(label = "scan_laser")
    val laserYRatio by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_pos"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .testTag("scan_qr_screen")
    ) {
        // Camera Preview
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .also {
                                    it.setAnalyzer(cameraExecutor, frameAnalyzer)
                                }

                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                            cameraProvider.unbindAll()
                            camera = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Permission placeholder
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Camera access required",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Grant camera permission to scan Olinam QR codes instantly",
                    color = Color(0xFF9CA3AF),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0160E3))
                ) {
                    Text("Grant Permission")
                }
            }
        }

        // Viewfinder and Controls Overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Viewfinder frame
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0x22000000))
                        .border(2.dp, Color(0xFF0160E3), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Scanning laser bar
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val currentY = size.height * laserYRatio
                        drawLine(
                            color = Color(0xFF3B82F6),
                            start = Offset(16.dp.toPx(), currentY),
                            end = Offset(size.width - 16.dp.toPx(), currentY),
                            strokeWidth = 3.dp.toPx()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Scan an Olinam QR code",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Align the code within the frame",
                    color = Color(0xFF9CA3AF),
                    fontSize = 12.5.sp
                )
            }

            // Bottom camera controls: Gallery and Torch (Matching Screenshot 5)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gallery picker button
                IconButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0x66FFFFFF))
                        .testTag("scan_gallery_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Scan from Gallery",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Flashlight toggle button
                IconButton(
                    onClick = {
                        val newFlash = !isFlashOn
                        isFlashOn = newFlash
                        try {
                            camera?.cameraControl?.enableTorch(newFlash)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(if (isFlashOn) Color(0xFF0160E3) else Color(0x66FFFFFF))
                        .testTag("scan_flash_button")
                ) {
                    Icon(
                        imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Torch",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Dialog when contact QR is detected
        if (detectedContact != null) {
            val contact = detectedContact!!
            AlertDialog(
                onDismissRequest = {
                    detectedContact = null
                    frameAnalyzer.resumeScanning()
                },
                icon = {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE2EDFC)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = null,
                            tint = Color(0xFF0160E3),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = if (contact.phoneNumber.isNotBlank()) "Start chat with ${contact.name}" else "Scanned Content",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (contact.phoneNumber.isNotBlank()) {
                            Text(
                                text = contact.phoneNumber,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0160E3)
                            )
                        } else {
                            Text(
                                text = contact.rawData,
                                fontSize = 13.sp,
                                color = Color(0xFF4B5563)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (contact.isOlinamUrl) "Verified Olinam user" else "Would you like to message this contact?",
                            fontSize = 12.5.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val name = contact.name
                            val phone = contact.phoneNumber.ifBlank { contact.rawData }
                            detectedContact = null
                            onOpenChat(name, phone)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0160E3)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Message")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            detectedContact = null
                            frameAnalyzer.resumeScanning()
                        },
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Cancel")
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}
