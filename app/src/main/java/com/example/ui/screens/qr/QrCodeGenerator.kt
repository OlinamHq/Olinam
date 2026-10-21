package com.example.ui.screens.qr

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.model.UserProfile
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.io.File
import java.io.FileOutputStream
import java.util.EnumMap

object QrCodeGenerator {

    /**
     * Builds standard Olinam shareable short link for a user.
     */
    fun buildOlinamChatUrl(user: UserProfile): String {
        val phone = Uri.encode(user.phoneNumber.trim())
        val name = Uri.encode(user.name.trim())
        val uid = Uri.encode(user.id.trim())
        return "https://olinam.com/chat?phone=$phone&name=$name&uid=$uid"
    }

    /**
     * Generates a high quality, scannable QR Code bitmap for the given content.
     */
    fun generateQrBitmap(
        content: String,
        width: Int = 512,
        height: Int = 512,
        darkColor: Int = AndroidColor.parseColor("#111827"),
        lightColor: Int = AndroidColor.WHITE
    ): Bitmap? {
        if (content.isBlank()) return null
        return try {
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
                put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
                put(EncodeHintType.MARGIN, 1)
            }

            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints)
            val matrixWidth = bitMatrix.width
            val matrixHeight = bitMatrix.height
            val pixels = IntArray(matrixWidth * matrixHeight)

            for (y in 0 until matrixHeight) {
                val offset = y * matrixWidth
                for (x in 0 until matrixWidth) {
                    pixels[offset + x] = if (bitMatrix.get(x, y)) darkColor else lightColor
                }
            }

            val bitmap = Bitmap.createBitmap(matrixWidth, matrixHeight, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, matrixWidth, 0, 0, matrixWidth, matrixHeight)
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Saves QR code bitmap to cache file and generates share Intent with FileProvider URI.
     */
    fun shareQrCodeImage(context: Context, user: UserProfile, bitmap: Bitmap?) {
        val olinamUrl = buildOlinamChatUrl(user)
        val shareText = "Chat with me on Olinam! Scan my QR code or tap the link: $olinamUrl"

        if (bitmap == null) {
            // Text-only fallback
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Connect on Olinam: ${user.name}")
                putExtra(Intent.EXTRA_TEXT, shareText)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(Intent.createChooser(intent, "Share QR code").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
            return
        }

        try {
            val qrDir = File(context.cacheDir, "qr").apply { if (!exists()) mkdirs() }
            val file = File(qrDir, "olinam_qr_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
            }

            val authority = "${context.packageName}.fileprovider"
            val contentUri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_TEXT, shareText)
                putExtra(Intent.EXTRA_SUBJECT, "Connect on Olinam: ${user.name}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Share QR code").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to text
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(Intent.createChooser(intent, "Share QR code").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }
    }
}
