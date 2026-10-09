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
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"] ?: "Olinam"
        val body = message.notification?.body ?: message.data["body"] ?: "New message"
        showNotification(title, body)
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
