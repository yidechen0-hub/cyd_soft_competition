package com.cyd.cyd_soft_competition.activity

// 文件名：MaskDemoActivity.kt（独立Activity，仅演示掩码功能）
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.cyd.cyd_soft_competition.mask.MaskImageView
import com.cyd.cyd_soft_competition.databinding.ActivityMaskBinding
import com.cyd.cyd_soft_competition.R

class MaskDemoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMaskBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val maskImageView = binding.maskImageView
        // 强制可见
        maskImageView.visibility = View.VISIBLE

//        // 加载图片（优先用 drawable 资源，避免路径问题）
//        maskImageView.setTargetImageRes(R.drawable.test_target) // 目标图（放入 drawable）
//        maskImageView.setMaskImageRes(R.drawable.test_mask)     // 掩码图（放入 drawable）

        // 若加载本地图片，确保路径正确（示例）
         val targetPath = "/sdcard/taiyi/test/AlbumSearchPipelineAbility/2.jpg"
         val maskPath = "/sdcard/taiyi/test/AlbumSearchPipelineAbility/spring.png"
         maskImageView.setTargetImage(targetPath)
         maskImageView.setMaskImage(maskPath)
    }
}