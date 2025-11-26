package com.cyd.cyd_soft_competition.tagActivity


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cyd.cyd_soft_competition.R
import com.cyd.cyd_soft_competition.databinding.ActivityTransitionBinding
import com.cyd.cyd_soft_competition.dbmsg.Messages


class TransitionActivity : AppCompatActivity() {
    private var current = 0
    private lateinit var binding: ActivityTransitionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransitionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        playForegroundVideoFromRaw()
        binding.vvPlayer.setOnClickListener {
            clicked()
        }
        binding.vvPlayer.setOnCompletionListener {
            clicked()
        }


    }

    private fun clicked() {
        when(current){
            0 ->{
                current = 1
                playForegroundVideoFromRaw()
                val tag = Messages().getTags()
                val tagNums = Messages().getTagNums()
                binding.tvLocation2.text = tag[0]
                binding.tvLocation3.text = tag[1]
                binding.tvLocation4.text = tagNums[0]
                binding.tvLocation5.text = tagNums[1]
                binding.tvLocation2.visibility = android.view.View.VISIBLE
                binding.tvLocation3.visibility = android.view.View.VISIBLE
                binding.tvLocation4.visibility = android.view.View.VISIBLE
                binding.tvLocation5.visibility = android.view.View.VISIBLE


            }
            1 ->{
                startActivity(Intent(this, SmileTransitionActivity::class.java))
                finish()
            }

        }
    }

    /**
     * 播放 raw 目录下的本地视频
     */
    private fun playForegroundVideoFromRaw() {
        val videoView = binding.vvPlayer
        // 1. 获取 raw 目录下视频的 Uri
        val videoUri = if (current == 0){
            Uri.parse("android.resource://${packageName}/${R.raw.transition1}")
        }else{
            Uri.parse("android.resource://${packageName}/${R.raw.transition2}")
        }
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