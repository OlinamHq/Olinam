package com.example.ui.screens.privacy

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OlinamPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePicturePrivacyScreen(
    privacyPrefs: PrivacyPreferences,
    onBackClick: () -> Unit
) {
    BackHandler { onBackClick() }

    val state by privacyPrefs.state.collectAsState()
    val primaryBlue = OlinamPrimary

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("profile_picture_privacy_screen"),
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile picture",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF101828)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
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
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Who can see my profile picture",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF475467),
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp)
            )

            PrivacyAudience.values().forEach { audience ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            privacyPrefs.setProfilePhotoAudience(audience)
                        }
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = state.profilePhotoAudience == audience,
                        onClick = {
                            privacyPrefs.setProfilePhotoAudience(audience)
                        },
                        colors = RadioButtonDefaults.colors(selectedColor = primaryBlue)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = audience.title,
                        fontSize = 16.sp,
                        color = Color(0xFF101828)
                    )
                }
            }
        }
    }
}
