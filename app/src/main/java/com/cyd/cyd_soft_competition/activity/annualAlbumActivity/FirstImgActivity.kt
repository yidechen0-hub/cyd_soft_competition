package com.cyd.cyd_soft_competition.activity.annualAlbumActivity


import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cyd.cyd_soft_competition.databinding.ActivityFirstImgBinding
import java.io.File
import com.cyd.cyd_soft_competition.dbmsg.Messages

class FirstImgActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFirstImgBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFirstImgBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // 本地图片路径
        val imagePath = Messages(this).getFirstImgPath()
        // 本地图片文件
        val imageFile = File(imagePath)

        // 检查文件是否存在
        if (imageFile.exists()) {
            try {
                val bitmap = BitmapFactory.decodeFile(imagePath)

                binding.ivForeground.setImageBitmap(bitmap)

                // 可选：设置缩放模式（确保图片适应固定大小）
                binding.ivForeground.scaleType = ImageView.ScaleType.FIT_CENTER
                binding.ivForeground.adjustViewBounds = true
            } catch (e: Exception) {
                // 异常处理（文件损坏、权限不足等）
                e.printStackTrace()
            }
        } else {
            Toast.makeText(this, "图片文件不存在", Toast.LENGTH_SHORT).show()
        }

        // 拍摄时间
        val time = Messages(this).getFirstImgMsg()
        binding.tvTime.text = time

        binding.clRoot.setOnClickListener {
            val intent = Intent(this, WholeYearActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        // 在 Activity 销毁时，释放 ImageView 的资源
        binding.ivForeground.setImageDrawable(null)
        binding.ivForeground.setImageBitmap(null)

    }
}