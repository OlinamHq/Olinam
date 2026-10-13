package com.example.ui.screens

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import androidx.core.content.ContextCompat
import com.example.model.UserProfile
import com.example.ui.theme.OlinamPrimary

private val android.content.Context.connectedServiceDataStore by preferencesDataStore(name = "connected_service_permissions")

private fun accountPermissionPrefix(user: UserProfile): String {
    val identity = when {
        user.id.isNotBlank() && user.id != "me" -> user.id
        user.phoneNumber.isNotBlank() -> user.phoneNumber
        else -> "unknown-account"
    }
    val digest = MessageDigest.getInstance("SHA-256").digest(identity.toByteArray())
    return digest.joinToString("") { byte -> "%02x".format(byte) }.take(24)
}

private fun servicePreferenceKey(prefix: String, service: String) =
    booleanPreferencesKey(prefix + "_" + service.lowercase().replace(Regex("[^a-z0-9]"), ""))

private data class ServiceItem(
    val name: String,
    val detail: String,
    val icon: ImageVector,
    val action: String,
    val permissionKey: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectedServicesScreen(
    currentUser: UserProfile,
    onBackClick: () -> Unit
) {
    BackHandler { onBackClick() }
    val context = LocalContext.current
    var showGoogleApiInfo by remember { mutableStateOf(false) }
    val accountPrefix = remember(currentUser.id, currentUser.phoneNumber) { accountPermissionPrefix(currentUser) }
    val coroutineScope = rememberCoroutineScope()

    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            try {
                context.startActivity(Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI))
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(context, "No contacts app is available on this device.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Contacts access was not granted.", Toast.LENGTH_SHORT).show()
        }
    }

    val services = listOf(
        ServiceItem("Gmail", "Open email using your installed mail app", Icons.Default.Email, "Open", "gmail"),
        ServiceItem("Google Calendar", "Create an event in your calendar app", Icons.Default.CalendarToday, "Open", "calendar"),
        ServiceItem("Google Maps", "Open Maps for places and directions", Icons.Default.Map, "Open", "maps"),
        ServiceItem("Google Contacts", "Choose a contact after Android permission", Icons.Default.Contacts, "Allow", "contacts"),
        ServiceItem("Google Drive", "Open your Drive files", Icons.Default.Cloud, "Open", "drive"),
        ServiceItem("Google Photos", "Choose photos using Android's photo picker", Icons.Default.Photo, "Choose", "photos")
    )

    val servicePermissions by context.connectedServiceDataStore.data.map { preferences ->
        services.associate { service ->
            service.permissionKey to (preferences[servicePreferenceKey(accountPrefix, service.permissionKey)] ?: false)
        }
    }.collectAsState(initial = emptyMap())

    fun openService(serviceName: String) {
        val intent = when (serviceName) {
            "Gmail" -> {
                val gmail = context.packageManager.getLaunchIntentForPackage("com.google.android.gm")
                gmail ?: Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
            }
            "Google Calendar" -> Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI)
            "Google Maps" -> {
                val maps = context.packageManager.getLaunchIntentForPackage("com.google.android.apps.maps")
                maps ?: Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=restaurants"))
            }
            "Google Drive" -> {
                val drive = context.packageManager.getLaunchIntentForPackage("com.google.android.apps.docs")
                drive ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://drive.google.com/drive/my-drive"))
            }
            "Google Photos" -> Intent(Intent.ACTION_GET_CONTENT).setType("image/*")
            else -> return
        }
        try {
            context.startActivity(if (serviceName == "Google Photos") Intent.createChooser(intent, "Choose a photo") else intent)
        } catch (_: ActivityNotFoundException) {
            try {
                val fallback = when (serviceName) {
                    "Gmail" -> Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
                    "Google Calendar" -> Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI)
                    "Google Maps" -> Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=restaurants"))
                    "Google Drive" -> Intent(Intent.ACTION_VIEW, Uri.parse("https://drive.google.com/drive/my-drive"))
                    else -> Intent(Intent.ACTION_GET_CONTENT).setType("image/*")
                }
                context.startActivity(Intent.createChooser(fallback, "Open with"))
            } catch (_: Exception) {
                Toast.makeText(context, "This service is not available on this device.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFFF6F8FC),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Connected apps", fontWeight = FontWeight.Bold, fontSize = 19.sp, color = Color(0xFF111827))
                        Text("Manage services for this Olinam account", fontSize = 12.sp, color = Color(0xFF667085))
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
                    Text("OLINAM ACCOUNT", color = Color(0xFFBBD3FF), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                    Text(currentUser.name.ifBlank { "Olinam user" }, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(currentUser.phoneNumber.ifBlank { "Phone number not available" }, color = Color(0xFFE0EAFF), fontSize = 14.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Manage each service preference for this Olinam account. Changes are stored separately for this account on this device.", color = Color(0xFFD6E3FF), fontSize = 12.sp, lineHeight = 18.sp)
                }
            }

            Text("YOUR SERVICES", color = Color(0xFF667085), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp, modifier = Modifier.padding(top = 6.dp))

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
                                Box(Modifier.size(6.dp).clip(CircleShape).background(if (servicePermissions[service.permissionKey] == true) Color(0xFF12B76A) else Color(0xFF98A2B3)))
                                Text(if (servicePermissions[service.permissionKey] == true) "Olinam preference: allowed" else "Olinam preference: off", color = Color(0xFF667085), fontSize = 11.sp)
                            }
                            Text("This setting is not a Google OAuth grant.", color = Color(0xFF98A2B3), fontSize = 10.sp)
                        }
                        Spacer(Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Switch(
                            checked = servicePermissions[service.permissionKey] == true,
                            onCheckedChange = { allowed ->
                                coroutineScope.launch {
                                    context.connectedServiceDataStore.edit { preferences ->
                                        preferences[servicePreferenceKey(accountPrefix, service.permissionKey)] = allowed
                                    }
                                }
                            }
                        )
                        Text("Allow", color = Color(0xFF667085), fontSize = 10.sp)
                        Button(
                            onClick = {
                                when (service.name) {
                                    "Google Contacts" -> {
                                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                                            try {
                                                context.startActivity(Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI))
                                            } catch (_: ActivityNotFoundException) {
                                                Toast.makeText(context, "No contacts app is available.", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                                        }
                                    }
                                    else -> openService(service.name)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 7.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OlinamPrimary)
                        ) {
                            Text(service.action, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
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
                    Text("Secure account connection", color = Color(0xFF163B78), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("The Allow switches save a local preference separately for this Olinam account on this device; they are not Google permissions and do not grant Olinam access to Google account data. The buttons only open the relevant app or Android picker. True Gmail, Calendar, Contacts, Drive and Photos API access still needs Google OAuth consent, enabled APIs and secure token handling. Maps API access needs a configured Google Maps API key. Nothing is shown as connected until that is implemented and verified.", color = Color(0xFF365780), fontSize = 12.sp, lineHeight = 18.sp)
                    TextButton(onClick = { showGoogleApiInfo = true }) {
                        Text("What still needs setup?", color = OlinamPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    if (showGoogleApiInfo) {
        AlertDialog(
            onDismissRequest = { showGoogleApiInfo = false },
            title = { Text("Google integration setup", fontWeight = FontWeight.Bold) },
            text = {
                Text("The switches on this screen are local preferences only. Each Olinam account must separately complete Google's OAuth consent flow before Olinam can access any Google data. The app also needs OAuth client configuration, the required Google APIs enabled, and a secure backend to use tokens. Google Maps additionally needs an API key restricted to Olinam's Android package and signing certificate. These credentials are not present in the repository, so this screen does not pretend the accounts are already connected.")
            },
            confirmButton = {
                TextButton(onClick = { showGoogleApiInfo = false }) { Text("Understood", color = OlinamPrimary) }
            }
        )
    }
}
