package com.cyd.cyd_soft_competition.activity.annualAlbumActivity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.cyd.cyd_soft_competition.R
import com.cyd.cyd_soft_competition.databinding.ActivityFaceBinding
import com.cyd.cyd_soft_competition.dbmsg.Messages
import com.cyd.cyd_soft_competition.activity.tagActivity.TransitionActivity

class FaceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFaceBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.maskImageView.visibility = View.VISIBLE
        // 保证在最上层
        binding.maskImageView.bringToFront()


        //
        val facePath = Messages(this).getFacePath()[0]
        binding.maskImageView.setTargetImage(facePath)
        binding.maskImageView.setMaskImageRes(R.drawable.bg_star)


        val facePath2 = Messages(this).getFacePath()[1]
        binding.maskImageView2.setTargetImage(facePath2)
        binding.maskImageView2.setMaskImageRes(R.drawable.bg_flower)


        val facePath3 = Messages(this).getFacePath()[2]
        binding.maskImageView3.setTargetImage(facePath3)
        binding.maskImageView3.setMaskImageRes(R.drawable.bg_bong)

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