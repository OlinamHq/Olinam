package com.example.ui.screens

import android.content.Intent
import android.provider.ContactsContract
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ContactHelper
import com.example.data.DeviceContact

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewContactScreen(
    onBackClick: () -> Unit,
    onQrClick: () -> Unit,
    onContactSaved: (name: String, phoneNumber: String) -> Unit
) {
    BackHandler { onBackClick() }

    val context = LocalContext.current
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf(COUNTRIES[0]) }
    var countryMenuExpanded by remember { mutableStateOf(false) }
    var syncToPhone by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "New contact",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
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
                actions = {
                    IconButton(onClick = onQrClick) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "QR Code",
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Section 1: Names with Person Icon on the left (Matching Screenshot 6)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF6B7280),
                        modifier = Modifier
                            .padding(top = 16.dp, end = 16.dp)
                            .size(26.dp)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = firstName,
                            onValueChange = { firstName = it },
                            label = { Text("First name") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("contact_first_name_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0160E3),
                                focusedLabelColor = Color(0xFF0160E3)
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            label = { Text("Last name") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("contact_last_name_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0160E3),
                                focusedLabelColor = Color(0xFF0160E3)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Section 2: Phone with Phone Icon on the left (Matching Screenshot 6)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = Color(0xFF6B7280),
                        modifier = Modifier
                            .padding(top = 16.dp, end = 16.dp)
                            .size(24.dp)
                    )

                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Country dropdown
                        Box {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
                                color = Color.White,
                                modifier = Modifier
                                    .height(56.dp)
                                    .clickable { countryMenuExpanded = true }
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "Country",
                                        fontSize = 11.sp,
                                        color = Color(0xFF6B7280)
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${selectedCountry.code} ${selectedCountry.dialCode}",
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF111827)
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            tint = Color(0xFF6B7280),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            DropdownMenu(
                                expanded = countryMenuExpanded,
                                onDismissRequest = { countryMenuExpanded = false }
                            ) {
                                COUNTRIES.forEach { country ->
                                    DropdownMenuItem(
                                        text = { Text("${country.name} (${country.dialCode})") },
                                        onClick = {
                                            selectedCountry = country
                                            countryMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it.filter { c -> c.isDigit() } },
                            label = { Text("Phone") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("contact_phone_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0160E3),
                                focusedLabelColor = Color(0xFF0160E3)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section 3: Sync contact to phone switch (Matching Screenshot 6)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { syncToPhone = !syncToPhone }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        tint = Color(0xFF6B7280),
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(24.dp)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Sync contact to phone",
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF111827)
                        )
                        Text(
                            text = "Only contacts with a phone number can be synced",
                            fontSize = 12.5.sp,
                            color = Color(0xFF6B7280)
                        )
                    }

                    Switch(
                        checked = syncToPhone,
                        onCheckedChange = { syncToPhone = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF00A884)
                        )
                    )
                }
            }

            // Bottom Save button (Matching Screenshot 6)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, bottom = 16.dp)
            ) {
                val isFormValid = firstName.isNotBlank() && phoneNumber.isNotBlank()

                Button(
                    onClick = {
                        if (isFormValid) {
                            val fullName = (firstName.trim() + " " + lastName.trim()).trim()
                            val fullPhone = "${selectedCountry.dialCode} ${phoneNumber.trim()}"

                            val newContact = DeviceContact(
                                id = java.util.UUID.randomUUID().toString(),
                                name = fullName,
                                phoneNumber = fullPhone,
                                status = "Available on Olinam",
                                hasOlinam = true
                            )
                            ContactHelper.saveRealContact(context, newContact)

                            if (syncToPhone) {
                                try {
                                    val insertIntent = Intent(Intent.ACTION_INSERT).apply {
                                        type = ContactsContract.Contacts.CONTENT_TYPE
                                        putExtra(ContactsContract.Intents.Insert.NAME, fullName)
                                        putExtra(ContactsContract.Intents.Insert.PHONE, fullPhone)
                                    }
                                    context.startActivity(insertIntent)
                                } catch (_: Exception) {}
                            }

                            onContactSaved(fullName, fullPhone)
                        }
                    },
                    enabled = isFormValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_contact_button"),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFormValid) Color(0xFF00A884) else Color(0xFFE5E7EB),
                        contentColor = if (isFormValid) Color.White else Color(0xFF9CA3AF),
                        disabledContainerColor = Color(0xFFE5E7EB),
                        disabledContentColor = Color(0xFF9CA3AF)
                    )
                ) {
                    Text(
                        text = "Save",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
