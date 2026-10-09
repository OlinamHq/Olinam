package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ChatViewModel
import com.example.ui.theme.OlinamPrimary
import com.example.ui.theme.OlinamPrimaryContainer

data class CountryItem(val code: String, val dialCode: String, val name: String)

val COUNTRIES = listOf(
    CountryItem("IN", "+91", "India"),
    CountryItem("US", "+1", "United States"),
    CountryItem("GB", "+44", "United Kingdom"),
    CountryItem("AE", "+971", "United Arab Emirates"),
    CountryItem("CA", "+1", "Canada"),
    CountryItem("AU", "+61", "Australia"),
    CountryItem("SG", "+65", "Singapore"),
    CountryItem("DE", "+49", "Germany"),
    CountryItem("FR", "+33", "France"),
    CountryItem("JP", "+81", "Japan")
)

@Composable
fun LoginScreen(
    viewModel: ChatViewModel
) {
    val context = LocalContext.current
    val generatedOtp by viewModel.generatedOtp.collectAsState()
    val secondsRemaining by viewModel.otpSecondsRemaining.collectAsState()

    // 0: Enter Phone, 1: Verify OTP, 2: Profile Setup
    var step by remember { mutableIntStateOf(0) }

    var selectedCountry by remember { mutableStateOf(COUNTRIES[0]) }
    var countryMenuExpanded by remember { mutableStateOf(false) }
    var phoneNumber by remember { mutableStateOf(viewModel.getSavedPhone(context)) }
    var enteredOtp by remember { mutableStateOf("") }

    var userName by remember { mutableStateOf(viewModel.getSavedName(context)) }
    var userHandle by remember { mutableStateOf(viewModel.getSavedHandle(context)) }
    var userStatus by remember { mutableStateOf("Hey there! I am using Olinam.") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val phoneVerified by viewModel.phoneVerified.collectAsState()
    val authError by viewModel.authError.collectAsState()

    LaunchedEffect(phoneVerified) {
        if (phoneVerified && step == 1) {
            val fullPhone = "${selectedCountry.dialCode}$phoneNumber"
            viewModel.checkExistingUserAndLogin(fullPhone, context) { hasProfile, _ ->
                if (!hasProfile) {
                    step = 2
                }
            }
            errorMessage = null
        }
    }

    LaunchedEffect(authError) {
        if (authError != null && step == 1) {
            errorMessage = authError
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .testTag("login_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // WhatsApp-style Olinam Logo & Branding in Royal Blue
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(OlinamPrimaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.ChatBubble,
                contentDescription = "Olinam",
                tint = OlinamPrimary,
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Olinam",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A),
            letterSpacing = (-0.5).sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = when (step) {
                0 -> "Enter your phone number"
                1 -> "Verifying your number"
                else -> "Profile info"
            },
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = OlinamPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = when (step) {
                0 -> "Olinam will send an SMS to verify your phone number. Carrier SMS charges may apply."
                1 -> "Waiting to automatically detect SMS sent to ${selectedCountry.dialCode} $phoneNumber"
                else -> "Please provide your name and an optional profile handle."
            },
            fontSize = 13.5.sp,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // STEP 0: Phone Number Entry
        if (step == 0) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Country Selector Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { countryMenuExpanded = true }
                            .testTag("country_selector"),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${selectedCountry.name} (${selectedCountry.dialCode})",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF0F172A)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select country",
                                tint = Color(0xFF64748B)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = countryMenuExpanded,
                        onDismissRequest = { countryMenuExpanded = false }
                    ) {
                        COUNTRIES.forEach { country ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "${country.name} (${country.dialCode})",
                                        fontWeight = if (country.code == selectedCountry.code) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    selectedCountry = country
                                    countryMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Phone Number Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier
                            .width(78.dp)
                            .height(54.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = selectedCountry.dialCode,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = {
                            if (it.length <= 12) {
                                phoneNumber = it.filter { char -> char.isDigit() }
                                errorMessage = null
                            }
                        },
                        placeholder = { Text("Phone number", color = Color(0xFF94A3B8)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                if (phoneNumber.length < 8) {
                                    errorMessage = "Please enter a valid phone number"
                                } else {
                                    errorMessage = null
                                    val fullPhone = "${selectedCountry.dialCode}$phoneNumber"
                                    viewModel.sendOtp(fullPhone, context)
                                    step = 1
                                }
                            }
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OlinamPrimary,
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .testTag("phone_number_input")
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFEF4444),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // NEXT Button (Royal Blue)
                Button(
                    onClick = {
                        if (phoneNumber.length < 8) {
                            errorMessage = "Please enter a valid phone number"
                        } else {
                            errorMessage = null
                            val fullPhone = "${selectedCountry.dialCode}$phoneNumber"
                            viewModel.sendOtp(fullPhone, context)
                            step = 1
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OlinamPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("send_otp_button")
                ) {
                    Text(
                        text = "Next",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // STEP 1: OTP Verification
        if (step == 1) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${selectedCountry.dialCode} $phoneNumber",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Wrong number?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OlinamPrimary,
                        modifier = Modifier
                            .clickable {
                                step = 0
                                errorMessage = null
                            }
                            .testTag("edit_number_button")
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 6-digit OTP Input Boxes
                BasicTextField(
                    value = enteredOtp,
                    onValueChange = {
                        if (it.length <= 6) {
                            enteredOtp = it.filter { char -> char.isDigit() }
                            errorMessage = null
                            if (enteredOtp.length == 6) {
                                viewModel.verifyOtp(enteredOtp) { success ->
                                    if (success) {
                                        val fullPhone = "${selectedCountry.dialCode}$phoneNumber"
                                        viewModel.checkExistingUserAndLogin(fullPhone, context) { hasProfile, existingName ->
                                            if (!hasProfile) {
                                                if (existingName.isNotBlank()) userName = existingName
                                                step = 2
                                            }
                                        }
                                    } else {
                                        errorMessage = "Invalid verification code. Please check and retry."
                                    }
                                }
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    cursorBrush = SolidColor(OlinamPrimary),
                    modifier = Modifier.testTag("otp_input_field"),
                    decorationBox = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (i in 0 until 6) {
                                val char = enteredOtp.getOrNull(i)?.toString() ?: ""
                                val isCurrent = enteredOtp.length == i
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(54.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF8FAFC),
                                    border = BorderStroke(
                                        width = if (isCurrent) 2.dp else 1.dp,
                                        color = if (isCurrent) OlinamPrimary else Color(0xFFE2E8F0)
                                    )
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = char,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                    }
                                }
                            }
                        }
                    }
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFEF4444),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Resend Timer or Button
                if (secondsRemaining > 0) {
                    Text(
                        text = "Resend SMS in 00:${String.format(java.util.Locale.US, "%02d", secondsRemaining)}",
                        fontSize = 14.sp,
                        color = Color(0xFF64748B)
                    )
                } else {
                    TextButton(
                        onClick = {
                            val fullPhone = "${selectedCountry.dialCode}$phoneNumber"
                            viewModel.sendOtp(fullPhone, context)
                            errorMessage = null
                        },
                        modifier = Modifier.testTag("resend_otp_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Resend",
                            tint = OlinamPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Resend SMS",
                            color = OlinamPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // VERIFY Button (Royal Blue)
                Button(
                    onClick = {
                        val code = enteredOtp.trim()
                        if (code.length < 6) {
                            errorMessage = "Please enter the 6-digit verification code"
                        } else {
                            viewModel.verifyOtp(code) { success ->
                                if (success) {
                                    val fullPhone = "${selectedCountry.dialCode}$phoneNumber"
                                    viewModel.checkExistingUserAndLogin(fullPhone, context) { hasProfile, existingName ->
                                        if (!hasProfile) {
                                            if (existingName.isNotBlank()) userName = existingName
                                            step = 2
                                        }
                                    }
                                } else {
                                    errorMessage = "Invalid verification code. Please check and retry."
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OlinamPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("verify_otp_button")
                ) {
                    Text(
                        text = "Verify OTP",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // STEP 2: WhatsApp-style Profile Setup
        if (step == 2) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Avatar Picker
                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(OlinamPrimaryContainer)
                            .border(2.dp, OlinamPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (userName.isNotBlank()) {
                            Text(
                                text = userName.take(1).uppercase(),
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = OlinamPrimary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = OlinamPrimary,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(OlinamPrimary)
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Change photo",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                OutlinedTextField(
                    value = userName,
                    onValueChange = {
                        userName = it
                        errorMessage = null
                    },
                    label = { Text("Your Name") },
                    placeholder = { Text("e.g. Ankit") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OlinamPrimary,
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_name_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = userHandle,
                    onValueChange = { userHandle = it },
                    label = { Text("Username Handle (optional)") },
                    placeholder = { Text("@username") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OlinamPrimary,
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_handle_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = userStatus,
                    onValueChange = { userStatus = it },
                    label = { Text("About Status") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OlinamPrimary,
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_status_input")
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFEF4444),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // FINISH & START MESSAGING (Royal Blue)
                Button(
                    onClick = {
                        if (userName.isBlank()) {
                            errorMessage = "Please enter your name"
                        } else {
                            val fullPhone = "${selectedCountry.dialCode}$phoneNumber"
                            viewModel.completeProfileSetup(
                                name = userName.trim(),
                                username = userHandle.trim(),
                                phone = fullPhone,
                                avatarUrl = null,
                                context = context
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OlinamPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("finish_profile_button")
                ) {
                    Text(
                        text = "Finish & Start Messaging",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Security Footnote
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Encrypted",
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "End-to-end encrypted • Your privacy is protected",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}
