package com.cyd.cyd_android.contentdb

import android.os.Build
import androidx.annotation.RequiresApi
import java.io.File
import java.net.URLConnection
import java.security.MessageDigest
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

object util {
    fun safeInt(x: Any?): Int? = when (x) {
        is Int -> x
        is Long -> x.toInt()
        is Double -> x.toInt()
        is Float -> x.toInt()
        is String -> x.toIntOrNull()
        else -> null
    }

    fun safeLong(x: Any?): Long? = when (x) {
        is Long -> x
        is Int -> x.toLong()
        is Double -> x.toLong()
        is Float -> x.toLong()
        is String -> x.toLongOrNull()
        else -> null
    }



    val FILENAME_PATTERNS = listOf(
        // IMG_20251004_130609.jpg / PXL_20231231_235959.jpg
        Regex("""(20\d{2})(\d{2})(\d{2})[_-]?(\d{2})(\d{2})(\d{2})"""),
        // 2025-10-04 13.06.09
        Regex("""(20\d{2})[-_](\d{2})[-_](\d{2})[ _](\d{2})[.:](\d{2})[.:](\d{2})""")
    )

    @RequiresApi(Build.VERSION_CODES.O)
    fun filenameDateTime(file: File): LocalDateTime? {
        val name = file.nameWithoutExtension
        for (pat in FILENAME_PATTERNS) {
            val m = pat.find(name) ?: continue
            val (y, mo, d, H, M, S) = m.destructured
            return try {
                LocalDateTime.of(
                    y.toInt(), mo.toInt(), d.toInt(),
                    H.toInt(), M.toInt(), S.toInt()
                )
            } catch (_: Exception) {
                null
            }
        }
        return null
    }

    fun fileTimes(file: File): Pair<Long?, Long?> {
        return try {
            val mtime = file.lastModified() / 1000L
            // Android 无统一创建时间，这里用 mtime 代替
            Pair(mtime, mtime)
        } catch (_: Exception) {
            Pair(null, null)
        }
    }

    fun md5File(file: File, chunkSize: Int = 1 shl 20): String {
        val md = MessageDigest.getInstance("MD5")
        file.inputStream().use { input ->
            val buf = ByteArray(chunkSize)
            while (true) {
                val n = input.read(buf)
                if (n <= 0) break
                md.update(buf, 0, n)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }

    fun mimeFromSuffix(file: File): String? {
        return URLConnection.guessContentTypeFromName(file.name)
    }

    // ExifInterface GPS 典型格式： "35/1,33/1,5678/100"
    fun parseExifRational(str: String): Double {
        val parts = str.split("/")
        return if (parts.size == 2) {
            parts[0].toDouble() / parts[1].toDouble()
        } else {
            str.toDouble()
        }
    }

    fun dmsToDeg(coord: String?, ref: String?): Double? {
        if (coord == null || ref == null) return null
        val parts = coord.split(",")
        if (parts.size != 3) return null
        return try {
            val d = parseExifRational(parts[0].trim())
            val m = parseExifRational(parts[1].trim())
            val s = parseExifRational(parts[2].trim())
            var deg = d + m / 60.0 + s / 3600.0
            if (ref == "S" || ref == "W") deg = -deg
            deg
        } catch (_: Exception) {
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    val EXIF_DT_FORMAT = DateTimeFormatter.ofPattern(
        "yyyy:MM:dd HH:mm:ss",
        Locale.US
    )



    @RequiresApi(Build.VERSION_CODES.O)
    fun guessTzOffsetMinutes(): Int? {
        return try {
            val offset = ZoneId.systemDefault()
                .rules
                .getOffset(Instant.now())
            (offset.totalSeconds / 60)
        } catch (_: Exception) {
            null
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun parseExifDateTime(str: String): LocalDateTime? {
        // "2025:10:04 13:06:09"
        return try {
            LocalDateTime.parse(str, EXIF_DT_FORMAT)
        } catch (_: DateTimeParseException) {
            null
        }
    }

    fun cleanStr(x: String?): String? {
        if (x == null) return null
        val s = x.trim()
        return if (s.isEmpty()) null else s
    }

    val IMAGE_EXTS = setOf(
        "jpg", "jpeg", "png", "webp", "heic", "heif", "tiff", "tif"
    )

    fun iterImages(root: File): Sequence<File> {
        return root.walkTopDown()
            .filter { f ->
                f.isFile && IMAGE_EXTS.contains(f.extension.lowercase(Locale.ROOT))
            }
    }
}