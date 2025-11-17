package com.cyd.cyd_soft_competition.re_geo_code

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import java.util.ArrayList

/**
 * photos 表查询工具类：专门读取 id、path、latitude、longitude 字段
 */
object PhotoDbQueryUtils {
    private val TARGET_COLUMNS = arrayOf(
        "id",
        "path",
        "latitude",
        "longitude"
    )

    fun queryAllPhotoLocations(db: SQLiteDatabase): List<PhotoLocation> {
        val resultList = ArrayList<PhotoLocation>()
        var cursor: Cursor? = null

        try {
            cursor = db.query(
                "photos",
                TARGET_COLUMNS,
                null, null, null, null,
                "id DESC"
            )

            while (cursor.moveToNext()) {
                // 1. 获取 id 和 path（非空字段，用 getColumnIndexOrThrow 强制校验字段存在）
                val idIndex = cursor.getColumnIndexOrThrow("id")
                val pathIndex = cursor.getColumnIndexOrThrow("path")
                val id = cursor.getString(idIndex)
                val path = cursor.getString(pathIndex)

                // 2. 获取 latitude 索引（可能不存在，返回 -1）
                val latitudeIndex = cursor.getColumnIndex("latitude")
                val latitude = if (latitudeIndex != -1 && !cursor.isNull(latitudeIndex)) {
                    cursor.getDouble(latitudeIndex)
                } else {
                    null // 字段不存在或值为 NULL，返回 null
                }

                // 3. 获取 longitude 索引（同样校验索引有效性）
                val longitudeIndex = cursor.getColumnIndex("longitude")
                val longitude = if (longitudeIndex != -1 && !cursor.isNull(longitudeIndex)) {
                    cursor.getDouble(longitudeIndex)
                } else {
                    null
                }

                resultList.add(PhotoLocation(id, path, latitude, longitude))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            resultList.clear()
        } finally {
            cursor?.close()
        }

        return resultList
    }

    // 按需查询方法同理修改...
}