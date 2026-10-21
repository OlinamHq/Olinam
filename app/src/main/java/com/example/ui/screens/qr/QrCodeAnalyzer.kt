package com.example.ui.screens.qr

import android.graphics.Bitmap
import android.net.Uri
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer

data class ScannedContactInfo(
    val name: String,
    val phoneNumber: String,
    val rawData: String,
    val isOlinamUrl: Boolean
)

object QrCodeAnalyzer {

    /**
     * Parses scanned text payload into structured contact information.
     */
    fun parseQrPayload(raw: String): ScannedContactInfo {
        val trimmed = raw.trim()
        val uri = try { Uri.parse(trimmed) } catch (_: Exception) { null }

        if (uri != null && (trimmed.contains("olinam.com/chat") || uri.scheme.equals("olinam", ignoreCase = true))) {
            val phone = uri.getQueryParameter("phone") ?: ""
            val name = uri.getQueryParameter("name") ?: (if (phone.isNotBlank()) phone else "Olinam Contact")
            return ScannedContactInfo(
                name = name.ifBlank { "Olinam Contact" },
                phoneNumber = phone,
                rawData = trimmed,
                isOlinamUrl = true
            )
        }

        if (trimmed.startsWith("tel:", ignoreCase = true)) {
            val phone = trimmed.substring(4).trim()
            return ScannedContactInfo(
                name = "Phone Contact",
                phoneNumber = phone,
                rawData = trimmed,
                isOlinamUrl = false
            )
        }

        // Check if raw text looks like a phone number
        val digitsOnly = trimmed.filter { it.isDigit() }
        if (digitsOnly.length in 7..15 && trimmed.length <= 25) {
            return ScannedContactInfo(
                name = "Contact",
                phoneNumber = trimmed,
                rawData = trimmed,
                isOlinamUrl = false
            )
        }

        // Generic payload
        return ScannedContactInfo(
            name = "Scanned Code",
            phoneNumber = "",
            rawData = trimmed,
            isOlinamUrl = false
        )
    }

    /**
     * Decodes QR code from a gallery Bitmap.
     */
    fun decodeBitmap(bitmap: Bitmap): String? {
        return try {
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            val source = RGBLuminanceSource(width, height, pixels)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            val reader = MultiFormatReader()
            val result = reader.decode(binaryBitmap)
            result.text
        } catch (_: NotFoundException) {
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * CameraX ImageAnalysis analyzer for live camera feed.
     */
    class CameraFrameAnalyzer(
        private val onQrCodeDetected: (String) -> Unit
    ) : ImageAnalysis.Analyzer {

        private val reader = MultiFormatReader()
        private var isProcessing = false

        override fun analyze(image: ImageProxy) {
            if (isProcessing) {
                image.close()
                return
            }

            try {
                val plane = image.planes[0]
                val buffer: ByteBuffer = plane.buffer
                val data = ByteArray(buffer.remaining())
                buffer.get(data)

                val width = image.width
                val height = image.height

                val source = PlanarYUVLuminanceSource(
                    data,
                    width,
                    height,
                    0,
                    0,
                    width,
                    height,
                    false
                )
                val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

                try {
                    val result = reader.decodeWithState(binaryBitmap)
                    val text = result.text
                    if (!text.isNullOrBlank()) {
                        isProcessing = true
                        onQrCodeDetected(text)
                    }
                } catch (_: NotFoundException) {
                    // No barcode in this frame, expected during normal camera feed
                }
            } catch (e: Exception) {
                // Ignore transient frame analysis errors
            } finally {
                image.close()
            }
        }

        fun resumeScanning() {
            isProcessing = false
        }
    }
}
