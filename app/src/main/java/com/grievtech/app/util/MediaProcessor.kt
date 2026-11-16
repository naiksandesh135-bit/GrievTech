package com.grievtech.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.grievtech.app.data.model.MediaType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

sealed class MediaProcessResult {
    data class Success(
        val bytes: ByteArray,
        val fileName: String,
        val mimeType: String,
        val mediaType: MediaType,
        val geoPoint: GeoPoint? = null
    ) : MediaProcessResult()

    data class Error(val message: String) : MediaProcessResult()
}

@Singleton
open class MediaProcessor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val MAX_IMAGE_SIZE_BYTES = 10 * 1024 * 1024L // 10 MB
        const val MAX_VIDEO_SIZE_BYTES = 50 * 1024 * 1024L // 50 MB
        const val MAX_PDF_SIZE_BYTES = 10 * 1024 * 1024L   // 10 MB
        const val MAX_IMAGE_DIMENSION = 1600
    }

    open fun processMediaUri(uri: Uri): MediaProcessResult {
        return try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri) ?: getMimeTypeFromExtension(uri)
            val fileSize = getFileSize(uri)

            val mediaType = when {
                mimeType.startsWith("image/") -> MediaType.IMAGE
                mimeType.startsWith("video/") -> MediaType.VIDEO
                mimeType == "application/pdf" || uri.toString().endsWith(".pdf", ignoreCase = true) -> MediaType.PDF
                else -> MediaType.IMAGE
            }

            val extractedGeoPoint = GeoTagReader.extractGeoPoint(context, uri)

            when (mediaType) {
                MediaType.IMAGE -> {
                    if (fileSize > MAX_IMAGE_SIZE_BYTES) {
                        return MediaProcessResult.Error("Image file size exceeds the 10 MB limit.")
                    }
                    val compressedBytes = compressImage(uri, extractedGeoPoint)
                    val extension = getExtensionFromMime(mimeType, "jpg")
                    val fileName = "img_${System.currentTimeMillis()}.$extension"
                    MediaProcessResult.Success(compressedBytes, fileName, mimeType, MediaType.IMAGE, extractedGeoPoint)
                }
                MediaType.VIDEO -> {
                    if (fileSize > MAX_VIDEO_SIZE_BYTES) {
                        return MediaProcessResult.Error("Video file size exceeds the 50 MB limit.")
                    }
                    val bytes = readBytes(uri)
                    val extension = getExtensionFromMime(mimeType, "mp4")
                    val fileName = "vid_${System.currentTimeMillis()}.$extension"
                    MediaProcessResult.Success(bytes, fileName, mimeType, MediaType.VIDEO, extractedGeoPoint)
                }
                MediaType.PDF -> {
                    if (fileSize > MAX_PDF_SIZE_BYTES) {
                        return MediaProcessResult.Error("PDF document size exceeds the 10 MB limit.")
                    }
                    val bytes = readBytes(uri)
                    val fileName = "doc_${System.currentTimeMillis()}.pdf"
                    MediaProcessResult.Success(bytes, fileName, "application/pdf", MediaType.PDF, null)
                }
            }
        } catch (e: Exception) {
            MediaProcessResult.Error(e.localizedMessage ?: "Failed to process media file.")
        }
    }

    private fun compressImage(uri: Uri, geoPoint: GeoPoint?): ByteArray {
        context.contentResolver.openInputStream(uri).use { inputStream ->
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(inputStream, null, options)

            val (width, height) = options.outWidth to options.outHeight
            var sampleSize = 1
            if (width > MAX_IMAGE_DIMENSION || height > MAX_IMAGE_DIMENSION) {
                val halfWidth = width / 2
                val halfHeight = height / 2
                while ((halfWidth / sampleSize) >= MAX_IMAGE_DIMENSION && (halfHeight / sampleSize) >= MAX_IMAGE_DIMENSION) {
                    sampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            val originalBitmap = context.contentResolver.openInputStream(uri).use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: throw IllegalArgumentException("Failed to decode image bitmap.")

            val rotatedBitmap = rotateBitmapIfRequired(uri, originalBitmap)
            val scaledBitmap = scaleBitmapToMax(rotatedBitmap, MAX_IMAGE_DIMENSION)

            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val compressedBytes = outputStream.toByteArray()

            if (geoPoint != null && geoPoint.isValid()) {
                try {
                    val tempFile = File.createTempFile("temp_compressed_", ".jpg", context.cacheDir)
                    tempFile.writeBytes(compressedBytes)
                    GeoTagReader.embedGpsIntoExif(tempFile.absolutePath, geoPoint.latitude, geoPoint.longitude)
                    val resultBytes = tempFile.readBytes()
                    tempFile.delete()
                    return resultBytes
                } catch (e: Exception) {
                    return compressedBytes
                }
            }
            return compressedBytes
        }
    }

    private fun rotateBitmapIfRequired(uri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val exif = inputStream?.use { ExifInterface(it) } ?: return bitmap
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                else -> return bitmap
            }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: Exception) {
            bitmap
        }
    }

    private fun scaleBitmapToMax(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDimension && height <= maxDimension) return bitmap

        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int
        if (width > height) {
            newWidth = maxDimension
            newHeight = (maxDimension / ratio).toInt()
        } else {
            newHeight = maxDimension
            newWidth = (maxDimension * ratio).toInt()
        }
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun getFileSize(uri: Uri): Long {
        return try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use {
                it.statSize
            } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    private fun readBytes(uri: Uri): ByteArray {
        return context.contentResolver.openInputStream(uri)?.use {
            it.readBytes()
        } ?: throw IllegalArgumentException("Could not read file data.")
    }

    private fun getMimeTypeFromExtension(uri: Uri): String {
        val path = uri.path ?: return "image/jpeg"
        return when {
            path.endsWith(".png", true) -> "image/png"
            path.endsWith(".mp4", true) -> "video/mp4"
            path.endsWith(".pdf", true) -> "application/pdf"
            else -> "image/jpeg"
        }
    }

    private fun getExtensionFromMime(mimeType: String, default: String): String {
        return when (mimeType) {
            "image/png" -> "png"
            "image/jpeg", "image/jpg" -> "jpg"
            "video/mp4" -> "mp4"
            "application/pdf" -> "pdf"
            else -> default
        }
    }
}
