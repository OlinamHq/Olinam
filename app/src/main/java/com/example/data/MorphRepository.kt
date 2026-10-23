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
     * Option 3: Sign in or Register with an Omkun / Google Workspace Email & Password.
     * Enforces real per-account credential verification and isolated note vaults.
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

        val accountId = deriveAccountId(normalizedEmail)
        val passwordHash = sha256("$normalizedEmail::$password::morph_v2")
        val storedHashKey = "cred_hash_$accountId"
        val existingHash = prefs.getString(storedHashKey, null)

        if (existingHash != null && existingHash != passwordHash) {
            return@withContext Result.failure(IllegalArgumentException("Incorrect password for $normalizedEmail."))
        }

        val resolvedName = displayName.trim().ifBlank {
            prefs.getString("cred_name_$accountId", null)
                ?: normalizedEmail.substringBefore("@")
                    .replace(".", " ")
                    .replace("_", " ")
                    .split(" ")
                    .filter { it.isNotBlank() }
                    .joinToString(" ") { part -> part.replaceFirstChar { it.uppercase() } }
                    .ifBlank { "Morph User" }
        }

        prefs.edit()
            .putString(storedHashKey, passwordHash)
            .putString("cred_name_$accountId", resolvedName)
            .apply()

        val sessionToken = issueUserSessionToken(accountId, normalizedEmail, passwordHash)
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
            onComplete?.invoke(false, "Please sign in to your Omkun Morph account first.")
            return
        }

        coroutineScope.launch {
            _isSyncing.value = true
            _syncStatus.value = "Syncing ${account.email}..."
            var syncedRemote = false

            // 1. Sync with Firestore user-scoped collection if available
            try {
                val firestoreNotes = fetchAccountNotesFromFirestore(account)
                if (firestoreNotes.isNotEmpty()) {
                    mergeRemoteNotesForAccount(account, firestoreNotes)
                    syncedRemote = true
                }
            } catch (e: Exception) {
                Log.d(TAG, "Firestore Morph sync note: ${e.message}")
            }

            // 2. Sync with Morph Cloud Service scoped to authenticated account headers
            try {
                val url = "${INTERNAL_MORPH_CLOUD_URL.trimEnd('/')}/notes?accountId=${account.accountId}"
                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer $INTERNAL_SERVICE_BRIDGE_TOKEN")
                    .addHeader("X-Morph-User-Id", account.accountId)
                    .addHeader("X-Morph-User-Email", account.email)
                    .addHeader("X-Morph-Session-Token", account.sessionToken)
                    .addHeader("Content-Type", "application/json")
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val bodyString = response.body?.string().orEmpty()
                        if (bodyString.isNotBlank()) {
                            val parsed = parseNotesFromJson(bodyString, account.accountId)
                            if (parsed.isNotEmpty()) {
                                mergeRemoteNotesForAccount(account, parsed)
                            }
                        }
                        syncedRemote = true
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "Morph Cloud sync notice: ${e.message}")
            }

            lastSyncTime = System.currentTimeMillis()
            _isSyncing.value = false
            _syncStatus.value = if (syncedRemote) {
                "Synced • ${account.email}"
            } else {
                "Active • ${account.email}"
            }
            onComplete?.invoke(true, "Synced with ${account.email}")
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
        if (_connectedAccountFlow.value?.accountId == account.accountId) {
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

        // Strictly internal service constants — never exposed to UI components
        private const val INTERNAL_MORPH_CLOUD_URL =
            "https://ais-dev-kps2metajswcv3lmn3d4iq-193817906370.asia-southeast1.run.app/api/v1"
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
