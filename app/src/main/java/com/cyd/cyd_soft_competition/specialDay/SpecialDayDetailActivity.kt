package com.cyd.cyd_soft_competition.specialDay

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cyd.cyd_soft_competition.databinding.ActivitySpecialDayDetailBinding
import com.cyd.cyd_soft_competition.dbmsg.Messages
import java.io.File

class SpecialDayDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySpecialDayDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpecialDayDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val specialDaySinglePath = Messages(this).getSpecialDaySinglePath()

        if (specialDaySinglePath.isNotEmpty()) {
            // Use Glide for async image loading
            Glide.with(this)
                .load(File(specialDaySinglePath))
                .fitCenter()
                .into(binding.ivForeground)
        }

        binding.root.setOnClickListener {
            startActivity(Intent(this, SpecialDayGridActivity::class.java))
            finish()
        }

        val date = Messages(this).getSpecialDayDate()
        if (date.isNotEmpty()) {
            binding.tvTime.text = date
        }

    }
}
