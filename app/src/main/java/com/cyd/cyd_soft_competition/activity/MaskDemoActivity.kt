package com.cyd.cyd_soft_competition.activity

// 文件名：MaskDemoActivity.kt（独立Activity，仅演示掩码功能）
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cyd.cyd_soft_competition.mask.MaskImageView
import com.cyd.cyd_soft_competition.databinding.ActivityMaskBinding

class MaskDemoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMaskBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. 获取 MaskImageView（布局中定义）
        val maskImageView: MaskImageView = binding.maskImageView

        // 2. 配置目标图和掩码图（二选一配置掩码）
        val targetImagePath = "/sdcard/taiyi/test/AlbumSearchPipelineAbility/1.jpg" // 你的目标图路径
        val maskImagePath = "/sdcard/taiyi/test/AlbumSearchPipelineAbility/spring.png" // 你的掩码图路径（本地）

        // 3. 设置图片（核心调用）
        maskImageView.setTargetImage(targetImagePath) // 设置目标图
        maskImageView.setMaskImage(maskImagePath) // 设置掩码图（本地路径）
        // maskImageView.setMaskImageRes(maskImageRes) // 或设置drawable资源

        // 4. 可选：添加点击事件
        maskImageView.setOnClickListener {
            // 点击逻辑
        }
    }
}