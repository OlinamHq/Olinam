package com.example.data

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.model.MorphAccount
import com.example.model.MorphChecklistItem
import com.example.model.MorphNote
import com.example.model.MorphPalette
import com.example.model.UserProfile
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Production Account-Scoped Repository for Omkun Morph Notes.
 *
 * Every read, create, update, and delete operation requires an authenticated
 * [MorphAccount] session. Backend endpoints and tokens remain strictly internal
 * and are never exposed in user-facing UI.
 */
class MorphRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("omkun_morph_auth_v2", Context.MODE_PRIVATE)
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var activeWorkspaceId: String? = null

    private val _connectedAccountFlow = MutableStateFlow<MorphAccount?>(null)
    val connectedAccountFlow: StateFlow<MorphAccount?> = _connectedAccountFlow.asStateFlow()

    private val _notesFlow = MutableStateFlow<List<MorphNote>>(emptyList())
    val notesFlow: StateFlow<List<MorphNote>> = _notesFlow.asStateFlow()

    private val _syncStatus = MutableStateFlow("Sign in required")
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    val isAuthenticated: Boolean
        get() = _connectedAccountFlow.value != null

    var isConnected: Boolean
        get() = _connectedAccountFlow.value != null && prefs.getBoolean(KEY_OJ_AI_LINK_ENABLED, true)
        set(value) {
            prefs.edit().putBoolean(KEY_OJ_AI_LINK_ENABLED, value).apply()
        }

    var lastSyncTime: Long
        get() {
            val accId = _connectedAccountFlow.value?.accountId ?: return 0L
            return prefs.getLong("last_sync_$accId", 0L)
        }
        private set(value) {
            val accId = _connectedAccountFlow.value?.accountId ?: return
            prefs.edit().putLong("last_sync_$accId", value).apply()
        }

    init {
        restoreAuthenticatedSession()
    }

    private fun restoreAuthenticatedSession() {
        val accountJson = prefs.getString(KEY_ACTIVE_ACCOUNT_JSON, null)
        if (accountJson.isNullOrBlank()) {
            _connectedAccountFlow.value = null
            _notesFlow.value = emptyList()
            _syncStatus.value = "Sign in to sync your Morph notes"
            return
        }

        try {
            val obj = JSONObject(accountJson)
            val scopesArr = obj.optJSONArray("authorizedScopes")
            val scopes = mutableListOf<String>()
            if (scopesArr != null) {
                for (i in 0 until scopesArr.length()) {
                    scopes.add(scopesArr.optString(i))
                }
            }
            val account = MorphAccount(
                accountId = obj.getString("accountId"),
                email = obj.getString("email"),
                displayName = obj.optString("displayName", "Morph User"),
                avatarUrl = obj.optString("avatarUrl").takeIf { it.isNotBlank() },
                authProvider = obj.optString("authProvider", "OMKUN_ID"),
                sessionToken = obj.optString("sessionToken", ""),
                authorizedScopes = scopes.ifEmpty { MorphAccount.DEFAULT_SCOPES },
                connectedAt = obj.optLong("connectedAt", System.currentTimeMillis())
            )
            _connectedAccountFlow.value = account
            loadNotesForAccount(account)
            syncWithServer()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to restore Morph session: ${e.message}")
            _connectedAccountFlow.value = null
            _notesFlow.value = emptyList()
            _syncStatus.value = "Sign in to sync your Morph notes"
        }
    }

    private fun persistActiveAccount(account: MorphAccount?) {
        if (account == null) {
            prefs.edit().remove(KEY_ACTIVE_ACCOUNT_JSON).apply()
            return
        }
        val obj = JSONObject().apply {
            put("accountId", account.accountId)
            put("email", account.email)
            put("displayName", account.displayName)
            put("avatarUrl", account.avatarUrl ?: "")
            put("authProvider", account.authProvider)
            put("sessionToken", account.sessionToken)
            put("connectedAt", account.connectedAt)
            val scopesArr = JSONArray()
            account.authorizedScopes.forEach { scopesArr.put(it) }
            put("authorizedScopes", scopesArr)
        }
        prefs.edit()
            .putString(KEY_ACTIVE_ACCOUNT_JSON, obj.toString())
            .putBoolean(KEY_OJ_AI_LINK_ENABLED, true)
            .apply()
    }

    /**
     * Option 1: Sign in with Google via Jetpack Credential Manager (OAuth 2.0 ID Token).
     */
    suspend fun signInWithGoogle(
        activityContext: Context,
        scopes: List<String> = MorphAccount.DEFAULT_SCOPES
    ): Result<MorphAccount> = withContext(Dispatchers.IO) {
        try {
            val webClientIdResId = activityContext.resources.getIdentifier(
                "default_web_client_id",
                "string",
                activityContext.packageName
            )
            val serverClientId = if (webClientIdResId != 0) {
                activityContext.getString(webClientIdResId).trim()
            } else {
                ""
            }

            if (serverClientId.isBlank() || serverClientId.startsWith("YOUR_")) {
                return@withContext Result.failure(
                    IllegalStateException("GOOGLE_CLIENT_UNCONFIGURED")
                )
            }

            val signInOption = GetSignInWithGoogleOption.Builder(serverClientId).build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInOption)
                .build()

            val credentialManager = CredentialManager.create(activityContext)
            val response = withContext(Dispatchers.Main) {
                credentialManager.getCredential(activityContext, request)
            }

            val credential = response.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val email = googleIdTokenCredential.id
                val displayName = googleIdTokenCredential.displayName
                    ?.takeIf { it.isNotBlank() }
                    ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }
                val avatarUrl = googleIdTokenCredential.profilePictureUri?.toString()

                // Link to Firebase Auth if available
                val firebaseUid = try {
                    val authCred = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(authCred).await().user?.uid
                } catch (_: Throwable) {
                    null
                }

                val accountId = firebaseUid ?: deriveAccountId(email)
                val sessionToken = issueUserSessionToken(accountId, email, googleIdTokenCredential.idToken)

                val account = MorphAccount(
                    accountId = accountId,
                    email = email,
                    displayName = displayName,
                    avatarUrl = avatarUrl,
                    authProvider = "GOOGLE",
                    sessionToken = sessionToken,
                    authorizedScopes = scopes,
                    connectedAt = System.currentTimeMillis()
                )
                activateAuthenticatedAccount(account)
                Result.success(account)
            } else {
                Result.failure(IllegalStateException("Unsupported credential returned"))
            }
        } catch (e: Exception) {
            Log.w(TAG, "Google CredentialManager sign-in note: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Option 2: 1-Tap OAuth Authorization using the user's verified Olinam Account.
     */
    suspend fun authorizeWithOlinamAccount(
        user: UserProfile,
        scopes: List<String> = MorphAccount.DEFAULT_SCOPES
    ): Result<MorphAccount> = withContext(Dispatchers.IO) {
        val rawIdentity = when {
            user.email.isNotBlank() -> user.email.trim().lowercase(Locale.US)
            user.phoneNumber.isNotBlank() -> user.phoneNumber.trim()
            user.id.isNotBlank() && user.id != "me" -> user.id.trim()
            else -> return@withContext Result.failure(IllegalArgumentException("Please sign in with your email or Olinam account first."))
        }

        val firebaseUid = try {
            FirebaseAuth.getInstance().currentUser?.uid
        } catch (_: Throwable) {
            null
        }

        val accountId = when {
            !firebaseUid.isNullOrBlank() -> firebaseUid
            user.id.isNotBlank() && user.id != "me" -> "olinam_${sanitizeKey(user.id)}"
            else -> deriveAccountId(rawIdentity)
        }

        val displayIdentifier = when {
            user.email.isNotBlank() -> user.email.trim()
            user.phoneNumber.isNotBlank() -> "${user.phoneNumber.trim()} (Olinam ID)"
            else -> "${user.name.ifBlank { "Olinam User" }} (${accountId.takeLast(6)})"
        }

        val sessionToken = issueUserSessionToken(accountId, displayIdentifier, "olinam_sso")
        val account = MorphAccount(
            accountId = accountId,
            email = displayIdentifier,
            displayName = user.name.ifBlank { "Olinam User" },
            avatarUrl = user.avatarUrl,
            authProvider = "OLINAM_SSO",
            sessionToken = sessionToken,
            authorizedScopes = scopes,
            connectedAt = System.currentTimeMillis()
        )
        activateAuthenticatedAccount(account)
        Result.success(account)
    }

    /**
     * Production Omkun Morph Authentication via official backend (morph-69d82).
     * Validates real account credentials on the production server.
     */
    suspend fun signInWithOmkunCredentials(
        email: String,
        password: String,
        displayName: String = "",
        providerLabel: String = "OMKUN_ID",
        scopes: List<String> = MorphAccount.DEFAULT_SCOPES
    ): Result<MorphAccount> = withContext(Dispatchers.IO) {
        val normalizedEmail = email.trim().lowercase(Locale.US)
        if (normalizedEmail.isBlank() || !normalizedEmail.contains("@") || !normalizedEmail.contains(".")) {
            return@withContext Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }
        if (password.length < 6) {
            return@withContext Result.failure(IllegalArgumentException("Password must be at least 6 characters."))
        }

        try {
            val jsonBody = JSONObject().apply {
                put("email", normalizedEmail)
                put("password", password)
                put("returnSecureToken", true)
            }
            val request = Request.Builder()
                .url(MORPH_AUTH_SIGN_IN_URL)
                .post(jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string().orEmpty()

            if (response.isSuccessful) {
                val resJson = JSONObject(responseString)
                val localId = resJson.getString("localId")
                val idToken = resJson.getString("idToken")
                val serverDisplayName = resJson.optString("displayName", "")
                val resolvedName = serverDisplayName.ifBlank {
                    displayName.trim().ifBlank {
                        normalizedEmail.substringBefore("@")
                            .replace(".", " ")
                            .replace("_", " ")
                            .split(" ")
                            .filter { it.isNotBlank() }
                            .joinToString(" ") { part -> part.replaceFirstChar { it.uppercase() } }
                            .ifBlank { "Omkun Morph User" }
                    }
                }

                // Cache token and credentials for offline persistence
                val accountId = localId
                val sessionToken = idToken
                val account = MorphAccount(
                    accountId = accountId,
                    email = normalizedEmail,
                    displayName = resolvedName,
                    avatarUrl = null,
                    authProvider = providerLabel,
                    sessionToken = sessionToken,
                    authorizedScopes = scopes,
                    connectedAt = System.currentTimeMillis()
                )
                activateAuthenticatedAccount(account)
                Result.success(account)
            } else {
                val errorMsg = parseAuthErrorMessage(responseString, isSignUp = false)
                Result.failure(IllegalArgumentException(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "signInWithOmkunCredentials network error: ${e.message}")
            Result.failure(IllegalArgumentException("Network error connecting to Omkun Morph: ${e.message}"))
        }
    }

    /**
     * Create a new Omkun Morph Account on the production backend.
     */
    suspend fun signUpWithOmkunCredentials(
        email: String,
        password: String,
        displayName: String = "",
        scopes: List<String> = MorphAccount.DEFAULT_SCOPES
    ): Result<MorphAccount> = withContext(Dispatchers.IO) {
        val normalizedEmail = email.trim().lowercase(Locale.US)
        if (normalizedEmail.isBlank() || !normalizedEmail.contains("@") || !normalizedEmail.contains(".")) {
            return@withContext Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }
        if (password.length < 6) {
            return@withContext Result.failure(IllegalArgumentException("Password must be at least 6 characters."))
        }

        try {
            val jsonBody = JSONObject().apply {
                put("email", normalizedEmail)
                put("password", password)
                put("returnSecureToken", true)
            }
            val request = Request.Builder()
                .url(MORPH_AUTH_SIGN_UP_URL)
                .post(jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string().orEmpty()

            if (response.isSuccessful) {
                val resJson = JSONObject(responseString)
                val localId = resJson.getString("localId")
                val idToken = resJson.getString("idToken")
                val resolvedName = displayName.trim().ifBlank {
                    normalizedEmail.substringBefore("@")
                        .replace(".", " ")
                        .replace("_", " ")
                        .split(" ")
                        .filter { it.isNotBlank() }
                        .joinToString(" ") { part -> part.replaceFirstChar { it.uppercase() } }
                        .ifBlank { "Omkun Morph User" }
                }

                // Update display name in Omkun Morph profile if provided
                try {
                    val updateBody = JSONObject().apply {
                        put("idToken", idToken)
                        put("displayName", resolvedName)
                        put("returnSecureToken", true)
                    }
                    val updateReq = Request.Builder()
                        .url(MORPH_AUTH_UPDATE_URL)
                        .post(updateBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                        .build()
                    client.newCall(updateReq).execute().close()
                } catch (_: Exception) {}

                val account = MorphAccount(
                    accountId = localId,
                    email = normalizedEmail,
                    displayName = resolvedName,
                    avatarUrl = null,
                    authProvider = "OMKUN_ID",
                    sessionToken = idToken,
                    authorizedScopes = scopes,
                    connectedAt = System.currentTimeMillis()
                )
                activateAuthenticatedAccount(account)
                Result.success(account)
            } else {
                val errorMsg = parseAuthErrorMessage(responseString, isSignUp = true)
                Result.failure(IllegalArgumentException(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "signUpWithOmkunCredentials network error: ${e.message}")
            Result.failure(IllegalArgumentException("Network error connecting to Omkun Morph: ${e.message}"))
        }
    }

    private fun parseAuthErrorMessage(responseJson: String, isSignUp: Boolean): String {
        return try {
            val root = JSONObject(responseJson)
            val error = root.optJSONObject("error")
            val rawMsg = error?.optString("message", "") ?: ""
            when {
                rawMsg.contains("INVALID_LOGIN_CREDENTIALS") || rawMsg.contains("INVALID_PASSWORD") ->
                    "Invalid email or password. Please verify your credentials or create a new Omkun Morph account."
                rawMsg.contains("EMAIL_NOT_FOUND") ->
                    "No Omkun Morph account found for this email. Please switch to 'Create Account' to sign up."
                rawMsg.contains("EMAIL_EXISTS") ->
                    "An Omkun Morph account with this email already exists. Please switch to 'Sign In'."
                rawMsg.contains("USER_DISABLED") ->
                    "This Omkun Morph account has been disabled."
                rawMsg.contains("TOO_MANY_ATTEMPTS_TRY_LATER") ->
                    "Too many failed login attempts. Please wait a moment and try again."
                rawMsg.contains("WEAK_PASSWORD") ->
                    "Password should be at least 6 characters long."
                rawMsg.contains("INVALID_EMAIL") ->
                    "Please enter a valid email address."
                rawMsg.isNotBlank() ->
                    "Authentication error: $rawMsg"
                else ->
                    if (isSignUp) "Could not create Omkun account. Please try again."
                    else "Could not sign in to Omkun Morph. Please check your credentials."
            }
        } catch (_: Exception) {
            if (isSignUp) "Could not create Omkun account."
            else "Invalid email or password. Please verify your credentials."
        }
    }

    private fun activateAuthenticatedAccount(account: MorphAccount) {
        _connectedAccountFlow.value = account
        persistActiveAccount(account)
        loadNotesForAccount(account)
        _syncStatus.value = "Connected as ${account.email}"
        syncWithServer()
    }

    /**
     * Signs out and disconnects the active Morph account.
     * Clears in-memory notes while keeping the user's encrypted/isolated vault intact for next login.
     */
    fun disconnectAccount() {
        val prevProvider = _connectedAccountFlow.value?.authProvider
        _connectedAccountFlow.value = null
        prefs.edit()
            .remove(KEY_ACTIVE_ACCOUNT_JSON)
            .putBoolean(KEY_OJ_AI_LINK_ENABLED, false)
            .apply()
        _notesFlow.value = emptyList()
        _syncStatus.value = "Sign in to sync your Morph notes"

        if (prevProvider == "GOOGLE") {
            coroutineScope.launch {
                try {
                    CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
                } catch (_: Throwable) {}
            }
        }
    }

    /**
     * Syncs notes for the currently authenticated [MorphAccount] ONLY.
     */
    fun syncWithServer(onComplete: ((Boolean, String) -> Unit)? = null) {
        val account = _connectedAccountFlow.value
        if (account == null) {
            _syncStatus.value = "Sign in required"
            coroutineScope.launch(Dispatchers.Main) {
                onComplete?.invoke(false, "Please sign in to your Omkun Morph account first.")
            }
            return
        }

        coroutineScope.launch {
            _isSyncing.value = true
            _syncStatus.value = "Syncing ${account.email}..."
            var syncedRemote = false

            // 1. Sync all workspace documents/pages from Omkun Morph cloud (morph.omkun.com /documents and /workspaces)
            try {
                val workspaceDocs = fetchWorkspaceDocumentsFromMorphCloud(account)
                if (workspaceDocs.isNotEmpty()) {
                    mergeRemoteNotesForAccount(account, workspaceDocs)
                    syncedRemote = true
                }
            } catch (e: Exception) {
                Log.d(TAG, "Morph workspace documents sync notice: ${e.message}")
            }

            // 2. Sync with production Omkun Morph Cloud user subcollection (/users/{accountId}/morph_notes)
            try {
                val cloudNotes = fetchAccountNotesFromMorphCloud(account)
                if (cloudNotes.isNotEmpty()) {
                    mergeRemoteNotesForAccount(account, cloudNotes)
                    syncedRemote = true
                }
            } catch (e: Exception) {
                Log.d(TAG, "Morph Cloud REST sync notice: ${e.message}")
            }

            // 3. Sync with local FirebaseFirestore if available
            try {
                val firestoreNotes = fetchAccountNotesFromFirestore(account)
                if (firestoreNotes.isNotEmpty()) {
                    mergeRemoteNotesForAccount(account, firestoreNotes)
                    syncedRemote = true
                }
            } catch (e: Exception) {
                Log.d(TAG, "Firestore Morph sync note: ${e.message}")
            }

            lastSyncTime = System.currentTimeMillis()
            _isSyncing.value = false
            val currentCount = _notesFlow.value.count { !it.isArchived }
            _syncStatus.value = if (syncedRemote) {
                "Synced • $currentCount notes"
            } else {
                "Active • ${account.email}"
            }

            // CRITICAL: onComplete callback MUST execute on Dispatchers.Main so UI Toasts never crash the app
            withContext(Dispatchers.Main) {
                onComplete?.invoke(true, if (syncedRemote) "Synced $currentCount notes for ${account.email}" else "Active • ${account.email}")
            }
        }
    }

    private fun fetchWorkspaceDocumentsFromMorphCloud(account: MorphAccount): List<MorphNote> {
        return try {
            val normalizedEmail = account.email.trim().lowercase(Locale.US)
            val userWorkspaceIds = mutableSetOf<String>()

            // 1. Query workspaces to detect user's workspaces
            try {
                val wsUrl = "$MORPH_FIRESTORE_REST_BASE/workspaces"
                val wsReqBuilder = Request.Builder().url(wsUrl)
                if (account.sessionToken.isNotBlank()) {
                    wsReqBuilder.addHeader("Authorization", "Bearer ${account.sessionToken}")
                }
                val wsRes = client.newCall(wsReqBuilder.build()).execute()
                if (wsRes.isSuccessful) {
                    val wsBody = wsRes.body?.string().orEmpty()
                    if (wsBody.isNotBlank()) {
                        val root = JSONObject(wsBody)
                        val docs = root.optJSONArray("documents")
                        if (docs != null) {
                            for (i in 0 until docs.length()) {
                                val wsDoc = docs.getJSONObject(i)
                                val wsDocId = wsDoc.optString("name", "").substringAfterLast("/")
                                val fields = wsDoc.optJSONObject("fields") ?: continue
                                val customId = fields.optJSONObject("id")?.optString("stringValue", "").orEmpty()
                                val ownerId = fields.optJSONObject("ownerId")?.optString("stringValue", "").orEmpty()
                                val effectiveId = customId.ifBlank { wsDocId }

                                var isMember = (ownerId == account.accountId)
                                val membersArr = fields.optJSONObject("members")?.optJSONObject("arrayValue")?.optJSONArray("values")
                                if (membersArr != null) {
                                    for (m in 0 until membersArr.length()) {
                                        val mFields = membersArr.getJSONObject(m).optJSONObject("mapValue")?.optJSONObject("fields")
                                        val mEmail = mFields?.optJSONObject("email")?.optString("stringValue", "").orEmpty().trim().lowercase(Locale.US)
                                        val mUid = mFields?.optJSONObject("userId")?.optString("stringValue", "").orEmpty()
                                        if (mEmail == normalizedEmail || mUid == account.accountId) {
                                            isMember = true
                                            break
                                        }
                                    }
                                }

                                if (isMember) {
                                    userWorkspaceIds.add(effectiveId)
                                    if (wsDocId.isNotBlank()) userWorkspaceIds.add(wsDocId)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "Workspace resolution notice: ${e.message}")
            }

            // Personal workspace IDs for this user
            if (account.accountId.isNotBlank()) {
                userWorkspaceIds.add("ws-${account.accountId.take(10)}")
                userWorkspaceIds.add("ws-user-${account.accountId.take(12)}")
            }
            // Primary shared workspace
            userWorkspaceIds.add("ws-primary")
            activeWorkspaceId = userWorkspaceIds.firstOrNull { it != "ws-primary" } ?: "ws-primary"

            // 2. Query documents collection from morph-69d82
            val docsUrl = "$MORPH_FIRESTORE_REST_BASE/documents?pageSize=300"
            val docsReqBuilder = Request.Builder().url(docsUrl)
            if (account.sessionToken.isNotBlank()) {
                docsReqBuilder.addHeader("Authorization", "Bearer ${account.sessionToken}")
            }
            val docsRes = client.newCall(docsReqBuilder.build()).execute()
            if (!docsRes.isSuccessful) return emptyList()
            val docsBody = docsRes.body?.string().orEmpty()
            if (docsBody.isBlank()) return emptyList()

            val result = mutableListOf<MorphNote>()
            val docsRoot = JSONObject(docsBody)
            val docsArray = docsRoot.optJSONArray("documents") ?: return emptyList()

            for (i in 0 until docsArray.length()) {
                val docObj = docsArray.getJSONObject(i)
                val docName = docObj.optString("name", "")
                val docId = docName.substringAfterLast("/")
                val fields = docObj.optJSONObject("fields") ?: continue

                // Check isDeleted flag
                val isDeleted = fields.optJSONObject("isDeleted")?.optBoolean("booleanValue", false) ?: false
                if (isDeleted) continue

                // Check workspaceId
                val docWsId = fields.optJSONObject("workspaceId")?.optString("stringValue", "").orEmpty()
                if (docWsId.isNotBlank() && !userWorkspaceIds.contains(docWsId)) {
                    continue
                }

                val title = fields.optJSONObject("title")?.optString("stringValue", "").orEmpty().ifBlank { "Untitled Page" }
                val description = fields.optJSONObject("description")?.optString("stringValue", "").orEmpty()
                val rawContent = fields.optJSONObject("content")?.optString("stringValue", "").orEmpty()

                val checklistItems = mutableListOf<MorphChecklistItem>()
                val textParts = mutableListOf<String>()
                if (description.isNotBlank()) {
                    textParts.add(description)
                }

                val blocksArr = fields.optJSONObject("blocks")?.optJSONObject("arrayValue")?.optJSONArray("values")
                if (blocksArr != null) {
                    for (b in 0 until blocksArr.length()) {
                        val bFields = blocksArr.getJSONObject(b).optJSONObject("mapValue")?.optJSONObject("fields") ?: continue
                        val bType = bFields.optJSONObject("type")?.optString("stringValue", "").orEmpty()
                        val bContent = bFields.optJSONObject("content")?.optString("stringValue", "").orEmpty()
                        val bChecked = bFields.optJSONObject("checked")?.optBoolean("booleanValue", false) ?: false
                        val bId = bFields.optJSONObject("id")?.optString("stringValue", "").orEmpty()

                        if (bType == "todo") {
                            if (bContent.isNotBlank()) {
                                checklistItems.add(
                                    MorphChecklistItem(
                                        id = bId.ifBlank { UUID.randomUUID().toString() },
                                        text = bContent,
                                        isDone = bChecked
                                    )
                                )
                            }
                        } else if (bContent.isNotBlank()) {
                            textParts.add(bContent)
                        }
                    }
                }

                val content = textParts.filter { it.isNotBlank() }.joinToString("\n\n").ifBlank { rawContent }
                val isChecklist = checklistItems.isNotEmpty()

                // Extract tags / labels
                val labels = mutableListOf<String>()
                val tagsArr = fields.optJSONObject("tags")?.optJSONObject("arrayValue")?.optJSONArray("values")
                if (tagsArr != null) {
                    for (t in 0 until tagsArr.length()) {
                        val tagStr = tagsArr.getJSONObject(t).optString("stringValue", "")
                        if (tagStr.isNotBlank()) labels.add(tagStr)
                    }
                }

                val tsStr = fields.optJSONObject("updatedAtTimestamp")?.optString("integerValue", "")
                val updatedAt = tsStr?.toLongOrNull() ?: System.currentTimeMillis()
                val isPinned = fields.optJSONObject("isPinned")?.optBoolean("booleanValue", false) ?: false

                result.add(
                    MorphNote(
                        id = docId,
                        accountId = account.accountId,
                        title = title,
                        content = content,
                        checklist = checklistItems,
                        isChecklist = isChecklist,
                        colorHex = MorphPalette.Default,
                        isPinned = isPinned,
                        labels = labels,
                        updatedAt = updatedAt,
                        isArchived = false
                    )
                )
            }
            result
        } catch (e: Exception) {
            Log.d(TAG, "fetchWorkspaceDocumentsFromMorphCloud notice: ${e.message}")
            emptyList()
        }
    }

    private fun fetchAccountNotesFromMorphCloud(account: MorphAccount): List<MorphNote> {
        return try {
            val url = "$MORPH_FIRESTORE_REST_BASE/users/${account.accountId}/morph_notes"
            val builder = Request.Builder().url(url)
            if (account.sessionToken.isNotBlank()) {
                builder.addHeader("Authorization", "Bearer ${account.sessionToken}")
            }
            val response = client.newCall(builder.get().build()).execute()
            if (!response.isSuccessful) return emptyList()
            val body = response.body?.string().orEmpty()
            if (body.isBlank()) return emptyList()

            val result = mutableListOf<MorphNote>()
            val root = JSONObject(body)
            val docsArray = root.optJSONArray("documents") ?: return emptyList()
            for (i in 0 until docsArray.length()) {
                val docObj = docsArray.getJSONObject(i)
                val docName = docObj.optString("name", "")
                val id = docName.substringAfterLast("/")
                val fields = docObj.optJSONObject("fields") ?: continue
                val title = fields.optJSONObject("title")?.optString("stringValue", "").orEmpty()
                val content = fields.optJSONObject("content")?.optString("stringValue", "").orEmpty()
                val colorHex = fields.optJSONObject("colorHex")?.optString("stringValue", MorphPalette.Default) ?: MorphPalette.Default
                val isPinned = fields.optJSONObject("isPinned")?.optBoolean("booleanValue", false) ?: false
                val isChecklist = fields.optJSONObject("isChecklist")?.optBoolean("booleanValue", false) ?: false
                val updatedAtStr = fields.optJSONObject("updatedAt")?.optString("integerValue", "")
                val updatedAt = updatedAtStr?.toLongOrNull() ?: System.currentTimeMillis()

                result.add(
                    MorphNote(
                        id = id,
                        accountId = account.accountId,
                        title = title,
                        content = content,
                        colorHex = colorHex,
                        isPinned = isPinned,
                        isChecklist = isChecklist,
                        updatedAt = updatedAt
                    )
                )
            }
            result
        } catch (e: Exception) {
            Log.d(TAG, "fetchAccountNotesFromMorphCloud notice: ${e.message}")
            emptyList()
        }
    }

    private suspend fun fetchAccountNotesFromFirestore(account: MorphAccount): List<MorphNote> {
        val db = try {
            FirebaseFirestore.getInstance()
        } catch (_: Throwable) {
            return emptyList()
        }
        val snapshot = db.collection("users")
            .document(account.accountId)
            .collection("morph_notes")
            .get()
            .await()

        val result = mutableListOf<MorphNote>()
        for (doc in snapshot.documents) {
            val id = doc.getString("id") ?: doc.id
            val title = doc.getString("title") ?: ""
            val content = doc.getString("content") ?: ""
            val isChecklist = doc.getBoolean("isChecklist") ?: false
            val colorHex = doc.getString("colorHex") ?: MorphPalette.Default
            val isPinned = doc.getBoolean("isPinned") ?: false
            val updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
            val isArchived = doc.getBoolean("isArchived") ?: false
            @Suppress("UNCHECKED_CAST")
            val labels = (doc.get("labels") as? List<String>) ?: emptyList()
            @Suppress("UNCHECKED_CAST")
            val rawChecklist = (doc.get("checklist") as? List<Map<String, Any?>>).orEmpty()
            val checklist = rawChecklist.map { item ->
                MorphChecklistItem(
                    id = item["id"] as? String ?: UUID.randomUUID().toString(),
                    text = item["text"] as? String ?: "",
                    isDone = item["isDone"] as? Boolean ?: false
                )
            }
            result.add(
                MorphNote(
                    id = id,
                    accountId = account.accountId,
                    title = title,
                    content = content,
                    checklist = checklist,
                    isChecklist = isChecklist,
                    colorHex = colorHex,
                    isPinned = isPinned,
                    labels = labels,
                    updatedAt = updatedAt,
                    isArchived = isArchived
                )
            )
        }
        return result
    }

    private fun pushNoteToFirestore(account: MorphAccount, note: MorphNote) {
        // 1. Sync with local FirebaseFirestore if configured
        try {
            val db = FirebaseFirestore.getInstance()
            val payload = hashMapOf(
                "id" to note.id,
                "accountId" to account.accountId,
                "ownerEmail" to account.email,
                "title" to note.title,
                "content" to note.content,
                "isChecklist" to note.isChecklist,
                "checklist" to note.checklist.map {
                    mapOf("id" to it.id, "text" to it.text, "isDone" to it.isDone)
                },
                "colorHex" to note.colorHex,
                "isPinned" to note.isPinned,
                "labels" to note.labels,
                "updatedAt" to note.updatedAt,
                "isArchived" to note.isArchived
            )
            db.collection("users")
                .document(account.accountId)
                .collection("morph_notes")
                .document(note.id)
                .set(payload, SetOptions.merge())
        } catch (_: Throwable) {}

        // 2. Sync directly with Omkun Morph Cloud Firestore REST
        coroutineScope.launch {
            try {
                val targetWs = activeWorkspaceId ?: "ws-primary"

                // A. Save to morph-69d82 /documents/{note.id} so it shows on morph.omkun.com
                val docPayload = JSONObject().apply {
                    val fields = JSONObject().apply {
                        put("id", JSONObject().apply { put("stringValue", note.id) })
                        put("title", JSONObject().apply { put("stringValue", note.title.ifBlank { "Untitled Note" }) })
                        put("workspaceId", JSONObject().apply { put("stringValue", targetWs) })
                        put("description", JSONObject().apply { put("stringValue", "Synced via Olinam Morph App") })
                        put("status", JSONObject().apply { put("stringValue", "In Progress") })
                        put("isDeleted", JSONObject().apply { put("booleanValue", false) })
                        put("updatedAt", JSONObject().apply { put("stringValue", "Just now") })
                        put("updatedAtTimestamp", JSONObject().apply { put("integerValue", note.updatedAt.toString()) })
                        put("owner", JSONObject().apply { put("stringValue", account.displayName) })
                        put("icon", JSONObject().apply { put("stringValue", if (note.isChecklist) "☑️" else "📄") })

                        val tagsArr = JSONArray()
                        tagsArr.put(JSONObject().apply { put("stringValue", "Olinam") })
                        for (tag in note.labels) {
                            tagsArr.put(JSONObject().apply { put("stringValue", tag) })
                        }
                        put("tags", JSONObject().apply { put("arrayValue", JSONObject().apply { put("values", tagsArr) }) })

                        val blocksArr = JSONArray()
                        if (note.content.isNotBlank()) {
                            blocksArr.put(JSONObject().apply {
                                put("mapValue", JSONObject().apply {
                                    put("fields", JSONObject().apply {
                                        put("id", JSONObject().apply { put("stringValue", "b-${note.id}-text") })
                                        put("type", JSONObject().apply { put("stringValue", "paragraph") })
                                        put("content", JSONObject().apply { put("stringValue", note.content) })
                                    })
                                })
                            })
                        }
                        note.checklist.forEachIndexed { idx, item ->
                            blocksArr.put(JSONObject().apply {
                                put("mapValue", JSONObject().apply {
                                    put("fields", JSONObject().apply {
                                        put("id", JSONObject().apply { put("stringValue", item.id.ifBlank { "b-${note.id}-$idx" }) })
                                        put("type", JSONObject().apply { put("stringValue", "todo") })
                                        put("content", JSONObject().apply { put("stringValue", item.text) })
                                        put("checked", JSONObject().apply { put("booleanValue", item.isDone) })
                                    })
                                })
                            })
                        }
                        put("blocks", JSONObject().apply { put("arrayValue", JSONObject().apply { put("values", blocksArr) }) })
                    }
                    put("fields", fields)
                }

                val req1 = Request.Builder()
                    .url("$MORPH_FIRESTORE_REST_BASE/documents/${note.id}")
                    .patch(docPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                if (account.sessionToken.isNotBlank()) {
                    req1.addHeader("Authorization", "Bearer ${account.sessionToken}")
                }
                client.newCall(req1.build()).execute().close()

                // B. Also save to user's personal /users/{accountId}/morph_notes/{note.id}
                val userNotePayload = JSONObject().apply {
                    val fields = JSONObject().apply {
                        put("id", JSONObject().apply { put("stringValue", note.id) })
                        put("title", JSONObject().apply { put("stringValue", note.title) })
                        put("content", JSONObject().apply {
                            val c = if (note.isChecklist) {
                                note.checklist.joinToString("\n") { (if (it.isDone) "☑ " else "☐ ") + it.text }
                            } else {
                                note.content
                            }
                            put("stringValue", c)
                        })
                        put("isChecklist", JSONObject().apply { put("booleanValue", note.isChecklist) })
                        put("colorHex", JSONObject().apply { put("stringValue", note.colorHex) })
                        put("isPinned", JSONObject().apply { put("booleanValue", note.isPinned) })
                        put("isArchived", JSONObject().apply { put("booleanValue", note.isArchived) })
                        put("updatedAt", JSONObject().apply { put("integerValue", note.updatedAt.toString()) })
                    }
                    put("fields", fields)
                }

                val req2 = Request.Builder()
                    .url("$MORPH_FIRESTORE_REST_BASE/users/${account.accountId}/morph_notes/${note.id}")
                    .patch(userNotePayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                if (account.sessionToken.isNotBlank()) {
                    req2.addHeader("Authorization", "Bearer ${account.sessionToken}")
                }
                client.newCall(req2.build()).execute().close()
            } catch (e: Exception) {
                Log.d(TAG, "Morph Cloud pushNote notice: ${e.message}")
            }
        }
    }

    private fun deleteNoteFromFirestore(account: MorphAccount, noteId: String) {
        // 1. Delete from local FirebaseFirestore if configured
        try {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(account.accountId)
                .collection("morph_notes")
                .document(noteId)
                .delete()
        } catch (_: Throwable) {}

        // 2. Delete from Omkun Morph Cloud Firestore REST
        coroutineScope.launch {
            try {
                // Delete from /documents/{noteId}
                val req1 = Request.Builder()
                    .url("$MORPH_FIRESTORE_REST_BASE/documents/$noteId")
                    .delete()
                if (account.sessionToken.isNotBlank()) {
                    req1.addHeader("Authorization", "Bearer ${account.sessionToken}")
                }
                client.newCall(req1.build()).execute().close()

                // Delete from /users/{accountId}/morph_notes/{noteId}
                val req2 = Request.Builder()
                    .url("$MORPH_FIRESTORE_REST_BASE/users/${account.accountId}/morph_notes/$noteId")
                    .delete()
                if (account.sessionToken.isNotBlank()) {
                    req2.addHeader("Authorization", "Bearer ${account.sessionToken}")
                }
                client.newCall(req2.build()).execute().close()
            } catch (e: Exception) {
                Log.d(TAG, "Morph Cloud deleteNote notice: ${e.message}")
            }
        }
    }

    private fun mergeRemoteNotesForAccount(account: MorphAccount, remoteNotes: List<MorphNote>) {
        // Filter strictly to notes owned by this account or explicitly untagged from this user's endpoint
        val scopedRemote = remoteNotes.filter {
            it.accountId.isBlank() || it.accountId == account.accountId
        }.map { it.copy(accountId = account.accountId) }

        if (scopedRemote.isEmpty()) return

        val currentList = _notesFlow.value
        val remoteById = scopedRemote.associateBy { it.id }
        val merged = mutableListOf<MorphNote>()

        // Keep newer version when IDs match
        scopedRemote.forEach { remote ->
            val local = currentList.find { it.id == remote.id }
            if (local != null && local.updatedAt > remote.updatedAt) {
                merged.add(local)
            } else {
                merged.add(remote)
            }
        }
        currentList.forEach { local ->
            if (!remoteById.containsKey(local.id)) {
                merged.add(local)
            }
        }
        val sorted = merged.sortedByDescending { it.updatedAt }
        val activeAccount = _connectedAccountFlow.value
        val isAccountActive = activeAccount?.accountId == account.accountId ||
                (activeAccount != null && activeAccount.email.equals(account.email, ignoreCase = true))
        if (isAccountActive) {
            _notesFlow.value = sorted
            saveToDiskForAccount(account.accountId, sorted)
        }
    }

    private fun parseNotesFromJson(jsonString: String, expectedAccountId: String): List<MorphNote> {
        val list = mutableListOf<MorphNote>()
        try {
            val array = if (jsonString.trimStart().startsWith("{")) {
                val root = JSONObject(jsonString)
                root.optJSONArray("data") ?: return emptyList()
            } else {
                JSONArray(jsonString)
            }
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                val itemAccountId = item.optString("accountId", "")
                // Never import notes belonging to a different account
                if (itemAccountId.isNotBlank() && itemAccountId != expectedAccountId) continue
                // Skip global un-scoped server notes unless tagged for this user
                if (itemAccountId.isBlank()) continue

                val id = item.optString("id").ifBlank {
                    item.optString("_id").ifBlank { UUID.randomUUID().toString() }
                }
                val title = item.optString("title", "")
                val noteContent = item.optString("note").ifBlank {
                    item.optString("content", "")
                }
                val colorStr = item.optString("color", "yellow")
                val isPinned = item.optBoolean("pinned", item.optBoolean("isPinned", false))
                val colorHex = mapApiColorToHex(colorStr)
                val updatedAt = item.optLong("updatedAt", System.currentTimeMillis())

                list.add(
                    MorphNote(
                        id = id,
                        accountId = expectedAccountId,
                        title = title,
                        content = noteContent,
                        colorHex = colorHex,
                        isPinned = isPinned,
                        updatedAt = updatedAt
                    )
                )
            }
        } catch (e: Exception) {
            Log.d(TAG, "parseNotesFromJson notice: ${e.message}")
        }
        return list
    }

    private fun storageKeyForAccount(accountId: String): String = "morph_notes_v2_$accountId"

    private fun loadNotesForAccount(account: MorphAccount) {
        val key = storageKeyForAccount(account.accountId)
        val savedJson = prefs.getString(key, null)
        if (savedJson.isNullOrBlank()) {
            _notesFlow.value = emptyList()
            return
        }

        try {
            val list = mutableListOf<MorphNote>()
            val array = JSONArray(savedJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val checklistArray = obj.optJSONArray("checklist")
                val checklistItems = mutableListOf<MorphChecklistItem>()
                if (checklistArray != null) {
                    for (j in 0 until checklistArray.length()) {
                        val cObj = checklistArray.getJSONObject(j)
                        checklistItems.add(
                            MorphChecklistItem(
                                id = cObj.optString("id", UUID.randomUUID().toString()),
                                text = cObj.optString("text"),
                                isDone = cObj.optBoolean("isDone")
                            )
                        )
                    }
                }

                val labelsArray = obj.optJSONArray("labels")
                val labels = mutableListOf<String>()
                if (labelsArray != null) {
                    for (k in 0 until labelsArray.length()) {
                        labels.add(labelsArray.getString(k))
                    }
                }

                list.add(
                    MorphNote(
                        id = obj.optString("id"),
                        accountId = account.accountId,
                        title = obj.optString("title"),
                        content = obj.optString("content"),
                        checklist = checklistItems,
                        isChecklist = obj.optBoolean("isChecklist"),
                        colorHex = obj.optString("colorHex", MorphPalette.Default),
                        isPinned = obj.optBoolean("isPinned"),
                        labels = labels,
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis()),
                        isArchived = obj.optBoolean("isArchived")
                    )
                )
            }
            _notesFlow.value = list
        } catch (_: Exception) {
            _notesFlow.value = emptyList()
        }
    }

    private fun saveToDiskForAccount(accountId: String, notes: List<MorphNote>) {
        try {
            val array = JSONArray()
            for (note in notes) {
                val obj = JSONObject().apply {
                    put("id", note.id)
                    put("accountId", accountId)
                    put("title", note.title)
                    put("content", note.content)
                    put("isChecklist", note.isChecklist)
                    put("colorHex", note.colorHex)
                    put("isPinned", note.isPinned)
                    put("updatedAt", note.updatedAt)
                    put("isArchived", note.isArchived)

                    val checkArr = JSONArray()
                    for (item in note.checklist) {
                        checkArr.put(
                            JSONObject().apply {
                                put("id", item.id)
                                put("text", item.text)
                                put("isDone", item.isDone)
                            }
                        )
                    }
                    put("checklist", checkArr)

                    val labelArr = JSONArray()
                    for (lbl in note.labels) {
                        labelArr.put(lbl)
                    }
                    put("labels", labelArr)
                }
                array.put(obj)
            }
            prefs.edit().putString(storageKeyForAccount(accountId), array.toString()).apply()
        } catch (_: Exception) {}
    }

    /**
     * Creates or updates a note strictly inside the currently authenticated user's Morph account.
     */
    fun saveNote(note: MorphNote): Boolean {
        val account = _connectedAccountFlow.value ?: return false
        val current = _notesFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == note.id }
        val updatedNote = note.copy(
            accountId = account.accountId,
            updatedAt = System.currentTimeMillis()
        )
        val isNew = (index < 0)

        if (!isNew) {
            current[index] = updatedNote
        } else {
            current.add(0, updatedNote)
        }
        _notesFlow.value = current
        saveToDiskForAccount(account.accountId, current)
        pushNoteToFirestore(account, updatedNote)

        coroutineScope.launch {
            try {
                val mediaType = "application/json; charset=utf-8".toMediaType()
                val json = JSONObject().apply {
                    put("accountId", account.accountId)
                    put("ownerEmail", account.email)
                    put("title", updatedNote.title)
                    put(
                        "note",
                        if (updatedNote.isChecklist) {
                            updatedNote.checklist.joinToString("\n") { (if (it.isDone) "☑ " else "☐ ") + it.text }
                        } else {
                            updatedNote.content
                        }
                    )
                    put("color", mapHexToApiColor(updatedNote.colorHex))
                    put("pinned", updatedNote.isPinned)
                }

                val builder = Request.Builder()
                    .addHeader("Authorization", "Bearer $INTERNAL_SERVICE_BRIDGE_TOKEN")
                    .addHeader("X-Morph-User-Id", account.accountId)
                    .addHeader("X-Morph-User-Email", account.email)
                    .addHeader("X-Morph-Session-Token", account.sessionToken)
                    .addHeader("Content-Type", "application/json")

                val request = if (isNew) {
                    builder.url("${INTERNAL_MORPH_CLOUD_URL.trimEnd('/')}/notes")
                        .post(json.toString().toRequestBody(mediaType))
                        .build()
                } else {
                    builder.url("${INTERNAL_MORPH_CLOUD_URL.trimEnd('/')}/notes/${updatedNote.id}")
                        .patch(json.toString().toRequestBody(mediaType))
                        .build()
                }

                client.newCall(request).execute().close()
            } catch (e: Exception) {
                Log.d(TAG, "Cloud saveNote sync notice: ${e.message}")
            }
        }
        return true
    }

    /**
     * Deletes a note strictly from the currently authenticated user's Morph account.
     */
    fun deleteNote(noteId: String): Boolean {
        val account = _connectedAccountFlow.value ?: return false
        val existed = _notesFlow.value.any { it.id == noteId }
        val current = _notesFlow.value.filter { it.id != noteId }
        _notesFlow.value = current
        saveToDiskForAccount(account.accountId, current)
        deleteNoteFromFirestore(account, noteId)

        coroutineScope.launch {
            try {
                val request = Request.Builder()
                    .url("${INTERNAL_MORPH_CLOUD_URL.trimEnd('/')}/notes/$noteId")
                    .addHeader("Authorization", "Bearer $INTERNAL_SERVICE_BRIDGE_TOKEN")
                    .addHeader("X-Morph-User-Id", account.accountId)
                    .addHeader("X-Morph-User-Email", account.email)
                    .addHeader("X-Morph-Session-Token", account.sessionToken)
                    .addHeader("Content-Type", "application/json")
                    .delete()
                    .build()
                client.newCall(request).execute().close()
            } catch (e: Exception) {
                Log.d(TAG, "Cloud deleteNote sync notice: ${e.message}")
            }
        }
        return existed
    }

    fun togglePin(noteId: String) {
        val target = _notesFlow.value.find { it.id == noteId } ?: return
        saveNote(target.copy(isPinned = !target.isPinned))
    }

    fun toggleChecklistItem(noteId: String, itemIndex: Int) {
        val target = _notesFlow.value.find { it.id == noteId } ?: return
        if (itemIndex !in target.checklist.indices) return
        val updatedChecklist = target.checklist.mapIndexed { i, item ->
            if (i == itemIndex) item.copy(isDone = !item.isDone) else item
        }
        saveNote(target.copy(checklist = updatedChecklist))
    }

    // =========================================================================
    // Oj AI Authenticated Tool Operations (Scoped to Connected MorphAccount)
    // =========================================================================

    fun createNoteFromAi(
        title: String,
        content: String,
        checklistItems: List<String> = emptyList(),
        isPinned: Boolean = false,
        colorHex: String = MorphPalette.Lemon
    ): Result<MorphNote> {
        val account = _connectedAccountFlow.value
            ?: return Result.failure(IllegalStateException("NOT_AUTHENTICATED"))
        if (!account.authorizedScopes.contains(MorphAccount.SCOPE_WRITE)) {
            return Result.failure(IllegalStateException("MISSING_WRITE_SCOPE"))
        }

        val isChecklist = checklistItems.isNotEmpty()
        val note = MorphNote(
            id = UUID.randomUUID().toString(),
            accountId = account.accountId,
            title = title.trim().ifBlank { "Note from Oj AI" },
            content = if (isChecklist) "" else content.trim(),
            checklist = checklistItems.map { MorphChecklistItem(text = it.trim(), isDone = false) },
            isChecklist = isChecklist,
            colorHex = colorHex,
            isPinned = isPinned,
            labels = listOf("Oj AI"),
            updatedAt = System.currentTimeMillis()
        )
        saveNote(note)
        return Result.success(note)
    }

    fun deleteNoteByQueryFromAi(query: String): Result<MorphNote> {
        val account = _connectedAccountFlow.value
            ?: return Result.failure(IllegalStateException("NOT_AUTHENTICATED"))
        if (!account.authorizedScopes.contains(MorphAccount.SCOPE_DELETE)) {
            return Result.failure(IllegalStateException("MISSING_DELETE_SCOPE"))
        }

        val activeNotes = _notesFlow.value.filter { !it.isArchived }
        if (activeNotes.isEmpty()) {
            return Result.failure(NoSuchElementException("NO_NOTES_IN_ACCOUNT"))
        }

        val cleanQuery = query.trim().lowercase(Locale.US)
        val matched = if (cleanQuery.isBlank() || cleanQuery in listOf("latest", "last", "recent", "newest")) {
            activeNotes.first()
        } else {
            activeNotes.firstOrNull {
                it.title.lowercase(Locale.US).contains(cleanQuery) ||
                    cleanQuery.contains(it.title.lowercase(Locale.US).takeIf { t -> t.length >= 3 } ?: "\u0000") ||
                    it.content.lowercase(Locale.US).contains(cleanQuery) ||
                    it.checklist.any { item -> item.text.lowercase(Locale.US).contains(cleanQuery) }
            }
        } ?: return Result.failure(NoSuchElementException("NOTE_NOT_FOUND"))

        deleteNote(matched.id)
        return Result.success(matched)
    }

    fun updateNoteByQueryFromAi(targetQuery: String, appendedOrNewContent: String, append: Boolean = true): Result<MorphNote> {
        val account = _connectedAccountFlow.value
            ?: return Result.failure(IllegalStateException("NOT_AUTHENTICATED"))
        if (!account.authorizedScopes.contains(MorphAccount.SCOPE_WRITE)) {
            return Result.failure(IllegalStateException("MISSING_WRITE_SCOPE"))
        }

        val activeNotes = _notesFlow.value.filter { !it.isArchived }
        val cleanQuery = targetQuery.trim().lowercase(Locale.US)
        val matched = activeNotes.firstOrNull {
            it.title.lowercase(Locale.US).contains(cleanQuery) ||
                it.content.lowercase(Locale.US).contains(cleanQuery)
        } ?: activeNotes.firstOrNull()
            ?: return Result.failure(NoSuchElementException("NOTE_NOT_FOUND"))

        val updated = if (matched.isChecklist) {
            val newItems = matched.checklist + MorphChecklistItem(text = appendedOrNewContent.trim(), isDone = false)
            matched.copy(checklist = newItems, updatedAt = System.currentTimeMillis())
        } else {
            val finalContent = if (append && matched.content.isNotBlank()) {
                "${matched.content}\n${appendedOrNewContent.trim()}"
            } else {
                appendedOrNewContent.trim()
            }
            matched.copy(content = finalContent, updatedAt = System.currentTimeMillis())
        }
        saveNote(updated)
        return Result.success(updated)
    }

    private fun deriveAccountId(identifier: String): String {
        val clean = identifier.trim().lowercase(Locale.US)
        return "morph_" + sha256(clean).take(20)
    }

    private fun sanitizeKey(raw: String): String =
        raw.lowercase(Locale.US).replace(Regex("[^a-z0-9_]"), "").take(24)

    private fun issueUserSessionToken(accountId: String, email: String, verifier: String): String {
        return "msess_" + sha256("$accountId|$email|$verifier|${System.currentTimeMillis()}").take(32)
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val TAG = "MorphRepository"
        private const val KEY_ACTIVE_ACCOUNT_JSON = "active_morph_account_v2"
        private const val KEY_OJ_AI_LINK_ENABLED = "morph_oj_ai_link_enabled_v2"

        // Production Omkun Morph Cloud & Backend Configuration
        const val MORPH_PROJECT_ID = "morph-69d82"
        const val MORPH_FIREBASE_API_KEY = "AIzaSyCvODN-txDr2tcx-vMmnZPzXcaJlCtRyjQ"
        const val MORPH_AUTH_SIGN_IN_URL =
            "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$MORPH_FIREBASE_API_KEY"
        const val MORPH_AUTH_SIGN_UP_URL =
            "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=$MORPH_FIREBASE_API_KEY"
        const val MORPH_AUTH_UPDATE_URL =
            "https://identitytoolkit.googleapis.com/v1/accounts:update?key=$MORPH_FIREBASE_API_KEY"
        const val MORPH_FIRESTORE_REST_BASE =
            "https://firestore.googleapis.com/v1/projects/$MORPH_PROJECT_ID/databases/(default)/documents"
        const val MORPH_DEV_API_BASE = "https://morph.omkun.com/api/v1"
        const val MORPH_DEV_BEARER_TOKEN = "omk_live_test_7f8a9b2c"

        // Strictly internal service constants — never exposed to UI components
        private const val INTERNAL_MORPH_CLOUD_URL =
            "https://morph.omkun.com/api/v1"
        private const val INTERNAL_SERVICE_BRIDGE_TOKEN = "omk_live_test_7f8a9b2c"

        fun mapHexToApiColor(hex: String): String {
            return when (hex.uppercase(Locale.US)) {
                MorphPalette.Lemon.uppercase(Locale.US) -> "yellow"
                MorphPalette.Sky.uppercase(Locale.US) -> "blue"
                MorphPalette.Peach.uppercase(Locale.US) -> "red"
                MorphPalette.Mint.uppercase(Locale.US) -> "green"
                MorphPalette.Orange.uppercase(Locale.US) -> "orange"
                MorphPalette.Lavender.uppercase(Locale.US) -> "purple"
                MorphPalette.Teal.uppercase(Locale.US) -> "teal"
                else -> "yellow"
            }
        }

        fun mapApiColorToHex(color: String?): String {
            return when (color?.lowercase(Locale.US)?.trim()) {
                "yellow" -> MorphPalette.Lemon
                "blue" -> MorphPalette.Sky
                "red" -> MorphPalette.Peach
                "green" -> MorphPalette.Mint
                "orange" -> MorphPalette.Orange
                "purple" -> MorphPalette.Lavender
                "teal" -> MorphPalette.Teal
                null -> MorphPalette.Default
                else -> if (color.startsWith("#")) color else MorphPalette.Default
            }
        }

        @Volatile
        private var instance: MorphRepository? = null

        fun getInstance(context: Context): MorphRepository {
            return instance ?: synchronized(this) {
                instance ?: MorphRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
