package com.cyd.cyd_soft_competition.specialDay

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cyd.cyd_soft_competition.databinding.ActivitySpecialDayTransitionBinding


class SpecialDayTransitionActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySpecialDayTransitionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpecialDayTransitionBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.root.setOnClickListener {
            startActivity(Intent(this, SpecialDayDetailActivity::class.java))
            finish()
        }
    }
}
