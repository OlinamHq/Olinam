package com.example.ui.screens.contacts

import android.content.Context
import android.content.SharedPreferences

enum class ContactSortOrder {
    FIRST_NAME,
    LAST_NAME
}

enum class ContactNameFormat {
    FIRST_NAME_FIRST,
    LAST_NAME_FIRST
}

class ContactSettingsPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("olinam_contact_prefs", Context.MODE_PRIVATE)

    var sortOrder: ContactSortOrder
        get() = try {
            ContactSortOrder.valueOf(prefs.getString(KEY_SORT_ORDER, ContactSortOrder.FIRST_NAME.name) ?: ContactSortOrder.FIRST_NAME.name)
        } catch (_: Exception) {
            ContactSortOrder.FIRST_NAME
        }
        set(value) {
            prefs.edit().putString(KEY_SORT_ORDER, value.name).apply()
        }

    var nameFormat: ContactNameFormat
        get() = try {
            ContactNameFormat.valueOf(prefs.getString(KEY_NAME_FORMAT, ContactNameFormat.FIRST_NAME_FIRST.name) ?: ContactNameFormat.FIRST_NAME_FIRST.name)
        } catch (_: Exception) {
            ContactNameFormat.FIRST_NAME_FIRST
        }
        set(value) {
            prefs.edit().putString(KEY_NAME_FORMAT, value.name).apply()
        }

    var isAutoSyncEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_SYNC, true)
        set(value) {
            prefs.edit().putBoolean(KEY_AUTO_SYNC, value).apply()
        }

    var lastSyncTimestamp: Long
        get() = prefs.getLong(KEY_LAST_SYNC_TS, 0L)
        set(value) {
            prefs.edit().putLong(KEY_LAST_SYNC_TS, value).apply()
        }

    companion object {
        private const val KEY_SORT_ORDER = "contact_sort_order"
        private const val KEY_NAME_FORMAT = "contact_name_format"
        private const val KEY_AUTO_SYNC = "contact_auto_sync"
        private const val KEY_LAST_SYNC_TS = "contact_last_sync_timestamp"
    }
}
