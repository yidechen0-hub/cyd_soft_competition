package com.cyd.cyd_soft_competition.contentdb

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import android.media.ExifInterface
import com.cyd.cyd_soft_competition.contentdb.util.cleanStr
import com.cyd.cyd_soft_competition.contentdb.util.fileTimes
import com.cyd.cyd_soft_competition.contentdb.util.filenameDateTime
import com.cyd.cyd_soft_competition.contentdb.util.md5File
import com.cyd.cyd_soft_competition.contentdb.util.mimeFromSuffix
import com.cyd.cyd_soft_competition.contentdb.util.parseExifDateTime


import java.io.File
import java.security.MessageDigest
import java.time.ZoneId
import kotlin.text.toDouble

class MetaExtractor(
    val fastHash: Boolean = false,
    val context: Context
) {
    @RequiresApi(Build.VERSION_CODES.O)
    fun extractOne(file: File): PhotoRecord? {
        if (!file.isFile) return null

        val exif: ExifInterface = try {
            ExifInterface(file.absolutePath)
        } catch (_: Exception) {
            return null
        }

        // Dimensions
        val width = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, -1)
            .takeIf { it > 0 }
        val height = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, -1)
            .takeIf { it > 0 }

        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1)
            .takeIf { it > 0 }

        val cameraMake = cleanStr(exif.getAttribute(ExifInterface.TAG_MAKE))
        val cameraModel = cleanStr(exif.getAttribute(ExifInterface.TAG_MODEL))

        // Time extraction priority: EXIF → filename → file mtime
        var takenAtSrc: String? = null
        var tzOffsetMin: Int? = null
        var takenAtUtc: Long? = null

        val dtRaw = cleanStr(
            exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
                ?: exif.getAttribute(ExifInterface.TAG_DATETIME)
        )
        if (dtRaw != null) {
            val dt = parseExifDateTime(dtRaw)
            if (dt != null) {
                takenAtSrc = "exif"
                // 使用本地时区当成相机拍摄时区
                val zone = ZoneId.systemDefault()
                val zdt = dt.atZone(zone)
                takenAtUtc = zdt.toEpochSecond()
                tzOffsetMin = (zdt.offset.totalSeconds / 60)
            }
        }

        if (takenAtUtc == null) {
            val fnDt = filenameDateTime(file)
            if (fnDt != null) {
                takenAtSrc = "filename"
                val zone = ZoneId.systemDefault()
                val zdt = fnDt.atZone(zone)
                takenAtUtc = zdt.toEpochSecond()
                tzOffsetMin = (zdt.offset.totalSeconds / 60)
            }
        }

        if (takenAtUtc == null) {
            val (mtime, _) = fileTimes(file)
            if (mtime != null) {
                takenAtSrc = "file_mtime"
                takenAtUtc = mtime
                tzOffsetMin = 0
            }
        }

        // GPS
        var latitude: Double? = null
        var longitude: Double? = null
        var locationSrc: String? = null
        var accuracy: Double? = null


        PhotoLatLngReader().readLatLngSync(file.absolutePath)?.let { exifReader ->
            latitude = exifReader.latitude
            longitude = exifReader.longitude
        }


        // File system
        val (fileMtime, fileCtime) = fileTimes(file)
        val fileSize = file.length()
            .takeIf { it >= 0L }

        val mime = mimeFromSuffix(file)

        val md5 = try {
            if (fastHash) {
                // Fast mode: hash path + size + mtime
                if (fileSize != null && fileMtime != null) {
                    val key = "${file.absolutePath}|$fileSize|$fileMtime"
                    val md = MessageDigest.getInstance("MD5")
                    md.update(key.toByteArray(Charsets.UTF_8))
                    md.digest().joinToString("") { "%02x".format(it) }
                } else null
            } else {
                md5File(file)
            }
        } catch (_: Exception) {
            null
        }

        return PhotoRecord(
            path = file.absolutePath,
            md5 = md5,
            width = width,
            height = height,
            orientation = orientation,
            cameraMake = cameraMake,
            cameraModel = cameraModel,
            takenAtUtc = takenAtUtc,
            takenAtSrc = takenAtSrc,
            tzOffsetMin = tzOffsetMin,
            latitude = latitude,
            longitude = longitude,
            locationSrc = locationSrc,
            locationAccuracyM = accuracy,
            fileMtime = fileMtime,
            fileCtime = fileCtime,
            fileSize = fileSize,
            mimeType = mime
        )
    }
}