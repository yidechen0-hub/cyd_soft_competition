package com.cyd.cyd_soft_competition.activity

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.cyd.cyd_soft_competition.databinding.ActivityTargetBinding


class TargetActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTargetBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTargetBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}