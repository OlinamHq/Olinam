package com.example.media

import android.content.Context
import android.net.Uri
import com.example.data.FirebaseChatRepository
import com.example.model.MediaType

/**
 * Reads a local file, uploads it to R2, then sends a chat message with the public URL.
 */
object MediaSender {
    suspend fun send(
        context: Context,
        repository: FirebaseChatRepository,
        conversationId: String,
        uri: Uri,
        mediaType: MediaType,
        caption: String = ""
    ): String {
        val resolver = context.contentResolver
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("Could not read selected file")
        val contentType = resolver.getType(uri) ?: defaultContentType(mediaType)
        val extension = extensionFor(contentType, mediaType)
        val uploaded = R2MediaUploader.upload(
            bytes = bytes,
            contentType = contentType,
            extension = extension,
            folder = folderFor(mediaType)
        )
        val preview = caption.ifBlank { previewFor(mediaType) }
        repository.sendMessage(
            conversationId = conversationId,
            text = preview,
            mediaUrl = uploaded.publicUrl,
            mediaType = mediaType
        )
        return uploaded.publicUrl
    }

    private fun folderFor(type: MediaType) = when (type) {
        MediaType.IMAGE -> "images"
        MediaType.AUDIO, MediaType.VOICE -> "voice"
        MediaType.VIDEO -> "videos"
        MediaType.DOCUMENT -> "docs"
        MediaType.TEXT -> "media"
    }

    private fun defaultContentType(type: MediaType) = when (type) {
        MediaType.IMAGE -> "image/jpeg"
        MediaType.AUDIO, MediaType.VOICE -> "audio/mp4"
        MediaType.VIDEO -> "video/mp4"
        MediaType.DOCUMENT -> "application/octet-stream"
        MediaType.TEXT -> "text/plain"
    }

    private fun extensionFor(contentType: String, type: MediaType): String {
        return when {
            contentType.contains("png") -> "png"
            contentType.contains("webp") -> "webp"
            contentType.contains("jpeg") || contentType.contains("jpg") -> "jpg"
            contentType.contains("mp4") && type == MediaType.VIDEO -> "mp4"
            contentType.contains("mpeg") || contentType.contains("mp3") -> "mp3"
            contentType.contains("ogg") -> "ogg"
            contentType.contains("pdf") -> "pdf"
            else -> when (type) {
                MediaType.IMAGE -> "jpg"
                MediaType.VIDEO -> "mp4"
                MediaType.AUDIO, MediaType.VOICE -> "m4a"
                MediaType.DOCUMENT -> "bin"
                MediaType.TEXT -> "txt"
            }
        }
    }

    private fun previewFor(type: MediaType) = when (type) {
        MediaType.IMAGE -> "Photo"
        MediaType.AUDIO, MediaType.VOICE -> "Voice message"
        MediaType.VIDEO -> "Video"
        MediaType.DOCUMENT -> "Document"
        MediaType.TEXT -> ""
    }
}
