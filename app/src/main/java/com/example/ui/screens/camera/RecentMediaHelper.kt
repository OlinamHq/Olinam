package com.example.ui.screens.camera

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import java.util.Locale

data class RecentMediaItem(
    val id: String,
    val uri: Uri,
    val isVideo: Boolean = false,
    val duration: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

object RecentMediaHelper {
    // In-memory cache for media captured/selected during the session
    private val sessionMedia = mutableListOf<RecentMediaItem>()

    fun registerCapturedMedia(uri: Uri, isVideo: Boolean = false, duration: String? = null) {
        val item = RecentMediaItem(
            id = "session_${System.currentTimeMillis()}",
            uri = uri,
            isVideo = isVideo,
            duration = duration,
            timestamp = System.currentTimeMillis()
        )
        sessionMedia.removeAll { it.uri == uri }
        sessionMedia.add(0, item)
    }

    fun getRecentMedia(context: Context): List<RecentMediaItem> {
        val result = mutableListOf<RecentMediaItem>()
        // First add session-captured media
        result.addAll(sessionMedia)

        // Query Device MediaStore for Images
        try {
            val imgProjection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATE_ADDED
            )
            val imgSortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                imgProjection,
                null,
                null,
                imgSortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                var count = 0
                while (cursor.moveToNext() && count < 20) {
                    val id = cursor.getLong(idCol)
                    val dateAdded = cursor.getLong(dateCol) * 1000L
                    val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                    result.add(
                        RecentMediaItem(
                            id = "img_$id",
                            uri = uri,
                            isVideo = false,
                            timestamp = dateAdded
                        )
                    )
                    count++
                }
            }
        } catch (_: Exception) {}

        // Query Device MediaStore for Videos
        try {
            val vidProjection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.DATE_ADDED
            )
            val vidSortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"
            context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                vidProjection,
                null,
                null,
                vidSortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val durCol = cursor.getColumnIndex(MediaStore.Video.Media.DURATION)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                var count = 0
                while (cursor.moveToNext() && count < 10) {
                    val id = cursor.getLong(idCol)
                    val durationMs = if (durCol != -1) cursor.getLong(durCol) else 0L
                    val dateAdded = cursor.getLong(dateCol) * 1000L
                    val uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                    val durStr = formatDuration(durationMs)
                    result.add(
                        RecentMediaItem(
                            id = "vid_$id",
                            uri = uri,
                            isVideo = true,
                            duration = durStr,
                            timestamp = dateAdded
                        )
                    )
                    count++
                }
            }
        } catch (_: Exception) {}

        // Sort by timestamp descending
        result.sortByDescending { it.timestamp }

        return result
    }

    private fun formatDuration(durationMs: Long): String {
        if (durationMs <= 0) return "0:15"
        val totalSeconds = durationMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
    }
}
