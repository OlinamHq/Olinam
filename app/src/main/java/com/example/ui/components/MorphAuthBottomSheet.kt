package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.MorphRepository
import com.example.model.MorphAccount
import com.example.model.UserProfile
import kotlinx.coroutines.launch

private val MorphBlue = Color(0xFF0160E3)
private val MorphPurple = Color(0xFF6D28D9)
private val MorphGreen = Color(0xFF16A34A)

/**
 * Consumer-grade OAuth-style Account Connection & Permission Sheet for Omkun Morph.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MorphAuthBottomSheet(
    repository: MorphRepository,
    currentUser: UserProfile? = null,
    onDismiss: () -> Unit,
    onAuthenticated: (MorphAccount) -> Unit = {},
    onDisconnected: () -> Unit = {},
    onOpenMorphNotes: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val connectedAccount by repository.connectedAccountFlow.collectAsState()
    val notes by repository.notesFlow.collectAsState()
    val syncStatus by repository.syncStatus.collectAsState()
    val isSyncing by repository.isSyncing.collectAsState()

    var isSwitchingAccount by remember { mutableStateOf(false) }
    var isSignUpMode by remember { mutableStateOf(false) }

    var emailInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var authError by remember { mutableStateOf<String?>(null) }
    var isAuthenticating by remember { mutableStateOf(false) }

    var allowRead by remember { mutableStateOf(true) }
    var allowWrite by remember { mutableStateOf(true) }
    var allowDelete by remember { mutableStateOf(true) }

    val selectedScopes = remember(allowRead, allowWrite, allowDelete) {
        buildList {
            if (allowRead) {
                add(MorphAccount.SCOPE_READ_DOCS)
                add(MorphAccount.SCOPE_READ_DATABASES)
                add(MorphAccount.SCOPE_READ_TASKS)
                add(MorphAccount.SCOPE_READ)
            }
            if (allowWrite) {
                add(MorphAccount.SCOPE_WRITE_DOCS)
                add(MorphAccount.SCOPE_WRITE_TASKS)
                add(MorphAccount.SCOPE_WRITE)
            }
            if (allowDelete) {
                add(MorphAccount.SCOPE_DELETE)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("morph_auth_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding()
        ) {
            val activeAccount = connectedAccount
            if (activeAccount != null && !isSwitchingAccount) {
                // =============================================================
                // CONNECTED ACCOUNT VIEW (Clean Google-Style Account Sheet)
                // =============================================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFEFF6FF),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_morph_notes),
                                    contentDescription = "Omkun Morph",
                                    tint = MorphBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Omkun Morph",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Surface(
                                shape = RoundedCornerShape(99.dp),
                                color = Color(0xFFDCFCE7)
                            ) {
                                Text(
                                    text = "Connected",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF15803D),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF64748B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(MorphBlue, MorphPurple))),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!activeAccount.avatarUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = activeAccount.avatarUrl,
                                        contentDescription = activeAccount.displayName,
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(
                                        text = activeAccount.displayName
                                            .ifBlank { activeAccount.email }
                                            .take(1)
                                            .uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 19.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = activeAccount.displayName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Verified Account",
                                        tint = MorphBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = activeAccount.email,
                                    fontSize = 13.sp,
                                    color = Color(0xFF475569),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                val providerLabel = when (activeAccount.authProvider) {
                                    "GOOGLE" -> "Google Account"
                                    "OLINAM_SSO" -> "Olinam Account"
                                    else -> "Omkun Account"
                                }
                                val activeCount = notes.count { !it.isArchived }
                                Text(
                                    text = "$providerLabel • $activeCount notes synced",
                                    fontSize = 12.sp,
                                    color = MorphBlue,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFFE2E8F0))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(MorphGreen)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = syncStatus,
                                    fontSize = 12.5.sp,
                                    color = Color(0xFF334155),
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            if (isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MorphBlue
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = MorphGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Olinam & Oj AI access",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        PermissionSummaryRow("View your Morph notes, checklists, and tasks")
                        if (activeAccount.canWriteDocs()) {
                            PermissionSummaryRow("Create and update notes & checklists")
                        }
                        if (activeAccount.canDelete()) {
                            PermissionSummaryRow("Remove notes when you ask")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            repository.syncWithServer { _, msg ->
                                try {
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                } catch (_: Exception) {}
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MorphBlue),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("morph_sync_now_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sync Now", fontWeight = FontWeight.SemiBold)
                    }

                    if (onOpenMorphNotes != null) {
                        OutlinedButton(
                            onClick = {
                                onDismiss()
                                onOpenMorphNotes()
                            },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Description,
                                contentDescription = null,
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open Morph", color = Color(0xFF0F172A), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = {
                            authError = null
                            isSwitchingAccount = true
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("morph_switch_account_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = MorphBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Switch Account", color = MorphBlue, fontWeight = FontWeight.SemiBold)
                    }

                    TextButton(
                        onClick = {
                            repository.disconnectAccount()
                            onDisconnected()
                            Toast.makeText(context, "Omkun Morph disconnected", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("morph_disconnect_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Disconnect", color = Color(0xFFDC2626), fontWeight = FontWeight.SemiBold)
                    }
                }
            } else {
                // =============================================================
                // CONSUMER-FRIENDLY OAUTH CONSENT & SIGN-IN SHEET
                // =============================================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = {
                            if (isSwitchingAccount && connectedAccount != null) {
                                isSwitchingAccount = false
                            } else {
                                onDismiss()
                            }
                        },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF64748B)
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Brush.linearGradient(listOf(MorphBlue, MorphPurple))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Olinam",
                                tint = Color.White,
                                modifier = Modifier.size(25.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFEFF6FF),
                            border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                            modifier = Modifier.size(50.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_morph_notes),
                                    contentDescription = "Omkun Morph",
                                    tint = MorphBlue,
                                    modifier = Modifier.size(25.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (isSwitchingAccount) "Switch Morph Account" else "Connect Omkun Morph",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sign in to sync your notes, checklists, and tasks with Olinam & Oj AI.",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Optional 1-Tap Olinam Account card if currentUser has an identifier
                val canOneTapOlinam = currentUser != null &&
                    (currentUser.email.isNotBlank() || currentUser.phoneNumber.isNotBlank() ||
                        (currentUser.id.isNotBlank() && currentUser.id != "me"))
                if (canOneTapOlinam && currentUser != null) {
                    val subLabel = currentUser.email.ifBlank { currentUser.phoneNumber }
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(enabled = !isAuthenticating) {
                                authError = null
                                isAuthenticating = true
                                scope.launch {
                                    val res = repository.authorizeWithOlinamAccount(
                                        user = currentUser,
                                        scopes = selectedScopes
                                    )
                                    isAuthenticating = false
                                    res.onSuccess { account ->
                                        isSwitchingAccount = false
                                        Toast.makeText(
                                            context,
                                            "Connected as ${account.displayName}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        onAuthenticated(account)
                                        onDismiss()
                                    }.onFailure { err ->
                                        authError = err.message ?: "Unable to connect account."
                                    }
                                }
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MorphBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentUser.name.ifBlank { "O" }.take(1).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Continue as ${currentUser.name.ifBlank { "Olinam User" }}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F172A),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (subLabel.isNotBlank()) {
                                    Text(
                                        text = subLabel,
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(99.dp),
                                color = Color(0xFFEFF6FF)
                            ) {
                                Text(
                                    text = "1-Tap",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MorphBlue,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
                        Text(
                            text = "  or sign in with Omkun Account  ",
                            fontSize = 11.5.sp,
                            color = Color(0xFF94A3B8)
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                if (authError != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFEF2F2),
                        border = BorderStroke(1.dp, Color(0xFFFECACA)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = authError!!,
                            color = Color(0xFFB91C1C),
                            fontSize = 12.5.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Segmented Toggle: Sign In / Create Account
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(9.dp),
                            color = if (!isSignUpMode) Color.White else Color.Transparent,
                            shadowElevation = if (!isSignUpMode) 1.dp else 0.dp,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(9.dp))
                                .clickable {
                                    isSignUpMode = false
                                    authError = null
                                }
                        ) {
                            Text(
                                text = "Sign In",
                                fontWeight = if (!isSignUpMode) FontWeight.Bold else FontWeight.Medium,
                                color = if (!isSignUpMode) MorphBlue else Color(0xFF64748B),
                                fontSize = 13.5.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 9.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(9.dp),
                            color = if (isSignUpMode) Color.White else Color.Transparent,
                            shadowElevation = if (isSignUpMode) 1.dp else 0.dp,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(9.dp))
                                .clickable {
                                    isSignUpMode = true
                                    authError = null
                                }
                        ) {
                            Text(
                                text = "Create Account",
                                fontWeight = if (isSignUpMode) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSignUpMode) MorphBlue else Color(0xFF64748B),
                                fontSize = 13.5.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 9.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (isSignUpMode) {
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Full Name") },
                            placeholder = { Text("Enter your name") },
                            leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MorphBlue) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("morph_name_input")
                        )
                    }

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = {
                            emailInput = it
                            authError = null
                        },
                        label = { Text("Email address") },
                        placeholder = { Text("you@example.com") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MorphBlue) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("morph_email_input")
                    )

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = {
                            passwordInput = it
                            authError = null
                        },
                        label = { Text("Password") },
                        placeholder = { Text("Enter your password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MorphBlue) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("morph_password_input")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Clean Google-OAuth-Style Permissions Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = MorphBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Olinam & Oj AI will be able to:",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        PermissionToggleRow(
                            title = "View your Morph notes, checklists, and tasks",
                            checked = allowRead,
                            enabled = false,
                            onCheckedChange = {}
                        )
                        PermissionToggleRow(
                            title = "Create and update notes & tasks",
                            checked = allowWrite,
                            enabled = true,
                            onCheckedChange = { allowWrite = it }
                        )
                        PermissionToggleRow(
                            title = "Delete items when you ask",
                            checked = allowDelete,
                            enabled = true,
                            onCheckedChange = { allowDelete = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        authError = null
                        isAuthenticating = true
                        scope.launch {
                            val res = if (isSignUpMode) {
                                repository.signUpWithOmkunCredentials(
                                    email = emailInput,
                                    password = passwordInput,
                                    displayName = nameInput,
                                    scopes = selectedScopes
                                )
                            } else {
                                repository.signInWithOmkunCredentials(
                                    email = emailInput,
                                    password = passwordInput,
                                    displayName = nameInput,
                                    scopes = selectedScopes
                                )
                            }
                            isAuthenticating = false
                            res.onSuccess { account ->
                                isSwitchingAccount = false
                                Toast.makeText(
                                    context,
                                    if (isSignUpMode) "Connected as ${account.email}"
                                    else "Signed in as ${account.email}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onAuthenticated(account)
                                onDismiss()
                            }.onFailure { err ->
                                authError = err.message ?: "Could not sign in. Please verify your email and password."
                            }
                        }
                    },
                    enabled = !isAuthenticating,
                    colors = ButtonDefaults.buttonColors(containerColor = MorphBlue),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("morph_authorize_submit_button")
                ) {
                    if (isAuthenticating) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    Text(
                        text = if (isSignUpMode) "Create Account & Connect" else "Allow & Connect",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "You can manage permissions or disconnect anytime in Olinam.",
                    fontSize = 11.5.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun PermissionSummaryRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = MorphGreen,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            color = Color(0xFF334155)
        )
    }
}

@Composable
private fun PermissionToggleRow(
    title: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = if (enabled) onCheckedChange else null,
            colors = CheckboxDefaults.colors(
                checkedColor = MorphBlue,
                disabledCheckedColor = MorphBlue.copy(alpha = 0.7f)
            ),
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 13.sp,
            color = Color(0xFF334155),
            fontWeight = FontWeight.Medium
        )
    }
}
