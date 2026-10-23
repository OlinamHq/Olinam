package com.example.ui.components.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
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

/**
 * Production-ready audio playback manager using Android MediaPlayer.
 * Supports per-item playback state, live scrubbing, variable playback speed (1x, 1.5x, 2x),
 * and automatic previous player reset.
 */
object AudioPlaybackManager {
    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    private var activeAudioId: String? = null

    private val _currentPlayingId = MutableStateFlow<String?>(null)
    val currentPlayingId: StateFlow<String?> = _currentPlayingId.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0f)
    val playbackProgress: StateFlow<Float> = _playbackProgress.asStateFlow()

    private val _currentPositionSec = MutableStateFlow(0)
    val currentPositionSec: StateFlow<Int> = _currentPositionSec.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    fun togglePlay(
        context: Context,
        audioId: String,
        audioSource: String,
        coroutineScope: CoroutineScope
    ) {
        if (activeAudioId == audioId && _isPlaying.value) {
            pause()
        } else if (activeAudioId == audioId && mediaPlayer != null) {
            resume(coroutineScope)
        } else {
            playNew(context, audioId, audioSource, coroutineScope)
        }
    }

    fun playNew(
        context: Context,
        audioId: String,
        audioSource: String,
        coroutineScope: CoroutineScope
    ) {
        stop()

        try {
            val player = MediaPlayer()
            val uri = Uri.parse(audioSource)

            if (audioSource.startsWith("http://") || audioSource.startsWith("https://")) {
                player.setDataSource(audioSource)
            } else {
                player.setDataSource(context, uri)
            }

            player.setOnPreparedListener { mp ->
                activeAudioId = audioId
                _currentPlayingId.value = audioId
                applySpeed(mp, _playbackSpeed.value)
                mp.start()
                _isPlaying.value = true
                startProgressTracker(coroutineScope)
            }

            player.setOnCompletionListener {
                _isPlaying.value = false
                _playbackProgress.value = 0f
                _currentPositionSec.value = 0
                progressJob?.cancel()
            }

            player.setOnErrorListener { _, what, extra ->
                Log.e("AudioPlaybackManager", "MediaPlayer error: what=$what extra=$extra")
                stop()
                true
            }

            player.prepareAsync()
            mediaPlayer = player
        } catch (e: Exception) {
            Log.e("AudioPlaybackManager", "Error preparing media player: ${e.message}", e)
            stop()
        }
    }

    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            }
        }
        _isPlaying.value = false
        progressJob?.cancel()
    }

    fun resume(coroutineScope: CoroutineScope) {
        mediaPlayer?.let {
            applySpeed(it, _playbackSpeed.value)
            it.start()
            _isPlaying.value = true
            startProgressTracker(coroutineScope)
        }
    }

    fun seekTo(progress: Float) {
        mediaPlayer?.let {
            val duration = it.duration
            if (duration > 0) {
                val targetMs = (progress.coerceIn(0f, 1f) * duration).toInt()
                it.seekTo(targetMs)
                _playbackProgress.value = progress
                _currentPositionSec.value = targetMs / 1000
            }
        }
    }

    fun cycleSpeed() {
        val nextSpeed = when (_playbackSpeed.value) {
            1.0f -> 1.5f
            1.5f -> 2.0f
            else -> 1.0f
        }
        _playbackSpeed.value = nextSpeed
        mediaPlayer?.let { applySpeed(it, nextSpeed) }
    }

    private fun applySpeed(player: MediaPlayer, speed: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val params = player.playbackParams ?: PlaybackParams()
                params.speed = speed
                player.playbackParams = params
            } catch (e: Exception) {
                Log.w("AudioPlaybackManager", "Unable to set playback speed: ${e.message}")
            }
        }
    }

    private fun startProgressTracker(scope: CoroutineScope) {
        progressJob?.cancel()
        progressJob = scope.launch(Dispatchers.Default) {
            while (isActive && _isPlaying.value) {
                mediaPlayer?.let { player ->
                    try {
                        val duration = player.duration
                        val current = player.currentPosition
                        if (duration > 0) {
                            _playbackProgress.value = current.toFloat() / duration.toFloat()
                            _currentPositionSec.value = current / 1000
                        }
                    } catch (_: Exception) {}
                }
                delay(100)
            }
        }
    }

    fun stop() {
        progressJob?.cancel()
        progressJob = null
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (_: Exception) {}
        mediaPlayer = null
        activeAudioId = null
        _currentPlayingId.value = null
        _isPlaying.value = false
        _playbackProgress.value = 0f
        _currentPositionSec.value = 0
    }
}
