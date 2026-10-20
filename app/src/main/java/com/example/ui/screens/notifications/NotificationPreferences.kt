package com.example.ui.screens.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ToneItem(
    val title: String,
    val uriString: String
)

enum class ChannelCategory {
    MESSAGES,
    GROUPS,
    CALLS,
    STATUS,
    SMS
}

data class NotificationSettingsState(
    // General
    val conversationTones: Boolean = true,
    val reminders: Boolean = true,

    // Messages
    val messageTone: String = "Default (System)",
    val messageToneUri: String = "",
    val messageVibrate: String = "Default",
    val messageLight: String = "White",
    val messageHighPriority: Boolean = true,
    val messageReactions: Boolean = true,

    // Groups
    val groupTone: String = "Default (System)",
    val groupToneUri: String = "",
    val groupVibrate: String = "Default",
    val groupLight: String = "White",
    val groupHighPriority: Boolean = true,
    val groupReactions: Boolean = true,

    // Calls
    val callRingtone: String = "Default (System)",
    val callRingtoneUri: String = "",
    val callVibrate: String = "Default",

    // Status
    val statusTone: String = "Default (System)",
    val statusToneUri: String = "",
    val statusVibrate: String = "Default",
    val statusHighPriority: Boolean = true,
    val statusReactions: Boolean = true
)

class NotificationPreferences(private val context: Context) {
    private val appContext = context.applicationContext
    private val prefs: SharedPreferences =
        appContext.getSharedPreferences("olinam_notification_prefs", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(loadState())
    val state: StateFlow<NotificationSettingsState> = _state.asStateFlow()

    private var currentPreviewRingtone: Ringtone? = null

    companion object {
        private var toneGenerator: ToneGenerator? = null

        private fun getToneGen(): ToneGenerator? {
            if (toneGenerator == null) {
                try {
                    toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
                } catch (_: Throwable) {}
            }
            return toneGenerator
        }
    }

    private fun loadState(): NotificationSettingsState {
        return NotificationSettingsState(
            conversationTones = prefs.getBoolean("conv_tones", true),
            reminders = prefs.getBoolean("reminders", true),

            messageTone = prefs.getString("msg_tone", "Default (System)") ?: "Default (System)",
            messageToneUri = prefs.getString("msg_tone_uri", "") ?: "",
            messageVibrate = prefs.getString("msg_vibrate", "Default") ?: "Default",
            messageLight = prefs.getString("msg_light", "White") ?: "White",
            messageHighPriority = prefs.getBoolean("msg_high_priority", true),
            messageReactions = prefs.getBoolean("msg_reactions", true),

            groupTone = prefs.getString("grp_tone", "Default (System)") ?: "Default (System)",
            groupToneUri = prefs.getString("grp_tone_uri", "") ?: "",
            groupVibrate = prefs.getString("grp_vibrate", "Default") ?: "Default",
            groupLight = prefs.getString("grp_light", "White") ?: "White",
            groupHighPriority = prefs.getBoolean("grp_high_priority", true),
            groupReactions = prefs.getBoolean("grp_reactions", true),

            callRingtone = prefs.getString("call_ringtone", "Default (System)") ?: "Default (System)",
            callRingtoneUri = prefs.getString("call_ringtone_uri", "") ?: "",
            callVibrate = prefs.getString("call_vibrate", "Default") ?: "Default",

            statusTone = prefs.getString("status_tone", "Default (System)") ?: "Default (System)",
            statusToneUri = prefs.getString("status_tone_uri", "") ?: "",
            statusVibrate = prefs.getString("status_vibrate", "Default") ?: "Default",
            statusHighPriority = prefs.getBoolean("status_high_priority", true),
            statusReactions = prefs.getBoolean("status_reactions", true)
        )
    }

    // General
    fun setConversationTones(enabled: Boolean) {
        prefs.edit().putBoolean("conv_tones", enabled).apply()
        _state.value = _state.value.copy(conversationTones = enabled)
    }

    fun setReminders(enabled: Boolean) {
        prefs.edit().putBoolean("reminders", enabled).apply()
        _state.value = _state.value.copy(reminders = enabled)
    }

    // Messages
    fun setMessageTone(toneTitle: String, uriString: String) {
        prefs.edit().putString("msg_tone", toneTitle).putString("msg_tone_uri", uriString).apply()
        _state.value = _state.value.copy(messageTone = toneTitle, messageToneUri = uriString)
        syncChannel(ChannelCategory.MESSAGES)
    }

    fun setMessageVibrate(vibrate: String) {
        prefs.edit().putString("msg_vibrate", vibrate).apply()
        _state.value = _state.value.copy(messageVibrate = vibrate)
        syncChannel(ChannelCategory.MESSAGES)
    }

    fun setMessageLight(light: String) {
        prefs.edit().putString("msg_light", light).apply()
        _state.value = _state.value.copy(messageLight = light)
        syncChannel(ChannelCategory.MESSAGES)
    }

    fun setMessageHighPriority(enabled: Boolean) {
        prefs.edit().putBoolean("msg_high_priority", enabled).apply()
        _state.value = _state.value.copy(messageHighPriority = enabled)
        syncChannel(ChannelCategory.MESSAGES)
    }

    fun setMessageReactions(enabled: Boolean) {
        prefs.edit().putBoolean("msg_reactions", enabled).apply()
        _state.value = _state.value.copy(messageReactions = enabled)
    }

    // Groups
    fun setGroupTone(toneTitle: String, uriString: String) {
        prefs.edit().putString("grp_tone", toneTitle).putString("grp_tone_uri", uriString).apply()
        _state.value = _state.value.copy(groupTone = toneTitle, groupToneUri = uriString)
        syncChannel(ChannelCategory.GROUPS)
    }

    fun setGroupVibrate(vibrate: String) {
        prefs.edit().putString("grp_vibrate", vibrate).apply()
        _state.value = _state.value.copy(groupVibrate = vibrate)
        syncChannel(ChannelCategory.GROUPS)
    }

    fun setGroupLight(light: String) {
        prefs.edit().putString("grp_light", light).apply()
        _state.value = _state.value.copy(groupLight = light)
        syncChannel(ChannelCategory.GROUPS)
    }

    fun setGroupHighPriority(enabled: Boolean) {
        prefs.edit().putBoolean("grp_high_priority", enabled).apply()
        _state.value = _state.value.copy(groupHighPriority = enabled)
        syncChannel(ChannelCategory.GROUPS)
    }

    fun setGroupReactions(enabled: Boolean) {
        prefs.edit().putBoolean("grp_reactions", enabled).apply()
        _state.value = _state.value.copy(groupReactions = enabled)
    }

    // Calls
    fun setCallRingtone(ringtoneTitle: String, uriString: String) {
        prefs.edit().putString("call_ringtone", ringtoneTitle).putString("call_ringtone_uri", uriString).apply()
        _state.value = _state.value.copy(callRingtone = ringtoneTitle, callRingtoneUri = uriString)
        syncChannel(ChannelCategory.CALLS)
    }

    fun setCallVibrate(vibrate: String) {
        prefs.edit().putString("call_vibrate", vibrate).apply()
        _state.value = _state.value.copy(callVibrate = vibrate)
        syncChannel(ChannelCategory.CALLS)
    }

    // Status
    fun setStatusTone(toneTitle: String, uriString: String) {
        prefs.edit().putString("status_tone", toneTitle).putString("status_tone_uri", uriString).apply()
        _state.value = _state.value.copy(statusTone = toneTitle, statusToneUri = uriString)
        syncChannel(ChannelCategory.STATUS)
    }

    fun setStatusVibrate(vibrate: String) {
        prefs.edit().putString("status_vibrate", vibrate).apply()
        _state.value = _state.value.copy(statusVibrate = vibrate)
        syncChannel(ChannelCategory.STATUS)
    }

    fun setStatusHighPriority(enabled: Boolean) {
        prefs.edit().putBoolean("status_high_priority", enabled).apply()
        _state.value = _state.value.copy(statusHighPriority = enabled)
        syncChannel(ChannelCategory.STATUS)
    }

    fun setStatusReactions(enabled: Boolean) {
        prefs.edit().putBoolean("status_reactions", enabled).apply()
        _state.value = _state.value.copy(statusReactions = enabled)
    }

    fun resetToDefaults() {
        prefs.edit().clear().apply()
        _state.value = loadState()
        // Recreate all channels with fresh defaults
        ChannelCategory.values().forEach { syncChannel(it) }
    }

    // --- Production Ringtones & Tones Query ---
    fun getAvailableTones(type: Int): List<ToneItem> {
        val list = mutableListOf<ToneItem>()
        // 1. Default system tone
        val defaultUri = RingtoneManager.getDefaultUri(type)
        val defaultRingtone = try { RingtoneManager.getRingtone(appContext, defaultUri) } catch (_: Throwable) { null }
        val defaultName = defaultRingtone?.getTitle(appContext) ?: "System Default"
        list.add(ToneItem("Default ($defaultName)", defaultUri?.toString() ?: ""))

        // 2. Silent option
        list.add(ToneItem("Silent (None)", "silent"))

        // 3. Query all installed system notification/ringtone files
        try {
            val ringtoneManager = RingtoneManager(appContext).apply { setType(type) }
            val cursor = ringtoneManager.cursor
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
                    val uri = ringtoneManager.getRingtoneUri(cursor.position)
                    if (uri != null && title != null) {
                        list.add(ToneItem(title, uri.toString()))
                    }
                }
            }
        } catch (_: Throwable) {}

        // Fallback bundled names if emulator has empty media library
        if (list.size <= 2) {
            list.add(ToneItem("Aurora (Gentle)", "fallback_aurora"))
            list.add(ToneItem("Chime (Melodic)", "fallback_chime"))
            list.add(ToneItem("Crystal (Crisp)", "fallback_crystal"))
            list.add(ToneItem("Echo (Modern)", "fallback_echo"))
            list.add(ToneItem("Horizon (Olinam)", "fallback_horizon"))
        }

        return list
    }

    fun playTonePreview(uriString: String, ringtoneType: Int = RingtoneManager.TYPE_NOTIFICATION) {
        stopTonePreview()
        if (uriString.isBlank() || uriString == "silent") return

        try {
            if (uriString.startsWith("content://") || uriString.startsWith("android.resource://")) {
                val uri = Uri.parse(uriString)
                currentPreviewRingtone = RingtoneManager.getRingtone(appContext, uri)
                currentPreviewRingtone?.play()
            } else {
                // Fallback tone generator beep
                playSampleTone()
            }
        } catch (_: Throwable) {
            playSampleTone()
        }
    }

    fun stopTonePreview() {
        try {
            currentPreviewRingtone?.stop()
            currentPreviewRingtone = null
        } catch (_: Throwable) {}
    }

    // --- Vibration Feedback ---
    fun getVibrationPattern(option: String): LongArray? {
        return when (option.trim().lowercase()) {
            "off", "none" -> null
            "short" -> longArrayOf(0, 150)
            "long" -> longArrayOf(0, 450)
            else -> longArrayOf(0, 200, 100, 200) // Default double pulse
        }
    }

    fun triggerVibrationFeedback(pattern: String) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            } ?: return

            if (!vibrator.hasVibrator()) return

            val waveform = getVibrationPattern(pattern)
            if (waveform == null) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (waveform.size == 2) {
                    vibrator.vibrate(VibrationEffect.createOneShot(waveform[1], VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    vibrator.vibrate(VibrationEffect.createWaveform(waveform, -1))
                }
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(waveform, -1)
            }
        } catch (_: Throwable) {}
    }

    fun playSampleTone() {
        try {
            val tg = getToneGen() ?: ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
            tg.startTone(ToneGenerator.TONE_PROP_BEEP2, 160)
        } catch (_: Throwable) {}
    }

    // --- In-App Conversation Tones (WhatsApp style outgoing/incoming pop) ---
    fun playOutgoingMessageTone() {
        if (!_state.value.conversationTones) return
        try {
            val tg = getToneGen() ?: ToneGenerator(AudioManager.STREAM_NOTIFICATION, 60)
            tg.startTone(ToneGenerator.TONE_PROP_PROMPT, 90)
        } catch (_: Throwable) {}
    }

    fun playIncomingMessageTone() {
        if (!_state.value.conversationTones) return
        try {
            val tg = getToneGen() ?: ToneGenerator(AudioManager.STREAM_NOTIFICATION, 75)
            tg.startTone(ToneGenerator.TONE_PROP_BEEP2, 120)
        } catch (_: Throwable) {}
    }

    // --- Light Color Helper ---
    private fun getLightColorInt(lightName: String): Int {
        return when (lightName.trim().lowercase()) {
            "white" -> 0xFFFFFFFF.toInt()
            "blue" -> 0xFF2563EB.toInt()
            "cyan" -> 0xFF06B6D4.toInt()
            "purple" -> 0xFFA855F7.toInt()
            "yellow" -> 0xFFEAB308.toInt()
            "red" -> 0xFFEF4444.toInt()
            else -> 0 // None / off
        }
    }

    // --- Production Notification Channel Lifecycle ---
    fun getChannelId(category: ChannelCategory): String {
        return when (category) {
            ChannelCategory.MESSAGES -> "olinam_messages_channel"
            ChannelCategory.GROUPS -> "olinam_groups_channel"
            ChannelCategory.CALLS -> "olinam_calls_channel"
            ChannelCategory.STATUS -> "olinam_status_channel"
            ChannelCategory.SMS -> "olinam_sms_channel"
        }
    }

    fun syncChannel(category: ChannelCategory): String {
        val channelId = getChannelId(category)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return channelId

        val manager = appContext.getSystemService(NotificationManager::class.java) ?: return channelId
        val currentState = _state.value

        val (name, description, isHighPriority, toneUriStr, vibrateOption, lightOption, isCall) = when (category) {
            ChannelCategory.MESSAGES -> Tuple7(
                "Direct Messages",
                "Notifications for individual direct chats",
                currentState.messageHighPriority,
                currentState.messageToneUri,
                currentState.messageVibrate,
                currentState.messageLight,
                false
            )
            ChannelCategory.GROUPS -> Tuple7(
                "Group Messages",
                "Notifications for group chats and community channels",
                currentState.groupHighPriority,
                currentState.groupToneUri,
                currentState.groupVibrate,
                currentState.groupLight,
                false
            )
            ChannelCategory.CALLS -> Tuple7(
                "Voice & Video Calls",
                "Alerts for incoming voice and video calls",
                true,
                currentState.callRingtoneUri,
                currentState.callVibrate,
                "White",
                true
            )
            ChannelCategory.STATUS -> Tuple7(
                "Status Updates",
                "Notifications for new status stories and replies",
                currentState.statusHighPriority,
                currentState.statusToneUri,
                currentState.statusVibrate,
                "White",
                false
            )
            ChannelCategory.SMS -> Tuple7(
                "Text Messages (SMS)",
                "Incoming cellular SMS messages",
                currentState.messageHighPriority,
                currentState.messageToneUri,
                currentState.messageVibrate,
                currentState.messageLight,
                false
            )
        }

        val importance = if (isCall) {
            NotificationManager.IMPORTANCE_MAX
        } else if (isHighPriority) {
            NotificationManager.IMPORTANCE_HIGH
        } else {
            NotificationManager.IMPORTANCE_DEFAULT
        }

        val channel = NotificationChannel(channelId, name, importance).apply {
            this.description = description
            setShowBadge(true)

            // Vibration
            val vibPattern = getVibrationPattern(vibrateOption)
            if (vibPattern != null) {
                enableVibration(true)
                vibrationPattern = vibPattern
            } else {
                enableVibration(false)
            }

            // Lights
            val lightColor = getLightColorInt(lightOption)
            if (lightColor != 0) {
                enableLights(true)
                this.lightColor = lightColor
            } else {
                enableLights(false)
            }

            // Sound
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(if (isCall) AudioAttributes.USAGE_NOTIFICATION_RINGTONE else AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val soundUri: Uri? = if (toneUriStr == "silent") {
                null
            } else if (toneUriStr.isNotBlank() && (toneUriStr.startsWith("content://") || toneUriStr.startsWith("android.resource://"))) {
                Uri.parse(toneUriStr)
            } else {
                RingtoneManager.getDefaultUri(if (isCall) RingtoneManager.TYPE_RINGTONE else RingtoneManager.TYPE_NOTIFICATION)
            }

            setSound(soundUri, audioAttributes)
        }

        manager.createNotificationChannel(channel)
        return channelId
    }

    fun applyNotificationPreferences(
        builder: NotificationCompat.Builder,
        category: ChannelCategory
    ): NotificationCompat.Builder {
        val channelId = syncChannel(category)
        builder.setChannelId(channelId)

        val s = _state.value
        val (highPriority, vibrate, toneUriStr, light) = when (category) {
            ChannelCategory.MESSAGES, ChannelCategory.SMS ->
                Tuple4(s.messageHighPriority, s.messageVibrate, s.messageToneUri, s.messageLight)
            ChannelCategory.GROUPS ->
                Tuple4(s.groupHighPriority, s.groupVibrate, s.groupToneUri, s.groupLight)
            ChannelCategory.CALLS ->
                Tuple4(true, s.callVibrate, s.callRingtoneUri, "White")
            ChannelCategory.STATUS ->
                Tuple4(s.statusHighPriority, s.statusVibrate, s.statusToneUri, "White")
        }

        builder.setPriority(if (highPriority) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)

        val vib = getVibrationPattern(vibrate)
        if (vib != null) {
            builder.setVibrate(vib)
        }

        val lightColor = getLightColorInt(light)
        if (lightColor != 0) {
            builder.setLights(lightColor, 1000, 1000)
        }

        if (toneUriStr == "silent") {
            builder.setSilent(true)
        } else if (toneUriStr.isNotBlank()) {
            try {
                builder.setSound(Uri.parse(toneUriStr))
            } catch (_: Throwable) {
                builder.setDefaults(NotificationCompat.DEFAULT_SOUND)
            }
        } else {
            builder.setDefaults(NotificationCompat.DEFAULT_SOUND)
        }

        return builder
    }

    // --- Open Native Android App Notification Settings ---
    fun openSystemNotificationSettings(context: Context) {
        try {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, appContext.packageName)
                }
            } else {
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", appContext.packageName, null)
                }
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Could not open system settings", Toast.LENGTH_SHORT).show()
        }
    }
}

private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
private data class Tuple7<A, B, C, D, E, F, G>(val a: A, val b: B, val c: C, val d: D, val e: E, val f: F, val g: G)
