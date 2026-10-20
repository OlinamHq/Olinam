package com.example.push

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.ui.screens.notifications.ChannelCategory
import com.example.ui.screens.notifications.NotificationPreferences
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class OlinamMessagingService : FirebaseMessagingService() {
    companion object {
        fun syncTokenSafely(context: android.content.Context) {
            try {
                com.google.firebase.messaging.FirebaseMessaging.getInstance().token
                    .addOnSuccessListener { token ->
                        if (!token.isNullOrBlank()) {
                            context.getSharedPreferences("olinam_user_prefs", MODE_PRIVATE)
                                .edit().putString("fcm_token", token).apply()
                        }
                    }
                    .addOnFailureListener {
                        android.util.Log.d("OlinamPush", "FCM token registration deferred: ${it.message}")
                    }
            } catch (t: Throwable) {
                android.util.Log.d("OlinamPush", "FCM service unavailable: ${t.message}")
            }
        }
    }

    override fun onNewToken(token: String) {
        getSharedPreferences("olinam_user_prefs", MODE_PRIVATE)
            .edit().putString("fcm_token", token).apply()
        try {
            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            if (!uid.isNullOrBlank()) {
                com.example.data.PresenceManager.publishIdentity(uid, "", token)
            }
        } catch (_: Throwable) {}
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val type = message.data["type"] ?: "message"
        val title = message.data["title"] ?: message.notification?.title ?: "Olinam"
        val body = message.data["body"] ?: message.notification?.body ?: "New message"
        val isGroup = message.data["isGroup"] == "true"

        val privacyPrefs = com.example.ui.screens.privacy.PrivacyPreferences(this)
        val senderId = message.data["senderId"] ?: message.data["sender"] ?: ""
        if (privacyPrefs.isBlocked(senderId) || privacyPrefs.isBlocked(title)) {
            // Silently drop messages and calls from blocked contacts
            return
        }

        if (type == "INCOMING_CALL") {
            val callerName = message.data["callerName"] ?: title
            val isVideo = message.data["isVideo"] == "true"
            showCallNotification(callerName, isVideo)
        } else {
            showNotification(title, body, isGroup)
        }
    }

    private fun showCallNotification(callerName: String, isVideo: Boolean) {
        val manager = getSystemService(NotificationManager::class.java) ?: return
        val prefs = NotificationPreferences(this)
        val channelId = prefs.syncChannel(ChannelCategory.CALLS)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pending = PendingIntent.getActivity(
            this,
            1,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val callType = if (isVideo) "Incoming Video Call" else "Incoming Voice Call"
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_phone_call)
            .setContentTitle(callType)
            .setContentText(callerName)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .setFullScreenIntent(pending, true)

        prefs.applyNotificationPreferences(builder, ChannelCategory.CALLS)
        manager.notify(7777, builder.build())
    }

    private fun showNotification(title: String, body: String, isGroup: Boolean = false) {
        val manager = getSystemService(NotificationManager::class.java) ?: return
        val prefs = NotificationPreferences(this)
        val category = if (isGroup) ChannelCategory.GROUPS else ChannelCategory.MESSAGES
        val channelId = prefs.syncChannel(category)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pending = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(com.example.R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(this, com.example.R.color.notification_accent))
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pending)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)

        prefs.applyNotificationPreferences(builder, category)
        manager.notify(body.hashCode(), builder.build())
    }
}
