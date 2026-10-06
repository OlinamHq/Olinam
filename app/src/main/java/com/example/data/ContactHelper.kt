package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.provider.ContactsContract
import org.json.JSONArray
import org.json.JSONObject

data class DeviceContact(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val status: String = "Mobile contact",
    val hasOlinam: Boolean = false
)

object ContactHelper {

    private const val PREFS_NAME = "olinam_real_contacts_pref"
    private const val KEY_SAVED_CONTACTS = "saved_real_contacts"

    fun saveRealContact(context: Context, contact: DeviceContact) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentList = getSavedContacts(context).toMutableList()
        val existingIndex = currentList.indexOfFirst { it.phoneNumber == contact.phoneNumber }
        if (existingIndex >= 0) {
            currentList[existingIndex] = contact
        } else {
            currentList.add(0, contact)
        }

        val jsonArray = JSONArray()
        for (item in currentList) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("name", item.name)
            obj.put("phoneNumber", item.phoneNumber)
            obj.put("status", item.status)
            obj.put("hasOlinam", item.hasOlinam)
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_SAVED_CONTACTS, jsonArray.toString()).apply()
    }

    private fun getSavedContacts(context: Context): List<DeviceContact> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_SAVED_CONTACTS, null) ?: return emptyList()
        val result = mutableListOf<DeviceContact>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                result.add(
                    DeviceContact(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        phoneNumber = obj.getString("phoneNumber"),
                        status = obj.optString("status", "Mobile contact"),
                        hasOlinam = obj.optBoolean("hasOlinam", false)
                    )
                )
            }
        } catch (_: Exception) {}
        return result
    }

    fun getDeviceContacts(context: Context): List<DeviceContact> {
        val contactList = mutableListOf<DeviceContact>()
        val seenNumbers = mutableSetOf<String>()

        // 1. Fetch 100% Real Device Contacts from Android Phone Book
        try {
            val cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                null,
                null,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
            )

            cursor?.use {
                val idIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                while (it.moveToNext()) {
                    val id = if (idIdx >= 0) it.getString(idIdx) else java.util.UUID.randomUUID().toString()
                    val name = if (nameIdx >= 0) it.getString(nameIdx) ?: "Contact" else "Contact"
                    val number = if (numIdx >= 0) it.getString(numIdx) ?: "" else ""

                    val cleanNumber = number.replace(" ", "").replace("-", "")
                    if (cleanNumber.isNotEmpty() && !seenNumbers.contains(cleanNumber)) {
                        seenNumbers.add(cleanNumber)
                        contactList.add(
                            DeviceContact(
                                id = id,
                                name = name.trim(),
                                phoneNumber = number.trim(),
                                status = "Mobile contact",
                                hasOlinam = false
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {
            // Permission not yet granted or system error
        }

        // 2. Include any real contacts saved by the user via "New contact"
        val userSaved = getSavedContacts(context)
        for (item in userSaved) {
            val clean = item.phoneNumber.replace(" ", "").replace("-", "")
            if (!seenNumbers.contains(clean)) {
                seenNumbers.add(clean)
                contactList.add(0, item)
            }
        }

        // Zero dummy data! Returns only real device contacts or user-entered contacts
        return contactList
    }
}
