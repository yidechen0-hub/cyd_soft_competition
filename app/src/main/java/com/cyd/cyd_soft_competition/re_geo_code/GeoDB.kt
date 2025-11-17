package com.cyd.cyd_soft_competition.re_geo_code

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.security.MessageDigest

class GeoDB(
    context: Context,
    dbName: String
) {

    val helper = GeoDBHelper(context, dbName)
    val db: SQLiteDatabase = helper.writableDatabase

    fun upsert(rec: GeoRecord) {

        // 这里用与 Python 类似的 ON CONFLICT 语法，并用 COALESCE 保留已有的插件字段
        val cols = listOf(
            "id", "path", "formattedAddress",
            "province", "city", "district", "township", "street", "streetNumber"
        )

        val placeholders = cols.joinToString(",") { "?" }

        val sql = """
            INSERT INTO geo (${cols.joinToString(",")})
            VALUES ($placeholders)
            ON CONFLICT(id) DO UPDATE SET
              path=excluded.path,
              formattedAddress=excluded.formattedAddress,
              province=excluded.province,
              city=excluded.city,
              district=excluded.district,
              township=excluded.township,
              street=excluded.street,
              streetNumber=excluded.streetNumber
        """.trimIndent()

        val args: Array<Any?> = arrayOf(
            rec.id,
            rec.path,
            rec.formattedAddress,
            rec.province,
            rec.city,
            rec.district,
            rec.township,
            rec.street,
            rec.streetNumber,
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