package com.cyd.cyd_soft_competition.activity.endingActivity


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cyd.cyd_soft_competition.activity.MainActivity
import com.cyd.cyd_soft_competition.databinding.ActivityEndingImgBinding


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

    }

    override fun onDestroy() {
        super.onDestroy()
        // 释放资源
        binding.ivBg.setImageBitmap(null)
    }
}