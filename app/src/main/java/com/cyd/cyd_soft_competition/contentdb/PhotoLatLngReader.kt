package com.cyd.cyd_soft_competition.contentdb

import android.util.Log
import android.media.ExifInterface
import java.io.File

class PhotoLatLngReader {

    private val TAG = "PhotoLatLngReader"

    data class LatLng(
        val latitude: Double,
        val longitude: Double
    )

    /**
     * 同步接口：读取照片经纬度（兼容 AndroidX ExifInterface，修复标签问题）
     */
    fun readLatLngSync(photoPath: String): LatLng? {
        val file = File(photoPath)
        if (!file.exists()) {
            Log.e(TAG, "文件不存在: $photoPath")
            return null
        }

        return try {
            val exif = ExifInterface(photoPath)

            // 解析纬度（AndroidX 标签：GPS_LATITUDE + GPS_LATITUDE_REF）
            val latitude = parseGpsCoordinate(
                exif = exif,
                coordinateTag = ExifInterface.TAG_GPS_LATITUDE,
                refTag = ExifInterface.TAG_GPS_LATITUDE_REF
            ) ?: run {
                Log.i(TAG, "${file.name}照片未包含 GPS 纬度数据")
                return null
            }

            // 解析经度（AndroidX 标签：GPS_LONGITUDE + GPS_LONGITUDE_REF）
            val longitude = parseGpsCoordinate(
                exif = exif,
                coordinateTag = ExifInterface.TAG_GPS_LONGITUDE,
                refTag = ExifInterface.TAG_GPS_LONGITUDE_REF
            ) ?: run {
                Log.i(TAG, "${file.name}照片未包含 GPS 经度数据")
                return null
            }

            LatLng(latitude = latitude, longitude = longitude)

        } catch (e: Exception) {
            Log.e(TAG, "读取经纬度失败: ${e.message}", e)
            null
        }
    }

    /**
     * 异步接口：子线程执行，主线程回调
     */
    fun readLatLngAsync(photoPath: String, callback: (LatLng?) -> Unit) {
        Thread {
            val result = readLatLngSync(photoPath)
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                callback(result)
            }
        }.start()
    }

    /**
     * 核心解析：从 AndroidX ExifInterface GPS 标签中提取经纬度（度分秒→十进制）
     * AndroidX GPS 标签格式：TAG_GPS_LATITUDE = "30/1, 59/1, 59.999/1"（度, 分, 秒）
     */
    private fun parseGpsCoordinate(
        exif: ExifInterface,
        coordinateTag: String, // AndroidX 标签：TAG_GPS_LATITUDE 或 TAG_GPS_LONGITUDE
        refTag: String         // 方向参考：TAG_GPS_LATITUDE_REF（N/S）或 TAG_GPS_LONGITUDE_REF（E/W）
    ): Double? {
        try {
            // 1. 读取 GPS 度分秒组合字符串（如 "21/1, 18/1, 24.998/1"）
            val coordinateStr = exif.getAttribute(coordinateTag) ?: return null
            Log.i(TAG,"coordinateStr:${coordinateStr}")
            if (coordinateStr.isBlank()) return null

            // 2. 拆分度、分、秒（按 "," 分割，再解析每个部分的分数/数字）
            val parts = coordinateStr.split(",").map { it.trim() }
            if (parts.size != 3) {
                Log.w(TAG, "GPS 格式异常：$coordinateStr（需度,分,秒三部分）")
                return null
            }

            val degrees = parseExifRationalStr(parts[0]) ?: return null // 度
            val minutes = parseExifRationalStr(parts[1]) ?: return null // 分
            val seconds = parseExifRationalStr(parts[2]) ?: return null // 秒

            // 3. 转换为十进制：度 + 分/60 + 秒/3600
            var coordinate = degrees + (minutes / 60.0) + (seconds / 3600.0)

            // 4. 根据方向调整正负（N/E 为正，S/W 为负）
            val ref = exif.getAttribute(refTag)
            if (ref.equals("S", ignoreCase = true) || ref.equals("W", ignoreCase = true)) {
                coordinate = -coordinate
            }
            Log.i(TAG, "coordinate:$coordinate")
            return coordinate
        } catch (e: Exception) {
            Log.e(TAG, "解析 GPS 坐标失败: ${e.message}")
            return null
        }
    }

    /**
     * 解析 EXIF 中的分数字符串（如 "21/1" → 21.0，"18/1" → 18.0，"24.998/1" → 24.998）
     */
    private fun parseExifRationalStr(str: String): Double? {
        return try {
            if (str.contains("/")) {
                val (numeratorStr, denominatorStr) = str.split("/")
                val numerator = numeratorStr.toDouble()
                val denominator = denominatorStr.toDouble()
                if (denominator == 0.0) {
                    Log.w(TAG, "分数分母为 0：$str")
                    null
                } else {
                    numerator / denominator
                }
            } else {
                // 纯数字格式（如 "21" → 21.0）
                str.toDouble()
            }
        } catch (e: Exception) {
            Log.w(TAG, "解析分数失败：$str", e)
            null
        }
    }
}