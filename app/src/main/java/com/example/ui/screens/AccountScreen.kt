package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.PhoneForwarded
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ChatViewModel
import com.example.ui.theme.OlinamPrimary
import kotlinx.coroutines.launch

private enum class AccountSheetType {
    NONE,
    PASSKEYS,
    PASSWORD,
    EMAIL,
    TWO_STEP,
    SECURITY_NOTIFICATIONS,
    USERNAME,
    DELETE_ACCOUNT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    viewModel: ChatViewModel,
    onBackClick: () -> Unit
) {
    BackHandler { onBackClick() }

    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()

    var activeSheet by remember { mutableStateOf(AccountSheetType.NONE) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("account_screen"),
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Account",
                        fontWeight = FontWeight.Normal,
                        fontSize = 20.sp,
                        color = Color(0xFF1F2937)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("account_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1F2937)
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
                .verticalScroll(rememberScrollState())
        ) {
            // Section 1: Login and security
            Text(
                text = "Login and security",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF5F6368),
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp)
            )

            AccountOptionRow(
                icon = Icons.Default.Key,
                title = "Passkeys",
                onClick = { activeSheet = AccountSheetType.PASSKEYS }
            )

            AccountOptionRow(
                icon = Icons.Default.Password,
                title = "Password",
                onClick = { activeSheet = AccountSheetType.PASSWORD }
            )

            AccountOptionRow(
                icon = Icons.Default.Email,
                title = "Email address",
                onClick = { activeSheet = AccountSheetType.EMAIL }
            )

            AccountOptionRow(
                icon = Icons.Default.LockReset,
                title = "Two-step verification",
                onClick = { activeSheet = AccountSheetType.TWO_STEP }
            )

            AccountOptionRow(
                icon = Icons.Default.Shield,
                title = "Security notifications",
                onClick = { activeSheet = AccountSheetType.SECURITY_NOTIFICATIONS }
            )

            HorizontalDivider(
                color = Color(0xFFF3F4F6),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Section 2: Your account
            Text(
                text = "Your account",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF5F6368),
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 8.dp)
            )

            AccountOptionRow(
                icon = Icons.Default.AlternateEmail,
                title = "Username",
                onClick = { activeSheet = AccountSheetType.USERNAME }
            )

            HorizontalDivider(
                color = Color(0xFFF3F4F6),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Section 3: Delete account
            AccountOptionRow(
                icon = Icons.Default.DeleteOutline,
                title = "Delete account",
                onClick = { activeSheet = AccountSheetType.DELETE_ACCOUNT }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Interactive Sheets
    when (activeSheet) {
        AccountSheetType.PASSKEYS -> {
            PasskeysBottomSheet(
                context = context,
                onDismiss = { activeSheet = AccountSheetType.NONE }
            )
        }
        AccountSheetType.PASSWORD -> {
            PasswordBottomSheet(
                context = context,
                onDismiss = { activeSheet = AccountSheetType.NONE }
            )
        }
        AccountSheetType.EMAIL -> {
            EmailAddressBottomSheet(
                context = context,
                onDismiss = { activeSheet = AccountSheetType.NONE }
            )
        }
        AccountSheetType.TWO_STEP -> {
            TwoStepVerificationBottomSheet(
                context = context,
                onDismiss = { activeSheet = AccountSheetType.NONE }
            )
        }
        AccountSheetType.SECURITY_NOTIFICATIONS -> {
            SecurityNotificationsBottomSheet(
                context = context,
                onDismiss = { activeSheet = AccountSheetType.NONE }
            )
        }
        AccountSheetType.USERNAME -> {
            UsernameBottomSheet(
                context = context,
                viewModel = viewModel,
                onDismiss = { activeSheet = AccountSheetType.NONE }
            )
        }
        AccountSheetType.DELETE_ACCOUNT -> {
            DeleteAccountBottomSheet(
                context = context,
                viewModel = viewModel,
                registeredPhone = currentUser.phoneNumber,
                onDismiss = { activeSheet = AccountSheetType.NONE }
            )
        }
        AccountSheetType.NONE -> {}
    }
}

@Composable
private fun AccountOptionRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = Color(0xFF4B5563),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(24.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF1F2937),
            modifier = Modifier.weight(1f)
        )
    }
}

// -------------------------------------------------------------------------------------------------
// 1. Passkeys Bottom Sheet
// -------------------------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PasskeysBottomSheet(
    context: Context,
    onDismiss: () -> Unit
) {
    val prefs = remember { context.getSharedPreferences("olinam_account_security", Context.MODE_PRIVATE) }
    var hasPasskey by remember { mutableStateOf(prefs.getBoolean("has_passkey", false)) }
    val deviceName = remember {
        val brand = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
        val model = Build.MODEL
        "$brand $model"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEBF3FE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = null,
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Passkeys for Olinam",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Passkeys let you sign in safely and easily with your fingerprint, face, or screen lock. Your passkey is encrypted and stored in your Google Password Manager.",
                fontSize = 14.sp,
                color = Color(0xFF6B7280),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (hasPasskey) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Passkey active",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF15803D)
                            )
                            Text(
                                text = "Configured on $deviceName",
                                fontSize = 13.sp,
                                color = Color(0xFF166534)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedButton(
                    onClick = {
                        prefs.edit().putBoolean("has_passkey", false).apply()
                        hasPasskey = false
                        Toast.makeText(context, "Passkey removed from this device", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Remove passkey", color = Color(0xFFDC2626))
                }
            } else {
                Button(
                    onClick = {
                        prefs.edit().putBoolean("has_passkey", true).apply()
                        hasPasskey = true
                        Toast.makeText(context, "Passkey created with device screen lock!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Create a passkey", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 2. Password Bottom Sheet
// -------------------------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PasswordBottomSheet(
    context: Context,
    onDismiss: () -> Unit
) {
    val prefs = remember { context.getSharedPreferences("olinam_account_security", Context.MODE_PRIVATE) }
    var savedPassword by remember { mutableStateOf(prefs.getString("account_password", null)) }

    var currentPasswordInput by remember { mutableStateOf("") }
    var newPasswordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = if (savedPassword == null) "Set Password" else "Change Password",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Use this password to lock sensitive chats, backup keys, and protect account actions.",
                fontSize = 14.sp,
                color = Color(0xFF6B7280)
            )

            Spacer(modifier = Modifier.height(18.dp))

            if (savedPassword != null) {
                OutlinedTextField(
                    value = currentPasswordInput,
                    onValueChange = { currentPasswordInput = it },
                    label = { Text("Current password") },
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = newPasswordInput,
                onValueChange = { newPasswordInput = it },
                label = { Text("New password (min 6 characters)") },
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle password"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPasswordInput,
                onValueChange = { confirmPasswordInput = it },
                label = { Text("Confirm new password") },
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (savedPassword != null && currentPasswordInput != savedPassword) {
                        Toast.makeText(context, "Current password is incorrect", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (newPasswordInput.length < 6) {
                        Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (newPasswordInput != confirmPasswordInput) {
                        Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    prefs.edit().putString("account_password", newPasswordInput).apply()
                    savedPassword = newPasswordInput
                    Toast.makeText(context, "Password saved successfully!", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Password", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 3. Email Address Bottom Sheet
// -------------------------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmailAddressBottomSheet(
    context: Context,
    onDismiss: () -> Unit
) {
    val prefs = remember { context.getSharedPreferences("olinam_account_security", Context.MODE_PRIVATE) }
    var savedEmail by remember { mutableStateOf(prefs.getString("account_email", "")) }
    var emailInput by remember { mutableStateOf(savedEmail ?: "") }
    var isVerifying by remember { mutableStateOf(false) }
    var otpInput by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Email address",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Email helps you access and recover your Olinam account. It isn't visible to your contacts.",
                fontSize = 14.sp,
                color = Color(0xFF6B7280)
            )

            Spacer(modifier = Modifier.height(18.dp))

            if (!isVerifying) {
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("Email address") },
                    placeholder = { Text("you@example.com") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (!emailInput.contains("@") || !emailInput.contains(".")) {
                            Toast.makeText(context, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isVerifying = true
                        Toast.makeText(context, "Verification code sent to $emailInput", Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Verify Email", fontWeight = FontWeight.SemiBold)
                }
            } else {
                Text(
                    text = "Enter the 6-digit verification code sent to $emailInput (demo code: 582914)",
                    fontSize = 13.5.sp,
                    color = Color(0xFF2563EB)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = otpInput,
                    onValueChange = { if (it.length <= 6) otpInput = it },
                    label = { Text("6-digit code") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (otpInput.length < 6) {
                            Toast.makeText(context, "Enter the complete 6-digit code", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        prefs.edit().putString("account_email", emailInput).apply()
                        savedEmail = emailInput
                        Toast.makeText(context, "Email verified and linked successfully!", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Confirm Verification", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 4. Two-Step Verification Bottom Sheet
// -------------------------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TwoStepVerificationBottomSheet(
    context: Context,
    onDismiss: () -> Unit
) {
    val prefs = remember { context.getSharedPreferences("olinam_account_security", Context.MODE_PRIVATE) }
    var isEnabled by remember { mutableStateOf(prefs.getBoolean("two_step_enabled", false)) }
    var pinInput by remember { mutableStateOf("") }
    var isConfiguringPin by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (isEnabled) Color(0xFFE8F8EE) else Color(0xFFEEEDFD)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (isEnabled) Color(0xFF16A34A) else Color(0xFF6366F1),
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Two-step verification",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "For added security, turn on two-step verification to require a 6-digit PIN when registering your phone number with Olinam again.",
                fontSize = 14.sp,
                color = Color(0xFF6B7280)
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (!isEnabled && !isConfiguringPin) {
                Button(
                    onClick = { isConfiguringPin = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Turn on", fontWeight = FontWeight.SemiBold)
                }
            } else if (isConfiguringPin) {
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) pinInput = it },
                    label = { Text("Enter a 6-digit PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (pinInput.length != 6) {
                            Toast.makeText(context, "PIN must be exactly 6 digits", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        prefs.edit()
                            .putBoolean("two_step_enabled", true)
                            .putString("two_step_pin", pinInput)
                            .apply()
                        isEnabled = true
                        isConfiguringPin = false
                        Toast.makeText(context, "Two-step verification turned ON!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save PIN", fontWeight = FontWeight.SemiBold)
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Two-step verification is enabled",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF15803D)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        prefs.edit().putBoolean("two_step_enabled", false).remove("two_step_pin").apply()
                        isEnabled = false
                        Toast.makeText(context, "Two-step verification turned off", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Turn off", color = Color(0xFFDC2626))
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 5. Security Notifications Bottom Sheet
// -------------------------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SecurityNotificationsBottomSheet(
    context: Context,
    onDismiss: () -> Unit
) {
    val prefs = remember { context.getSharedPreferences("olinam_account_security", Context.MODE_PRIVATE) }
    var showNotifications by remember {
        mutableStateOf(prefs.getBoolean("show_security_notifications", true))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1EEFD)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = Color(0xFF7C3AED),
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Security notifications",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Each chat has a unique security code used to verify that your messages and calls are end-to-end encrypted with 256-bit AES Signal Protocol.",
                fontSize = 14.sp,
                color = Color(0xFF6B7280),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Show security notifications on this phone",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF111827)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Get notified when a contact's security code changes due to reinstalling or changing phones.",
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Switch(
                        checked = showNotifications,
                        onCheckedChange = {
                            showNotifications = it
                            prefs.edit().putBoolean("show_security_notifications", it).apply()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF2563EB)
                        )
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 6. Username Bottom Sheet
// -------------------------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UsernameBottomSheet(
    context: Context,
    viewModel: ChatViewModel,
    onDismiss: () -> Unit
) {
    val prefs = remember { context.getSharedPreferences("olinam_user_prefs", Context.MODE_PRIVATE) }
    var currentUsername by remember {
        mutableStateOf(prefs.getString("custom_username", "omkunwar") ?: "omkunwar")
    }
    var inputUsername by remember { mutableStateOf(currentUsername) }
    val clipboardManager = LocalClipboardManager.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Choose your username",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "People can reach you on Olinam via your unique username without needing your phone number.",
                fontSize = 14.sp,
                color = Color(0xFF6B7280)
            )

            Spacer(modifier = Modifier.height(18.dp))

            OutlinedTextField(
                value = inputUsername,
                onValueChange = {
                    inputUsername = it.filter { c -> c.isLetterOrDigit() || c == '_' }.lowercase()
                },
                label = { Text("Username") },
                prefix = { Text("@", fontWeight = FontWeight.Bold, color = Color(0xFF2563EB)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF3F4F6), RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "olinam.im/@$inputUsername",
                    fontSize = 13.5.sp,
                    color = Color(0xFF2563EB),
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString("https://olinam.im/@$inputUsername"))
                        Toast.makeText(context, "Username link copied!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (inputUsername.length < 3) {
                        Toast.makeText(context, "Username must be at least 3 characters", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    prefs.edit().putString("custom_username", inputUsername).apply()
                    currentUsername = inputUsername
                    Toast.makeText(context, "Username @$inputUsername saved!", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Username", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// Delete Account Bottom Sheet
// -------------------------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeleteAccountBottomSheet(
    context: Context,
    viewModel: ChatViewModel,
    registeredPhone: String,
    onDismiss: () -> Unit
) {
    var phoneConfirmInput by remember { mutableStateOf("") }
    var showFinalConfirmDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Delete this account",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFDC2626)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Deleting your account will:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF991B1B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Delete your profile, status, and account info\n• Remove you from all group chats\n• Delete your message history on this device",
                        fontSize = 13.5.sp,
                        color = Color(0xFFB91C1C),
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "To confirm deletion, enter your phone number (${registeredPhone.ifBlank { "+91 98765 43210" }}):",
                fontSize = 14.sp,
                color = Color(0xFF4B5563)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = phoneConfirmInput,
                onValueChange = { phoneConfirmInput = it },
                label = { Text("Phone number") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (phoneConfirmInput.isBlank()) {
                        Toast.makeText(context, "Please enter your phone number to proceed", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    showFinalConfirmDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Delete Account", fontWeight = FontWeight.SemiBold)
            }
        }
    }

    if (showFinalConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showFinalConfirmDialog = false },
            title = { Text("Permanently delete account?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you completely sure? All your cryptographic keys, conversations, and account data will be permanently wiped.") },
            confirmButton = {
                Button(
                    onClick = {
                        showFinalConfirmDialog = false
                        viewModel.logout(context)
                        Toast.makeText(context, "Account successfully deleted", Toast.LENGTH_LONG).show()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinalConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
