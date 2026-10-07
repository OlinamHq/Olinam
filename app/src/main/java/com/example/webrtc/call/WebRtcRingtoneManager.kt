package com.example.webrtc.call

import android.content.Context
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class WebRtcRingtoneManager(private val context: Context) {
    private var toneGenerator: ToneGenerator? = null
    private var incomingRingtone: Ringtone? = null
    private var vibrator: Vibrator? = null

    init {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun startOutgoingRingback() {
        stopAll()
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_VOICE_CALL, ToneGenerator.MAX_VOLUME)
            toneGenerator?.startTone(ToneGenerator.TONE_SUP_RINGTONE)
        } catch (_: Exception) {}
    }

    fun startIncomingRinging() {
        stopAll()
        try {
            val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            incomingRingtone = RingtoneManager.getRingtone(context, notificationUri)
            incomingRingtone?.play()

            // Vibrate pattern
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val pattern = longArrayOf(0, 1000, 1000)
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 1000, 1000), 0)
            }
        } catch (_: Exception) {}
    }

    fun playDisconnectTone() {
        stopAll()
        try {
            val tone = ToneGenerator(AudioManager.STREAM_VOICE_CALL, ToneGenerator.MAX_VOLUME)
            tone.startTone(ToneGenerator.TONE_SUP_BUSY, 800)
        } catch (_: Exception) {}
    }

    fun stopAll() {
        try {
            toneGenerator?.stopTone()
            toneGenerator?.release()
            toneGenerator = null

            incomingRingtone?.stop()
            incomingRingtone = null

            vibrator?.cancel()
        } catch (_: Exception) {}
    }
}
