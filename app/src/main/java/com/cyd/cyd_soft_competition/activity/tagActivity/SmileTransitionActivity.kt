package com.cyd.cyd_soft_competition.activity.tagActivity


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cyd.cyd_soft_competition.databinding.ActivitySmileTransitionBinding
import com.cyd.cyd_soft_competition.dbmsg.Messages


class SmileTransitionActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySmileTransitionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySmileTransitionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvTime.text = Messages(this).getSmileCounts()



        binding.clRoot.setOnClickListener {
            val intent = Intent(this, SmileVedioActivity::class.java)
            startActivity(intent)
            finish()
        }


    }


    override fun onDestroy() {
        super.onDestroy()
        // 释放资源
        binding.ivBg.setImageBitmap(null)
        binding.tvTime.text = ""
    }
}