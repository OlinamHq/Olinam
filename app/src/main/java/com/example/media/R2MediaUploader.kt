package com.example.media

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Uploads media to Cloudflare R2 via a presign worker.
 * Secrets stay on the worker. The app only receives a one-time PUT URL.
 */
object R2MediaUploader {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    data class UploadResult(val publicUrl: String, val objectKey: String)

    suspend fun upload(
        bytes: ByteArray,
        contentType: String,
        extension: String,
        folder: String = "media"
    ): UploadResult = withContext(Dispatchers.IO) {
        val presignUrl = BuildConfig.R2_PRESIGN_URL.ifBlank {
            "https://olinam-r2-media.olinamhq.workers.dev/"
        }
        val publicBase = BuildConfig.R2_PUBLIC_BASE.ifBlank {
            "https://olinam-r2-media.olinamhq.workers.dev/file"
        }.trimEnd('/')
        require(presignUrl.isNotBlank()) { "R2_PRESIGN_URL is empty." }
        require(publicBase.isNotBlank()) { "R2_PUBLIC_BASE is empty." }

        val objectKey = "$folder/${UUID.randomUUID()}.$extension"
        val presignRequest = Request.Builder()
            .url(presignUrl)
            .post(
                JSONObject()
                    .put("key", objectKey)
                    .put("contentType", contentType)
                    .toString()
                    .toRequestBody("application/json".toMediaType())
            )
            .build()

        val presignResponse = client.newCall(presignRequest).execute()
        val presignBody = presignResponse.body?.string().orEmpty()
        if (!presignResponse.isSuccessful) {
            error("R2 presign failed ${presignResponse.code}: $presignBody")
        }
        val uploadUrl = JSONObject(presignBody).getString("uploadUrl")

        val put = Request.Builder()
            .url(uploadUrl)
            .put(bytes.toRequestBody(contentType.toMediaType()))
            .build()
        val putResponse = client.newCall(put).execute()
        if (!putResponse.isSuccessful) {
            error("R2 upload failed ${putResponse.code}")
        }
        UploadResult(publicUrl = "$publicBase/$objectKey", objectKey = objectKey)
    }
}
