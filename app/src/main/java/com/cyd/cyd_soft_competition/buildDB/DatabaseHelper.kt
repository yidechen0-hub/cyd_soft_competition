package com.cyd.cyd_soft_competition.buildDB

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "competition_database.db"
        private const val DATABASE_VERSION = 1
        private const val TAG = "DatabaseHelper"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create image_metadata table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS image_metadata (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                path TEXT NOT NULL,
                year INTEGER,
                month INTEGER,
                day INTEGER,
                hour INTEGER,
                minute INTEGER,
                second INTEGER,
                country TEXT,
                province TEXT,
                latitude REAL,
                longitude REAL,
                aesthetic_score REAL,
                vlm_caption TEXT,
                tag TEXT,
                url TEXT
            )
        """)

        // Create url_count table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS url_count (
                url TEXT PRIMARY KEY,
                count INTEGER DEFAULT 0
            )
        """)

        Log.i(TAG, "Tables 'image_metadata' and 'url_count' created.")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // 简单处理：删除重建（生产环境应做迁移）
        db.execSQL("DROP TABLE IF EXISTS image_metadata")
        db.execSQL("DROP TABLE IF EXISTS url_count")
        onCreate(db)
    }
}