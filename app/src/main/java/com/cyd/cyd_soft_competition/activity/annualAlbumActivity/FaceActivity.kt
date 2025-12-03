package com.cyd.cyd_soft_competition.activity.annualAlbumActivity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cyd.cyd_soft_competition.R
import com.cyd.cyd_soft_competition.databinding.ActivityFaceBinding
import com.cyd.cyd_soft_competition.dbmsg.Messages
import com.cyd.cyd_soft_competition.activity.tagActivity.TransitionActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FaceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFaceBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.maskImageView.visibility = View.VISIBLE
        // 保证在最上层
        binding.maskImageView.bringToFront()

        // 使用协程在后台线程加载数据
        lifecycleScope.launch(Dispatchers.IO) {
            val facePaths = Messages(this@FaceActivity).getFacePath()
            
            withContext(Dispatchers.Main) {
                if (facePaths.isNotEmpty()) {
                    binding.maskImageView.setTargetImage(facePaths.getOrNull(0) ?: "")
                    binding.maskImageView.setMaskImageRes(R.drawable.bg_star)
                }
                
                if (facePaths.size > 1) {
                    binding.maskImageView2.setTargetImage(facePaths.getOrNull(1) ?: "")
                    binding.maskImageView2.setMaskImageRes(R.drawable.bg_flower)
                }
                
                if (facePaths.size > 2) {
                    binding.maskImageView3.setTargetImage(facePaths.getOrNull(2) ?: "")
                    binding.maskImageView3.setMaskImageRes(R.drawable.bg_bong)
                }
            }
        }

        binding.clRoot.setOnClickListener {
            val intent = Intent(this, TransitionActivity::class.java)
            startActivity(intent)
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        // 释放资源
        binding.ivBg.setImageBitmap(null)
        binding.tvTime.text = ""
    }
}