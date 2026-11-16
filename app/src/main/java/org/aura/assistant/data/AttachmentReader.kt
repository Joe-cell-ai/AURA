package org.aura.assistant.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

object AttachmentReader {
    private const val MAX_IMAGE_BYTES = 1500000
    private const val MAX_DOCUMENT_BYTES = 32000

    fun imageDataUrl(context: Context, uri: Uri): String {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Cannot open image." }
            BitmapFactory.decodeStream(input, null, bounds)
        }
        require(bounds.outWidth > 0 && bounds.outHeight > 0) { "Unsupported image." }
        var sample = 1
        while (bounds.outWidth / sample > 1400 || bounds.outHeight / sample > 1400) sample *= 2
        val options = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.RGB_565
        }
        val bitmap = context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Cannot reopen image." }
            BitmapFactory.decodeStream(input, null, options)
        } ?: error("Could not decode image.")
        try {
            val output = ByteArrayOutputStream()
            require(bitmap.compress(Bitmap.CompressFormat.JPEG, 78, output)) { "Image encoding failed." }
            val bytes = output.toByteArray()
            require(bytes.size <= MAX_IMAGE_BYTES) { "Image exceeds the 1.5 MB upload limit." }
            return "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
        } finally {
            bitmap.recycle()
        }
    }

    fun documentText(context: Context, uri: Uri): String {
        val mime = context.contentResolver.getType(uri).orEmpty().lowercase()
        require(mime == "text/plain" || mime == "text/markdown" || mime == "text/csv" || mime == "application/json") {
            "Choose a text, Markdown, CSV or JSON document. PDF extraction is not available yet."
        }
        val bytes = context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Cannot open document." }
            input.readNBytes(MAX_DOCUMENT_BYTES + 1)
        }
        require(bytes.size <= MAX_DOCUMENT_BYTES) { "Document exceeds the 32 KB limit." }
        return bytes.toString(Charsets.UTF_8).trim().also {
            require(it.isNotBlank()) { "Document is empty." }
        }
    }

    fun isDocumentSizeAllowed(size: Int): Boolean = size in 1..MAX_DOCUMENT_BYTES
}
