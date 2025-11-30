package com.cyd.cyd_soft_competition.activity.endingActivity


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cyd.cyd_soft_competition.databinding.ActivityEndingImgBinding


class EndingImgActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEndingImgBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEndingImgBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBg.setOnClickListener {
            finish()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        // 释放资源
        binding.ivBg.setImageBitmap(null)
    }
}