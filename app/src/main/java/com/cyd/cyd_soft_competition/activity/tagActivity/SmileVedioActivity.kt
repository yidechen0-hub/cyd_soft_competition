package com.cyd.cyd_soft_competition.activity.tagActivity



import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cyd.cyd_soft_competition.databinding.ActivitySmileVedioBinding
import com.cyd.cyd_soft_competition.dbmsg.Messages
import com.cyd.cyd_soft_competition.activity.seasonActivity.SeasonTransitionActivity
import kotlin.text.startsWith

class SmileVedioActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySmileVedioBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySmileVedioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val vedioPath = Messages(this).getSmileVedioPath()
        initPlayer(vedioPath)


        binding.clRoot.setOnClickListener {
            startActivity(Intent(this, SeasonTransitionActivity::class.java))
            finish()
        }
        binding.vvSmileVedio.setOnClickListener {
            if (binding.vvSmileVedio.isPlaying()){
                binding.vvSmileVedio.pause()
            }else{
                binding.vvSmileVedio.start()
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        binding.vvSmileVedio.stopPlayback() // 停止播放并释放资源

    }

    /**
     * 初始化播放器，加载并播放当前索引的视频
     */
    private fun initPlayer(vedioPath: String) {

        val videoUri: Uri?

        // 区分本地视频和网络视频（本地路径以 file:// 开头，网络以 http/https 开头）
        if (vedioPath.startsWith("http")) {
            videoUri = Uri.parse(vedioPath) // 网络视频
        } else {
            videoUri = Uri.parse("file://" + vedioPath) // 本地视频（需传入绝对路径）
        }

        // 设置视频地址并播放

        binding.vvSmileVedio!!.setVideoURI(videoUri)
        binding.vvSmileVedio.start()

    }
}