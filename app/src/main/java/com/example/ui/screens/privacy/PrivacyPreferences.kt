package com.example.ui.screens.privacy

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class PrivacyAudience(val title: String) {
    EVERYONE("Everyone"),
    MY_CONTACTS("My contacts"),
    MY_CONTACTS_EXCEPT("My contacts except..."),
    NOBODY("Nobody")
}

enum class DisappearingTimer(val label: String, val seconds: Long) {
    OFF("Off", 0L),
    HOURS_24("24 hours", 86400L),
    DAYS_7("7 days", 604800L),
    DAYS_90("90 days", 7776000L)
}

data class PrivacySettingsState(
    val lastSeenAudience: PrivacyAudience = PrivacyAudience.EVERYONE,
    val onlineAudienceSameAsLastSeen: Boolean = true,
    val profilePhotoAudience: PrivacyAudience = PrivacyAudience.EVERYONE,
    val aboutAudience: PrivacyAudience = PrivacyAudience.EVERYONE,
    val statusAudience: PrivacyAudience = PrivacyAudience.MY_CONTACTS,
    val statusAllowSharing: Boolean = false,
    val readReceiptsEnabled: Boolean = true,
    val defaultMessageTimer: DisappearingTimer = DisappearingTimer.OFF,
    val groupsAudience: PrivacyAudience = PrivacyAudience.EVERYONE,
    val silenceUnknownCallers: Boolean = false,
    val protectIpInCalls: Boolean = false,
    val disableLinkPreviews: Boolean = false,
    val blockUnknownMessages: Boolean = false,
    val strictAccountSettings: Boolean = false,
    val isChatLockBiometricEnabled: Boolean = false,
    val blockedContacts: Set<String> = emptySet()
)

class PrivacyPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("olinam_privacy_prefs", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(loadState())
    val state: StateFlow<PrivacySettingsState> = _state.asStateFlow()

    private fun loadState(): PrivacySettingsState {
        val lastSeenName = prefs.getString("last_seen_audience", PrivacyAudience.EVERYONE.name) ?: PrivacyAudience.EVERYONE.name
        val photoName = prefs.getString("profile_photo_audience", PrivacyAudience.EVERYONE.name) ?: PrivacyAudience.EVERYONE.name
        val aboutName = prefs.getString("about_audience", PrivacyAudience.EVERYONE.name) ?: PrivacyAudience.EVERYONE.name
        val statusName = prefs.getString("status_audience", PrivacyAudience.MY_CONTACTS.name) ?: PrivacyAudience.MY_CONTACTS.name
        val groupsName = prefs.getString("groups_audience", PrivacyAudience.EVERYONE.name) ?: PrivacyAudience.EVERYONE.name
        val timerName = prefs.getString("default_message_timer", DisappearingTimer.OFF.name) ?: DisappearingTimer.OFF.name

        return PrivacySettingsState(
            lastSeenAudience = runCatching { PrivacyAudience.valueOf(lastSeenName) }.getOrDefault(PrivacyAudience.EVERYONE),
            onlineAudienceSameAsLastSeen = prefs.getBoolean("online_same_as_last_seen", true),
            profilePhotoAudience = runCatching { PrivacyAudience.valueOf(photoName) }.getOrDefault(PrivacyAudience.EVERYONE),
            aboutAudience = runCatching { PrivacyAudience.valueOf(aboutName) }.getOrDefault(PrivacyAudience.EVERYONE),
            statusAudience = runCatching { PrivacyAudience.valueOf(statusName) }.getOrDefault(PrivacyAudience.MY_CONTACTS),
            statusAllowSharing = prefs.getBoolean("status_allow_sharing", false),
            readReceiptsEnabled = prefs.getBoolean("read_receipts_enabled", true),
            defaultMessageTimer = runCatching { DisappearingTimer.valueOf(timerName) }.getOrDefault(DisappearingTimer.OFF),
            groupsAudience = runCatching { PrivacyAudience.valueOf(groupsName) }.getOrDefault(PrivacyAudience.EVERYONE),
            silenceUnknownCallers = prefs.getBoolean("silence_unknown_callers", false),
            protectIpInCalls = prefs.getBoolean("protect_ip_in_calls", false),
            disableLinkPreviews = prefs.getBoolean("disable_link_previews", false),
            blockUnknownMessages = prefs.getBoolean("block_unknown_messages", false),
            strictAccountSettings = prefs.getBoolean("strict_account_settings", false),
            isChatLockBiometricEnabled = prefs.getBoolean("chat_lock_biometric", false),
            blockedContacts = prefs.getStringSet("blocked_contacts", emptySet()) ?: emptySet()
        )
    }

    fun setLastSeenAudience(audience: PrivacyAudience, sameAsLastSeen: Boolean) {
        prefs.edit()
            .putString("last_seen_audience", audience.name)
            .putBoolean("online_same_as_last_seen", sameAsLastSeen)
            .apply()
        _state.value = _state.value.copy(
            lastSeenAudience = audience,
            onlineAudienceSameAsLastSeen = sameAsLastSeen
        )
    }

    fun setProfilePhotoAudience(audience: PrivacyAudience) {
        prefs.edit().putString("profile_photo_audience", audience.name).apply()
        _state.value = _state.value.copy(profilePhotoAudience = audience)
    }

    fun setAboutAudience(audience: PrivacyAudience) {
        prefs.edit().putString("about_audience", audience.name).apply()
        _state.value = _state.value.copy(aboutAudience = audience)
    }

    fun setStatusAudience(audience: PrivacyAudience, allowSharing: Boolean) {
        prefs.edit()
            .putString("status_audience", audience.name)
            .putBoolean("status_allow_sharing", allowSharing)
            .apply()
        _state.value = _state.value.copy(
            statusAudience = audience,
            statusAllowSharing = allowSharing
        )
    }

    fun setReadReceipts(enabled: Boolean) {
        prefs.edit().putBoolean("read_receipts_enabled", enabled).apply()
        _state.value = _state.value.copy(readReceiptsEnabled = enabled)
    }

    fun setDefaultMessageTimer(timer: DisappearingTimer) {
        prefs.edit().putString("default_message_timer", timer.name).apply()
        _state.value = _state.value.copy(defaultMessageTimer = timer)
    }

    fun setGroupsAudience(audience: PrivacyAudience) {
        prefs.edit().putString("groups_audience", audience.name).apply()
        _state.value = _state.value.copy(groupsAudience = audience)
    }

    fun setSilenceUnknownCallers(enabled: Boolean) {
        prefs.edit().putBoolean("silence_unknown_callers", enabled).apply()
        _state.value = _state.value.copy(silenceUnknownCallers = enabled)
    }

    fun setProtectIpInCalls(enabled: Boolean) {
        prefs.edit().putBoolean("protect_ip_in_calls", enabled).apply()
        _state.value = _state.value.copy(protectIpInCalls = enabled)
    }

    fun setDisableLinkPreviews(enabled: Boolean) {
        prefs.edit().putBoolean("disable_link_previews", enabled).apply()
        _state.value = _state.value.copy(disableLinkPreviews = enabled)
    }

    fun setBlockUnknownMessages(enabled: Boolean) {
        prefs.edit().putBoolean("block_unknown_messages", enabled).apply()
        _state.value = _state.value.copy(blockUnknownMessages = enabled)
    }

    fun setStrictAccountSettings(enabled: Boolean) {
        prefs.edit().putBoolean("strict_account_settings", enabled).apply()
        _state.value = _state.value.copy(strictAccountSettings = enabled)
    }

    fun setChatLockBiometric(enabled: Boolean) {
        prefs.edit().putBoolean("chat_lock_biometric", enabled).apply()
        _state.value = _state.value.copy(isChatLockBiometricEnabled = enabled)
    }

    fun blockContact(contactIdentifier: String) {
        val updated = _state.value.blockedContacts.toMutableSet()
        updated.add(contactIdentifier)
        prefs.edit().putStringSet("blocked_contacts", updated).apply()
        _state.value = _state.value.copy(blockedContacts = updated)
    }

    fun unblockContact(contactIdentifier: String) {
        val updated = _state.value.blockedContacts.toMutableSet()
        updated.remove(contactIdentifier)
        prefs.edit().putStringSet("blocked_contacts", updated).apply()
        _state.value = _state.value.copy(blockedContacts = updated)
    }

    fun isBlocked(identifier: String?): Boolean {
        if (identifier.isNullOrBlank()) return false
        val clean = identifier.replace(Regex("[^0-9a-zA-Z]"), "").lowercase()
        return _state.value.blockedContacts.any { blocked ->
            val cleanBlocked = blocked.replace(Regex("[^0-9a-zA-Z]"), "").lowercase()
            clean == cleanBlocked || 
            (clean.length >= 7 && cleanBlocked.length >= 7 && (clean.endsWith(cleanBlocked) || cleanBlocked.endsWith(clean))) ||
            identifier.equals(blocked, ignoreCase = true)
        }
    }

    fun canUseBiometrics(context: Context): Boolean {
        val km = context.getSystemService(Context.KEYGUARD_SERVICE) as? android.app.KeyguardManager
        return km?.isDeviceSecure == true
    }
}
