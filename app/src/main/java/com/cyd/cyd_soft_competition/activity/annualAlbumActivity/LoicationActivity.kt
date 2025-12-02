package com.cyd.cyd_soft_competition.activity.annualAlbumActivity


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cyd.cyd_soft_competition.R
import com.cyd.cyd_soft_competition.databinding.ActivityLocationBinding
import com.cyd.cyd_soft_competition.dbmsg.Messages


class LoicationActivity : AppCompatActivity() {
    private var current = 0
    private lateinit var binding: ActivityLocationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        playForegroundVideoFromRaw()
        binding.tvLocation.text = Messages(this).getLocationMsg()
        binding.vvPlayer.setOnClickListener {
            clicked()
        }
        binding.vvPlayer.setOnCompletionListener {
            clicked()
        }


    }

    private fun clicked() {
//        when (current) {
//            0 -> {
//                current = 1
//                playForegroundVideoFromRaw()
//                binding.tvLocation2.visibility = View.VISIBLE
//                binding.tvLocation3.visibility = View.VISIBLE
//
//            }
//
//            1 -> {
                startActivity(Intent(this, FaceActivity::class.java))
//            }
//
//        }
    }

    /**
     * 播放 raw 目录下的本地视频
     */
    private fun playForegroundVideoFromRaw() {
        val videoView = binding.vvPlayer
        // 1. 获取 raw 目录下视频的 Uri
        val videoUri = Uri.parse("android.resource://${packageName}/${R.raw.bg_location}")
        // 2. 设置视频源
        videoView.setVideoURI(videoUri)
        // 3. 配置播放参数（循环播放、静音等）
        videoView.setOnPreparedListener { mediaPlayer ->
//            mediaPlayer.isLooping = true // 循环播放（根据需求调整）
//            mediaPlayer.setVolume(0f, 0f) // 静音（若需声音可去掉）
            mediaPlayer.start() // 开始播放
        }
        // 4. 视频加载失败处理
        videoView.setOnErrorListener { _, _, _ ->
            Toast.makeText(this, "视频播放失败", Toast.LENGTH_SHORT).show()
            true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 在 Activity 销毁时，释放资源
        binding.vvPlayer.stopPlayback()


    }
}