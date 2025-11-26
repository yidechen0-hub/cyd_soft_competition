package com.cyd.cyd_soft_competition.competitionActivity


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.cyd.cyd_soft_competition.R
import com.cyd.cyd_soft_competition.annualAlbumActivity.FirstImgActivity
import com.cyd.cyd_soft_competition.databinding.ActivityCopywritingBinding


class CopywritingActivity : AppCompatActivity() {
    private var vvPlayer: VideoView? = null
    private var ivNext: ImageView? = null
    private val videoList: MutableList<String> = mutableListOf() // 所有视频的地址集合（本地路径/网络URL）
    private var currentIndex = 0 // 当前播放的视频索引
    private lateinit var binding: ActivityCopywritingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCopywritingBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // 示例：构建视频列表（本地路径/网络URL）
        videoList.add("/sdcard/taiyi/competition/start/copywriting1.mp4") // 网络视频1
        videoList.add("/sdcard/taiyi/competition/start/copywriting2.mp4") // 网络视频2
        videoList.add("/sdcard/taiyi/competition/start/copywriting3.mp4") // 本地视频（需申请存储权限）


        // 初始化控件
        initView()
        // 初始化播放器并播放当前视频
        initPlayer()
        // 监听播放完成事件
        setPlayerListener()
        // 下一个视频点击事件
        setNextClickListener()
    }

    private fun initView() {
        vvPlayer = binding.vvPlayer
        ivNext = binding.ivNext
        // 可选：添加系统媒体控制器（进度条、暂停/播放按钮）
//        vvPlayer!!.setMediaController(MediaController(this))
    }

    /**
     * 接收上个页面传递的视频数据：
     * - videoList：所有视频的地址（本地路径/网络URL）
     * - currentIndex：初始播放的视频索引
     */
//    private fun receiveVideoData() {
//        val intent = getIntent()
//        if (intent != null) {
//            videoList = intent.getStringArrayListExtra("VIDEO_LIST")
//            currentIndex = intent.getIntExtra("CURRENT_INDEX", 0)
//        }
//    }

    /**
     * 初始化播放器，加载并播放当前索引的视频
     */
    private fun initPlayer() {
        if (videoList == null || videoList!!.isEmpty() || currentIndex >= videoList!!.size) {
            finish() // 数据异常，关闭页面
            return
        }

        val currentVideoPath = videoList!!.get(currentIndex)
        val videoUri: Uri?

        // 区分本地视频和网络视频（本地路径以 file:// 开头，网络以 http/https 开头）
        if (currentVideoPath.startsWith("http")) {
            videoUri = Uri.parse(currentVideoPath) // 网络视频
        } else {
            videoUri = Uri.parse("file://" + currentVideoPath) // 本地视频（需传入绝对路径）
        }

        // 设置视频地址并播放

        vvPlayer!!.setVideoURI(videoUri)
        vvPlayer!!.start()
        // 隐藏下一个按钮（播放中不显示）
//        ivNext?.visibility = View.GONE
        if (currentIndex==videoList.size-1){
            ivNext?.setImageResource(R.drawable.icon_enter2025)
        }
    }

    /**
     * 监听视频播放完成事件
     */
    private fun setPlayerListener() {
//        vvPlayer?.setOnPreparedListener { mediaPlayer ->
//            mediaPlayer.isLooping = true // 所有视频都循环
//        }
        vvPlayer?.setOnCompletionListener {
            if (currentIndex == videoList!!.size - 1){
                startActivity(Intent(this, FirstImgActivity::class.java))
                finish()
            }
            playNextVideo()
        }
        vvPlayer?.setOnClickListener {
            playNextVideo()
        }
    }

    /**
     * 「下一个」按钮点击事件，播放下一个视频
     */
    private fun setNextClickListener() {
        ivNext?.setOnClickListener {
            startActivity(Intent(this, FirstImgActivity::class.java))
            finish()
        }
    }

    /**
     * 播放下一个视频（核心逻辑）
     */
    private fun playNextVideo() {
        currentIndex++ // 索引+1
        if (currentIndex < videoList!!.size) {
            // 还有下一个视频，重新初始化播放器
            initPlayer()
        } else {
            // 所有视频播放完毕，关闭页面（或提示用户）
            startActivity(Intent(this, FirstImgActivity::class.java))
            finish()
        }
    }

    // 生命周期优化：暂停时停止播放，恢复时继续播放
    override fun onPause() {
        super.onPause()
        if (vvPlayer!!.isPlaying()) {
            vvPlayer!!.pause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!vvPlayer!!.isPlaying()) {
            vvPlayer!!.start()
        }
    }

    // 释放资源：防止内存泄漏
    override fun onDestroy() {
        super.onDestroy()
        vvPlayer!!.stopPlayback() // 停止播放并释放资源
    }
}