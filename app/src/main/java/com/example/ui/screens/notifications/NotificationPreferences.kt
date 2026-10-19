package com.example.ui.screens.notifications

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NotificationSettingsState(
    // General
    val conversationTones: Boolean = true,
    val reminders: Boolean = true,

    // Messages
    val messageTone: String = "Default (Xylophone)",
    val messageVibrate: String = "Default",
    val messageLight: String = "White",
    val messageHighPriority: Boolean = true,
    val messageReactions: Boolean = true,

    // Groups
    val groupTone: String = "Default (Xylophone)",
    val groupVibrate: String = "Default",
    val groupLight: String = "White",
    val groupHighPriority: Boolean = true,
    val groupReactions: Boolean = true,

    // Calls
    val callRingtone: String = "Default (MiRemix)",
    val callVibrate: String = "Default",

    // Status
    val statusTone: String = "Default (Xylophone)",
    val statusVibrate: String = "Default",
    val statusHighPriority: Boolean = true,
    val statusReactions: Boolean = true
)

class NotificationPreferences(private val context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("olinam_notification_prefs", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(loadState())
    val state: StateFlow<NotificationSettingsState> = _state.asStateFlow()

    private fun loadState(): NotificationSettingsState {
        return NotificationSettingsState(
            conversationTones = prefs.getBoolean("conv_tones", true),
            reminders = prefs.getBoolean("reminders", true),

            messageTone = prefs.getString("msg_tone", "Default (Xylophone)") ?: "Default (Xylophone)",
            messageVibrate = prefs.getString("msg_vibrate", "Default") ?: "Default",
            messageLight = prefs.getString("msg_light", "White") ?: "White",
            messageHighPriority = prefs.getBoolean("msg_high_priority", true),
            messageReactions = prefs.getBoolean("msg_reactions", true),

            groupTone = prefs.getString("grp_tone", "Default (Xylophone)") ?: "Default (Xylophone)",
            groupVibrate = prefs.getString("grp_vibrate", "Default") ?: "Default",
            groupLight = prefs.getString("grp_light", "White") ?: "White",
            groupHighPriority = prefs.getBoolean("grp_high_priority", true),
            groupReactions = prefs.getBoolean("grp_reactions", true),

            callRingtone = prefs.getString("call_ringtone", "Default (MiRemix)") ?: "Default (MiRemix)",
            callVibrate = prefs.getString("call_vibrate", "Default") ?: "Default",

            statusTone = prefs.getString("status_tone", "Default (Xylophone)") ?: "Default (Xylophone)",
            statusVibrate = prefs.getString("status_vibrate", "Default") ?: "Default",
            statusHighPriority = prefs.getBoolean("status_high_priority", true),
            statusReactions = prefs.getBoolean("status_reactions", true)
        )
    }

    fun setConversationTones(enabled: Boolean) {
        prefs.edit().putBoolean("conv_tones", enabled).apply()
        _state.value = _state.value.copy(conversationTones = enabled)
    }

    fun setReminders(enabled: Boolean) {
        prefs.edit().putBoolean("reminders", enabled).apply()
        _state.value = _state.value.copy(reminders = enabled)
    }

    // Message Settings
    fun setMessageTone(tone: String) {
        prefs.edit().putString("msg_tone", tone).apply()
        _state.value = _state.value.copy(messageTone = tone)
    }

    fun setMessageVibrate(vibrate: String) {
        prefs.edit().putString("msg_vibrate", vibrate).apply()
        _state.value = _state.value.copy(messageVibrate = vibrate)
    }

    fun setMessageLight(light: String) {
        prefs.edit().putString("msg_light", light).apply()
        _state.value = _state.value.copy(messageLight = light)
    }

    fun setMessageHighPriority(enabled: Boolean) {
        prefs.edit().putBoolean("msg_high_priority", enabled).apply()
        _state.value = _state.value.copy(messageHighPriority = enabled)
    }

    fun setMessageReactions(enabled: Boolean) {
        prefs.edit().putBoolean("msg_reactions", enabled).apply()
        _state.value = _state.value.copy(messageReactions = enabled)
    }

    // Group Settings
    fun setGroupTone(tone: String) {
        prefs.edit().putString("grp_tone", tone).apply()
        _state.value = _state.value.copy(groupTone = tone)
    }

    fun setGroupVibrate(vibrate: String) {
        prefs.edit().putString("grp_vibrate", vibrate).apply()
        _state.value = _state.value.copy(groupVibrate = vibrate)
    }

    fun setGroupLight(light: String) {
        prefs.edit().putString("grp_light", light).apply()
        _state.value = _state.value.copy(groupLight = light)
    }

    fun setGroupHighPriority(enabled: Boolean) {
        prefs.edit().putBoolean("grp_high_priority", enabled).apply()
        _state.value = _state.value.copy(groupHighPriority = enabled)
    }

    fun setGroupReactions(enabled: Boolean) {
        prefs.edit().putBoolean("grp_reactions", enabled).apply()
        _state.value = _state.value.copy(groupReactions = enabled)
    }

    // Call Settings
    fun setCallRingtone(ringtone: String) {
        prefs.edit().putString("call_ringtone", ringtone).apply()
        _state.value = _state.value.copy(callRingtone = ringtone)
    }

    fun setCallVibrate(vibrate: String) {
        prefs.edit().putString("call_vibrate", vibrate).apply()
        _state.value = _state.value.copy(callVibrate = vibrate)
    }

    // Status Settings
    fun setStatusTone(tone: String) {
        prefs.edit().putString("status_tone", tone).apply()
        _state.value = _state.value.copy(statusTone = tone)
    }

    fun setStatusVibrate(vibrate: String) {
        prefs.edit().putString("status_vibrate", vibrate).apply()
        _state.value = _state.value.copy(statusVibrate = vibrate)
    }

    fun setStatusHighPriority(enabled: Boolean) {
        prefs.edit().putBoolean("status_high_priority", enabled).apply()
        _state.value = _state.value.copy(statusHighPriority = enabled)
    }

    fun setStatusReactions(enabled: Boolean) {
        prefs.edit().putBoolean("status_reactions", enabled).apply()
        _state.value = _state.value.copy(statusReactions = enabled)
    }

    fun resetToDefaults() {
        prefs.edit().clear().apply()
        _state.value = loadState()
    }

    fun triggerVibrationFeedback(pattern: String) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            } ?: return

            if (!vibrator.hasVibrator()) return

            when (pattern.lowercase()) {
                "off" -> { /* No vibration */ }
                "short" -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(100)
                    }
                }
                "long" -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(400)
                    }
                }
                else -> { // "Default"
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 150, 100, 150), -1))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(200)
                    }
                }
            }
        } catch (_: Throwable) {}
    }

    fun playSampleTone() {
        try {
            val toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP2, 180)
        } catch (_: Throwable) {}
    }
}
