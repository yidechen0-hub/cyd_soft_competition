package com.cyd.cyd_soft_competition.re_geo_code

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class GeoDBHelper(
    context: Context,
    dbName: String
) : SQLiteOpenHelper(context, dbName, null, 1) {



    override fun onCreate(db: SQLiteDatabase) {
        // 执行多条语句
        scheme_sql_geo().SCHEMA_SQL.trimIndent()
            .split(";")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .forEach { stmt ->
                db.execSQL("$stmt;")
            }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // 简单场景可以直接 drop 重新建表，真实项目要做迁移
    }
}
