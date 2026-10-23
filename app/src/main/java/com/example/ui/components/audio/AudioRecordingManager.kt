package com.example.ui.components.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

/**
 * Production-ready Android MediaRecorder manager for WhatsApp-style voice messages.
 * Handles mic initialization, AAC encoding, amplitude sampling for live waveforms,
 * duration tracking, and cleanup.
 */
class AudioRecordingManager(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var timerJob: Job? = null
    private var startTimeMillis: Long = 0L

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _durationSeconds = MutableStateFlow(0)
    val durationSeconds: StateFlow<Int> = _durationSeconds.asStateFlow()

    private val _amplitude = MutableStateFlow(0f)
    val amplitude: StateFlow<Float> = _amplitude.asStateFlow()

    fun startRecording(coroutineScope: CoroutineScope): Boolean {
        if (_isRecording.value) return false
        try {
            val voiceDir = File(context.cacheDir, "voice_notes").apply { mkdirs() }
            val outputFile = File(voiceDir, "voice_${System.currentTimeMillis()}.m4a")
            currentOutputFile = outputFile

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(64000)
                setAudioSamplingRate(44100)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            _isRecording.value = true
            startTimeMillis = System.currentTimeMillis()
            _durationSeconds.value = 0

            // Live duration ticker & amplitude polling
            timerJob?.cancel()
            timerJob = coroutineScope.launch(Dispatchers.Default) {
                while (isActive && _isRecording.value) {
                    val elapsed = ((System.currentTimeMillis() - startTimeMillis) / 1000).toInt()
                    _durationSeconds.value = elapsed

                    try {
                        val maxAmp = mediaRecorder?.maxAmplitude ?: 0
                        val norm = (maxAmp.toFloat() / 32767f).coerceIn(0.05f, 1f)
                        _amplitude.value = norm
                    } catch (_: Exception) {
                        _amplitude.value = 0.1f
                    }

                    delay(100)
                }
            }

            return true
        } catch (e: Exception) {
            Log.e("AudioRecordingManager", "Failed to start recording: ${e.message}", e)
            cleanup()
            return false
        }
    }

    /**
     * Stops recording and returns the recorded audio File along with total duration in seconds.
     */
    fun stopRecording(): Pair<File, Int>? {
        if (!_isRecording.value) return null
        timerJob?.cancel()
        timerJob = null

        val finalDuration = _durationSeconds.value.coerceAtLeast(1)
        val file = currentOutputFile

        try {
            mediaRecorder?.apply {
                try {
                    stop()
                } catch (e: Exception) {
                    Log.w("AudioRecordingManager", "MediaRecorder.stop() failed: ${e.message}")
                }
                release()
            }
        } catch (e: Exception) {
            Log.e("AudioRecordingManager", "Error stopping recorder", e)
        } finally {
            mediaRecorder = null
            _isRecording.value = false
            _amplitude.value = 0f
        }

        return if (file != null && file.exists() && file.length() > 0) {
            Pair(file, finalDuration)
        } else {
            file?.delete()
            null
        }
    }

    /**
     * Discards the active recording without saving.
     */
    fun cancelRecording() {
        timerJob?.cancel()
        timerJob = null

        try {
            mediaRecorder?.apply {
                try {
                    stop()
                } catch (_: Exception) {}
                release()
            }
        } catch (_: Exception) {}
        finally {
            mediaRecorder = null
            _isRecording.value = false
            _amplitude.value = 0f
            currentOutputFile?.delete()
            currentOutputFile = null
        }
    }

    private fun cleanup() {
        timerJob?.cancel()
        timerJob = null
        try {
            mediaRecorder?.release()
        } catch (_: Exception) {}
        mediaRecorder = null
        _isRecording.value = false
        currentOutputFile?.delete()
        currentOutputFile = null
    }
}
