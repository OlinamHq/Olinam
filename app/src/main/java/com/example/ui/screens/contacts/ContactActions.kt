package com.example.ui.screens.contacts

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Settings
import android.widget.Toast

object ContactActions {

    const val HELP_URL = "https://help.olinam.com"
    const val DEFAULT_INVITE_MESSAGE = "Hey! Let's chat on Olinam. It's a fast, simple, and secure messaging and calling app. Download it at: https://olinam.com/download"

    /**
     * Opens Olinam Help Center at https://help.olinam.com in the default browser.
     */
    fun openHelpWebpage(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(HELP_URL)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open browser: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Opens SMS app or native share sheet to invite a friend to Olinam via SMS.
     */
    fun inviteFriendViaSms(context: Context, phoneNumber: String? = null, message: String = DEFAULT_INVITE_MESSAGE) {
        try {
            val smsUri = if (!phoneNumber.isNullOrBlank()) {
                Uri.parse("smsto:${phoneNumber.trim()}")
            } else {
                Uri.parse("smsto:")
            }
            val smsIntent = Intent(Intent.ACTION_SENDTO, smsUri).apply {
                putExtra("sms_body", message)
                putExtra(Intent.EXTRA_TEXT, message)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            if (smsIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(smsIntent)
            } else {
                // Fallback to standard share sheet
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(Intent.createChooser(shareIntent, "Invite a friend via SMS").apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }
        } catch (e: Exception) {
            // Generic share fallback
            try {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(Intent.createChooser(shareIntent, "Invite via SMS").apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            } catch (ex: Exception) {
                Toast.makeText(context, "Unable to send invite: ${ex.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Opens the device's native contact editor to insert a new contact.
     */
    fun launchSystemNewContact(context: Context, initialName: String = "", initialPhone: String = "") {
        try {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                type = ContactsContract.Contacts.CONTENT_TYPE
                if (initialName.isNotBlank()) {
                    putExtra(ContactsContract.Intents.Insert.NAME, initialName)
                }
                if (initialPhone.isNotBlank()) {
                    putExtra(ContactsContract.Intents.Insert.PHONE, initialPhone)
                }
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open contacts app: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Opens system app settings for permission management.
     */
    fun openAppSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to open settings", Toast.LENGTH_SHORT).show()
        }
    }
}
