package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OlinamPrimary

data class AppLanguageItem(
    val code: String,
    val nativeName: String,
    val englishName: String,
    val isDeviceDefault: Boolean = false
)

private val ALL_LANGUAGES = listOf(
    AppLanguageItem("default", "Device's language", "English", isDeviceDefault = true),
    AppLanguageItem("en", "English", "English"),
    AppLanguageItem("hi", "हिन्दी", "Hindi"),
    AppLanguageItem("bn", "বাংলা", "Bengali"),
    AppLanguageItem("mr", "मराठी", "Marathi"),
    AppLanguageItem("te", "తెలుగు", "Telugu"),
    AppLanguageItem("ta", "தமிழ்", "Tamil"),
    AppLanguageItem("gu", "ગુજરાતી", "Gujarati"),
    AppLanguageItem("ur", "اردو", "Urdu"),
    AppLanguageItem("kn", "ಕನ್ನಡ", "Kannada"),
    AppLanguageItem("ml", "മലയാളം", "Malayalam"),
    AppLanguageItem("pa", "ਪੰਜਾਬੀ", "Punjabi"),
    AppLanguageItem("es", "Español", "Spanish"),
    AppLanguageItem("fr", "Français", "French"),
    AppLanguageItem("ar", "العربية", "Arabic"),
    AppLanguageItem("pt", "Português", "Portuguese"),
    AppLanguageItem("ru", "Русский", "Russian"),
    AppLanguageItem("de", "Deutsch", "German"),
    AppLanguageItem("id", "Bahasa Indonesia", "Indonesian"),
    AppLanguageItem("ja", "日本語", "Japanese")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLanguageScreen(
    onBackClick: () -> Unit,
    onLanguageSelected: (AppLanguageItem) -> Unit
) {
    BackHandler { onBackClick() }

    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("olinam_app_prefs", Context.MODE_PRIVATE) }
    var selectedCode by remember {
        mutableStateOf(prefs.getString("app_language_code", "en") ?: "en")
    }

    var searchQuery by remember { mutableStateOf("") }

    val filteredLanguages = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            ALL_LANGUAGES
        } else {
            ALL_LANGUAGES.filter {
                it.nativeName.contains(searchQuery, ignoreCase = true) ||
                        it.englishName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val primaryBlue = OlinamPrimary

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_language_screen"),
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "App language",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF101828)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("app_language_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF101828)
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
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search language...", color = Color(0xFF98A2B3)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF667085)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = Color(0xFF667085)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryBlue,
                    unfocusedBorderColor = Color(0xFFE4E7EC)
                )
            )

            HorizontalDivider(
                color = Color(0xFFF2F4F7),
                thickness = 1.dp,
                modifier = Modifier.padding(top = 4.dp)
            )

            // Language List
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredLanguages) { langItem ->
                    val isSelected = (selectedCode == langItem.code) ||
                            (langItem.isDeviceDefault && selectedCode == "default")

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedCode = langItem.code
                                val displayName = if (langItem.isDeviceDefault) {
                                    "English (Device's language)"
                                } else {
                                    "${langItem.nativeName} (${langItem.englishName})"
                                }
                                prefs
                                    .edit()
                                    .putString("app_language_code", langItem.code)
                                    .putString("app_language_name", displayName)
                                    .apply()

                                Toast
                                    .makeText(
                                        context,
                                        "App language changed to ${langItem.nativeName}",
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()

                                onLanguageSelected(langItem)
                                onBackClick()
                            }
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                selectedCode = langItem.code
                                val displayName = if (langItem.isDeviceDefault) {
                                    "English (Device's language)"
                                } else {
                                    "${langItem.nativeName} (${langItem.englishName})"
                                }
                                prefs
                                    .edit()
                                    .putString("app_language_code", langItem.code)
                                    .putString("app_language_name", displayName)
                                    .apply()

                                onLanguageSelected(langItem)
                                onBackClick()
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = primaryBlue)
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = langItem.nativeName,
                                fontSize = 16.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) primaryBlue else Color(0xFF101828)
                            )
                            if (langItem.englishName.isNotBlank() && langItem.englishName != langItem.nativeName) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = langItem.englishName,
                                    fontSize = 13.5.sp,
                                    color = Color(0xFF667085)
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color = Color(0xFFF9FAFB),
                        thickness = 1.dp,
                        modifier = Modifier.padding(start = 64.dp)
                    )
                }
            }
        }
    }
}
