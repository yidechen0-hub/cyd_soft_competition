package com.cyd.cyd_soft_competition.buildDB

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log

class DatabaseManager(context: Context) {
    private val dbHelper = DatabaseHelper(context)
    private val TAG = "DatabaseManager"

    /**
     * 初始化表（实际由 SQLiteOpenHelper 自动处理，但可显式调用确保）
     */
    fun initializeTables() {
        try {
            val db = dbHelper.writableDatabase
            db.close() // 触发 onCreate（如果未创建）
            Log.i(TAG, "Database initialized successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing database", e)
        }
    }

    /**
     * 插入图片元数据
     */
    fun insertImageMetadata(metadata: ImageMetadataInfo) {
        val db: SQLiteDatabase = dbHelper.writableDatabase

        try {
            val values = android.content.ContentValues().apply {
                put("path", metadata.path)
                put("year", metadata.year)
                put("month", metadata.month)
                put("day", metadata.day)
                put("hour", metadata.hour)
                put("minute", metadata.minute)
                put("second", metadata.second)
                put("latitude", metadata.latitude)
                put("longitude", metadata.longitude)
                put("country", metadata.country)
                put("province", metadata.province)
                // 其他字段如 aesthetic_score, vlm_caption 等可后续添加
            }

            val newRowId = db.insert("image_metadata", null, values)
            if (newRowId == -1L) {
                Log.e(TAG, "Failed to insert metadata for ${metadata.path}")
            } else {
                Log.d(TAG, "Inserted metadata with ID: $newRowId for ${metadata.path}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting metadata for ${metadata.path}", e)
        } finally {
            db.close()
        }
    }

    // 可选：提供关闭数据库的方法（通常不需要，SQLiteOpenHelper 会管理）
    fun close() {
        dbHelper.close()
    }

    // --- Query Methods for Messages ---

    fun getFirstImageMetadata(): ImageMetadataInfo? {
        val db = dbHelper.readableDatabase
        var info: ImageMetadataInfo? = null
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        val cursor = db.query(
            "image_metadata",
            null,
            "year = ?", // Filter by current year
            arrayOf(currentYear.toString()),
            null,
            null,
            "year ASC, month ASC, day ASC, hour ASC, minute ASC, second ASC",
            "1"
        )
        if (cursor.moveToFirst()) {
            info = cursorToMetadata(cursor)
        }
        cursor.close()
        return info
    }

    fun getImageCount(): Int {
        val db = dbHelper.readableDatabase
        return android.database.DatabaseUtils.queryNumEntries(db, "image_metadata").toInt()
    }

    fun getVideoCount(): Int {
        // Placeholder: Currently ImageScanner only scans images.
        return 0
    }

    fun getDistinctDayCount(): Int {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(DISTINCT year || '-' || month || '-' || day) FROM image_metadata WHERE year IS NOT NULL",
            null
        )
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
    }

    fun getLocationCounts(): Pair<Int, Int> {
        val db = dbHelper.readableDatabase
        // Count distinct countries
        val cursorCountry = db.rawQuery("SELECT COUNT(DISTINCT country) FROM image_metadata WHERE country IS NOT NULL", null)
        var countryCount = 0
        if (cursorCountry.moveToFirst()) {
            countryCount = cursorCountry.getInt(0)
        }
        cursorCountry.close()

        // Count distinct provinces/cities
        val cursorProvince = db.rawQuery("SELECT COUNT(DISTINCT province) FROM image_metadata WHERE province IS NOT NULL", null)
        var provinceCount = 0
        if (cursorProvince.moveToFirst()) {
            provinceCount = cursorProvince.getInt(0)
        }
        cursorProvince.close()

        return Pair(countryCount, provinceCount)
    }

    fun getTopTags(limit: Int): List<Pair<String, Int>> {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT tag, COUNT(*) as c FROM image_metadata WHERE tag IS NOT NULL GROUP BY tag ORDER BY c DESC LIMIT ?",
            arrayOf(limit.toString())
        )
        val list = mutableListOf<Pair<String, Int>>()
        while (cursor.moveToNext()) {
            val tag = cursor.getString(0)
            val count = cursor.getInt(1)
            list.add(Pair(tag, count))
        }
        cursor.close()
        return list
    }

    fun getSmileCount(): Int {
        val db = dbHelper.readableDatabase
        return android.database.DatabaseUtils.queryNumEntries(db, "url_count").toInt()
    }

    fun getSmileVideoPath(): String? {
        return null
    }

    fun getSeasonPaths(): List<String> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<String>()

        // Helper function to get best scenery photo for a month range
        fun getBestPathForSeason(condition: String): String? {
            val cursor = db.query(
                "image_metadata",
                arrayOf("path"),
                "($condition) AND tag LIKE '%风景%'",
                null,
                null,
                null,
                "aesthetic_score DESC",
                "1"
            )
            var path: String? = null
            if (cursor.moveToFirst()) {
                path = cursor.getString(0)
            }
            cursor.close()
            return path
        }

        // Spring: 3-5
        list.add(getBestPathForSeason("month >= 3 AND month <= 5") ?: "")
        // Summer: 6-8
        list.add(getBestPathForSeason("month >= 6 AND month <= 8") ?: "")
        // Autumn: 9-11
        list.add(getBestPathForSeason("month >= 9 AND month <= 11") ?: "")
        // Winter: 12, 1, 2
        list.add(getBestPathForSeason("month = 12 OR month <= 2") ?: "")

        return list
    }

    fun getFaceUrl(): List<String> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<String>()
        val cursor = db.query(
            "url_count",
            arrayOf("url"),
            null,
            null,
            null,
            null,
            "count DESC",
            "3"
        )
        while (cursor.moveToNext()) {
            list.add(cursor.getString(0))
        }
        cursor.close()
        return list
    }

    fun getFacePaths(): List<String> {
        val urls = getFaceUrl()
        val localPaths = mutableListOf<String>()
        val dirPath = "/sdcard/taiyi/competition/face/"
        val dir = java.io.File(dirPath)
        if (!dir.exists()) {
            dir.mkdirs()
        }

        for ((index, url) in urls.withIndex()) {
            // Generate a filename. You might want to extract extension or use a hash.
            // For simplicity, using face_{index}.jpg (assuming jpg or handling generic)
            // Or try to keep original name if possible.
            val fileName = "face_${index + 1}.jpg" 
            val file = java.io.File(dir, fileName)
            
            // Download if not exists or overwrite? User said "save to", implies action.
            // But for performance, maybe check existence? 
            // Let's try to download.
            if (downloadFile(url, file)) {
                localPaths.add(file.absolutePath)
            } else {
                // If download fails, maybe add a placeholder or skip?
                // For now, let's just log it and maybe add the path anyway if it exists?
                if (file.exists()) {
                    localPaths.add(file.absolutePath)
                }
            }
        }
        
        // Ensure we have enough paths to avoid crashes in UI if possible, 
        // but strictly following logic, we return what we have.
        return localPaths
    }

    private fun downloadFile(urlStr: String, destFile: java.io.File): Boolean {
        try {
            val url = java.net.URL(urlStr)
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.requestMethod = "GET"
            connection.connect()

            if (connection.responseCode == java.net.HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                val outputStream = java.io.FileOutputStream(destFile)
                val buffer = ByteArray(1024)
                var len: Int
                while (inputStream.read(buffer).also { len = it } != -1) {
                    outputStream.write(buffer, 0, len)
                }
                outputStream.close()
                inputStream.close()
                return true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading file: $urlStr", e)
        }
        return false
    }

    /**
     * Get the path of the most special photo (taken between 0-4 AM, latest time is most special)
     */
    fun getSpecialDaySinglePath(): String? {
        val db = dbHelper.readableDatabase
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        
        val cursor = db.query(
            "image_metadata",
            arrayOf("path"),
            "year = ? AND hour >= 0 AND hour < 4",
            arrayOf(currentYear.toString()),
            null,
            null,
            "hour DESC, minute DESC, second DESC",
            "1"
        )
        
        var path: String? = null
        if (cursor.moveToFirst()) {
            path = cursor.getString(0)
        }
        cursor.close()
        return path
    }

    /**
     * Get the date of the most special photo (for display purposes)
     * Returns formatted date string like "8月5日"
     */
    fun getSpecialDayDate(): String? {
        val db = dbHelper.readableDatabase
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        
        val cursor = db.query(
            "image_metadata",
            arrayOf("month", "day"),
            "year = ? AND hour >= 0 AND hour < 4",
            arrayOf(currentYear.toString()),
            null,
            null,
            "hour DESC, minute DESC, second DESC",
            "1"
        )
        
        var dateStr: String? = null
        if (cursor.moveToFirst()) {
            val month = cursor.getInt(0)
            val day = cursor.getInt(1)
            dateStr = "${month}月${day}日"
        }
        cursor.close()
        return dateStr
    }

    /**
     * Get special day grid data: all photos from the day with most photos taken
     * Returns List of photo paths from the day with most photos
     */
    fun getSpecialDayGridData(): List<String> {
        val db = dbHelper.readableDatabase
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        
        // Find the day with most photos
        val dayCursor = db.rawQuery(
            """
            SELECT year, month, day, COUNT(*) as photo_count 
            FROM image_metadata 
            WHERE year = ? 
            GROUP BY year, month, day 
            ORDER BY photo_count DESC 
            LIMIT 1
            """.trimIndent(),
            arrayOf(currentYear.toString())
        )
        
        var year: Int? = null
        var month: Int? = null
        var day: Int? = null
        
        if (dayCursor.moveToFirst()) {
            year = dayCursor.getInt(0)
            month = dayCursor.getInt(1)
            day = dayCursor.getInt(2)
        }
        dayCursor.close()
        
        if (year == null || month == null || day == null) {
            return emptyList()
        }
        
        // Get all photos from that day
        val photosCursor = db.query(
            "image_metadata",
            arrayOf("path"),
            "year = ? AND month = ? AND day = ?",
            arrayOf(year.toString(), month.toString(), day.toString()),
            null,
            null,
            "hour ASC, minute ASC, second ASC",
            null
        )
        
        val photosList = mutableListOf<String>()
        while (photosCursor.moveToNext()) {
            photosList.add(photosCursor.getString(0))
        }
        photosCursor.close()
        
        return photosList
    }

    /**
     * Get info about the day with most photos (date and count)
     * Returns Pair<dateString, photoCount> like Pair("8月5日", 578)
     */
    fun getMostPhotoDayInfo(): Pair<String, Int> {
        val db = dbHelper.readableDatabase
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        
        val cursor = db.rawQuery(
            """
            SELECT month, day, COUNT(*) as photo_count 
            FROM image_metadata 
            WHERE year = ? 
            GROUP BY year, month, day 
            ORDER BY photo_count DESC 
            LIMIT 1
            """.trimIndent(),
            arrayOf(currentYear.toString())
        )
        
        var dateStr = ""
        var count = 0
        
        if (cursor.moveToFirst()) {
            val month = cursor.getInt(0)
            val day = cursor.getInt(1)
            count = cursor.getInt(2)
            dateStr = "${month}月${day}日"
        }
        cursor.close()
        
        return Pair(dateStr, count)
    }

    /**
     * Get paths of top 20 photos with highest aesthetic scores
     * Returns List of photo paths sorted by aesthetic_score DESC
     */
    fun getTop20Paths(): List<String> {
        val db = dbHelper.readableDatabase
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        
        val cursor = db.query(
            "image_metadata",
            arrayOf("path"),
            "year = ? AND aesthetic_score IS NOT NULL",
            arrayOf(currentYear.toString()),
            null,
            null,
            "aesthetic_score DESC",
            "20"
        )
        
        val photosList = mutableListOf<String>()
        while (cursor.moveToNext()) {
            photosList.add(cursor.getString(0))
        }
        cursor.close()
        
        return photosList
    }

    private fun cursorToMetadata(cursor: android.database.Cursor): ImageMetadataInfo {
        return ImageMetadataInfo(
            path = cursor.getString(cursor.getColumnIndexOrThrow("path")),
            year = cursor.getInt(cursor.getColumnIndexOrThrow("year")),
            month = cursor.getInt(cursor.getColumnIndexOrThrow("month")),
            day = cursor.getInt(cursor.getColumnIndexOrThrow("day")),
            hour = cursor.getInt(cursor.getColumnIndexOrThrow("hour")),
            minute = cursor.getInt(cursor.getColumnIndexOrThrow("minute")),
            second = cursor.getInt(cursor.getColumnIndexOrThrow("second")),
            latitude = cursor.getDouble(cursor.getColumnIndexOrThrow("latitude")),
            longitude = cursor.getDouble(cursor.getColumnIndexOrThrow("longitude")),
            country = cursor.getString(cursor.getColumnIndexOrThrow("country")),
            province = cursor.getString(cursor.getColumnIndexOrThrow("province"))
        )
    }

    /**
     * Update the URL for a specific image path
     */
    fun updateImageUrl(path: String, url: String) {
        val db = dbHelper.writableDatabase
        try {
            val values = android.content.ContentValues().apply {
                put("url", url)
            }
            val rowsAffected = db.update("image_metadata", values, "path = ?", arrayOf(path))
            if (rowsAffected > 0) {
                Log.d(TAG, "Updated URL for $path")
            } else {
                Log.w(TAG, "No metadata found for $path to update URL")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating URL for $path", e)
        } finally {
            db.close()
        }
    }

    /**
     * Get all image paths and their URLs where URL is not null
     */
    fun getAllImagePathsAndUrls(): List<Pair<String, String>> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<Pair<String, String>>()
        val cursor = db.query(
            "image_metadata",
            arrayOf("path", "url"),
            "url IS NOT NULL AND url != ''",
            null,
            null,
            null,
            null
        )
        while (cursor.moveToNext()) {
            val path = cursor.getString(0)
            val url = cursor.getString(1)
            if (url == null) {
                continue
            }
            list.add(Pair(path, url))
        }
        cursor.close()
        return list
    }

    /**
     * Update image analysis result (score, caption, tag)
     */
    fun updateImageAnalysisResult(path: String, score: Double, caption: String, tag: String) {
        val db = dbHelper.writableDatabase
        try {
            val values = android.content.ContentValues().apply {
                put("aesthetic_score", score)
                put("vlm_caption", caption)
                put("tag", tag)
            }
            val rowsAffected = db.update("image_metadata", values, "path = ?", arrayOf(path))
            if (rowsAffected > 0) {
                Log.d(TAG, "Updated analysis result for $path")
            } else {
                Log.w(TAG, "No metadata found for $path to update analysis result")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating analysis result for $path", e)
        } finally {
            db.close()
        }
    }

    /**
     * Update image analysis result by URL (score, caption, tag)
     */
    fun updateImageAnalysisResultByUrl(url: String, score: Double, caption: String, tag: String) {
        val db = dbHelper.writableDatabase
        try {
            val values = android.content.ContentValues().apply {
                put("aesthetic_score", score)
                put("vlm_caption", caption)
                put("tag", tag)
            }
            val rowsAffected = db.update("image_metadata", values, "url = ?", arrayOf(url))
            if (rowsAffected > 0) {
                Log.d(TAG, "Updated analysis result for URL $url")
            } else {
                Log.w(TAG, "No metadata found for URL $url to update analysis result")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating analysis result for URL $url", e)
        } finally {
            db.close()
        }
    }



    /**
     * Get images that have coordinates but missing country/province
     * Returns list of ImageMetadataInfo
     */
    fun getImagesWithoutLocation(): List<ImageMetadataInfo> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<ImageMetadataInfo>()
        // Select images where lat/long are not 0 (assuming 0,0 is invalid or default) and (country is null/empty OR province is null/empty)
        val cursor = db.query(
            "image_metadata",
            null,
            "(latitude != 0 OR longitude != 0) AND (country IS NULL OR country = '' OR province IS NULL OR province = '')",
            null,
            null,
            null,
            null
        )
        while (cursor.moveToNext()) {
            list.add(cursorToMetadata(cursor))
        }
        cursor.close()
        return list
    }

    /**
     * Update location (country, province) for a specific image path
     */
    fun updateLocation(path: String, country: String, province: String) {
        val db = dbHelper.writableDatabase
        try {
            val values = android.content.ContentValues().apply {
                put("country", country)
                put("province", province)
            }
            val rowsAffected = db.update("image_metadata", values, "path = ?", arrayOf(path))
            if (rowsAffected > 0) {
                Log.d(TAG, "Updated location for $path: $country, $province")
            } else {
                Log.w(TAG, "No metadata found for $path to update location")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating location for $path", e)
        } finally {
            db.close()
        }
    }
}