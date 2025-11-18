package com.cyd.cyd_soft_competition.activity

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.cyd.cyd_soft_competition.databinding.ActivityScrollImageBinding

class ScrollImageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScrollImageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScrollImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 找到可点击图标
        val clickableIcon: ImageView = binding.ivClickableIcon

        // 设置点击事件（跳转示例：跳转到 TargetActivity）
        clickableIcon.setOnClickListener {
            val intent = Intent(this, AnimVideoActivity::class.java)
            startActivity(intent)
            // 可选：添加跳转动画
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
}