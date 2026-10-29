package com.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.receiver.SmsNotificationHelper
import com.example.ui.ChatViewModel
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val chatViewModel: ChatViewModel by viewModels()

    private val smsInternalReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            chatViewModel.syncDeviceSms(this@MainActivity)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chatViewModel.restoreSavedSession(this)
        handleIntent(intent)
        enableEdgeToEdge()

        try {
            val filter = IntentFilter(SmsNotificationHelper.ACTION_SMS_RECEIVED_INTERNAL)
            ContextCompat.registerReceiver(
                this,
                smsInternalReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        } catch (_: Exception) {}

        val isIntentLaunch = intent?.hasExtra("OPEN_SMS_ADDRESS") == true ||
                intent?.action == Intent.ACTION_SENDTO ||
                intent?.action == Intent.ACTION_VIEW

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    HomeScreen(viewModel = chatViewModel)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(smsInternalReceiver)
        } catch (_: Exception) {}
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        val smsAddress = intent.getStringExtra("OPEN_SMS_ADDRESS")
        if (!smsAddress.isNullOrBlank()) {
            val name = intent.getStringExtra("OPEN_SMS_NAME") ?: smsAddress
            val convId = chatViewModel.getOrCreateConversationForContact(
                name = name,
                phoneNumber = smsAddress,
                isSms = true
            )
            chatViewModel.openConversation(convId, this)
            return
        }

        if (intent.action == Intent.ACTION_SENDTO || intent.action == Intent.ACTION_VIEW) {
            val uri = intent.data
            if (uri != null && (uri.scheme == "sms" || uri.scheme == "smsto")) {
                val address = uri.schemeSpecificPart?.filter { it.isDigit() || it == '+' }
                if (!address.isNullOrBlank()) {
                    val convId = chatViewModel.getOrCreateConversationForContact(
                        name = address,
                        phoneNumber = address,
                        isSms = true
                    )
                    chatViewModel.openConversation(convId, this)
                }
            }
        }
    }
}
