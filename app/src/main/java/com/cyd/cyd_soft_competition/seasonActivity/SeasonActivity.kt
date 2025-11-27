package com.cyd.cyd_soft_competition.seasonActivity

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.cyd.cyd_soft_competition.activity.AnimVideoActivity
import com.cyd.cyd_soft_competition.databinding.ActivitySeasonBinding
import com.cyd.cyd_soft_competition.R
import com.cyd.cyd_soft_competition.dbmsg.Messages
import com.cyd.cyd_soft_competition.specialDay.SpecialDayTransitionActivity


class SeasonActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySeasonBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeasonBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val path = Messages(this).getSeasonPath()
        loadSpring(path)

        binding.ivFullBackground.setOnClickListener {
            val intent = Intent(this, SpecialDayTransitionActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.ivSpring.setOnClickListener {
            val intent = Intent(this, SpecialDayTransitionActivity::class.java)
            startActivity(intent)
            finish()
        }
//        binding.ivSummer.setOnClickListener {
//            val intent = Intent(this, SpecialDayTransitionActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
//        binding.ivAutumn.setOnClickListener {
//            val intent = Intent(this, SpecialDayTransitionActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
//        binding.ivWinter.setOnClickListener {
//            val intent = Intent(this, SpecialDayTransitionActivity::class.java)
//            startActivity(intent)
//            finish()
//        }



    }

    private fun loadSpring(path: List<String>){
        val springPath = path[0]
        binding.maskSpring.setMaskImageRes(R.drawable.spring)
        binding.maskSpring.setTargetImage(springPath)

        val bitmap = BitmapFactory.decodeFile(springPath)

        binding.ivSpring.setImageBitmap(bitmap)

        // 可选：设置缩放模式（确保图片适应固定大小）
        binding.ivSpring.scaleType = ImageView.ScaleType.FIT_XY
        binding.ivSpring.adjustViewBounds = true

    }
}