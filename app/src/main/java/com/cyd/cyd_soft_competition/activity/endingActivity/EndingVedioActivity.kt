package com.cyd.cyd_soft_competition.activity.endingActivity


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.cyd.cyd_soft_competition.R
import com.cyd.cyd_soft_competition.activity.InsightDetailActivity
import com.cyd.cyd_soft_competition.databinding.ActivityEndingVedioBinding
import com.cyd.cyd_soft_competition.llm.PersonaGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope


class EndingVedioActivity : AppCompatActivity() {
    private var vvPlayer: VideoView? = null

    private val videoList: MutableList<Int> = mutableListOf() // 所有视频的地址集合（本地路径/网络URL）
    private var currentIndex = 0 // 当前播放的视频索引
    private lateinit var binding: ActivityEndingVedioBinding
    private var resultJson: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEndingVedioBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // 示例：构建视频列表（本地路径/网络URL）
        videoList.add(R.raw.ending1) // 网络视频) // 网络视频1
        videoList.add(R.raw.ending2) // 网络视频2



        // 初始化控件
        initView()
        // 初始化播放器并播放当前视频
        initPlayer()
        // 监听播放完成事件
        setPlayerListener()
        binding.vvPlayer.setOnClickListener {
            playNextVideo()
        }
        loadInsightData()

    }

    private fun initView() {
        vvPlayer = binding.vvPlayer
        // 可选：添加系统媒体控制器（进度条、暂停/播放按钮）
//        vvPlayer!!.setMediaController(MediaController(this))
    }


    /**
     * 初始化播放器，加载并播放当前索引的视频
     */
    private fun initPlayer() {
        if (videoList == null || videoList!!.isEmpty() || currentIndex >= videoList!!.size) {
            finish() // 数据异常，关闭页面
            return
        }

        val currentVideoPath = videoList!!.get(currentIndex)
        // 1. 获取 raw 目录下视频的 Uri
        val videoUri = Uri.parse("android.resource://${packageName}/${currentVideoPath}")

        // 2. 设置视频源
        vvPlayer!!.setVideoURI(videoUri)
        // 3. 配置播放参数（循环播放、静音等）
        vvPlayer!!.start()

    }

    private fun loadInsightData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val dbPath = getDatabasePath("competition_database.db").absolutePath
            try {
                val resultObj = PersonaGenerator(dbPath, null).generate()
                resultJson = resultObj!!.toString(2)

            } catch (e: Exception) {
                e.printStackTrace()
            }
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
                val intent: Intent = Intent(this, InsightDetailActivity::class.java)
                intent.putExtra("resultJson", resultJson ?: "{}")
                startActivity(intent)
            }
            playNextVideo()
        }
        vvPlayer?.setOnClickListener {
            playNextVideo()
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
            val intent: Intent = Intent(this, InsightDetailActivity::class.java)
            intent.putExtra("resultJson", resultJson ?: "{}")
            startActivity(intent)
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