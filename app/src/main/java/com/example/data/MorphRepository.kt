package com.example.data

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.model.MorphAccount
import com.example.model.MorphChecklistItem
import com.example.model.MorphDatabaseInfo
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
import java.net.URLEncoder
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Production Account-Scoped Repository for Omkun Morph (REST API v1.2 + Firestore).
 *
 * Supports:
 * - Scoped Developer API Key (`omk_live_...`) via Cloudflare Edge (`api.omkunmorph.workers.dev/api/v1`)
 *   with automatic fallback to Canonical Production Host (`morph.omkun.com/api/v1`).
 * - Official Endpoints: `/files`, `/files/:id`, `/files/:id/export`, `/docs/:id/blocks`,
 *   `/databases`, `/databases/:id/query`, `/tasks`, `/search`.
 * - Omkun Account (Email/Password), Google Sign-In, and Olinam SSO with strict per-account isolation.
 */
class MorphRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("omkun_morph_auth_v2", Context.MODE_PRIVATE)
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var activeWorkspaceId: String? = null

    private val _connectedAccountFlow = MutableStateFlow<MorphAccount?>(null)
    val connectedAccountFlow: StateFlow<MorphAccount?> = _connectedAccountFlow.asStateFlow()

    private val _notesFlow = MutableStateFlow<List<MorphNote>>(emptyList())
    val notesFlow: StateFlow<List<MorphNote>> = _notesFlow.asStateFlow()

    private val _databasesFlow = MutableStateFlow<List<MorphDatabaseInfo>>(emptyList())
    val databasesFlow: StateFlow<List<MorphDatabaseInfo>> = _databasesFlow.asStateFlow()

    private val _syncStatus = MutableStateFlow("Sign in required")
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastApiError = MutableStateFlow<String?>(null)
    val lastApiError: StateFlow<String?> = _lastApiError.asStateFlow()

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
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

    fun clearApiError() {
        _lastApiError.value = null
    }

    private fun restoreAuthenticatedSession() {
        val accountJson = prefs.getString(KEY_ACTIVE_ACCOUNT_JSON, null)
        if (accountJson.isNullOrBlank()) {
            _connectedAccountFlow.value = null
            _notesFlow.value = emptyList()
            _databasesFlow.value = emptyList()
            _syncStatus.value = "Sign in to sync your Morph workspace"
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
                refreshToken = obj.optString("refreshToken", ""),
                apiKey = obj.optString("apiKey", ""),
                endpointEnvironment = obj.optString("endpointEnvironment", MorphAccount.ENV_EDGE),
                workspaceId = obj.optString("workspaceId", ""),
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
            _databasesFlow.value = emptyList()
            _syncStatus.value = "Sign in to sync your Morph workspace"
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
            put("refreshToken", account.refreshToken)
            put("apiKey", account.apiKey)
            put("endpointEnvironment", account.endpointEnvironment)
            put("workspaceId", account.workspaceId)
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

    // =========================================================================
    // Authentication Methods
    // =========================================================================

    /**
     * Connect directly using an Omkun Morph Scoped Developer Bearer Key (`omk_live_...`)
     * generated at `https://morph.omkun.com/developer-api`.
     * Verifies the key live against Edge (`api.omkunmorph.workers.dev/api/v1`) and Canonical hosts.
     */
    suspend fun connectWithApiKey(
        apiKey: String,
        accountLabel: String = "",
        endpointEnvironment: String = MorphAccount.ENV_EDGE
    ): Result<MorphAccount> = withContext(Dispatchers.IO) {
        val cleanKey = apiKey.trim()
        if (cleanKey.length < 10) {
            return@withContext Result.failure(
                IllegalArgumentException("Please enter a valid Omkun Morph API key (omk_live_...).")
            )
        }

        val tempAccount = MorphAccount(
            accountId = "morph_key_${sha256(cleanKey).take(16)}",
            email = accountLabel.trim().ifBlank { "omk_live_••••${cleanKey.takeLast(4)}" },
            displayName = accountLabel.trim().ifBlank { "Morph Workspace" },
            authProvider = "API_KEY",
            sessionToken = cleanKey,
            apiKey = cleanKey,
            endpointEnvironment = endpointEnvironment
        )

        // Probe /files, /tasks, /databases to validate token & discover granted scopes
        val filesProbe = executeMorphApiRequest(
            account = tempAccount,
            relativePath = "/files?limit=50",
            method = "GET"
        )
        val tasksProbe = executeMorphApiRequest(
            account = tempAccount,
            relativePath = "/tasks?limit=50",
            method = "GET"
        )
        val dbsProbe = executeMorphApiRequest(
            account = tempAccount,
            relativePath = "/databases?limit=20",
            method = "GET"
        )

        val anySuccess = filesProbe.isSuccess || tasksProbe.isSuccess || dbsProbe.isSuccess
        val anyScopeError = listOf(filesProbe, tasksProbe, dbsProbe).any {
            it.httpCode == 403 && it.errorCode == "INSUFFICIENT_SCOPE"
        }

        if (!anySuccess && !anyScopeError) {
            val detail = when {
                filesProbe.httpCode == 401 || tasksProbe.httpCode == 401 ->
                    "Invalid or revoked Morph API key. Please check your key at morph.omkun.com/developer-api."
                filesProbe.errorMessage.isNotBlank() -> filesProbe.errorMessage
                else -> "Could not connect to Omkun Morph API (HTTP ${filesProbe.httpCode})."
            }
            return@withContext Result.failure(IllegalArgumentException(detail))
        }

        val detectedScopes = detectScopesFromProbes(
            apiKey = cleanKey,
            filesProbe = filesProbe,
            tasksProbe = tasksProbe,
            dbsProbe = dbsProbe
        )

        val resolvedAccount = tempAccount.copy(
            authorizedScopes = detectedScopes,
            connectedAt = System.currentTimeMillis()
        )
        _lastApiError.value = null
        activateAuthenticatedAccount(resolvedAccount, skipImmediateSync = true)

        // Immediately hydrate fetched documents, tasks & databases from the probe responses
        val initialNotes = mutableListOf<MorphNote>()
        if (filesProbe.isSuccess) {
            initialNotes.addAll(parseFilesResponse(filesProbe.body, resolvedAccount.accountId))
        }
        if (tasksProbe.isSuccess) {
            initialNotes.addAll(parseTasksResponse(tasksProbe.body, resolvedAccount.accountId))
        }
        if (initialNotes.isNotEmpty()) {
            mergeRemoteNotesForAccount(resolvedAccount, initialNotes)
        }
        if (dbsProbe.isSuccess) {
            _databasesFlow.value = parseDatabasesResponse(dbsProbe.body)
        }

        lastSyncTime = System.currentTimeMillis()
        val activeCount = _notesFlow.value.count { !it.isArchived }
        _syncStatus.value = "Synced • $activeCount items"
        Result.success(resolvedAccount)
    }

    /**
     * Attaches or updates a Developer API Key (`omk_live_...`) on an already authenticated account.
     */
    suspend fun attachApiKeyToCurrentAccount(
        apiKey: String,
        endpointEnvironment: String = MorphAccount.ENV_EDGE
    ): Result<MorphAccount> = withContext(Dispatchers.IO) {
        val current = _connectedAccountFlow.value
            ?: return@withContext connectWithApiKey(apiKey, "", endpointEnvironment)

        val cleanKey = apiKey.trim()
        if (cleanKey.isBlank()) {
            val updated = current.copy(apiKey = "", endpointEnvironment = endpointEnvironment)
            _connectedAccountFlow.value = updated
            persistActiveAccount(updated)
            return@withContext Result.success(updated)
        }

        val probeAcc = current.copy(apiKey = cleanKey, endpointEnvironment = endpointEnvironment)
        val filesProbe = executeMorphApiRequest(probeAcc, "/files?limit=50", "GET")
        val tasksProbe = executeMorphApiRequest(probeAcc, "/tasks?limit=50", "GET")
        val dbsProbe = executeMorphApiRequest(probeAcc, "/databases?limit=20", "GET")

        if (!filesProbe.isSuccess && !tasksProbe.isSuccess && !dbsProbe.isSuccess) {
            val is403Scope = listOf(filesProbe, tasksProbe, dbsProbe).any {
                it.httpCode == 403 && it.errorCode == "INSUFFICIENT_SCOPE"
            }
            if (!is403Scope) {
                val msg = if (filesProbe.httpCode == 401) {
                    "Invalid or revoked Morph API key."
                } else {
                    filesProbe.errorMessage.ifBlank { "Unable to verify Morph API key." }
                }
                return@withContext Result.failure(IllegalArgumentException(msg))
            }
        }

        val detectedScopes = detectScopesFromProbes(cleanKey, filesProbe, tasksProbe, dbsProbe)
        val updated = probeAcc.copy(authorizedScopes = detectedScopes)
        _connectedAccountFlow.value = updated
        persistActiveAccount(updated)
        _lastApiError.value = null

        val fetched = mutableListOf<MorphNote>()
        if (filesProbe.isSuccess) fetched.addAll(parseFilesResponse(filesProbe.body, updated.accountId))
        if (tasksProbe.isSuccess) fetched.addAll(parseTasksResponse(tasksProbe.body, updated.accountId))
        if (fetched.isNotEmpty()) mergeRemoteNotesForAccount(updated, fetched)
        if (dbsProbe.isSuccess) _databasesFlow.value = parseDatabasesResponse(dbsProbe.body)

        lastSyncTime = System.currentTimeMillis()
        _syncStatus.value = "Synced • ${_notesFlow.value.count { !it.isArchived }} items"
        Result.success(updated)
    }

    fun setEndpointEnvironment(env: String) {
        val current = _connectedAccountFlow.value ?: return
        val normalized = if (env == MorphAccount.ENV_CANONICAL) MorphAccount.ENV_CANONICAL else MorphAccount.ENV_EDGE
        val updated = current.copy(endpointEnvironment = normalized)
        _connectedAccountFlow.value = updated
        persistActiveAccount(updated)
    }

    private fun detectScopesFromProbes(
        apiKey: String,
        filesProbe: MorphApiResult,
        tasksProbe: MorphApiResult,
        dbsProbe: MorphApiResult
    ): List<String> {
        // 1. Check explicit scope header if returned by Cloudflare Worker
        val headerScopes = (filesProbe.headerScopes.ifBlank { tasksProbe.headerScopes }).trim()
        if (headerScopes.isNotBlank()) {
            val parsed = headerScopes.split(",", " ").map { it.trim() }.filter { it.isNotBlank() }
            if (parsed.isNotEmpty()) return parsed
        }

        // 2. Public read-only widget keys (`omk_live_publ_...`) are restricted to read scopes
        if (apiKey.startsWith("omk_live_publ_", ignoreCase = true)) {
            return buildList {
                if (filesProbe.isSuccess) add(MorphAccount.SCOPE_READ_DOCS)
                if (dbsProbe.isSuccess) add(MorphAccount.SCOPE_READ_DATABASES)
                if (tasksProbe.isSuccess) add(MorphAccount.SCOPE_READ_TASKS)
                add(MorphAccount.SCOPE_READ)
            }.distinct()
        }

        // 3. Infer granted scopes from probes (for test/full keys default to read+write,
        //    and if any write endpoint later returns INSUFFICIENT_SCOPE we update dynamically)
        val scopes = mutableListOf<String>()
        if (filesProbe.isSuccess) {
            scopes.add(MorphAccount.SCOPE_READ_DOCS)
            scopes.add(MorphAccount.SCOPE_WRITE_DOCS)
            scopes.add(MorphAccount.SCOPE_READ)
            scopes.add(MorphAccount.SCOPE_WRITE)
            scopes.add(MorphAccount.SCOPE_DELETE)
        }
        if (dbsProbe.isSuccess) {
            scopes.add(MorphAccount.SCOPE_READ_DATABASES)
        }
        if (tasksProbe.isSuccess) {
            scopes.add(MorphAccount.SCOPE_READ_TASKS)
            scopes.add(MorphAccount.SCOPE_WRITE_TASKS)
        }
        if (filesProbe.isSuccess && dbsProbe.isSuccess && tasksProbe.isSuccess &&
            !apiKey.contains("read", ignoreCase = true)
        ) {
            scopes.add(MorphAccount.SCOPE_ALL)
        }
        return scopes.distinct().ifEmpty { MorphAccount.DEFAULT_SCOPES }
    }

    /**
     * Sign in with Google via Jetpack Credential Manager (OAuth 2.0 ID Token).
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
     * 1-Tap OAuth Authorization using the user's verified Olinam Account.
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
     * Production Omkun Morph Authentication via official backend (morph-69d82)
     * with optional Scoped Developer API Key (`omk_live_...`) linking.
     */
    suspend fun signInWithOmkunCredentials(
        email: String,
        password: String,
        displayName: String = "",
        providerLabel: String = "OMKUN_ID",
        scopes: List<String> = MorphAccount.DEFAULT_SCOPES,
        optionalApiKey: String = ""
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
                val refreshToken = resJson.optString("refreshToken", "")
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

                val account = MorphAccount(
                    accountId = localId,
                    email = normalizedEmail,
                    displayName = resolvedName,
                    avatarUrl = null,
                    authProvider = providerLabel,
                    sessionToken = idToken,
                    refreshToken = refreshToken,
                    apiKey = optionalApiKey.trim(),
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
        scopes: List<String> = MorphAccount.DEFAULT_SCOPES,
        optionalApiKey: String = ""
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
                val refreshToken = resJson.optString("refreshToken", "")
                val resolvedName = displayName.trim().ifBlank {
                    normalizedEmail.substringBefore("@")
                        .replace(".", " ")
                        .replace("_", " ")
                        .split(" ")
                        .filter { it.isNotBlank() }
                        .joinToString(" ") { part -> part.replaceFirstChar { it.uppercase() } }
                        .ifBlank { "Omkun Morph User" }
                }

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
                    refreshToken = refreshToken,
                    apiKey = optionalApiKey.trim(),
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

    private fun activateAuthenticatedAccount(account: MorphAccount, skipImmediateSync: Boolean = false) {
        _connectedAccountFlow.value = account
        persistActiveAccount(account)
        loadNotesForAccount(account)
        _syncStatus.value = "Connected as ${account.email}"
        if (!skipImmediateSync) {
            syncWithServer()
        }
    }

    fun disconnectAccount() {
        val prevProvider = _connectedAccountFlow.value?.authProvider
        _connectedAccountFlow.value = null
        prefs.edit()
            .remove(KEY_ACTIVE_ACCOUNT_JSON)
            .putBoolean(KEY_OJ_AI_LINK_ENABLED, false)
            .apply()
        _notesFlow.value = emptyList()
        _databasesFlow.value = emptyList()
        _lastApiError.value = null
        _syncStatus.value = "Sign in to sync your Morph workspace"

        if (prevProvider == "GOOGLE") {
            coroutineScope.launch {
                try {
                    CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
                } catch (_: Throwable) {}
            }
        }
    }

    // =========================================================================
    // Sync & Morph REST API v1.2 Operations
    // =========================================================================

    fun syncWithServer(onComplete: ((Boolean, String) -> Unit)? = null) {
        val initialAccount = _connectedAccountFlow.value
        if (initialAccount == null) {
            _syncStatus.value = "Sign in required"
            coroutineScope.launch(Dispatchers.Main) {
                onComplete?.invoke(false, "Please sign in to your Omkun Morph account first.")
            }
            return
        }

        coroutineScope.launch {
            _isSyncing.value = true
            _syncStatus.value = "Syncing..."
            val account = refreshIdTokenIfNeeded(initialAccount)
            var syncedRemote = false

            // 1. Sync via Morph REST API (works with API Key or authenticated session token)
            if (account.apiKey.isNotBlank() || account.sessionToken.isNotBlank()) {
                try {
                    if (account.canReadDocs()) {
                        val (ok, restFiles) = fetchFilesFromMorphRestApi(account)
                        if (ok) syncedRemote = true
                        if (restFiles.isNotEmpty()) {
                            mergeRemoteNotesForAccount(account, restFiles)
                        }
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Morph REST /files sync notice: ${e.message}")
                }

                try {
                    if (account.canReadTasks()) {
                        val (ok, restTasks) = fetchTasksFromMorphRestApi(account)
                        if (ok) syncedRemote = true
                        if (restTasks.isNotEmpty()) {
                            mergeRemoteNotesForAccount(account, restTasks)
                        }
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Morph REST /tasks sync notice: ${e.message}")
                }

                try {
                    if (account.canReadDatabases()) {
                        val (ok, restDbs) = fetchDatabasesFromMorphRestApi(account)
                        if (ok) syncedRemote = true
                        if (restDbs.isNotEmpty()) {
                            _databasesFlow.value = restDbs
                        }
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Morph REST /databases sync notice: ${e.message}")
                }
            }

            // 2. Sync user's Morph Cloud documents & notes from morph-69d82 Firestore
            if (account.authProvider != "API_KEY") {
                try {
                    val (cloudReachable, workspaceDocs) = fetchWorkspaceDocumentsFromMorphCloud(account)
                    if (cloudReachable) syncedRemote = true
                    if (workspaceDocs.isNotEmpty()) {
                        mergeRemoteNotesForAccount(account, workspaceDocs)
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Morph workspace documents sync notice: ${e.message}")
                }

                try {
                    val (notesReachable, cloudNotes) = fetchAccountNotesFromMorphCloud(account)
                    if (notesReachable) syncedRemote = true
                    if (cloudNotes.isNotEmpty()) {
                        mergeRemoteNotesForAccount(account, cloudNotes)
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Morph Cloud REST sync notice: ${e.message}")
                }

                try {
                    val firestoreNotes = fetchAccountNotesFromFirestore(account)
                    if (firestoreNotes.isNotEmpty()) {
                        mergeRemoteNotesForAccount(account, firestoreNotes)
                        syncedRemote = true
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Firestore Morph sync note: ${e.message}")
                }
            }

            lastSyncTime = System.currentTimeMillis()
            _isSyncing.value = false
            val currentCount = _notesFlow.value.count { !it.isArchived }
            _syncStatus.value = if (syncedRemote) {
                "Synced • $currentCount items"
            } else {
                "Connected • $currentCount items"
            }

            withContext(Dispatchers.Main) {
                onComplete?.invoke(
                    true,
                    if (syncedRemote) "Synced $currentCount items" else "Connected • $currentCount items"
                )
            }
        }
    }

    private fun refreshIdTokenIfNeeded(account: MorphAccount): MorphAccount {
        if (account.refreshToken.isBlank() || account.authProvider == "API_KEY") return account
        return try {
            val formBody = JSONObject().apply {
                put("grant_type", "refresh_token")
                put("refresh_token", account.refreshToken)
            }
            val req = Request.Builder()
                .url("https://securetoken.googleapis.com/v1/token?key=$MORPH_FIREBASE_API_KEY")
                .post(formBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()
            client.newCall(req).execute().use { res ->
                val body = res.body?.string().orEmpty()
                if (!res.isSuccessful || body.isBlank()) return account
                val json = JSONObject(body)
                val newIdToken = json.optString("id_token", "")
                val newRefresh = json.optString("refresh_token", account.refreshToken)
                if (newIdToken.isNotBlank()) {
                    val updated = account.copy(sessionToken = newIdToken, refreshToken = newRefresh)
                    if (_connectedAccountFlow.value?.accountId == account.accountId) {
                        _connectedAccountFlow.value = updated
                        persistActiveAccount(updated)
                    }
                    updated
                } else {
                    account
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Morph token refresh notice: ${e.message}")
            account
        }
    }

    /**
     * Fetches full document details (`GET /api/v1/files/:id` with fallback `/files/:id/export`)
     * when opening a note whose blocks/content were summarized in `files.list`.
     */
    suspend fun fetchFileDetailsIfNeeded(noteId: String): MorphNote? = withContext(Dispatchers.IO) {
        val account = _connectedAccountFlow.value ?: return@withContext null
        if ((account.apiKey.isBlank() && account.sessionToken.isBlank()) || !account.canReadDocs()) return@withContext null

        val detailRes = executeMorphApiRequest(account, "/files/${encodePath(noteId)}", "GET")
        if (detailRes.isSuccess && detailRes.body.isNotBlank()) {
            val parsed = parseSingleFileResponse(detailRes.body, account.accountId, noteId)
            if (parsed != null) {
                mergeRemoteNotesForAccount(account, listOf(parsed))
                return@withContext parsed
            }
        }

        val exportRes = executeMorphApiRequest(account, "/files/${encodePath(noteId)}/export", "GET")
        if (exportRes.isSuccess && exportRes.body.isNotBlank()) {
            val exportedText = extractExportedContent(exportRes.body)
            val existing = _notesFlow.value.find { it.id == noteId }
            if (existing != null && exportedText.isNotBlank() && existing.content.isBlank()) {
                val updated = existing.copy(content = exportedText, remoteSynced = true)
                mergeRemoteNotesForAccount(account, listOf(updated))
                return@withContext updated
            }
        }
        null
    }

    /**
     * Exports a Morph document using `GET /api/v1/files/:id/export`.
     */
    suspend fun exportFile(noteId: String): Result<String> = withContext(Dispatchers.IO) {
        val account = _connectedAccountFlow.value
            ?: return@withContext Result.failure(IllegalStateException("NOT_AUTHENTICATED"))
        if (account.apiKey.isBlank()) {
            val local = _notesFlow.value.find { it.id == noteId }
                ?: return@withContext Result.failure(NoSuchElementException("Document not found"))
            return@withContext Result.success(buildString {
                if (local.title.isNotBlank()) append("# ${local.title}\n\n")
                if (local.isChecklist) {
                    local.checklist.forEach { append((if (it.isDone) "- [x] " else "- [ ] ") + "${it.text}\n") }
                } else {
                    append(local.content)
                }
            })
        }

        val res = executeMorphApiRequest(account, "/files/${encodePath(noteId)}/export", "GET")
        if (res.isSuccess) {
            Result.success(extractExportedContent(res.body))
        } else {
            Result.failure(IllegalStateException(res.formattedError()))
        }
    }

    /**
     * Unified workspace search via `GET /api/v1/search?q=...`.
     */
    suspend fun searchMorphWorkspace(query: String): Result<List<MorphNote>> = withContext(Dispatchers.IO) {
        val account = _connectedAccountFlow.value
            ?: return@withContext Result.failure(IllegalStateException("NOT_AUTHENTICATED"))
        val trimmed = query.trim()
        if (trimmed.isBlank()) return@withContext Result.success(_notesFlow.value.filter { !it.isArchived })

        if ((account.apiKey.isNotBlank() || account.sessionToken.isNotBlank()) && account.canReadDocs()) {
            val encoded = URLEncoder.encode(trimmed, "UTF-8")
            val res = executeMorphApiRequest(account, "/search?q=$encoded", "GET")
            if (res.isSuccess) {
                val parsed = parseSearchResponse(res.body, account.accountId)
                if (parsed.isNotEmpty()) {
                    mergeRemoteNotesForAccount(account, parsed)
                }
                return@withContext Result.success(parsed)
            }
        }

        val lower = trimmed.lowercase(Locale.US)
        val localMatches = _notesFlow.value.filter { !it.isArchived }.filter { note ->
            note.title.lowercase(Locale.US).contains(lower) ||
                note.content.lowercase(Locale.US).contains(lower) ||
                note.checklist.any { it.text.lowercase(Locale.US).contains(lower) }
        }
        Result.success(localMatches)
    }

    /**
     * Queries a Morph database via `POST /api/v1/databases/:id/query`.
     */
    suspend fun queryDatabase(databaseId: String, filterJson: JSONObject? = null): Result<String> =
        withContext(Dispatchers.IO) {
            val account = _connectedAccountFlow.value
                ?: return@withContext Result.failure(IllegalStateException("NOT_AUTHENTICATED"))
            if (account.apiKey.isBlank() && account.sessionToken.isBlank()) {
                return@withContext Result.failure(IllegalStateException("Please sign in to your Omkun Morph account first."))
            }
            if (!account.canReadDatabases()) {
                return@withContext Result.failure(IllegalStateException("Permission needed to view workspace databases."))
            }

            val body = filterJson ?: JSONObject().apply { put("limit", 25) }
            val res = executeMorphApiRequest(
                account = account,
                relativePath = "/databases/${encodePath(databaseId)}/query",
                method = "POST",
                jsonBody = body
            )
            if (res.isSuccess) {
                Result.success(res.body)
            } else {
                Result.failure(IllegalStateException(res.formattedError()))
            }
        }

    // =========================================================================
    // HTTP Executor for Morph REST API v1.2 (Edge Primary + Canonical Fallback)
    // =========================================================================

    private data class MorphApiResult(
        val isSuccess: Boolean,
        val httpCode: Int,
        val body: String,
        val errorCode: String = "",
        val errorMessage: String = "",
        val requiredScope: String = "",
        val headerScopes: String = ""
    ) {
        fun formattedError(): String = when {
            errorCode.isNotBlank() && errorMessage.isNotBlank() -> "$errorCode: $errorMessage"
            errorMessage.isNotBlank() -> errorMessage
            else -> "HTTP $httpCode"
        }
    }

    private fun executeMorphApiRequest(
        account: MorphAccount,
        relativePath: String,
        method: String = "GET",
        jsonBody: JSONObject? = null
    ): MorphApiResult {
        val token = account.apiKey.ifBlank { account.sessionToken }
        if (token.isBlank()) {
            return MorphApiResult(
                isSuccess = false,
                httpCode = 401,
                body = "",
                errorCode = "UNAUTHORIZED",
                errorMessage = "Missing API bearer token"
            )
        }

        val normalizedPath = if (relativePath.startsWith("/")) relativePath else "/$relativePath"
        val primaryBase = if (account.endpointEnvironment == MorphAccount.ENV_CANONICAL) {
            MORPH_CANONICAL_API_BASE
        } else {
            MORPH_EDGE_API_BASE
        }
        val fallbackBase = if (primaryBase == MORPH_EDGE_API_BASE) {
            MORPH_CANONICAL_API_BASE
        } else {
            MORPH_EDGE_API_BASE
        }

        val firstAttempt = executeSingleMorphHttpCall(primaryBase + normalizedPath, method, token, jsonBody, account)
        // Fallback to secondary host only on transport failures or 5xx/404 edge routing glitches
        if (!firstAttempt.isSuccess && (firstAttempt.httpCode == 0 || firstAttempt.httpCode >= 500 || firstAttempt.httpCode == 404)) {
            val secondAttempt = executeSingleMorphHttpCall(fallbackBase + normalizedPath, method, token, jsonBody, account)
            if (secondAttempt.isSuccess || secondAttempt.httpCode in 400..499) {
                return secondAttempt
            }
        }
        return firstAttempt
    }

    private fun executeSingleMorphHttpCall(
        fullUrl: String,
        method: String,
        bearerToken: String,
        jsonBody: JSONObject?,
        account: MorphAccount? = null
    ): MorphApiResult {
        return try {
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val builder = Request.Builder()
                .url(fullUrl)
                .addHeader("Authorization", "Bearer $bearerToken")
                .addHeader("Accept", "application/json")
            if (account != null) {
                if (account.accountId.isNotBlank()) builder.addHeader("X-User-Id", account.accountId)
                if (account.email.isNotBlank()) builder.addHeader("X-User-Email", account.email)
                val ws = account.workspaceId.ifBlank { activeWorkspaceId.orEmpty() }
                if (ws.isNotBlank()) builder.addHeader("X-Workspace-Id", ws)
            }

            when (method.uppercase(Locale.US)) {
                "POST" -> builder.post((jsonBody?.toString() ?: "{}").toRequestBody(mediaType))
                "PATCH" -> builder.patch((jsonBody?.toString() ?: "{}").toRequestBody(mediaType))
                "PUT" -> builder.put((jsonBody?.toString() ?: "{}").toRequestBody(mediaType))
                "DELETE" -> builder.delete()
                else -> builder.get()
            }

            client.newCall(builder.build()).execute().use { response ->
                val respBody = response.body?.string().orEmpty()
                val headerScopes = response.header("X-Morph-Scopes")
                    ?: response.header("X-Api-Scopes")
                    ?: response.header("X-Scopes")
                    ?: ""

                if (response.isSuccessful) {
                    return MorphApiResult(
                        isSuccess = true,
                        httpCode = response.code,
                        body = respBody,
                        headerScopes = headerScopes
                    )
                }

                // Parse Standard Morph Error Format:
                // { "error": { "code": "INSUFFICIENT_SCOPE", "message": "Requires scope 'write:docs'.", "requiredScope": "write:docs" } }
                var errCode = ""
                var errMsg = ""
                var reqScope = ""
                try {
                    val root = JSONObject(respBody)
                    val errObj = root.optJSONObject("error")
                    if (errObj != null) {
                        errCode = errObj.optString("code", "")
                        errMsg = errObj.optString("message", "")
                        reqScope = errObj.optString("requiredScope", "")
                    } else {
                        errMsg = root.optString("message", root.optString("error", ""))
                    }
                } catch (_: Exception) {
                    if (respBody.isNotBlank() && !respBody.trimStart().startsWith("<")) {
                        errMsg = respBody.take(180)
                    }
                }

                MorphApiResult(
                    isSuccess = false,
                    httpCode = response.code,
                    body = respBody,
                    errorCode = errCode,
                    errorMessage = errMsg.ifBlank { "HTTP ${response.code}" },
                    requiredScope = reqScope,
                    headerScopes = headerScopes
                )
            }
        } catch (e: Exception) {
            Log.d(TAG, "Morph API call error ($fullUrl): ${e.message}")
            MorphApiResult(
                isSuccess = false,
                httpCode = 0,
                body = "",
                errorCode = "NETWORK_ERROR",
                errorMessage = e.message ?: "Network error"
            )
        }
    }

    // =========================================================================
    // Morph REST API v1.2 Fetchers & Parsers
    // =========================================================================

    private fun fetchFilesFromMorphRestApi(account: MorphAccount): Pair<Boolean, List<MorphNote>> {
        val res = executeMorphApiRequest(account, "/files?limit=50", "GET")
        if (!res.isSuccess) {
            if (account.apiKey.isNotBlank() && res.httpCode == 403 && res.errorCode == "INSUFFICIENT_SCOPE") {
                _lastApiError.value = "Some Morph permissions are restricted for this account."
            }
            return false to emptyList()
        }
        return true to parseFilesResponse(res.body, account.accountId)
    }

    private fun fetchTasksFromMorphRestApi(account: MorphAccount): Pair<Boolean, List<MorphNote>> {
        val res = executeMorphApiRequest(account, "/tasks?limit=50", "GET")
        if (!res.isSuccess) return false to emptyList()
        return true to parseTasksResponse(res.body, account.accountId)
    }

    private fun fetchDatabasesFromMorphRestApi(account: MorphAccount): Pair<Boolean, List<MorphDatabaseInfo>> {
        val res = executeMorphApiRequest(account, "/databases?limit=20", "GET")
        if (!res.isSuccess) return false to emptyList()
        return true to parseDatabasesResponse(res.body)
    }

    private fun parseFilesResponse(body: String, accountId: String): List<MorphNote> {
        val array = extractJsonArray(body, "files", "documents", "docs", "data", "items", "results")
            ?: return emptyList()
        val list = mutableListOf<MorphNote>()
        for (i in 0 until array.length()) {
            val item = array.optJSONObject(i) ?: continue
            parseDocumentJsonObject(item, accountId)?.let { list.add(it) }
        }
        return list
    }

    private fun parseSingleFileResponse(body: String, accountId: String, fallbackId: String): MorphNote? {
        return try {
            val trimmed = body.trim()
            if (!trimmed.startsWith("{")) return null
            val root = JSONObject(trimmed)
            val docObj = root.optJSONObject("file")
                ?: root.optJSONObject("document")
                ?: root.optJSONObject("doc")
                ?: root.optJSONObject("data")
                ?: root
            parseDocumentJsonObject(docObj, accountId, fallbackId)
        } catch (_: Exception) {
            null
        }
    }

    private fun parseDocumentJsonObject(
        item: JSONObject,
        accountId: String,
        fallbackId: String = ""
    ): MorphNote? {
        val isDeleted = item.optBoolean("isDeleted", false)
        if (isDeleted) return null

        val id = item.optString("id")
            .ifBlank { item.optString("_id") }
            .ifBlank { item.optString("docId") }
            .ifBlank { fallbackId }
            .ifBlank { UUID.randomUUID().toString() }

        val title = item.optString("title")
            .ifBlank { item.optString("name") }
            .ifBlank { "Untitled Document" }
        val description = item.optString("description", "")
        val directContent = item.optString("content")
            .ifBlank { item.optString("markdown") }
            .ifBlank { item.optString("body") }
            .ifBlank { item.optString("text") }
            .ifBlank { item.optString("note") }

        val checklistItems = mutableListOf<MorphChecklistItem>()
        val textParts = mutableListOf<String>()
        if (description.isNotBlank() && description != directContent) {
            textParts.add(description)
        }
        if (directContent.isNotBlank()) {
            textParts.add(directContent)
        }

        val blocksArr = item.optJSONArray("blocks")
        if (blocksArr != null) {
            for (b in 0 until blocksArr.length()) {
                val block = blocksArr.optJSONObject(b) ?: continue
                val bType = block.optString("type", "paragraph").lowercase(Locale.US)
                val bText = block.optString("content")
                    .ifBlank { block.optString("text") }
                    .ifBlank { block.optJSONObject("data")?.optString("text").orEmpty() }
                val bChecked = block.optBoolean("checked", block.optBoolean("isDone", false))
                val bId = block.optString("id").ifBlank { UUID.randomUUID().toString() }

                if (bType == "todo" || bType == "checklist" || bType == "task") {
                    if (bText.isNotBlank()) {
                        checklistItems.add(MorphChecklistItem(id = bId, text = bText, isDone = bChecked))
                    }
                } else if (bText.isNotBlank() && !textParts.contains(bText)) {
                    textParts.add(bText)
                }
            }
        }

        val labels = mutableListOf<String>()
        val tagsArr = item.optJSONArray("tags") ?: item.optJSONArray("labels")
        if (tagsArr != null) {
            for (t in 0 until tagsArr.length()) {
                val tagStr = tagsArr.optString(t, "").trim()
                if (tagStr.isNotBlank()) labels.add(tagStr)
            }
        }

        val status = item.optString("status", "")
        val isPinned = item.optBoolean("isPinned", item.optBoolean("pinned", false))
        val colorStr = item.optString("color", item.optString("colorHex", ""))
        val colorHex = if (colorStr.isNotBlank()) mapApiColorToHex(colorStr) else MorphPalette.Default
        val updatedAt = parseTimestamp(item)

        return MorphNote(
            id = id,
            accountId = accountId,
            title = title,
            content = textParts.filter { it.isNotBlank() }.joinToString("\n\n"),
            checklist = checklistItems,
            isChecklist = checklistItems.isNotEmpty(),
            colorHex = colorHex,
            isPinned = isPinned,
            labels = labels.distinct(),
            updatedAt = updatedAt,
            isArchived = false,
            sourceType = MorphNote.SOURCE_DOC,
            status = status,
            remoteSynced = true
        )
    }

    private fun parseTasksResponse(body: String, accountId: String): List<MorphNote> {
        val array = extractJsonArray(body, "tasks", "data", "items", "results") ?: return emptyList()
        val list = mutableListOf<MorphNote>()
        for (i in 0 until array.length()) {
            val item = array.optJSONObject(i) ?: continue
            val rawId = item.optString("id").ifBlank { item.optString("_id") }.ifBlank { UUID.randomUUID().toString() }
            val id = if (rawId.startsWith("task_")) rawId else "task_$rawId"
            val title = item.optString("title").ifBlank { item.optString("name") }.ifBlank { "Morph Task" }
            val desc = item.optString("description").ifBlank { item.optString("notes") }
            val status = item.optString("status", "Todo")
            val isDone = item.optBoolean("completed", false) ||
                status.equals("done", ignoreCase = true) ||
                status.equals("completed", ignoreCase = true)

            val subItems = mutableListOf<MorphChecklistItem>()
            val itemsArr = item.optJSONArray("subtasks") ?: item.optJSONArray("items")
            if (itemsArr != null) {
                for (j in 0 until itemsArr.length()) {
                    val sub = itemsArr.optJSONObject(j)
                    if (sub != null) {
                        val t = sub.optString("title").ifBlank { sub.optString("text") }
                        if (t.isNotBlank()) {
                            subItems.add(
                                MorphChecklistItem(
                                    id = sub.optString("id").ifBlank { UUID.randomUUID().toString() },
                                    text = t,
                                    isDone = sub.optBoolean("done", sub.optBoolean("isDone", false))
                                )
                            )
                        }
                    } else {
                        val t = itemsArr.optString(j)
                        if (t.isNotBlank()) subItems.add(MorphChecklistItem(text = t, isDone = isDone))
                    }
                }
            }
            if (subItems.isEmpty()) {
                subItems.add(MorphChecklistItem(id = "${id}_main", text = desc.ifBlank { title }, isDone = isDone))
            }

            val labels = mutableListOf("Task")
            if (status.isNotBlank()) labels.add(status)

            list.add(
                MorphNote(
                    id = id,
                    accountId = accountId,
                    title = title,
                    content = desc,
                    checklist = subItems,
                    isChecklist = true,
                    colorHex = MorphPalette.Sky,
                    isPinned = item.optBoolean("priorityPinned", false),
                    labels = labels.distinct(),
                    updatedAt = parseTimestamp(item),
                    isArchived = false,
                    sourceType = MorphNote.SOURCE_TASK,
                    status = status,
                    remoteSynced = true
                )
            )
        }
        return list
    }

    private fun parseDatabasesResponse(body: String): List<MorphDatabaseInfo> {
        val array = extractJsonArray(body, "databases", "data", "items", "results") ?: return emptyList()
        val list = mutableListOf<MorphDatabaseInfo>()
        for (i in 0 until array.length()) {
            val item = array.optJSONObject(i) ?: continue
            val id = item.optString("id").ifBlank { continue }
            val title = item.optString("title").ifBlank { item.optString("name", id) }
            val desc = item.optString("description", "")
            val count = item.optInt("recordCount", item.optInt("rowCount", 0))
            list.add(MorphDatabaseInfo(id = id, title = title, description = desc, recordCount = count))
        }
        return list
    }

    private fun parseSearchResponse(body: String, accountId: String): List<MorphNote> {
        val array = extractJsonArray(body, "results", "files", "documents", "data", "items") ?: return emptyList()
        val list = mutableListOf<MorphNote>()
        for (i in 0 until array.length()) {
            val item = array.optJSONObject(i) ?: continue
            val type = item.optString("type", "").lowercase(Locale.US)
            if (type == "task") {
                list.addAll(parseTasksResponse(JSONArray().put(item).toString(), accountId))
            } else {
                parseDocumentJsonObject(item, accountId)?.let { list.add(it) }
            }
        }
        return list
    }

    private fun extractExportedContent(body: String): String {
        val trimmed = body.trim()
        if (!trimmed.startsWith("{")) return trimmed
        return try {
            val root = JSONObject(trimmed)
            root.optString("markdown")
                .ifBlank { root.optString("content") }
                .ifBlank { root.optString("text") }
                .ifBlank { root.optString("export") }
                .ifBlank { root.optJSONObject("data")?.optString("content").orEmpty() }
                .ifBlank { trimmed }
        } catch (_: Exception) {
            trimmed
        }
    }

    private fun extractJsonArray(body: String, vararg keys: String): JSONArray? {
        val trimmed = body.trim()
        if (trimmed.isBlank()) return null
        return try {
            if (trimmed.startsWith("[")) {
                JSONArray(trimmed)
            } else if (trimmed.startsWith("{")) {
                val root = JSONObject(trimmed)
                for (key in keys) {
                    val arr = root.optJSONArray(key)
                    if (arr != null) return arr
                }
                val dataObj = root.optJSONObject("data")
                if (dataObj != null) {
                    for (key in keys) {
                        val arr = dataObj.optJSONArray(key)
                        if (arr != null) return arr
                    }
                }
                null
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun parseTimestamp(obj: JSONObject): Long {
        val numeric = obj.optLong("updatedAtTimestamp", 0L)
            .takeIf { it > 0L }
            ?: obj.optLong("updatedAt", 0L).takeIf { it > 1_000_000_000L }
        if (numeric != null) return numeric

        val iso = obj.optString("updatedAt").ifBlank { obj.optString("createdAt") }
        if (iso.isNotBlank()) {
            iso.toLongOrNull()?.let { return it }
            val patterns = listOf(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd HH:mm:ss"
            )
            for (p in patterns) {
                try {
                    val sdf = SimpleDateFormat(p, Locale.US).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }
                    sdf.parse(iso)?.time?.let { return it }
                } catch (_: Exception) {}
            }
        }
        return System.currentTimeMillis()
    }

    private fun encodePath(segment: String): String =
        URLEncoder.encode(segment, "UTF-8").replace("+", "%20")

    // =========================================================================
    // Morph Cloud Firestore REST Sync (For Omkun ID / Google / SSO Sessions)
    // =========================================================================

    private fun fetchWorkspaceDocumentsFromMorphCloud(account: MorphAccount): Pair<Boolean, List<MorphNote>> {
        return try {
            val normalizedEmail = account.email.trim().lowercase(Locale.US)
            val userWorkspaceIds = mutableSetOf<String>()
            var anyReachable = false

            fun isUserWorkspaceDoc(fields: JSONObject): Boolean {
                val ownerCandidates = listOf("ownerId", "ownerUid", "userId", "uid", "createdBy", "authorId")
                    .mapNotNull { fields.optJSONObject(it)?.optString("stringValue", "")?.takeIf { s -> s.isNotBlank() } }
                if (ownerCandidates.any { it == account.accountId }) return true

                val emailCandidates = listOf("ownerEmail", "email", "owner")
                    .mapNotNull { fields.optJSONObject(it)?.optString("stringValue", "")?.trim()?.lowercase(Locale.US)?.takeIf { s -> s.isNotBlank() } }
                if (normalizedEmail.isNotBlank() && emailCandidates.any { it == normalizedEmail || it == account.accountId.lowercase(Locale.US) }) {
                    return true
                }

                // Check `members` array (map objects or plain strings)
                val membersArr = fields.optJSONObject("members")?.optJSONObject("arrayValue")?.optJSONArray("values")
                if (membersArr != null) {
                    for (m in 0 until membersArr.length()) {
                        val entry = membersArr.optJSONObject(m) ?: continue
                        val strVal = entry.optString("stringValue", "").trim()
                        if (strVal.isNotBlank() && (strVal == account.accountId || strVal.equals(normalizedEmail, ignoreCase = true))) {
                            return true
                        }
                        val mFields = entry.optJSONObject("mapValue")?.optJSONObject("fields") ?: continue
                        val mEmail = mFields.optJSONObject("email")?.optString("stringValue", "").orEmpty().trim().lowercase(Locale.US)
                        val mUid = mFields.optJSONObject("userId")?.optString("stringValue", "")
                            .orEmpty()
                            .ifBlank { mFields.optJSONObject("uid")?.optString("stringValue", "").orEmpty() }
                            .ifBlank { mFields.optJSONObject("id")?.optString("stringValue", "").orEmpty() }
                        if ((normalizedEmail.isNotBlank() && mEmail == normalizedEmail) || (account.accountId.isNotBlank() && mUid == account.accountId)) {
                            return true
                        }
                    }
                }

                // Check `members` as mapValue { "<uid>": ... }
                val membersMap = fields.optJSONObject("members")?.optJSONObject("mapValue")?.optJSONObject("fields")
                if (membersMap != null && account.accountId.isNotBlank() && membersMap.has(account.accountId)) {
                    return true
                }

                // Check `memberIds` / `memberEmails`
                listOf("memberIds", "memberUids", "users", "collaborators").forEach { arrKey ->
                    val arr = fields.optJSONObject(arrKey)?.optJSONObject("arrayValue")?.optJSONArray("values")
                    if (arr != null) {
                        for (m in 0 until arr.length()) {
                            val v = arr.optJSONObject(m)?.optString("stringValue", "").orEmpty().trim()
                            if (v == account.accountId || (normalizedEmail.isNotBlank() && v.equals(normalizedEmail, ignoreCase = true))) {
                                return true
                            }
                        }
                    }
                }
                val memberEmails = fields.optJSONObject("memberEmails")?.optJSONObject("arrayValue")?.optJSONArray("values")
                if (memberEmails != null && normalizedEmail.isNotBlank()) {
                    for (m in 0 until memberEmails.length()) {
                        val v = memberEmails.optJSONObject(m)?.optString("stringValue", "").orEmpty().trim().lowercase(Locale.US)
                        if (v == normalizedEmail) return true
                    }
                }
                return false
            }

            fun collectWorkspaceDocs(docsList: List<JSONObject>) {
                for (wsDoc in docsList) {
                    val wsDocId = wsDoc.optString("name", "").substringAfterLast("/")
                    val fields = wsDoc.optJSONObject("fields") ?: continue
                    val customId = fields.optJSONObject("id")?.optString("stringValue", "").orEmpty()
                    val effectiveId = customId.ifBlank { wsDocId }
                    if (isUserWorkspaceDoc(fields)) {
                        if (effectiveId.isNotBlank()) userWorkspaceIds.add(effectiveId)
                        if (wsDocId.isNotBlank()) userWorkspaceIds.add(wsDocId)
                    }
                }
            }

            // 1A. Structured queries on workspaces (required when Firestore rules restrict list queries)
            val wsQueries = buildList {
                if (account.accountId.isNotBlank()) {
                    add(Triple("ownerId", "EQUAL", JSONObject().apply { put("stringValue", account.accountId) }))
                    add(Triple("userId", "EQUAL", JSONObject().apply { put("stringValue", account.accountId) }))
                    add(Triple("uid", "EQUAL", JSONObject().apply { put("stringValue", account.accountId) }))
                    add(Triple("createdBy", "EQUAL", JSONObject().apply { put("stringValue", account.accountId) }))
                    add(Triple("memberIds", "ARRAY_CONTAINS", JSONObject().apply { put("stringValue", account.accountId) }))
                    add(Triple("members", "ARRAY_CONTAINS", JSONObject().apply { put("stringValue", account.accountId) }))
                }
                if (normalizedEmail.isNotBlank()) {
                    add(Triple("ownerEmail", "EQUAL", JSONObject().apply { put("stringValue", normalizedEmail) }))
                    add(Triple("memberEmails", "ARRAY_CONTAINS", JSONObject().apply { put("stringValue", normalizedEmail) }))
                }
            }
            for ((field, op, valueJson) in wsQueries) {
                val (ok, wsDocs) = runFirestoreStructuredQuery(account.sessionToken, "", "workspaces", field, op, valueJson)
                if (ok) anyReachable = true
                if (wsDocs.isNotEmpty()) collectWorkspaceDocs(wsDocs)
            }

            // 1B. Fallback unfiltered workspaces GET (if rules permit)
            val (wsGetOk, allWsDocs) = getFirestoreCollection(account.sessionToken, "workspaces", maxPages = 2)
            if (wsGetOk) {
                anyReachable = true
                collectWorkspaceDocs(allWsDocs)
            }

            if (account.accountId.isNotBlank()) {
                userWorkspaceIds.add("ws-${account.accountId.take(10)}")
                userWorkspaceIds.add("ws-user-${account.accountId.take(12)}")
            }
            if (account.workspaceId.isNotBlank()) {
                userWorkspaceIds.add(account.workspaceId)
            }
            activeWorkspaceId = userWorkspaceIds.firstOrNull { !it.startsWith("ws-user-") } ?: userWorkspaceIds.firstOrNull()

            // 2. Collect raw document/task/note JSON objects from all user-accessible Firestore paths
            val rawDocsById = linkedMapOf<String, Pair<JSONObject, String>>() // id -> (docJson, defaultSourceType)

            fun addMatchingDoc(docObj: JSONObject, sourceHint: String, requireOwnershipOrWsMatch: Boolean) {
                val docId = docObj.optString("name", "").substringAfterLast("/")
                val fields = docObj.optJSONObject("fields") ?: return
                val isDeleted = fields.optJSONObject("isDeleted")?.optBoolean("booleanValue", false) ?: false
                if (isDeleted) return

                if (requireOwnershipOrWsMatch) {
                    val docWsId = fields.optJSONObject("workspaceId")?.optString("stringValue", "").orEmpty()
                    val ownerId = fields.optJSONObject("ownerId")?.optString("stringValue", "").orEmpty()
                    val userId = fields.optJSONObject("userId")?.optString("stringValue", "").orEmpty()
                    val uid = fields.optJSONObject("uid")?.optString("stringValue", "").orEmpty()
                    val createdBy = fields.optJSONObject("createdBy")?.optString("stringValue", "").orEmpty()
                    val authorId = fields.optJSONObject("authorId")?.optString("stringValue", "").orEmpty()
                    val ownerEmail = fields.optJSONObject("ownerEmail")?.optString("stringValue", "").orEmpty().trim().lowercase(Locale.US)
                    val emailField = fields.optJSONObject("email")?.optString("stringValue", "").orEmpty().trim().lowercase(Locale.US)

                    val ownedByUser = (account.accountId.isNotBlank() &&
                        (ownerId == account.accountId || userId == account.accountId || uid == account.accountId ||
                            createdBy == account.accountId || authorId == account.accountId)) ||
                        (normalizedEmail.isNotBlank() && (ownerEmail == normalizedEmail || emailField == normalizedEmail))

                    val inUserWorkspace = docWsId.isNotBlank() && userWorkspaceIds.contains(docWsId)
                    if (!ownedByUser && !inUserWorkspace) return
                }

                val key = docId.ifBlank { fields.optJSONObject("id")?.optString("stringValue", "").orEmpty() }
                if (key.isNotBlank() && !rawDocsById.containsKey(key)) {
                    rawDocsById[key] = docObj to sourceHint
                }
            }

            // 2A. Structured queries on top-level `documents`, `files`, `notes`, `tasks`
            val docCollections = listOf(
                "documents" to MorphNote.SOURCE_DOC,
                "files" to MorphNote.SOURCE_DOC,
                "notes" to MorphNote.SOURCE_NOTE,
                "tasks" to MorphNote.SOURCE_TASK
            )
            for ((colId, srcType) in docCollections) {
                if (account.accountId.isNotBlank()) {
                    for (ownerField in listOf("ownerId", "userId", "uid", "createdBy", "authorId")) {
                        val (ok, list) = runFirestoreStructuredQuery(
                            account.sessionToken, "", colId, ownerField, "EQUAL",
                            JSONObject().apply { put("stringValue", account.accountId) }
                        )
                        if (ok) anyReachable = true
                        list.forEach { addMatchingDoc(it, srcType, requireOwnershipOrWsMatch = false) }
                    }
                }
                if (normalizedEmail.isNotBlank()) {
                    for (emailField in listOf("ownerEmail", "email")) {
                        val (ok, list) = runFirestoreStructuredQuery(
                            account.sessionToken, "", colId, emailField, "EQUAL",
                            JSONObject().apply { put("stringValue", normalizedEmail) }
                        )
                        if (ok) anyReachable = true
                        list.forEach { addMatchingDoc(it, srcType, requireOwnershipOrWsMatch = false) }
                    }
                }
                for (wsId in userWorkspaceIds) {
                    val (ok, list) = runFirestoreStructuredQuery(
                        account.sessionToken, "", colId, "workspaceId", "EQUAL",
                        JSONObject().apply { put("stringValue", wsId) }
                    )
                    if (ok) anyReachable = true
                    list.forEach { addMatchingDoc(it, srcType, requireOwnershipOrWsMatch = false) }
                }
            }

            // 2B. Workspace subcollections `/workspaces/{wsId}/{documents,files,notes,tasks}`
            for (wsId in userWorkspaceIds) {
                for ((subCol, srcType) in docCollections) {
                    val (ok, list) = getFirestoreCollection(account.sessionToken, "workspaces/$wsId/$subCol", maxPages = 2)
                    if (ok) anyReachable = true
                    list.forEach { addMatchingDoc(it, srcType, requireOwnershipOrWsMatch = false) }
                }
            }

            // 2C. User subcollections `/users/{uid}/{documents,files,notes,tasks}`
            if (account.accountId.isNotBlank()) {
                for ((subCol, srcType) in docCollections) {
                    val (ok, list) = getFirestoreCollection(account.sessionToken, "users/${account.accountId}/$subCol", maxPages = 2)
                    if (ok) anyReachable = true
                    list.forEach { addMatchingDoc(it, srcType, requireOwnershipOrWsMatch = false) }
                }
            }

            // 2D. Fallback paginated GET on top-level `documents` (filtered strictly to user/workspace)
            val (docsGetOk, fallbackDocs) = getFirestoreCollection(account.sessionToken, "documents", maxPages = 2)
            if (docsGetOk) {
                anyReachable = true
                fallbackDocs.forEach { addMatchingDoc(it, MorphNote.SOURCE_DOC, requireOwnershipOrWsMatch = true) }
            }

            val parsed = rawDocsById.values.mapNotNull { (docObj, srcHint) ->
                parseFirestoreDocumentToMorphNote(docObj, account.accountId, srcHint)
            }
            anyReachable to parsed
        } catch (e: Exception) {
            Log.d(TAG, "fetchWorkspaceDocumentsFromMorphCloud notice: ${e.message}")
            false to emptyList()
        }
    }

    private fun runFirestoreStructuredQuery(
        sessionToken: String,
        parentRelativePath: String,
        collectionId: String,
        fieldPath: String,
        op: String,
        valueJson: JSONObject
    ): Pair<Boolean, List<JSONObject>> {
        return try {
            val base = MORPH_FIRESTORE_REST_BASE.removeSuffix("/documents")
            val parent = if (parentRelativePath.isBlank()) {
                "$base/documents"
            } else {
                "$base/documents/${parentRelativePath.trimStart('/')}"
            }
            val queryJson = JSONObject().apply {
                put("structuredQuery", JSONObject().apply {
                    put("from", JSONArray().put(JSONObject().apply { put("collectionId", collectionId) }))
                    put("where", JSONObject().apply {
                        put("fieldFilter", JSONObject().apply {
                            put("field", JSONObject().apply { put("fieldPath", fieldPath) })
                            put("op", op)
                            put("value", valueJson)
                        })
                    })
                    put("limit", 200)
                })
            }
            val req = Request.Builder()
                .url("$parent:runQuery")
                .post(queryJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            if (sessionToken.isNotBlank()) {
                req.addHeader("Authorization", "Bearer $sessionToken")
            }
            client.newCall(req.build()).execute().use { res ->
                if (!res.isSuccessful) return false to emptyList()
                val body = res.body?.string().orEmpty()
                if (body.isBlank()) return true to emptyList()
                val arr = JSONArray(body)
                val docs = mutableListOf<JSONObject>()
                for (i in 0 until arr.length()) {
                    val doc = arr.optJSONObject(i)?.optJSONObject("document")
                    if (doc != null) docs.add(doc)
                }
                true to docs
            }
        } catch (e: Exception) {
            false to emptyList()
        }
    }

    private fun getFirestoreCollection(
        sessionToken: String,
        collectionPath: String,
        maxPages: Int = 3
    ): Pair<Boolean, List<JSONObject>> {
        return try {
            val allDocs = mutableListOf<JSONObject>()
            var pageToken = ""
            var anyOk = false
            for (page in 0 until maxPages) {
                val url = buildString {
                    append("$MORPH_FIRESTORE_REST_BASE/${collectionPath.trimStart('/')}?pageSize=200")
                    if (pageToken.isNotBlank()) {
                        append("&pageToken=${URLEncoder.encode(pageToken, "UTF-8")}")
                    }
                }
                val req = Request.Builder().url(url).get()
                if (sessionToken.isNotBlank()) {
                    req.addHeader("Authorization", "Bearer $sessionToken")
                }
                val (ok, nextToken) = client.newCall(req.build()).execute().use { res ->
                    if (!res.isSuccessful) return@use false to ""
                    val body = res.body?.string().orEmpty()
                    if (body.isBlank()) return@use true to ""
                    val root = JSONObject(body)
                    val arr = root.optJSONArray("documents")
                    if (arr != null) {
                        for (i in 0 until arr.length()) {
                            arr.optJSONObject(i)?.let { allDocs.add(it) }
                        }
                    }
                    true to root.optString("nextPageToken", "")
                }
                if (!ok) break
                anyOk = true
                if (nextToken.isBlank()) break
                pageToken = nextToken
            }
            anyOk to allDocs
        } catch (e: Exception) {
            false to emptyList()
        }
    }

    private fun parseFirestoreDocumentToMorphNote(
        docObj: JSONObject,
        accountId: String,
        defaultSourceType: String
    ): MorphNote? {
        val docName = docObj.optString("name", "")
        val docId = docName.substringAfterLast("/")
        val fields = docObj.optJSONObject("fields") ?: return null
        val isDeleted = fields.optJSONObject("isDeleted")?.optBoolean("booleanValue", false) ?: false
        if (isDeleted) return null

        val rawTitle = fields.optJSONObject("title")?.optString("stringValue", "").orEmpty()
            .ifBlank { fields.optJSONObject("name")?.optString("stringValue", "").orEmpty() }
            .ifBlank { "Untitled Page" }
        val description = fields.optJSONObject("description")?.optString("stringValue", "").orEmpty()
        val rawContent = fields.optJSONObject("content")?.optString("stringValue", "").orEmpty()
            .ifBlank { fields.optJSONObject("body")?.optString("stringValue", "").orEmpty() }
            .ifBlank { fields.optJSONObject("markdown")?.optString("stringValue", "").orEmpty() }

        val checklistItems = mutableListOf<MorphChecklistItem>()
        val textParts = mutableListOf<String>()
        if (description.isNotBlank() && description != rawContent) textParts.add(description)

        val blocksArr = fields.optJSONObject("blocks")?.optJSONObject("arrayValue")?.optJSONArray("values")
        if (blocksArr != null) {
            for (b in 0 until blocksArr.length()) {
                val bFields = blocksArr.optJSONObject(b)?.optJSONObject("mapValue")?.optJSONObject("fields") ?: continue
                val bType = bFields.optJSONObject("type")?.optString("stringValue", "").orEmpty().lowercase(Locale.US)
                val bContent = bFields.optJSONObject("content")?.optString("stringValue", "").orEmpty()
                    .ifBlank { bFields.optJSONObject("text")?.optString("stringValue", "").orEmpty() }
                val bChecked = bFields.optJSONObject("checked")?.optBoolean("booleanValue", false)
                    ?: bFields.optJSONObject("isDone")?.optBoolean("booleanValue", false)
                    ?: false
                val bId = bFields.optJSONObject("id")?.optString("stringValue", "").orEmpty()

                if (bType == "todo" || bType == "checklist" || bType == "task") {
                    if (bContent.isNotBlank()) {
                        checklistItems.add(
                            MorphChecklistItem(
                                id = bId.ifBlank { UUID.randomUUID().toString() },
                                text = bContent,
                                isDone = bChecked
                            )
                        )
                    }
                } else if (bContent.isNotBlank() && !textParts.contains(bContent)) {
                    textParts.add(bContent)
                }
            }
        }

        // Also parse direct checklist array if stored on doc/note
        val directChecklist = fields.optJSONObject("checklist")?.optJSONObject("arrayValue")?.optJSONArray("values")
        if (directChecklist != null && checklistItems.isEmpty()) {
            for (c in 0 until directChecklist.length()) {
                val cFields = directChecklist.optJSONObject(c)?.optJSONObject("mapValue")?.optJSONObject("fields") ?: continue
                val cText = cFields.optJSONObject("text")?.optString("stringValue", "").orEmpty()
                val cDone = cFields.optJSONObject("isDone")?.optBoolean("booleanValue", false) ?: false
                val cId = cFields.optJSONObject("id")?.optString("stringValue", "").orEmpty()
                if (cText.isNotBlank()) {
                    checklistItems.add(MorphChecklistItem(id = cId.ifBlank { UUID.randomUUID().toString() }, text = cText, isDone = cDone))
                }
            }
        }

        val content = textParts.filter { it.isNotBlank() }.joinToString("\n\n").ifBlank { rawContent }
        val labels = mutableListOf<String>()
        val tagsArr = (fields.optJSONObject("tags") ?: fields.optJSONObject("labels"))
            ?.optJSONObject("arrayValue")?.optJSONArray("values")
        if (tagsArr != null) {
            for (t in 0 until tagsArr.length()) {
                val tagStr = tagsArr.optJSONObject(t)?.optString("stringValue", "").orEmpty()
                if (tagStr.isNotBlank()) labels.add(tagStr)
            }
        }

        val tsStr = fields.optJSONObject("updatedAtTimestamp")?.optString("integerValue", "")
            ?.takeIf { it.isNotBlank() }
            ?: fields.optJSONObject("updatedAt")?.optString("integerValue", "")
        val updatedAt = tsStr?.toLongOrNull() ?: System.currentTimeMillis()
        val isPinned = fields.optJSONObject("isPinned")?.optBoolean("booleanValue", false)
            ?: fields.optJSONObject("pinned")?.optBoolean("booleanValue", false)
            ?: false
        val colorStr = fields.optJSONObject("colorHex")?.optString("stringValue", "")
            ?.takeIf { it.isNotBlank() }
            ?: fields.optJSONObject("color")?.optString("stringValue", "")
        val colorHex = if (!colorStr.isNullOrBlank()) mapApiColorToHex(colorStr) else MorphPalette.Default
        val status = fields.optJSONObject("status")?.optString("stringValue", "").orEmpty()
        val typeField = fields.optJSONObject("sourceType")?.optString("stringValue", "").orEmpty()
            .ifBlank { defaultSourceType }

        val finalId = if (typeField == MorphNote.SOURCE_TASK && !docId.startsWith("task_")) {
            "task_$docId"
        } else {
            docId
        }

        return MorphNote(
            id = finalId,
            accountId = accountId,
            title = rawTitle,
            content = content,
            checklist = checklistItems,
            isChecklist = checklistItems.isNotEmpty() || (fields.optJSONObject("isChecklist")?.optBoolean("booleanValue", false) == true),
            colorHex = colorHex,
            isPinned = isPinned,
            labels = labels.distinct(),
            updatedAt = updatedAt,
            isArchived = fields.optJSONObject("isArchived")?.optBoolean("booleanValue", false) ?: false,
            sourceType = typeField,
            status = status,
            remoteSynced = true
        )
    }

    private fun fetchAccountNotesFromMorphCloud(account: MorphAccount): Pair<Boolean, List<MorphNote>> {
        return try {
            val (ok, docs) = getFirestoreCollection(account.sessionToken, "users/${account.accountId}/morph_notes", maxPages = 2)
            if (!ok) return false to emptyList()
            val result = docs.mapNotNull { docObj ->
                parseFirestoreDocumentToMorphNote(docObj, account.accountId, MorphNote.SOURCE_NOTE)
            }
            true to result
        } catch (e: Exception) {
            Log.d(TAG, "fetchAccountNotesFromMorphCloud notice: ${e.message}")
            false to emptyList()
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
                    isArchived = isArchived,
                    sourceType = MorphNote.SOURCE_NOTE,
                    remoteSynced = true
                )
            )
        }
        return result
    }

    private fun pushNoteToFirestore(account: MorphAccount, note: MorphNote) {
        if (account.authProvider == "API_KEY") return

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

        coroutineScope.launch {
            try {
                val freshAcc = refreshIdTokenIfNeeded(account)
                val targetWs = activeWorkspaceId
                    ?: freshAcc.workspaceId.takeIf { it.isNotBlank() }
                    ?: "ws-user-${freshAcc.accountId.take(12)}"

                val checklistArr = JSONArray()
                note.checklist.forEach { item ->
                    checklistArr.put(JSONObject().apply {
                        put("mapValue", JSONObject().apply {
                            put("fields", JSONObject().apply {
                                put("id", JSONObject().apply { put("stringValue", item.id) })
                                put("text", JSONObject().apply { put("stringValue", item.text) })
                                put("isDone", JSONObject().apply { put("booleanValue", item.isDone) })
                            })
                        })
                    })
                }

                val labelsArr = JSONArray()
                note.labels.forEach { lbl ->
                    labelsArr.put(JSONObject().apply { put("stringValue", lbl) })
                }

                val docPayload = JSONObject().apply {
                    val fields = JSONObject().apply {
                        put("id", JSONObject().apply { put("stringValue", note.id) })
                        put("title", JSONObject().apply { put("stringValue", note.title.ifBlank { "Untitled Note" }) })
                        put("workspaceId", JSONObject().apply { put("stringValue", targetWs) })
                        put("ownerId", JSONObject().apply { put("stringValue", freshAcc.accountId) })
                        put("userId", JSONObject().apply { put("stringValue", freshAcc.accountId) })
                        put("uid", JSONObject().apply { put("stringValue", freshAcc.accountId) })
                        put("ownerEmail", JSONObject().apply { put("stringValue", freshAcc.email.trim().lowercase(Locale.US)) })
                        put("description", JSONObject().apply { put("stringValue", note.content.take(180)) })
                        put("content", JSONObject().apply { put("stringValue", note.content) })
                        put("status", JSONObject().apply { put("stringValue", note.status.ifBlank { "In Progress" }) })
                        put("sourceType", JSONObject().apply { put("stringValue", note.sourceType) })
                        put("isDeleted", JSONObject().apply { put("booleanValue", false) })
                        put("isPinned", JSONObject().apply { put("booleanValue", note.isPinned) })
                        put("isChecklist", JSONObject().apply { put("booleanValue", note.isChecklist) })
                        put("colorHex", JSONObject().apply { put("stringValue", note.colorHex) })
                        put("updatedAt", JSONObject().apply { put("integerValue", note.updatedAt.toString()) })
                        put("updatedAtTimestamp", JSONObject().apply { put("integerValue", note.updatedAt.toString()) })
                        put("owner", JSONObject().apply { put("stringValue", freshAcc.displayName) })
                        put("icon", JSONObject().apply { put("stringValue", if (note.isChecklist) "☑️" else "📄") })
                        put("checklist", JSONObject().apply { put("arrayValue", JSONObject().apply { put("values", checklistArr) }) })

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
                if (freshAcc.sessionToken.isNotBlank()) {
                    req1.addHeader("Authorization", "Bearer ${freshAcc.sessionToken}")
                }
                client.newCall(req1.build()).execute().close()

                val userNotePayload = JSONObject().apply {
                    val fields = JSONObject().apply {
                        put("id", JSONObject().apply { put("stringValue", note.id) })
                        put("ownerId", JSONObject().apply { put("stringValue", freshAcc.accountId) })
                        put("title", JSONObject().apply { put("stringValue", note.title) })
                        put("content", JSONObject().apply { put("stringValue", note.content) })
                        put("checklist", JSONObject().apply { put("arrayValue", JSONObject().apply { put("values", checklistArr) }) })
                        put("labels", JSONObject().apply { put("arrayValue", JSONObject().apply { put("values", labelsArr) }) })
                        put("isChecklist", JSONObject().apply { put("booleanValue", note.isChecklist) })
                        put("colorHex", JSONObject().apply { put("stringValue", note.colorHex) })
                        put("isPinned", JSONObject().apply { put("booleanValue", note.isPinned) })
                        put("isArchived", JSONObject().apply { put("booleanValue", note.isArchived) })
                        put("sourceType", JSONObject().apply { put("stringValue", note.sourceType) })
                        put("status", JSONObject().apply { put("stringValue", note.status) })
                        put("updatedAt", JSONObject().apply { put("integerValue", note.updatedAt.toString()) })
                    }
                    put("fields", fields)
                }

                val req2 = Request.Builder()
                    .url("$MORPH_FIRESTORE_REST_BASE/users/${freshAcc.accountId}/morph_notes/${note.id}")
                    .patch(userNotePayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                if (freshAcc.sessionToken.isNotBlank()) {
                    req2.addHeader("Authorization", "Bearer ${freshAcc.sessionToken}")
                }
                client.newCall(req2.build()).execute().close()
            } catch (e: Exception) {
                Log.d(TAG, "Morph Cloud pushNote notice: ${e.message}")
            }
        }
    }

    /**
     * Pushes creates and appends to the official Morph REST API v1.2 endpoints:
     * - `POST /api/v1/files` (`files.create`)
     * - `POST /api/v1/docs/:id/blocks` (`blocks.append`)
     * - `POST /api/v1/tasks` (`tasks.create`)
     */
    private fun pushNoteToMorphRestApi(account: MorphAccount, note: MorphNote, isNew: Boolean) {
        if (account.apiKey.isBlank() && account.sessionToken.isBlank()) return

        coroutineScope.launch {
            try {
                if (note.sourceType == MorphNote.SOURCE_TASK && isNew) {
                    val taskJson = JSONObject().apply {
                        put("title", note.title.ifBlank { "Untitled Task" })
                        put(
                            "description",
                            note.content.ifBlank {
                                note.checklist.joinToString("\n") { it.text }
                            }
                        )
                        put("status", note.status.ifBlank { "Todo" })
                    }
                    val res = executeMorphApiRequest(account, "/tasks", "POST", taskJson)
                    handleWriteApiResponse(account, note, res)
                    return@launch
                }

                val blocksArray = buildBlocksJsonArray(note)

                if (isNew || !note.remoteSynced) {
                    val createJson = JSONObject().apply {
                        put("title", note.title.ifBlank { "Untitled Document" })
                        put(
                            "content",
                            if (note.isChecklist) {
                                note.checklist.joinToString("\n") { (if (it.isDone) "[x] " else "[ ] ") + it.text }
                            } else {
                                note.content
                            }
                        )
                        put("description", note.content.take(180))
                        put("pinned", note.isPinned)
                        put("color", mapHexToApiColor(note.colorHex))
                        if (note.labels.isNotEmpty()) {
                            put("tags", JSONArray(note.labels))
                        }
                        if (blocksArray.length() > 0) {
                            put("blocks", blocksArray)
                        }
                    }
                    val res = executeMorphApiRequest(account, "/files", "POST", createJson)
                    handleWriteApiResponse(account, note, res)
                } else {
                    val appendJson = JSONObject().apply {
                        put("blocks", blocksArray)
                    }
                    val res = executeMorphApiRequest(
                        account = account,
                        relativePath = "/docs/${encodePath(note.id)}/blocks",
                        method = "POST",
                        jsonBody = appendJson
                    )
                    handleWriteApiResponse(account, note, res)
                }
            } catch (e: Exception) {
                Log.d(TAG, "Morph REST API v1 push notice: ${e.message}")
            }
        }
    }

    private fun buildBlocksJsonArray(note: MorphNote): JSONArray {
        val blocksArray = JSONArray()
        if (note.content.isNotBlank()) {
            blocksArray.put(JSONObject().apply {
                put("type", "paragraph")
                put("content", note.content)
            })
        }
        note.checklist.forEach { item ->
            blocksArray.put(JSONObject().apply {
                put("type", "todo")
                put("content", item.text)
                put("checked", item.isDone)
            })
        }
        return blocksArray
    }

    private fun handleWriteApiResponse(account: MorphAccount, localNote: MorphNote, res: MorphApiResult) {
        if (res.isSuccess) {
            _lastApiError.value = null
            // Capture canonical server ID if created via POST /files or POST /tasks
            val remoteId = try {
                val root = JSONObject(res.body)
                root.optString("id")
                    .ifBlank { root.optJSONObject("file")?.optString("id").orEmpty() }
                    .ifBlank { root.optJSONObject("document")?.optString("id").orEmpty() }
                    .ifBlank { root.optJSONObject("task")?.optString("id").orEmpty() }
                    .ifBlank { root.optJSONObject("data")?.optString("id").orEmpty() }
            } catch (_: Exception) {
                ""
            }

            val finalId = if (remoteId.isNotBlank() && localNote.sourceType == MorphNote.SOURCE_TASK && !remoteId.startsWith("task_")) {
                "task_$remoteId"
            } else {
                remoteId.ifBlank { localNote.id }
            }

            val updatedList = _notesFlow.value.map {
                if (it.id == localNote.id) {
                    it.copy(id = finalId, remoteSynced = true)
                } else {
                    it
                }
            }
            if (_connectedAccountFlow.value?.accountId == account.accountId) {
                _notesFlow.value = updatedList
                saveToDiskForAccount(account.accountId, updatedList)
            }
        } else if (account.apiKey.isNotBlank() && res.httpCode == 403 && res.errorCode == "INSUFFICIENT_SCOPE") {
            _lastApiError.value = "Some Morph permissions are restricted for this account."
            // Narrow authorized scopes dynamically if server rejected with INSUFFICIENT_SCOPE
            if (res.requiredScope.isNotBlank() && _connectedAccountFlow.value?.accountId == account.accountId) {
                val updatedScopes = account.authorizedScopes
                    .filterNot { it == res.requiredScope || it == MorphAccount.SCOPE_ALL }
                    .filterNot { res.requiredScope == MorphAccount.SCOPE_WRITE_DOCS && it == MorphAccount.SCOPE_WRITE }
                val updatedAcc = account.copy(authorizedScopes = updatedScopes)
                _connectedAccountFlow.value = updatedAcc
                persistActiveAccount(updatedAcc)
            }
        }
    }

    private fun deleteNoteFromFirestore(account: MorphAccount, noteId: String) {
        try {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(account.accountId)
                .collection("morph_notes")
                .document(noteId)
                .delete()
        } catch (_: Throwable) {}

        coroutineScope.launch {
            try {
                val freshAcc = refreshIdTokenIfNeeded(account)
                if (freshAcc.authProvider != "API_KEY") {
                    val req1 = Request.Builder()
                        .url("$MORPH_FIRESTORE_REST_BASE/documents/$noteId")
                        .delete()
                    if (freshAcc.sessionToken.isNotBlank()) {
                        req1.addHeader("Authorization", "Bearer ${freshAcc.sessionToken}")
                    }
                    client.newCall(req1.build()).execute().close()

                    val req2 = Request.Builder()
                        .url("$MORPH_FIRESTORE_REST_BASE/users/${freshAcc.accountId}/morph_notes/$noteId")
                        .delete()
                    if (freshAcc.sessionToken.isNotBlank()) {
                        req2.addHeader("Authorization", "Bearer ${freshAcc.sessionToken}")
                    }
                    client.newCall(req2.build()).execute().close()
                }
                if (freshAcc.apiKey.isNotBlank() || freshAcc.sessionToken.isNotBlank()) {
                    val path = if (noteId.startsWith("task_")) {
                        "/tasks/${encodePath(noteId.removePrefix("task_"))}"
                    } else {
                        "/files/${encodePath(noteId)}"
                    }
                    executeMorphApiRequest(freshAcc, path, "DELETE")
                }
            } catch (e: Exception) {
                Log.d(TAG, "Morph Cloud deleteNote notice: ${e.message}")
            }
        }
    }

    private fun mergeRemoteNotesForAccount(account: MorphAccount, remoteNotes: List<MorphNote>) {
        val scopedRemote = remoteNotes.filter {
            it.accountId.isBlank() || it.accountId == account.accountId
        }.map { it.copy(accountId = account.accountId) }

        if (scopedRemote.isEmpty()) return

        val currentList = _notesFlow.value
        val remoteById = scopedRemote.associateBy { it.id }
        val merged = mutableListOf<MorphNote>()

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
                        isArchived = obj.optBoolean("isArchived"),
                        sourceType = obj.optString("sourceType", MorphNote.SOURCE_DOC),
                        status = obj.optString("status", ""),
                        remoteSynced = obj.optBoolean("remoteSynced", false)
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
                    put("sourceType", note.sourceType)
                    put("status", note.status)
                    put("remoteSynced", note.remoteSynced)

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
     * Creates or updates a document/task/note strictly inside the currently authenticated user's Morph account.
     */
    fun saveNote(note: MorphNote): Boolean {
        val account = _connectedAccountFlow.value ?: return false
        val current = _notesFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == note.id }
        val isNew = (index < 0)
        val existingRemoteSynced = if (!isNew) current[index].remoteSynced else note.remoteSynced

        val updatedNote = note.copy(
            accountId = account.accountId,
            updatedAt = System.currentTimeMillis(),
            remoteSynced = existingRemoteSynced
        )

        if (!isNew) {
            current[index] = updatedNote
        } else {
            current.add(0, updatedNote)
        }
        _notesFlow.value = current
        saveToDiskForAccount(account.accountId, current)
        pushNoteToFirestore(account, updatedNote)
        pushNoteToMorphRestApi(account, updatedNote, isNew)
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
        if (!account.canWriteDocs()) {
            return Result.failure(IllegalStateException("INSUFFICIENT_SCOPE: Requires scope 'write:docs'."))
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
            updatedAt = System.currentTimeMillis(),
            sourceType = MorphNote.SOURCE_DOC
        )
        saveNote(note)
        return Result.success(note)
    }

    fun createTaskFromAi(
        title: String,
        description: String = "",
        status: String = "Todo"
    ): Result<MorphNote> {
        val account = _connectedAccountFlow.value
            ?: return Result.failure(IllegalStateException("NOT_AUTHENTICATED"))
        if (!account.canWriteTasks()) {
            return Result.failure(IllegalStateException("INSUFFICIENT_SCOPE: Requires scope 'write:tasks'."))
        }

        val taskNote = MorphNote(
            id = "task_${UUID.randomUUID()}",
            accountId = account.accountId,
            title = title.trim().ifBlank { "New Morph Task" },
            content = description.trim(),
            checklist = listOf(MorphChecklistItem(text = title.trim().ifBlank { "New Morph Task" }, isDone = false)),
            isChecklist = true,
            colorHex = MorphPalette.Sky,
            labels = listOf("Task", status),
            updatedAt = System.currentTimeMillis(),
            sourceType = MorphNote.SOURCE_TASK,
            status = status
        )
        saveNote(taskNote)
        return Result.success(taskNote)
    }

    fun deleteNoteByQueryFromAi(query: String): Result<MorphNote> {
        val account = _connectedAccountFlow.value
            ?: return Result.failure(IllegalStateException("NOT_AUTHENTICATED"))
        if (!account.canDelete()) {
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

    fun updateNoteByQueryFromAi(
        targetQuery: String,
        appendedOrNewContent: String,
        append: Boolean = true
    ): Result<MorphNote> {
        val account = _connectedAccountFlow.value
            ?: return Result.failure(IllegalStateException("NOT_AUTHENTICATED"))
        if (!account.canWriteDocs()) {
            return Result.failure(IllegalStateException("INSUFFICIENT_SCOPE: Requires scope 'write:docs'."))
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

        // Official Omkun Morph REST API v1.2 Hosts (https://morph.omkun.com/developer-api)
        const val MORPH_EDGE_API_BASE = "https://api.omkunmorph.workers.dev/api/v1"
        const val MORPH_CANONICAL_API_BASE = "https://morph.omkun.com/api/v1"
        const val MORPH_DEV_PORTAL_URL = "https://morph.omkun.com/developer-api"

        // Production Omkun Morph Auth & Firestore Backend (morph-69d82)
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
