package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ChatViewModel

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

private val WhatsAppGreen = Color(0xFF00A884)
private val WhatsAppDarkGreen = Color(0xFF008069)

@Composable
fun LoginScreen(
    viewModel: ChatViewModel
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Phone, 1: Google, 2: Email

    // Form inputs
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf(COUNTRIES[0]) }
    var countryMenuExpanded by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

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
        Spacer(modifier = Modifier.height(28.dp))

        // WhatsApp-style brand logo & title
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(WhatsAppGreen.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.ChatBubble,
                contentDescription = "Olinam",
                tint = WhatsAppGreen,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Welcome to Olinam",
            fontSize = 23.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Simple. Secure. Real-time messaging with end-to-end encryption.",
            fontSize = 13.5.sp,
            color = Color(0xFF6B7280),
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(22.dp))

        // Tab Row: Phone vs Google vs Email (supporting mobile number with all options)
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = WhatsAppGreen,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = WhatsAppGreen
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = {
                    selectedTab = 0
                    errorMessage = null
                },
                text = {
                    Text(
                        text = "Phone",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.5.sp,
                        color = if (selectedTab == 0) WhatsAppGreen else Color(0xFF6B7280)
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = {
                    selectedTab = 1
                    errorMessage = null
                },
                text = {
                    Text(
                        text = "Google",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.5.sp,
                        color = if (selectedTab == 1) WhatsAppGreen else Color(0xFF6B7280)
                    )
                }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = {
                    selectedTab = 2
                    errorMessage = null
                },
                text = {
                    Text(
                        text = "Email",
                        fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.5.sp,
                        color = if (selectedTab == 2) WhatsAppGreen else Color(0xFF6B7280)
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Common Name Field
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Your Name") },
            placeholder = { Text("Enter your full name") },
            leadingIcon = {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF6B7280))
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("login_name_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = WhatsAppGreen,
                focusedLabelColor = WhatsAppGreen,
                cursorColor = WhatsAppGreen
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        when (selectedTab) {
            0 -> {
                // Phone Number Tab (Pure WhatsApp style)
                PhoneInputSection(
                    selectedCountry = selectedCountry,
                    countryMenuExpanded = countryMenuExpanded,
                    onCountryExpandChange = { countryMenuExpanded = it },
                    onCountrySelect = { selectedCountry = it },
                    phoneNumber = phoneNumber,
                    onPhoneNumberChange = { phoneNumber = it }
                )
            }
            1 -> {
                // Google Tab (Google Sign-In with Mobile Number)
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = {
                            // Quick fill for Google identification if name is blank
                            if (name.isBlank()) {
                                name = "Google Account"
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("google_signin_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFF9FAFB))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4285F4)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "G",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Signed in with Google",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1F2937)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Enter your mobile number to connect with your contacts:",
                        fontSize = 12.5.sp,
                        color = Color(0xFF4B5563),
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PhoneInputSection(
                        selectedCountry = selectedCountry,
                        countryMenuExpanded = countryMenuExpanded,
                        onCountryExpandChange = { countryMenuExpanded = it },
                        onCountrySelect = { selectedCountry = it },
                        phoneNumber = phoneNumber,
                        onPhoneNumberChange = { phoneNumber = it }
                    )
                }
            }
            2 -> {
                // Email Tab (Email & Password with Mobile Number)
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email address") },
                        placeholder = { Text("your.email@example.com") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF6B7280))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_email_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WhatsAppGreen,
                            focusedLabelColor = WhatsAppGreen,
                            cursorColor = WhatsAppGreen
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF6B7280))
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WhatsAppGreen,
                            focusedLabelColor = WhatsAppGreen,
                            cursorColor = WhatsAppGreen
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Enter your mobile number to link your chats:",
                        fontSize = 12.5.sp,
                        color = Color(0xFF4B5563),
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PhoneInputSection(
                        selectedCountry = selectedCountry,
                        countryMenuExpanded = countryMenuExpanded,
                        onCountryExpandChange = { countryMenuExpanded = it },
                        onCountrySelect = { selectedCountry = it },
                        phoneNumber = phoneNumber,
                        onPhoneNumberChange = { phoneNumber = it }
                    )
                }
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = errorMessage ?: "",
                color = Color(0xFFDC2626),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(26.dp))

        // WhatsApp-style primary "Agree & Continue" button
        Button(
            onClick = {
                val trimmedName = name.trim()
                if (trimmedName.isBlank()) {
                    errorMessage = "Please enter your name"
                    return@Button
                }

                val trimmedPhone = phoneNumber.trim()
                if (trimmedPhone.length < 5) {
                    errorMessage = "Please enter your mobile phone number"
                    return@Button
                }

                val fullPhone = "${selectedCountry.dialCode} $trimmedPhone"

                when (selectedTab) {
                    0 -> {
                        viewModel.loginWithPhone(trimmedName, fullPhone, context)
                    }
                    1 -> {
                        val googleEmail = if (email.contains("@")) email.trim() else "google.user@olinam.com"
                        viewModel.loginWithGoogle(trimmedName, googleEmail, fullPhone, context)
                    }
                    2 -> {
                        if (!email.contains("@") || password.length < 4) {
                            errorMessage = "Please enter a valid email and password (min 4 characters)"
                            return@Button
                        }
                        viewModel.loginWithEmail(trimmedName, email.trim(), fullPhone, context)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("login_submit_button"),
            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text(
                text = "Agree & Continue",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Tap 'Agree & Continue' to accept Olinam's Terms of Service and Privacy Policy.",
            fontSize = 11.5.sp,
            color = Color(0xFF9CA3AF),
            textAlign = TextAlign.Center,
            lineHeight = 16.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )
    }
}

@Composable
private fun PhoneInputSection(
    selectedCountry: CountryItem,
    countryMenuExpanded: Boolean,
    onCountryExpandChange: (Boolean) -> Unit,
    onCountrySelect: (CountryItem) -> Unit,
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Country Code Picker
        Box {
            Surface(
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
                color = Color(0xFFF9FAFB),
                modifier = Modifier
                    .height(56.dp)
                    .clickable { onCountryExpandChange(true) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${selectedCountry.code} ${selectedCountry.dialCode}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF111827)
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Country",
                        tint = Color(0xFF6B7280)
                    )
                }
            }

            DropdownMenu(
                expanded = countryMenuExpanded,
                onDismissRequest = { onCountryExpandChange(false) }
            ) {
                COUNTRIES.forEach { country ->
                    DropdownMenuItem(
                        text = { Text("${country.name} (${country.dialCode})") },
                        onClick = {
                            onCountrySelect(country)
                            onCountryExpandChange(false)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Phone Number field
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { onPhoneNumberChange(it.filter { char -> char.isDigit() }) },
            label = { Text("Phone number") },
            placeholder = { Text("Mobile number") },
            leadingIcon = {
                Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF6B7280))
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("login_phone_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = WhatsAppGreen,
                focusedLabelColor = WhatsAppGreen,
                cursorColor = WhatsAppGreen
            )
        )
    }
}
