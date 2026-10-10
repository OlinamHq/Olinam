package com.example.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
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
                        // Suppress hard failure log when FCM is not registered on the current backend/device
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

        if (type == "INCOMING_CALL") {
            val callerName = message.data["callerName"] ?: title
            val isVideo = message.data["isVideo"] == "true"
            showCallNotification(callerName, isVideo)
        } else {
            showNotification(title, body)
        }
    }

    private fun showCallNotification(callerName: String, isVideo: Boolean) {
        val manager = getSystemService(NotificationManager::class.java)
        val channelId = "olinam_calls"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Calls", NotificationManager.IMPORTANCE_MAX).apply {
                description = "Incoming voice & video calls"
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }
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
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_phone_call)
            .setContentTitle(callType)
            .setContentText(callerName)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()
        manager.notify(7777, notification)
    }

    private fun showNotification(title: String, body: String) {
        val manager = getSystemService(NotificationManager::class.java)
        val channelId = "olinam_messages"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(channelId, "Messages", NotificationManager.IMPORTANCE_HIGH)
            )
        }
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pending = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        manager.notify(body.hashCode(), notification)
    }
}
