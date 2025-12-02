package com.cyd.cyd_soft_competition.activity.endingActivity


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cyd.cyd_soft_competition.activity.MainActivity
import com.cyd.cyd_soft_competition.databinding.ActivityEndingImgBinding
import com.cyd.cyd_soft_competition.dbmsg.Messages


class EndingImgActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEndingImgBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEndingImgBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBg.setOnClickListener {
            startActivity(Intent(this@EndingImgActivity, MainActivity::class.java))
            finish()
        }
        binding.tvCountImg.text = Messages(this).getImgAndVideoNum()
        binding.tvCountCountry.text = Messages(this).getLocationMsg()
        binding.tvCountSmile.text = Messages(this).getSmileCounts()
        val tags = Messages(this).getTags()
        binding.tvTag.text = if (tags.size >= 2) {
            "${tags[0]}和${tags[1]}"
        } else if (tags.size == 1) {
            tags[0] // 只有1个标签时直接显示
        } else {
            "" // 无标签时显示空字符串（也可自定义默认值，如"暂无标签"）
        }



    }

    override fun onDestroy() {
        super.onDestroy()
        // 释放资源
        binding.ivBg.setImageBitmap(null)
    }
}