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
}