package com.cyd.cyd_soft_competition.activity

import android.app.Application
import android.util.Log
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

class CydApplication : Application() {
    private val TAG = "ChaquopyKotlinDemo"

    override fun onCreate() {
        super.onCreate()
        // 初始化 Python 环境（仅需执行一次）
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
            Log.d(TAG, "Python 环境初始化成功！")
        }
    }
}