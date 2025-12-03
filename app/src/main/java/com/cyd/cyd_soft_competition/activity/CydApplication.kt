package com.cyd.cyd_soft_competition.activity

import android.app.Application
import android.util.Log
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.cyd.cyd_soft_competition.activity.BackgroundMusicManager

class CydApplication : Application() {
    private val TAG = "ChaquopyKotlinDemo"

    override fun onCreate() {
        super.onCreate()
        // 初始化 Python 环境（仅需执行一次）
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
            Log.d(TAG, "Python 环境初始化成功！")
        }

        // 初始化背景音乐管理器
        val musicManager = BackgroundMusicManager.getInstance(this)
        musicManager.initMusic()
        // 启动播放（可选：也可在首页手动触发）
        musicManager.play()
    }

    override fun onTerminate() {
        super.onTerminate()
        // 进程结束时释放播放器
        BackgroundMusicManager.getInstance(this).release()
    }
}