package com.cyd.cyd_soft_competition.llm
import android.database.sqlite.SQLiteDatabase

class DBReader(private val dbPath: String) {

    fun loadMetadata(): List<ImageMeta> {
        val db = SQLiteDatabase.openDatabase(
            dbPath, null, SQLiteDatabase.OPEN_READONLY
        )

        val cursor = db.rawQuery("""
            SELECT year, month, day, hour, country, province,
                   aesthetic_score, vlm_caption, tag
            FROM image_metadata
        """.trimIndent(), null)

        val list = mutableListOf<ImageMeta>()

        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    ImageMeta(
                        year = it.getIntOrNull(0),
                        month = it.getIntOrNull(1),
                        day = it.getIntOrNull(2),
                        hour = it.getIntOrNull(3),
                        country = it.getStringOrNull(4),
                        province = it.getStringOrNull(5),
                        aestheticScore = it.getDoubleOrNull(6),
                        caption = it.getStringOrNull(7),
                        tag = it.getStringOrNull(8)
                    )
                )
            }
        }
        db.close()
        return list
    }
}

// --- SQLite Cursor helper ---
fun android.database.Cursor.getIntOrNull(idx: Int) =
    if (isNull(idx)) null else getInt(idx)

fun android.database.Cursor.getDoubleOrNull(idx: Int) =
    if (isNull(idx)) null else getDouble(idx)

fun android.database.Cursor.getStringOrNull(idx: Int) =
    if (isNull(idx)) null else getString(idx)
