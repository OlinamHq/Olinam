package com.example.webrtc.media

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.os.PowerManager
import android.util.Log

enum class AudioDevice {
    SPEAKERPHONE,
    EARPIECE,
    BLUETOOTH_HEADSET,
    WIRED_HEADSET
}

class WebRtcAudioSwitch(private val context: Context) : SensorEventListener {
    private val tag = "WebRtcAudioSwitch"
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager

    private var proximitySensor: Sensor? = null
    private var proximityWakeLock: PowerManager.WakeLock? = null

    var selectedAudioDevice: AudioDevice = AudioDevice.EARPIECE
        private set

    init {
        proximitySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        try {
            if (powerManager?.isWakeLockLevelSupported(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK) == true) {
                proximityWakeLock = powerManager.newWakeLock(
                    PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
                    "olinam:webrtc_proximity_lock"
                )
            }
        } catch (_: Exception) {}
    }

    fun start(isSpeakerDefault: Boolean = false) {
        audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION

        if (proximitySensor != null) {
            sensorManager?.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL)
        }

        if (isSpeakerDefault) {
            selectAudioDevice(AudioDevice.SPEAKERPHONE)
        } else {
            selectAudioDevice(AudioDevice.EARPIECE)
        }
    }

    fun selectAudioDevice(device: AudioDevice) {
        selectedAudioDevice = device
        when (device) {
            AudioDevice.SPEAKERPHONE -> {
                audioManager?.isSpeakerphoneOn = true
                releaseProximityLock()
            }
            AudioDevice.EARPIECE -> {
                audioManager?.isSpeakerphoneOn = false
            }
            AudioDevice.BLUETOOTH_HEADSET -> {
                audioManager?.isSpeakerphoneOn = false
                audioManager?.startBluetoothSco()
                audioManager?.isBluetoothScoOn = true
            }
            AudioDevice.WIRED_HEADSET -> {
                audioManager?.isSpeakerphoneOn = false
            }
        }
    }

    fun toggleSpeaker(): Boolean {
        val newDevice = if (selectedAudioDevice == AudioDevice.SPEAKERPHONE) {
            AudioDevice.EARPIECE
        } else {
            AudioDevice.SPEAKERPHONE
        }
        selectAudioDevice(newDevice)
        return selectedAudioDevice == AudioDevice.SPEAKERPHONE
    }

    fun stop() {
        try {
            sensorManager?.unregisterListener(this)
            releaseProximityLock()
            audioManager?.isSpeakerphoneOn = false
            audioManager?.mode = AudioManager.MODE_NORMAL
        } catch (e: Exception) {
            Log.e(tag, "Error resetting audio device: ${e.message}")
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || proximitySensor == null) return
        val distance = event.values[0]
        val maxRange = proximitySensor?.maximumRange ?: 5f
        val isNear = distance < maxRange

        if (isNear && selectedAudioDevice == AudioDevice.EARPIECE) {
            acquireProximityLock()
        } else {
            releaseProximityLock()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun acquireProximityLock() {
        if (proximityWakeLock?.isHeld == false) {
            try {
                proximityWakeLock?.acquire(30 * 60 * 1000L) // 30 mins timeout
            } catch (_: Exception) {}
        }
    }

    private fun releaseProximityLock() {
        if (proximityWakeLock?.isHeld == true) {
            try {
                proximityWakeLock?.release()
            } catch (_: Exception) {}
        }
    }
}
