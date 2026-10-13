package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserProfile
import com.example.ui.theme.OlinamPrimary

private data class ServiceItem(
    val name: String,
    val detail: String,
    val icon: ImageVector,
    val scopes: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectedServicesScreen(
    currentUser: UserProfile,
    onBackClick: () -> Unit
) {
    BackHandler { onBackClick() }
    var selectedService by remember { mutableStateOf<ServiceItem?>(null) }

    val services = listOf(
        ServiceItem("Gmail", "Email access you approve", Icons.Default.Email, "Gmail API access"),
        ServiceItem("Google Calendar", "Events and reminders", Icons.Default.CalendarToday, "Calendar API access"),
        ServiceItem("Google Maps", "Maps and location features", Icons.Default.Map, "Maps platform and location permissions"),
        ServiceItem("Google Contacts", "Contacts you choose to share", Icons.Default.Contacts, "Contacts API or Android Contacts permission"),
        ServiceItem("Google Drive", "Your selected files and documents", Icons.Default.Cloud, "Google Drive API access"),
        ServiceItem("Google Photos", "Photos you choose to share", Icons.Default.Photo, "Google Photos API access")
    )

    Scaffold(
        containerColor = Color(0xFFF6F8FC),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Connected apps", fontWeight = FontWeight.Bold, fontSize = 19.sp, color = Color(0xFF111827))
                        Text("Manage access for this Olinam account", fontSize = 12.sp, color = Color(0xFF667085))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF111827))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF6F8FC))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF102A56)),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("YOUR ACCOUNT", color = Color(0xFFBBD3FF), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                    Text(currentUser.name.ifBlank { "Olinam user" }, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(currentUser.phoneNumber.ifBlank { "Phone number not available" }, color = Color(0xFFE0EAFF), fontSize = 14.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Permissions are managed separately for this signed-in phone account.", color = Color(0xFFD6E3FF), fontSize = 12.sp, lineHeight = 18.sp)
                }
            }

            Text("AVAILABLE SERVICES", color = Color(0xFF667085), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp, modifier = Modifier.padding(top = 6.dp))

            services.forEach { service ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFEAF1FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(service.icon, contentDescription = null, tint = OlinamPrimary, modifier = Modifier.size(23.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(service.name, color = Color(0xFF101828), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(service.detail, color = Color(0xFF667085), fontSize = 12.sp, lineHeight = 16.sp)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                Box(Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF98A2B3)))
                                Text("Not connected", color = Color(0xFF667085), fontSize = 11.sp)
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = { selectedService = service },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 7.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OlinamPrimary)
                        ) {
                            Text("Set up", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF1FF)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 22.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("Privacy first", color = Color(0xFF163B78), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Olinam should request only the access you approve. Each Google service needs a secure Google sign-in/consent flow before it can show as connected. Nothing is marked connected until that setup is implemented and verified.", color = Color(0xFF365780), fontSize = 12.sp, lineHeight = 18.sp)
                }
            }
        }
    }

    selectedService?.let { service ->
        AlertDialog(
            onDismissRequest = { selectedService = null },
            title = { Text("Set up ${service.name}", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Secure connection is not configured yet. This service needs verified Google OAuth consent and the correct API access (${service.scopes}) before Olinam can connect it. Your account will remain disconnected until setup is complete."
                )
            },
            confirmButton = {
                TextButton(onClick = { selectedService = null }) { Text("Got it", color = OlinamPrimary) }
            }
        )
    }
}
