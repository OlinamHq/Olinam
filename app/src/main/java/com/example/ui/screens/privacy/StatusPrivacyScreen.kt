package com.example.ui.screens.privacy

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
fun StatusPrivacyScreen(
    privacyPrefs: PrivacyPreferences,
    onBackClick: () -> Unit
) {
    BackHandler { onBackClick() }

    val state by privacyPrefs.state.collectAsState()
    val primaryBlue = OlinamPrimary

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("status_privacy_screen"),
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Status privacy",
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
                text = "Audience who can see my status",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF475467),
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp)
            )

            val options = listOf(
                PrivacyAudience.MY_CONTACTS to "My contacts",
                PrivacyAudience.MY_CONTACTS_EXCEPT to "My contacts except...",
                PrivacyAudience.NOBODY to "Only share with..."
            )

            options.forEach { (audience, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            privacyPrefs.setStatusAudience(audience, state.statusAllowSharing)
                        }
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = state.statusAudience == audience,
                        onClick = {
                            privacyPrefs.setStatusAudience(audience, state.statusAllowSharing)
                        },
                        colors = RadioButtonDefaults.colors(selectedColor = primaryBlue)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = label,
                        fontSize = 16.sp,
                        color = Color(0xFF101828)
                    )
                }
            }

            HorizontalDivider(
                color = Color(0xFFF2F4F7),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Allow sharing",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF101828)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Let people who can see your status reshare and forward it.",
                        fontSize = 13.5.sp,
                        color = Color(0xFF667085)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Switch(
                    checked = state.statusAllowSharing,
                    onCheckedChange = {
                        privacyPrefs.setStatusAudience(state.statusAudience, it)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = primaryBlue
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Changes to your privacy settings won't affect status updates you shared already.",
                fontSize = 13.5.sp,
                color = Color(0xFF667085),
                lineHeight = 19.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}
