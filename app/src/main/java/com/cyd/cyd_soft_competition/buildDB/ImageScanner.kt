package com.cyd.cyd_soft_competition.buildDB


import android.util.Log
import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.ExifSubIFDDirectory
import com.drew.metadata.exif.GpsDirectory
import java.io.File
import java.util.Calendar

class ImageScanner {
    private val TAG = "ImageScanner"

    fun scanDirectory(path: String): List<File> {
        val root = File(path)
        if (!root.exists() || !root.isDirectory) {
            Log.i(TAG,"Invalid directory: $path")
            return emptyList()
        }

        return root.walk()
            .filter { it.isFile && isImage(it) }
            .toList()
    }

    private fun isImage(file: File): Boolean {
        val extensions = listOf("jpg", "jpeg", "png", "webp", "heic")
        return extensions.any { file.extension.equals(it, ignoreCase = true) }
    }

    fun extractMetadata(file: File): ImageMetadataInfo {
        var year: Int? = null
        var month: Int? = null
        var day: Int? = null
        var hour: Int? = null
        var minute: Int? = null
        var second: Int? = null
        var latitude: Double? = null
        var longitude: Double? = null

        try {
            val metadata = ImageMetadataReader.readMetadata(file)

            // Extract Date/Time
            val directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory::class.java)
            val date = directory?.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL)
            if (date != null) {
                val calendar = Calendar.getInstance()
                calendar.time = date
                year = calendar.get(Calendar.YEAR)
                month = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-indexed
                day = calendar.get(Calendar.DAY_OF_MONTH)
                hour = calendar.get(Calendar.HOUR_OF_DAY)
                minute = calendar.get(Calendar.MINUTE)
                second = calendar.get(Calendar.SECOND)
            }

            // Extract GPS
            val gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory::class.java)
            if (gpsDirectory != null) {
                val location = gpsDirectory.geoLocation
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                }
            }

        } catch (e: Exception) {
            Log.i(TAG,"Error reading metadata for ${file.name}: ${e.message}")
        }

        return ImageMetadataInfo(
            path = file.absolutePath,
            year = year,
            month = month,
            day = day,
            hour = hour,
            minute = minute,
            second = second,
            latitude = latitude,
            longitude = longitude
        )
    }
}

data class ImageMetadataInfo(
    val path: String,
    val year: Int?,
    val month: Int?,
    val day: Int?,
    val hour: Int?,
    val minute: Int?,
    val second: Int?,
    val latitude: Double?,
    val longitude: Double?,
    val country: String? = null,
    val province: String? = null
)
