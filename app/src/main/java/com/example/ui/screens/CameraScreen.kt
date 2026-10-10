package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CropRotate
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.ui.theme.OlinamPrimary

// Data class for drawing on the image in Screen 2
data class DrawPath(val points: List<Offset>, val color: Color, val strokeWidth: Float = 10f)

@Composable
fun CameraScreen(
    onDismiss: () -> Unit,
    onSendStatus: (caption: String, mediaUri: String?) -> Unit
) {
    BackHandler { onDismiss() }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Permission check for CAMERA
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Camera settings state (Screenshot 6)
    var isFrontCamera by remember { mutableStateOf(false) }
    var flashMode by remember { mutableStateOf(0) } // 0: Off, 1: On, 2: Auto
    var selectedMode by remember { mutableStateOf("Photo") } // "Video" or "Photo"
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    // Photo captured / selected state (transition to Screenshot 3)
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Screen 2 Editor states (Screenshot 3)
    var captionText by remember { mutableStateOf("") }
    var isViewOnce by remember { mutableStateOf(false) }
    var isHdEnabled by remember { mutableStateOf(true) }
    var showEmojiDialog by remember { mutableStateOf(false) }
    var showTextDialog by remember { mutableStateOf(false) }
    var customTextOverlay by remember { mutableStateOf("") }
    var isDrawingMode by remember { mutableStateOf(false) }
    val drawnPaths = remember { mutableStateListOf<DrawPath>() }
    var currentPathPoints = remember { mutableStateListOf<Offset>() }
    var currentDrawColor by remember { mutableStateOf(Color.Yellow) }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            capturedImageUri = uri
        }
    }

    // SCREEN 2: Photo Editor & Status Send (Screenshot 3)
    if (capturedImageUri != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .testTag("photo_editor_screen")
        ) {
            // Main image preview
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = capturedImageUri,
                    contentDescription = "Selected image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                // Drawing Canvas Overlay (allows user to draw with finger)
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(isDrawingMode) {
                            if (isDrawingMode) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        currentPathPoints.clear()
                                        currentPathPoints.add(offset)
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        currentPathPoints.add(change.position)
                                    },
                                    onDragEnd = {
                                        if (currentPathPoints.isNotEmpty()) {
                                            drawnPaths.add(
                                                DrawPath(
                                                    points = currentPathPoints.toList(),
                                                    color = currentDrawColor
                                                )
                                            )
                                            currentPathPoints.clear()
                                        }
                                    }
                                )
                            }
                        }
                ) {
                    for (pathItem in drawnPaths) {
                        if (pathItem.points.size > 1) {
                            val path = Path().apply {
                                moveTo(pathItem.points[0].x, pathItem.points[0].y)
                                for (i in 1 until pathItem.points.size) {
                                    lineTo(pathItem.points[i].x, pathItem.points[i].y)
                                }
                            }
                            drawPath(
                                path = path,
                                color = pathItem.color,
                                style = Stroke(
                                    width = pathItem.strokeWidth,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }

                    if (currentPathPoints.size > 1) {
                        val path = Path().apply {
                            moveTo(currentPathPoints[0].x, currentPathPoints[0].y)
                            for (i in 1 until currentPathPoints.size) {
                                lineTo(currentPathPoints[i].x, currentPathPoints[i].y)
                            }
                        }
                        drawPath(
                            path = path,
                            color = currentDrawColor,
                            style = Stroke(
                                width = 10f,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }

                // Custom Text Overlay
                if (customTextOverlay.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = customTextOverlay,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Top Action Bar (matching Screenshot 3: Close, Download, HD, Crop/Rotate, Emoji, Text Aa, Pen)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Close button (X)
                IconButton(
                    onClick = {
                        capturedImageUri = null
                        drawnPaths.clear()
                        customTextOverlay = ""
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                        .testTag("discard_photo_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Discard",
                        tint = Color.White
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Download / Save
                    IconButton(
                        onClick = { /* Saved to gallery notification */ },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Save",
                            tint = Color.White
                        )
                    }

                    // HD icon
                    IconButton(
                        onClick = { isHdEnabled = !isHdEnabled },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Hd,
                            contentDescription = "HD",
                            tint = if (isHdEnabled) OlinamPrimary else Color.White
                        )
                    }

                    // Crop / Rotate
                    IconButton(
                        onClick = { /* Crop/rotate action */ },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CropRotate,
                            contentDescription = "Crop",
                            tint = Color.White
                        )
                    }

                    // Sticker / Emoji
                    IconButton(
                        onClick = { showEmojiDialog = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mood,
                            contentDescription = "Sticker",
                            tint = Color.White
                        )
                    }

                    // Text Aa
                    IconButton(
                        onClick = { showTextDialog = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TextFields,
                            contentDescription = "Text overlay",
                            tint = Color.White
                        )
                    }

                    // Pen / Draw
                    IconButton(
                        onClick = { isDrawingMode = !isDrawingMode },
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (isDrawingMode) OlinamPrimary.copy(alpha = 0.6f) else Color.Transparent,
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Draw",
                            tint = if (isDrawingMode) Color.Yellow else Color.White
                        )
                    }
                }
            }

            // Bottom Caption & Send Bar (matching Screenshot 3)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // "Swipe up for filters" indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Swipe up",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Swipe up for filters",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                }

                // Caption row with View Once and Royal Blue Send Checkmark button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Caption input pill (matching Screenshot 3)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        color = Color(0xFF1E293B).copy(alpha = 0.85f),
                        border = androidx.compose.foundation.BorderStroke(0.6.dp, Color(0xFF334155))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Media",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                if (captionText.isEmpty()) {
                                    Text(
                                        text = "Add a caption...",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 15.sp
                                    )
                                }
                                BasicTextField(
                                    value = captionText,
                                    onValueChange = { captionText = it },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    cursorBrush = SolidColor(OlinamPrimary),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                    keyboardActions = KeyboardActions(
                                        onSend = {
                                            val uriStr = capturedImageUri?.toString() ?: "camera_snap_${System.currentTimeMillis()}"
                                            val finalCaption = captionText.ifBlank { "Status update ✨" }
                                            onSendStatus(finalCaption, uriStr)
                                        }
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("caption_input_field")
                                )
                            }

                            // View Once toggle ("①" circle icon)
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isViewOnce) OlinamPrimary else Color(0xFF334155)
                                    )
                                    .clickable { isViewOnce = !isViewOnce },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "1",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Circular Send Button with Checkmark in Royal Blue (OlinamPrimary)
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(OlinamPrimary)
                            .clickable {
                                val uriStr = capturedImageUri?.toString() ?: "camera_snap_${System.currentTimeMillis()}"
                                val finalCaption = captionText.ifBlank { "Status update ✨" }
                                onSendStatus(finalCaption, uriStr)
                            }
                            .testTag("send_status_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Send Status",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            // Emoji / Sticker Picker Dialog
            if (showEmojiDialog) {
                AlertDialog(
                    onDismissRequest = { showEmojiDialog = false },
                    title = { Text("Choose a Sticker") },
                    text = {
                        val emojis = listOf("🔥", "❤️", "😍", "🎉", "✨", "💯", "😎", "🚀", "👍", "🙏", "🤩", "🌟")
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(vertical = 12.dp)
                        ) {
                            items(emojis) { emoji ->
                                Text(
                                    text = emoji,
                                    fontSize = 32.sp,
                                    modifier = Modifier
                                        .clickable {
                                            customTextOverlay = if (customTextOverlay.isBlank()) emoji else "$customTextOverlay $emoji"
                                            showEmojiDialog = false
                                        }
                                        .padding(4.dp)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showEmojiDialog = false }) {
                            Text("Done")
                        }
                    }
                )
            }

            // Text Aa Overlay Dialog
            if (showTextDialog) {
                var inputText by remember { mutableStateOf(customTextOverlay) }
                AlertDialog(
                    onDismissRequest = { showTextDialog = false },
                    title = { Text("Add Text Overlay") },
                    text = {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("Type text here...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                customTextOverlay = inputText
                                showTextDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OlinamPrimary)
                        ) {
                            Text("Add")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showTextDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
        return
    }

    // SCREEN 1: Fullscreen Camera Viewfinder (Screenshot 6)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("camera_screen_viewfinder")
    ) {
        // CameraX Live Preview View
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        val capture = ImageCapture.Builder()
                            .setFlashMode(
                                when (flashMode) {
                                    1 -> ImageCapture.FLASH_MODE_ON
                                    2 -> ImageCapture.FLASH_MODE_AUTO
                                    else -> ImageCapture.FLASH_MODE_OFF
                                }
                            )
                            .build()
                        imageCapture = capture

                        val selector = if (isFrontCamera) {
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        } else {
                            CameraSelector.DEFAULT_BACK_CAMERA
                        }
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                selector,
                                preview,
                                capture
                            )
                        } catch (e: Exception) {
                            try {
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    selector,
                                    preview
                                )
                            } catch (_: Exception) {}
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Permission request placeholder view
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F172A)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Camera",
                        tint = Color.White,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Camera Permission Required",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = OlinamPrimary)
                    ) {
                        Text("Grant Permission")
                    }
                }
            }
        }

        // Top Bar (matching Screenshot 6: Close X button on left, Flash toggle on right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Close button (X)
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(42.dp)
                    .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                    .testTag("camera_close_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Flash Toggle
            IconButton(
                onClick = { flashMode = (flashMode + 1) % 3 },
                modifier = Modifier
                    .size(42.dp)
                    .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                    .testTag("camera_flash_toggle")
            ) {
                Icon(
                    imageVector = when (flashMode) {
                        1 -> Icons.Default.FlashOn
                        2 -> Icons.Default.FlashAuto
                        else -> Icons.Default.FlashOff
                    },
                    contentDescription = "Flash",
                    tint = if (flashMode > 0) Color.Yellow else Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Bottom Controls Container (matching Screenshot 6)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mini Recent Media Thumbnails Strip
            val sampleThumbnails = listOf(
                "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=200",
                "https://images.unsplash.com/photo-1541701494587-cb58502866ab?w=200",
                "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=200",
                "https://images.unsplash.com/photo-1518770660439-4636190af475?w=200",
                "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=200"
            )
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sampleThumbnails) { imgUrl ->
                    Box(
                        modifier = Modifier
                            .size(width = 54.dp, height = 54.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                            .clickable {
                                capturedImageUri = Uri.parse(imgUrl)
                            }
                    ) {
                        AsyncImage(
                            model = imgUrl,
                            contentDescription = "Recent photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Shutter Controls Row: Gallery, Big White Shutter, Flip Camera
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gallery Icon Button (opens system photo picker)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable {
                            photoPickerLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        }
                        .testTag("gallery_picker_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Gallery",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Big White Circular Shutter Button (Screenshot 6)
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .border(4.dp, Color.White, CircleShape)
                        .clickable {
                            val cap = imageCapture
                            val photoFile = java.io.File(context.cacheDir, "camera_snap_${System.currentTimeMillis()}.jpg")
                            if (cap != null) {
                                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                                cap.takePicture(
                                    outputOptions,
                                    ContextCompat.getMainExecutor(context),
                                    object : ImageCapture.OnImageSavedCallback {
                                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                            capturedImageUri = Uri.fromFile(photoFile)
                                        }
                                        override fun onError(exc: androidx.camera.core.ImageCaptureException) {
                                            photoPickerLauncher.launch(
                                                androidx.activity.result.PickVisualMediaRequest(
                                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                                )
                                            )
                                        }
                                    }
                                )
                            } else {
                                photoPickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            }
                        }
                        .testTag("camera_shutter_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }

                // Flip Camera Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { isFrontCamera = !isFrontCamera }
                        .testTag("flip_camera_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FlipCameraAndroid,
                        contentDescription = "Flip camera",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Mode Selector Pill: "Video" | "Photo" (Screenshot 6)
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("Video", "Photo").forEach { mode ->
                    val isSelected = selectedMode == mode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) Color.White.copy(alpha = 0.2f) else Color.Transparent
                            )
                            .clickable { selectedMode = mode }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = mode,
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
