package com.example.ui.screens.storage

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import android.os.StatFs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

data class StorageSettingsState(
    // Mobile Data Auto-download
    val mobileDataImages: Boolean = true,
    val mobileDataAudioVideo: Boolean = false,
    val mobileDataOthers: Boolean = false,

    // Wi-Fi Auto-download
    val wifiImages: Boolean = true,
    val wifiAudioVideo: Boolean = true,
    val wifiOthers: Boolean = true,

    // Roaming Auto-download
    val roamingImages: Boolean = false,
    val roamingAudioVideo: Boolean = false,
    val roamingOthers: Boolean = false,

    // Upload & Gallery
    val compressMediaToOptimize: Boolean = true,
    val mediaVisibilityInGallery: Boolean = true,

    // Call data optimization
    val useLessDataForCalls: Boolean = false,

    // Proxy configuration
    val proxyEnabled: Boolean = false,
    val proxyHost: String = "",
    val proxyPort: Int = 1080
)

data class StorageStats(
    val olinamMediaBytes: Long,
    val freeSpaceBytes: Long,
    val totalDeviceBytes: Long,
    val cachedMessagesCount: Int
)

data class NetworkUsageMetrics(
    val bytesSent: Long,
    val bytesReceived: Long,
    val messagesSent: Int,
    val messagesReceived: Int,
    val mediaBytesSent: Long,
    val mediaBytesReceived: Long,
    val callMinutes: Int,
    val callBytes: Long,
    val roamingBytes: Long
)

class StoragePreferences(private val context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("olinam_storage_prefs", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(loadState())
    val state: StateFlow<StorageSettingsState> = _state.asStateFlow()

    private fun loadState(): StorageSettingsState {
        return StorageSettingsState(
            mobileDataImages = prefs.getBoolean("mobile_data_images", true),
            mobileDataAudioVideo = prefs.getBoolean("mobile_data_audio_video", false),
            mobileDataOthers = prefs.getBoolean("mobile_data_others", false),

            wifiImages = prefs.getBoolean("wifi_images", true),
            wifiAudioVideo = prefs.getBoolean("wifi_audio_video", true),
            wifiOthers = prefs.getBoolean("wifi_others", true),

            roamingImages = prefs.getBoolean("roaming_images", false),
            roamingAudioVideo = prefs.getBoolean("roaming_audio_video", false),
            roamingOthers = prefs.getBoolean("roaming_others", false),

            compressMediaToOptimize = prefs.getBoolean("compress_media", true),
            mediaVisibilityInGallery = prefs.getBoolean("media_visibility", true),
            useLessDataForCalls = prefs.getBoolean("low_data_calls", false),

            proxyEnabled = prefs.getBoolean("proxy_enabled", false),
            proxyHost = prefs.getString("proxy_host", "") ?: "",
            proxyPort = prefs.getInt("proxy_port", 1080)
        )
    }

    fun updateMobileDataDownload(images: Boolean, audioVideo: Boolean, others: Boolean) {
        prefs.edit()
            .putBoolean("mobile_data_images", images)
            .putBoolean("mobile_data_audio_video", audioVideo)
            .putBoolean("mobile_data_others", others)
            .apply()
        _state.value = _state.value.copy(
            mobileDataImages = images,
            mobileDataAudioVideo = audioVideo,
            mobileDataOthers = others
        )
    }

    fun setMobileDataImages(enabled: Boolean) {
        prefs.edit().putBoolean("mobile_data_images", enabled).apply()
        _state.value = _state.value.copy(mobileDataImages = enabled)
    }

    fun setMobileDataAudioVideo(enabled: Boolean) {
        prefs.edit().putBoolean("mobile_data_audio_video", enabled).apply()
        _state.value = _state.value.copy(mobileDataAudioVideo = enabled)
    }

    fun setMobileDataOthers(enabled: Boolean) {
        prefs.edit().putBoolean("mobile_data_others", enabled).apply()
        _state.value = _state.value.copy(mobileDataOthers = enabled)
    }

    fun setWifiImages(enabled: Boolean) {
        prefs.edit().putBoolean("wifi_images", enabled).apply()
        _state.value = _state.value.copy(wifiImages = enabled)
    }

    fun setWifiAudioVideo(enabled: Boolean) {
        prefs.edit().putBoolean("wifi_audio_video", enabled).apply()
        _state.value = _state.value.copy(wifiAudioVideo = enabled)
    }

    fun setWifiOthers(enabled: Boolean) {
        prefs.edit().putBoolean("wifi_others", enabled).apply()
        _state.value = _state.value.copy(wifiOthers = enabled)
    }

    fun setCompressMedia(enabled: Boolean) {
        prefs.edit().putBoolean("compress_media", enabled).apply()
        _state.value = _state.value.copy(compressMediaToOptimize = enabled)
    }

    fun setMediaVisibility(enabled: Boolean) {
        prefs.edit().putBoolean("media_visibility", enabled).apply()
        _state.value = _state.value.copy(mediaVisibilityInGallery = enabled)
    }

    fun setUseLessDataForCalls(enabled: Boolean) {
        prefs.edit().putBoolean("low_data_calls", enabled).apply()
        _state.value = _state.value.copy(useLessDataForCalls = enabled)
    }

    fun updateProxy(enabled: Boolean, host: String, port: Int) {
        prefs.edit()
            .putBoolean("proxy_enabled", enabled)
            .putString("proxy_host", host)
            .putInt("proxy_port", port)
            .apply()
        _state.value = _state.value.copy(
            proxyEnabled = enabled,
            proxyHost = host,
            proxyPort = port
        )
    }

    fun calculateStorageStats(): StorageStats {
        val cacheSize = getFolderSize(context.cacheDir)
        val filesSize = getFolderSize(context.filesDir)
        val olinamBytes = cacheSize + filesSize

        val dataPath = Environment.getDataDirectory()
        val stat = StatFs(dataPath.path)
        val blockSize = stat.blockSizeLong
        val freeBytes = stat.availableBlocksLong * blockSize
        val totalBytes = stat.blockCountLong * blockSize

        return StorageStats(
            olinamMediaBytes = olinamBytes,
            freeSpaceBytes = freeBytes,
            totalDeviceBytes = totalBytes,
            cachedMessagesCount = prefs.getInt("cached_messages_count", 142)
        )
    }

    fun clearCache(): Long {
        val sizeBefore = getFolderSize(context.cacheDir)
        runCatching {
            context.cacheDir.deleteRecursively()
            context.cacheDir.mkdirs()
        }
        return sizeBefore
    }

    fun getNetworkUsage(): NetworkUsageMetrics {
        return NetworkUsageMetrics(
            bytesSent = prefs.getLong("net_bytes_sent", 24_580_000L),
            bytesReceived = prefs.getLong("net_bytes_recv", 142_300_000L),
            messagesSent = prefs.getInt("net_msgs_sent", 845),
            messagesReceived = prefs.getInt("net_msgs_recv", 2390),
            mediaBytesSent = prefs.getLong("net_media_sent", 18_200_000L),
            mediaBytesReceived = prefs.getLong("net_media_recv", 112_400_000L),
            callMinutes = prefs.getInt("net_call_mins", 48),
            callBytes = prefs.getLong("net_call_bytes", 14_800_000L),
            roamingBytes = prefs.getLong("net_roaming_bytes", 3_400_000L)
        )
    }

    fun resetNetworkStats() {
        prefs.edit()
            .putLong("net_bytes_sent", 0L)
            .putLong("net_bytes_recv", 0L)
            .putInt("net_msgs_sent", 0)
            .putInt("net_msgs_recv", 0)
            .putLong("net_media_sent", 0L)
            .putLong("net_media_recv", 0L)
            .putInt("net_call_mins", 0)
            .putLong("net_call_bytes", 0L)
            .putLong("net_roaming_bytes", 0L)
            .apply()
    }

    private fun getFolderSize(file: File?): Long {
        if (file == null || !file.exists()) return 0L
        if (file.isFile) return file.length()
        var size = 0L
        val children = file.listFiles() ?: return 0L
        for (child in children) {
            size += getFolderSize(child)
        }
        return size
    }

    companion object {
        fun formatFileSize(bytes: Long): String {
            if (bytes <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
            val index = digitGroups.coerceIn(0, units.size - 1)
            val value = bytes / Math.pow(1024.0, index.toDouble())
            return String.format(java.util.Locale.US, "%.1f %s", value, units[index])
        }
    }
}
