package com.cyd.cyd_soft_competition.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.cyd.cyd_soft_competition.databinding.ActivityAnimVideoBinding
import androidx.activity.OnBackPressedCallback

class AnimVideoActivity : AppCompatActivity() {
    private lateinit var videoView: VideoView
    private lateinit var binding: ActivityAnimVideoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnimVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // 隐藏状态栏和导航栏，全屏播放
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        videoView = binding.vvJumpAnim
        // 设置视频路径（raw 文件夹下的视频，格式：android.resource://包名/raw/视频名）
        val videoUri = Uri.parse("android.resource://${packageName}/raw/jump_anim")
        videoView.setVideoURI(videoUri)

        // 视频准备完成后自动播放
        videoView.setOnPreparedListener {
            it.isLooping = false // 不循环播放
            videoView.start()
        }

        // 视频播放完成后跳转目标页面
        videoView.setOnCompletionListener {
            jumpToTarget() // 复用跳转逻辑
        }
        // 关键：使用 OnBackPressedDispatcher 处理返回逻辑（兼容物理键+返回手势）
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 点击物理返回键或触发返回手势时执行：停止视频播放并关闭当前页面
                videoView.stopPlayback() // 停止视频播放，释放资源
                finish() // 关闭动画页面
                // 可选：添加返回过渡动画
                overridePendingTransition(0, 0)
            }
        })
        videoView.setOnClickListener {
            jumpToTarget() // 复用跳转逻辑
        }
    }
    /**
     * 统一跳转逻辑：跳转到 TargetActivity
     */
    private fun jumpToTarget() {
        videoView.stopPlayback() // 跳转前先停止视频（避免后台继续播放）
        val intent = Intent(this, TargetActivity::class.java)
        startActivity(intent)
        finish() // 关闭当前动画页面，避免返回时重复显示
        overridePendingTransition(0, 0)
    }


}