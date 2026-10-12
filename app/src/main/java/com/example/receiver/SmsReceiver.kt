package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        SmsNotificationHelper.handleIncomingSms(context, intent, isDeliverAction = true)
    }
}
