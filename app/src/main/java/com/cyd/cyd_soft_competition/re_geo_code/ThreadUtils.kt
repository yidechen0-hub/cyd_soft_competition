package com.cyd.cyd_soft_competition.re_geo_code

import android.os.Handler
import android.os.Looper

/**
 * 全局主线程切换工具类
 */
object ThreadUtils {
    // 主线程的 Handler（Handler 绑定哪个 Looper，就会在哪个线程执行任务）
    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * 切换到主线程执行代码块
     * @param block 要在主线程执行的逻辑（如 UI 操作）
     */
    fun runOnMain(block: () -> Unit) {
        if (isMainThread()) {
            // 已在主线程，直接执行，避免不必要的切换
            block()
        } else {
            // 子线程，通过 Handler 发送任务到主线程
            mainHandler.post(block)
        }
    }

    /**
     * 判断当前线程是否是主线程（辅助方法）
     */
    fun isMainThread(): Boolean {
        // Looper.myLooper() 获取当前线程的 Looper，主线程 Looper 是 Looper.getMainLooper()
        return Looper.myLooper() == Looper.getMainLooper()
    }
}