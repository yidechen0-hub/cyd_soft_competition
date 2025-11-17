package com.cyd.cyd_android.contentdb

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.exifinterface.media.ExifInterface
import com.cyd.cyd_android.contentdb.util.cleanStr
import com.cyd.cyd_android.contentdb.util.dmsToDeg
import com.cyd.cyd_android.contentdb.util.fileTimes
import com.cyd.cyd_android.contentdb.util.filenameDateTime
import com.cyd.cyd_android.contentdb.util.md5File
import com.cyd.cyd_android.contentdb.util.mimeFromSuffix
import com.cyd.cyd_android.contentdb.util.parseExifDateTime
import com.cyd.cyd_soft_competition.contentdb.ImageProcessor

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
        Log.d("cyd", "=== 原始文件 GPS 标签详情 ===")
        Log.d("cyd", "file.absolutePath:${file.absolutePath}")
        Log.d("cyd", "TAG_GPS_LATITUDE: ${exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)}")
        Log.d("cyd", "TAG_GPS_LATITUDE_REF: ${exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF)}")
        Log.d("cyd", "TAG_GPS_LONGITUDE: ${exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)}")
        Log.d("cyd", "TAG_GPS_LONGITUDE_REF: ${exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF)}")
        Log.d("cyd", "==========================")

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

//        // GPS
//        var latitude: Double? = null
//        var longitude: Double? = null
//        var locationSrc: String? = null
//        var accuracy: Double? = null
//
//        try {
//            val lat = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)
//            val latRef = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF)
//            val lon = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)
//            val lonRef = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF)
//
//            if (lat != null && latRef != null && lon != null && lonRef != null) {
//                latitude = dmsToDeg(lat, latRef)
//                longitude = dmsToDeg(lon, lonRef)
//                if (latitude != null && longitude != null) {
//                    locationSrc = "exif"
//                }
//            }
//
//            // 非标准的精度字段：没有统一规范，这里简单尝试自定义 Tag
//            val accStr = exif.getAttribute("GPSHPositioningError")
//                ?: exif.getAttribute("GPSDOP")
//            if (accStr != null) {
//                accuracy = try {
//                    accStr.toDouble()
//                } catch (_: Exception) {
//                    null
//                }
//            }
//        } catch (_: Exception) {
//            // ignore gps errors
//        }
        // GPS
        var latitude: Double? = null
        var longitude: Double? = null
        var locationSrc: String? = null
        var accuracy: Double? = null

//        try {
//            // ✅ 1. 优先用 ExifInterface 内置的 getLatLong / latLong
//            val latLong = exif.latLong   // 需要较新 androidx.exifinterface 版本
//            if (latLong != null && latLong.size == 2) {
//                latitude = latLong[0].toDouble()
//                longitude = latLong[1].toDouble()
//                locationSrc = "exif"
//            } else {
//                // ✅ 2. 兜底：自己解析 DMS 字符串
//                val latStr = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)
//                val latRef = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF)
//                val lonStr = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)
//                val lonRef = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF)
//
//                if (latStr != null && latRef != null && lonStr != null && lonRef != null) {
//                    latitude = dmsToDeg(latStr, latRef)
//                    longitude = dmsToDeg(lonStr, lonRef)
//                    if (latitude != null && longitude != null) {
//                        locationSrc = "exif"
//                    }
//                }
//            }
//
//            // 3. 非标准精度字段（可能没有）
//            val accStr = exif.getAttribute("GPSHPositioningError")
//                ?: exif.getAttribute("GPSDOP")
//            if (accStr != null) {
//                accuracy = accStr.toDoubleOrNull()
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }

//        val processor = ImageProcessor(context)
//        val private_path = processor.processExternalImage(file.absolutePath)

        PhotoLatLngReader().readLatLngSync(file.absolutePath)?.let { exifReader ->
            latitude = exifReader.latitude
            longitude = exifReader.longitude
        }
//        processor.processImageGpsPython(private_path!!).let {
//            latitude = it?.first
//            longitude = it?.second
//        }

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