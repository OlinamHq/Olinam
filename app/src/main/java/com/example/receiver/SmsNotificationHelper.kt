package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import android.provider.Telephony
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.data.SmsHelper

object SmsNotificationHelper {

    const val CHANNEL_ID = "olinam_sms_channel"
    const val ACTION_SMS_RECEIVED_INTERNAL = "com.example.olinam.SMS_RECEIVED_INTERNAL"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java) ?: return
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Text Messages (SMS)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Incoming text messages and cellular SMS alerts"
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    null
                )
            }
            manager.createNotificationChannel(channel)
        }
    }

    fun handleIncomingSms(context: Context, intent: Intent, isDeliverAction: Boolean) {
        try {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            if (messages.isNullOrEmpty()) return

            val sender = messages[0].displayOriginatingAddress
                ?: messages[0].originatingAddress
                ?: return

            val body = messages.joinToString("") {
                it.displayMessageBody ?: it.messageBody ?: ""
            }

            val timestamp = if (messages[0].timestampMillis > 0) {
                messages[0].timestampMillis
            } else {
                System.currentTimeMillis()
            }

            // Save incoming SMS to Android system Telephony provider if delivered as default app
            if (isDeliverAction) {
                SmsHelper.saveIncomingSms(context, sender, body, timestamp)
            }

            // Ensure channel exists
            createNotificationChannel(context)

            // Resolve contact name or use phone number
            val contactName = SmsHelper.resolveContactName(context, sender) ?: sender

            // Prepare open chat PendingIntent on tap
            val openIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("OPEN_SMS_ADDRESS", sender)
                putExtra("OPEN_SMS_NAME", contactName)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                sender.hashCode(),
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Post Notification if permission is granted
            val canPostNotification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }

            if (canPostNotification) {
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.stat_notify_chat)
                    .setContentTitle(contactName)
                    .setContentText(body)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .build()

                notificationManager.notify(sender.hashCode(), notification)
            }

            // Broadcast internally so MainActivity & ViewModel refresh the chat in real time
            val broadcast = Intent(ACTION_SMS_RECEIVED_INTERNAL).apply {
                setPackage(context.packageName)
                putExtra("sender", sender)
                putExtra("body", body)
                putExtra("timestamp", timestamp)
            }
            context.sendBroadcast(broadcast)

        } catch (e: Exception) {
            Log.e("SmsNotificationHelper", "Failed to handle incoming SMS: ${e.message}", e)
        }
    }
}
