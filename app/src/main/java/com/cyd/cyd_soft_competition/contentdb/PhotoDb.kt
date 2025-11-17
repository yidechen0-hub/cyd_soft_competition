package com.cyd.cyd_soft_competition.contentdb

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.security.MessageDigest

class PhotoDb(
    context: Context,
    dbName: String
) {


    fun stableId(rec: PhotoRecord): String {
        if (!rec.md5.isNullOrEmpty()) return rec.md5
        val key = "${rec.path}|${rec.fileSize}|${rec.fileMtime}"
        val md = MessageDigest.getInstance("MD5")
        md.update(key.toByteArray(Charsets.UTF_8))
        return md.digest().joinToString("") { "%02x".format(it) }
    }

    val helper = PhotoDbHelper(context, dbName)
    val db: SQLiteDatabase = helper.writableDatabase

    fun upsert(rec: PhotoRecord) {
        val id = stableId(rec)

        // 这里用与 Python 类似的 ON CONFLICT 语法，并用 COALESCE 保留已有的插件字段
        val cols = listOf(
            "id", "path", "file_size", "mime_type",
            "width", "height", "orientation", "camera_make", "camera_model",
            "taken_at_utc", "tz_offset_min", "taken_at_src",
            "latitude", "longitude", "location_accuracy_m", "location_src",
            "file_mtime", "file_ctime",
            "caption", "aesthetic_score", "clip_query", "clip_vector"
        )

        val placeholders = cols.joinToString(",") { "?" }

        val sql = """
            INSERT INTO photos (${cols.joinToString(",")})
            VALUES ($placeholders)
            ON CONFLICT(id) DO UPDATE SET
              path=excluded.path,
              file_size=excluded.file_size,
              mime_type=excluded.mime_type,
              width=excluded.width,
              height=excluded.height,
              orientation=excluded.orientation,
              camera_make=excluded.camera_make,
              camera_model=excluded.camera_model,
              taken_at_utc=excluded.taken_at_utc,
              tz_offset_min=excluded.tz_offset_min,
              taken_at_src=excluded.taken_at_src,
              latitude=excluded.latitude,
              longitude=excluded.longitude,
              location_accuracy_m=excluded.location_accuracy_m,
              location_src=excluded.location_src,
              file_mtime=excluded.file_mtime,
              file_ctime=excluded.file_ctime,
              caption=COALESCE(excluded.caption, photos.caption),
              aesthetic_score=COALESCE(excluded.aesthetic_score, photos.aesthetic_score),
              clip_query=COALESCE(excluded.clip_query, photos.clip_query),
              clip_vector=COALESCE(excluded.clip_vector, photos.clip_vector)
        """.trimIndent()

        val args: Array<Any?> = arrayOf(
            id,
            rec.path,
            rec.fileSize,
            rec.mimeType,
            rec.width,
            rec.height,
            rec.orientation,
            rec.cameraMake,
            rec.cameraModel,
            rec.takenAtUtc,
            rec.tzOffsetMin,
            rec.takenAtSrc,
            rec.latitude,
            rec.longitude,
            rec.locationAccuracyM,
            rec.locationSrc,
            rec.fileMtime,
            rec.fileCtime,
            rec.caption,
            rec.aestheticScore,
            rec.clipQuery,
            rec.clipVector
        )

        db.execSQL(sql, args)
    }

    fun commit() {
        // 在 Android SQLite 中，一般由事务控制，这里可以根据需要补充 BEGIN/COMMIT
    }

    fun close() {
        helper.close()
    }
}