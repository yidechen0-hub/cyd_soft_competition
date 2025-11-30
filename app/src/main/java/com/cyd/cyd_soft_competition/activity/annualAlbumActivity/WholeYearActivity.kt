package com.cyd.cyd_soft_competition.activity.annualAlbumActivity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cyd.cyd_soft_competition.R
import com.cyd.cyd_soft_competition.databinding.ActivityWholeYearBinding
import com.cyd.cyd_soft_competition.dbmsg.Messages

class WholeYearActivity : AppCompatActivity() {
    private var currentImageIndex = 0
    private lateinit var binding: ActivityWholeYearBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWholeYearBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // 拍摄时间
        val time = Messages(this).getFirstImgMsg()
        binding.tvTime.text = Messages(this).getImgAndVideoNum()

        binding.clRoot.setOnClickListener {
            when(currentImageIndex){
                0 ->{
                    currentImageIndex = 1
                    showSec()
                }
                1 ->{
                    startActivity(Intent(this, LoicationActivity::class.java))
                    finish()
                }
            }
        }

    }

    fun showSec(){
        binding.ivBg.setImageResource(R.drawable.bg_whole_year2)
        binding.tvTime.text = Messages(this).getAvgShootNum()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 释放资源
        binding.ivBg.setImageBitmap(null)
        binding.tvTime.text = ""
    }
}