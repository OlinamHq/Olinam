package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.ChatViewModel
import kotlinx.coroutines.launch

@Composable
fun EditProfileScreen(
    viewModel: ChatViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentUser by viewModel.currentUser.collectAsState()

    var isUploadingPhoto by remember { mutableStateOf(false) }

    // Navigation state to open dedicated edit full screens matching user design
    var editingField by remember { mutableStateOf<String?>(null) } // "name", "bio", "phone"

    // When editing a specific field, show the dedicated fullscreen view matching Screenshot 3
    if (editingField != null) {
        when (editingField) {
            "name" -> {
                EditFieldFullScreen(
                    title = "Edit name",
                    label = "Name",
                    initialValue = currentUser.name,
                    maxLength = 75,
                    onBackClick = { editingField = null },
                    onSave = { newName ->
                        viewModel.updateProfile(
                            name = newName,
                            status = currentUser.statusMessage,
                            phone = currentUser.phoneNumber
                        )
                        editingField = null
                        Toast.makeText(context, "Name updated", Toast.LENGTH_SHORT).show()
                    }
                )
                return
            }
            "bio" -> {
                EditFieldFullScreen(
                    title = "Edit bio",
                    label = "Bio",
                    initialValue = currentUser.statusMessage,
                    maxLength = 139,
                    onBackClick = { editingField = null },
                    onSave = { newBio ->
                        viewModel.updateProfile(
                            name = currentUser.name,
                            status = newBio,
                            phone = currentUser.phoneNumber
                        )
                        editingField = null
                        Toast.makeText(context, "Bio updated", Toast.LENGTH_SHORT).show()
                    }
                )
                return
            }
            "phone" -> {
                EditFieldFullScreen(
                    title = "Edit phone",
                    label = "Phone number",
                    initialValue = currentUser.phoneNumber,
                    maxLength = 15,
                    onBackClick = { editingField = null },
                    onSave = { newPhone ->
                        viewModel.updateProfile(
                            name = currentUser.name,
                            status = currentUser.statusMessage,
                            phone = newPhone
                        )
                        editingField = null
                        Toast.makeText(context, "Phone number updated", Toast.LENGTH_SHORT).show()
                    }
                )
                return
            }
        }
    }

    BackHandler { onBackClick() }

    // Photo Picker for Profile DP
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            isUploadingPhoto = true
            scope.launch {
                val result = viewModel.uploadProfilePhotoToR2(context, uri)
                isUploadingPhoto = false
                result.onSuccess { publicUrl ->
                    viewModel.updateProfile(
                        name = currentUser.name,
                        status = currentUser.statusMessage,
                        phone = currentUser.phoneNumber,
                        avatarUrl = publicUrl
                    )
                    Toast.makeText(context, "Profile photo updated", Toast.LENGTH_SHORT).show()
                }.onFailure { err ->
                    Toast.makeText(context, "Failed to upload photo: ${err.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("edit_profile_screen"),
        containerColor = Color.White,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .background(Color.White)
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("edit_profile_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF111827),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Edit profile",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            // Large Circular Avatar (Matching screenshot)
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFCBD5E1))
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                    .testTag("profile_avatar_box"),
                contentAlignment = Alignment.Center
            ) {
                if (!currentUser.avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = currentUser.avatarUrl,
                        contentDescription = "Profile Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(76.dp)
                    )
                }

                if (isUploadingPhoto) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(36.dp),
                            strokeWidth = 3.dp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // "Set profile photo" clickable text button (Clean, no marketing)
            Text(
                text = if (isUploadingPhoto) "Uploading photo..." else "Set profile photo",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1D4ED8),
                modifier = Modifier
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                    .padding(8.dp)
                    .testTag("set_profile_photo_text")
            )

            Spacer(modifier = Modifier.height(36.dp))

            // --- 1. NAME FIELD ---
            ProfileInfoRow(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = "Name",
                value = currentUser.name.ifBlank { "Add your name" },
                showChevron = true,
                onClick = { editingField = "name" },
                tag = "profile_field_name"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- 2. BIO FIELD ---
            ProfileInfoRow(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = "Bio",
                value = currentUser.statusMessage.ifBlank { "Hello! I'm using Olinam" },
                showChevron = true,
                onClick = { editingField = "bio" },
                tag = "profile_field_bio"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- 3. MOBILE FIELD ---
            ProfileInfoRow(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = "Mobile",
                value = currentUser.phoneNumber.ifBlank { "Not configured" },
                showChevron = true,
                onClick = { editingField = "phone" },
                tag = "profile_field_mobile"
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

/**
 * Dedicated Full Screen field editor matching the user's provided design (Screenshot 3).
 * Contains:
 * - TopAppBar with back arrow and title (e.g. "Edit name")
 * - Rounded OutlinedTextField with floating label, clear (X) icon
 * - Character counter below (e.g. "5/75")
 * - Full-width pill-shaped "SAVE" button at the bottom that moves cleanly above keyboard.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFieldFullScreen(
    title: String,
    label: String,
    initialValue: String,
    maxLength: Int = 75,
    onBackClick: () -> Unit,
    onSave: (String) -> Unit
) {
    BackHandler { onBackClick() }
    var textValue by remember { mutableStateOf(initialValue) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .testTag("edit_field_fullscreen_${label.lowercase()}"),
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF111827)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("edit_field_back_button")
                    ) {
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
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            OutlinedTextField(
                value = textValue,
                onValueChange = {
                    if (it.length <= maxLength) {
                        textValue = it
                    }
                },
                label = { Text(label) },
                singleLine = maxLength <= 75,
                maxLines = if (maxLength <= 75) 1 else 4,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF0160E3),
                    unfocusedBorderColor = Color(0xFF0160E3),
                    focusedLabelColor = Color(0xFF0160E3),
                    cursorColor = Color(0xFF0160E3),
                    focusedTextColor = Color(0xFF111827),
                    unfocusedTextColor = Color(0xFF111827)
                ),
                trailingIcon = {
                    if (textValue.isNotEmpty()) {
                        IconButton(
                            onClick = { textValue = "" },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = "Clear",
                                tint = Color(0xFF6B7280),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_field_input")
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Character counter matching Screenshot 3 (e.g. 5/75)
            Text(
                text = "${textValue.length}/$maxLength",
                fontSize = 12.5.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.End)
                    .padding(end = 4.dp)
                    .testTag("char_counter")
            )

            Spacer(modifier = Modifier.weight(1f))

            val isSaveEnabled = textValue.isNotBlank()
            Button(
                onClick = {
                    if (isSaveEnabled) {
                        onSave(textValue.trim())
                    }
                },
                enabled = isSaveEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0160E3),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFE5E7EB),
                    disabledContentColor = Color(0xFF9CA3AF)
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(bottom = 8.dp)
                    .testTag("save_button")
            ) {
                Text(
                    text = "SAVE",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: @Composable () -> Unit,
    label: String,
    value: String,
    showChevron: Boolean = true,
    onClick: () -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(32.dp),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF6B7280)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF111827)
            )
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(
                color = Color(0xFFE5E7EB),
                thickness = 1.dp
            )
        }

        if (showChevron) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF374151),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
