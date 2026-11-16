package com.grievtech.app.util

import android.content.Context
import android.location.Location
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import java.io.InputStream
import java.util.regex.Pattern

data class GeoPoint(
    val latitude: Double,
    val longitude: Double
) {
    fun isValid(): Boolean {
        if (latitude == 0.0 && longitude == 0.0) return false
        if (latitude < -90.0 || latitude > 90.0) return false
        if (longitude < -180.0 || longitude > 180.0) return false
        return true
    }
}

object GeoTagReader {

    private val ISO_6709_PATTERN = Pattern.compile("([+-]\\d+\\.\\d+)([+-]\\d+\\.\\d+)")

    fun extractGeoPoint(context: Context, uri: Uri): GeoPoint? {
        val mimeType = context.contentResolver.getType(uri) ?: ""
        return if (mimeType.startsWith("video/")) {
            extractVideoGeoPoint(context, uri)
        } else {
            extractImageGeoPoint(context, uri)
        }
    }

    fun extractImageGeoPoint(context: Context, uri: Uri): GeoPoint? {
        return try {
            val targetUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && uri.scheme == "content") {
                try {
                    MediaStore.setRequireOriginal(uri)
                } catch (e: Exception) {
                    uri
                }
            } else {
                uri
            }

            val inputStream: InputStream? = context.contentResolver.openInputStream(targetUri)
                ?: context.contentResolver.openInputStream(uri)

            inputStream?.use { stream ->
                val exif = ExifInterface(stream)
                val latLongArray = exif.latLong
                if (latLongArray != null && latLongArray.size >= 2) {
                    val point = GeoPoint(latLongArray[0], latLongArray[1])
                    if (point.isValid()) point else null
                } else null
            }
        } catch (e: Exception) {
            Log.w("GeoTagReader", "Failed to extract image EXIF GPS: ${e.message}")
            null
        }
    }

    fun extractVideoGeoPoint(context: Context, uri: Uri): GeoPoint? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            val locationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_LOCATION)
            parseIso6709Location(locationStr)
        } catch (e: Exception) {
            Log.w("GeoTagReader", "Failed to extract video location: ${e.message}")
            null
        } finally {
            try {
                retriever.release()
            } catch (ignored: Exception) {}
        }
    }

    fun parseIso6709Location(locationStr: String?): GeoPoint? {
        if (locationStr.isNullOrBlank()) return null
        val matcher = ISO_6709_PATTERN.matcher(locationStr as CharSequence)
        return if (matcher.find()) {
            try {
                val lat = matcher.group(1)?.toDouble() ?: 0.0
                val lng = matcher.group(2)?.toDouble() ?: 0.0
                val point = GeoPoint(lat, lng)
                if (point.isValid()) point else null
            } catch (e: Exception) {
                null
            }
        } else null
    }

    fun embedGpsIntoExif(filePath: String, latitude: Double, longitude: Double) {
        try {
            val exif = ExifInterface(filePath)
            val loc = Location("dummy").apply {
                this.latitude = latitude
                this.longitude = longitude
            }
            exif.setGpsInfo(loc)
            exif.saveAttributes()
            Log.d("GeoTagReader", "Embedded GPS into EXIF: $latitude, $longitude at $filePath")
        } catch (e: Exception) {
            Log.e("GeoTagReader", "Failed to embed GPS into EXIF: ${e.message}")
        }
    }

    fun copyGpsAttributes(sourceExif: ExifInterface, targetExif: ExifInterface) {
        try {
            val latLongArray = sourceExif.latLong
            if (latLongArray != null && latLongArray.size >= 2) {
                val loc = Location("dummy").apply {
                    this.latitude = latLongArray[0]
                    this.longitude = latLongArray[1]
                }
                targetExif.setGpsInfo(loc)
            }
        } catch (e: Exception) {
            Log.w("GeoTagReader", "Failed to copy GPS EXIF tags: ${e.message}")
        }
    }
}
