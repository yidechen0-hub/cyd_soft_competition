package com.cyd.cyd_soft_competition.activity

// BackgroundMusicManager.kt
import android.app.Application
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.cyd.cyd_soft_competition.R

/**
 * 背景音乐管理器：单例 + 全局生命周期监听
 */
class BackgroundMusicManager private constructor(private val app: Application) : LifecycleObserver {
    // 播放器实例
    private var mediaPlayer: MediaPlayer? = null
    // 主线程Handler（确保播放器操作在主线程）
    private val mainHandler = Handler(Looper.getMainLooper())

    // 单例模式（双重校验锁）
    companion object {
        @Volatile
        private var INSTANCE: BackgroundMusicManager? = null

        fun getInstance(app: Application): BackgroundMusicManager {
            return INSTANCE ?: synchronized(this) {
                val instance = BackgroundMusicManager(app)
                // 监听应用全局生命周期（前台/后台）
                ProcessLifecycleOwner.get().lifecycle.addObserver(instance)
                INSTANCE = instance
                instance
            }
        }
    }

    /**
     * 初始化播放器（加载音频文件）
     */
    fun initMusic() {
        if (mediaPlayer == null) {
            // 加载raw目录下的bgm.mp3（替换为你的音频文件名）
            mediaPlayer = MediaPlayer.create(app, R.raw.bg_music).apply {
                isLooping = true // 设置无限循环
                setVolume(0.5f, 0.5f) // 音量（左右声道，0~1）
                // 监听播放完成（防止循环失效时兜底）
                setOnCompletionListener {
                    it.start() // 重新播放，确保循环
                }
            }
        }
    }

    /**
     * 播放背景音乐
     */
    fun play() {
        mainHandler.post {
            if (mediaPlayer?.isPlaying == false) {
                mediaPlayer?.start()
            }
        }
    }

    /**
     * 暂停背景音乐
     */
    fun pause() {
        mainHandler.post {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
        }
    }

    /**
     * 停止并释放播放器（进程结束时调用）
     */
    fun release() {
        mainHandler.post {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            // 移除生命周期监听
            ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        }
    }

    // ====================== 应用生命周期回调 ======================
    /**
     * 应用进入前台（任意Activity可见）
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onAppForeground() {
        play()
    }

    /**
     * 应用进入后台（所有Activity都不可见）
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onAppBackground() {
        pause()
    }

    /**
     * 应用进程结束（可选：主动释放资源）
     */
    fun onAppTerminate() {
        release()
    }
}