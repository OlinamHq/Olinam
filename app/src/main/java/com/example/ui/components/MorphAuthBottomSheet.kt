package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
private val MorphPurple = Color(0xFF7C3AED)
private val MorphGreen = Color(0xFF16A34A)

/**
 * Production OAuth-style Account Login & Permission Consent Bottom Sheet for Omkun Morph.
 * Never exposes internal API URLs, keys, or raw tokens to the user.
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
            if (allowRead) add(MorphAccount.SCOPE_READ)
            if (allowWrite) add(MorphAccount.SCOPE_WRITE)
            if (allowDelete) add(MorphAccount.SCOPE_DELETE)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        modifier = Modifier.testTag("morph_auth_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                .padding(bottom = 28.dp)
                .navigationBarsPadding()
        ) {
            val activeAccount = connectedAccount
            if (activeAccount != null && !isSwitchingAccount) {
                // =========================================================
                // CONNECTED ACCOUNT MANAGEMENT VIEW
                // =========================================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFEFF6FF),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_morph_notes),
                                    contentDescription = "Omkun Morph",
                                    tint = MorphBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Omkun Morph Account",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                            Text(
                                text = "Authorized with Oj AI",
                                fontSize = 12.5.sp,
                                color = MorphGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF6B7280)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Connected User Profile Card
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
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(listOf(MorphBlue, MorphPurple))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!activeAccount.avatarUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = activeAccount.avatarUrl,
                                        contentDescription = activeAccount.displayName,
                                        modifier = Modifier.size(52.dp).clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(
                                        text = activeAccount.displayName.take(1).uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
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
                                Spacer(modifier = Modifier.height(4.dp))
                                val providerBadge = when (activeAccount.authProvider) {
                                    "GOOGLE" -> "Google Account"
                                    "OLINAM_SSO" -> "Olinam Verified SSO"
                                    else -> "Omkun Account"
                                }
                                Text(
                                    text = "$providerBadge • ${notes.count { !it.isArchived }} notes",
                                    fontSize = 11.5.sp,
                                    color = MorphBlue,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Color(0xFFE2E8F0))
                        Spacer(modifier = Modifier.height(12.dp))

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

                // Granted Permissions Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF0FDF4),
                    border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Authorized Oj AI Permissions",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF166534)
                        )
                        PermissionBadgeRow("Read notes & checklists in ${activeAccount.email}")
                        if (activeAccount.authorizedScopes.contains(MorphAccount.SCOPE_WRITE)) {
                            PermissionBadgeRow("Create & edit notes when requested")
                        }
                        if (activeAccount.authorizedScopes.contains(MorphAccount.SCOPE_DELETE)) {
                            PermissionBadgeRow("Delete notes when requested")
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
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MorphBlue),
                        shape = RoundedCornerShape(12.dp),
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
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Text("Open Morph", color = Color(0xFF0F172A), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = {
                            authError = null
                            isSwitchingAccount = true
                        },
                        modifier = Modifier.testTag("morph_switch_account_button")
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
                            Toast.makeText(context, "Omkun Morph account disconnected", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        modifier = Modifier.testTag("morph_disconnect_button")
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
                // =========================================================
                // OAUTH SIGN-IN & CONSENT FLOW
                // =========================================================
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
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF6B7280)
                        )
                    }
                }

                // OAuth Brand Connection Header (Oj AI <-> Omkun Morph)
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
                                .background(
                                    Brush.linearGradient(listOf(MorphBlue, MorphPurple))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Oj AI",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(22.dp)
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
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Sign in to Omkun Morph",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Choose which Morph account Oj AI should read, create, and manage notes in.",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Scope Permissions Consent Box (like Google OAuth screen)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = MorphBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Permissions requested by Oj AI",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        ScopeCheckRow(
                            title = "Read your Morph notes & checklists",
                            checked = allowRead,
                            enabled = false, // Core required scope
                            onCheckedChange = {}
                        )
                        ScopeCheckRow(
                            title = "Create & edit notes in your account",
                            checked = allowWrite,
                            enabled = true,
                            onCheckedChange = { allowWrite = it }
                        )
                        ScopeCheckRow(
                            title = "Delete notes when you ask Oj AI",
                            checked = allowDelete,
                            enabled = true,
                            onCheckedChange = { allowDelete = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (authError != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
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

                // Direct Omkun Account Authentication Form (Production Only)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Mode Toggle: Sign In vs Create Account
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
                                    modifier = Modifier.padding(vertical = 8.dp)
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
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }

                    if (isSignUpMode) {
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Your Full Name") },
                            placeholder = { Text("e.g. Om Kunwar") },
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
                        label = { Text("Omkun Account Email") },
                        placeholder = { Text("you@omkun.com or your email") },
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
                        label = { Text("Account Password") },
                        placeholder = { Text("Minimum 6 characters") },
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

                    Spacer(modifier = Modifier.height(6.dp))

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
                                        if (isSignUpMode) "Omkun Morph account created: ${account.email}"
                                        else "Connected as ${account.email}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    onAuthenticated(account)
                                    onDismiss()
                                }.onFailure { err ->
                                    authError = err.message ?: "Authentication failed. Please verify credentials."
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
                            text = if (isSignUpMode) "Create Omkun Account" else "Sign In to Omkun Morph",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    TextButton(
                        onClick = {
                            isSignUpMode = !isSignUpMode
                            authError = null
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = if (isSignUpMode) "Already have an Omkun account? Sign In" else "Don't have an Omkun account? Create one",
                            color = MorphBlue,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "🔒 Encrypted OAuth session. Your credentials are never shared in plain text and you can disconnect Oj AI anytime.",
                    fontSize = 11.5.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ScopeCheckRow(
    title: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(vertical = 2.dp),
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
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            fontSize = 13.sp,
            color = Color(0xFF334155),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PermissionBadgeRow(label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = MorphGreen,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF166534)
        )
    }
}
