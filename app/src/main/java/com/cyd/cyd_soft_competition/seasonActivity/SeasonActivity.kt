package com.cyd.cyd_soft_competition.seasonActivity

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.cyd.cyd_soft_competition.R
import com.cyd.cyd_soft_competition.databinding.ActivitySeasonBinding
import com.cyd.cyd_soft_competition.databinding.ItemSeasonBinding
import com.cyd.cyd_soft_competition.dbmsg.Messages
import com.cyd.cyd_soft_competition.specialDay.SpecialDayTransitionActivity

class SeasonActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySeasonBinding

    // 季节数据类
    data class SeasonItem(
        val title: String,
        val imagePath: String,
        val maskResId: Int
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeasonBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val seasonPaths = Messages(this).getSeasonPath()

        // 创建季节数据列表
        val seasonItems = listOf(
            SeasonItem("春天，是万物复苏的生机", seasonPaths.getOrElse(0) { "" }, R.drawable.spring),
            SeasonItem("夏天，是热情似火的活力", seasonPaths.getOrElse(1) { "" }, R.drawable.summer),
            SeasonItem("秋天，是硕果累累的收获", seasonPaths.getOrElse(2) { "" }, R.drawable.autumn),
            SeasonItem("冬天，是银装素裹的宁静", seasonPaths.getOrElse(3) { "" }, R.drawable.winter)
        )

        // 动态添加季节视图到 LinearLayout
        seasonItems.forEach { item ->
            addSeasonView(item)
        }
    }

    private fun addSeasonView(item: SeasonItem) {
        // 为每个季节创建独立的视图
        val itemBinding = ItemSeasonBinding.inflate(LayoutInflater.from(this))
        
        // 设置标题
        itemBinding.tvSeasonTitle.text = item.title
        
        // 设置 MaskImageView（每个都是独立实例，不会被回收）
        if (item.imagePath.isNotEmpty()) {
            // 先设置目标图片，再设置遮罩
            itemBinding.maskSeason.setTargetImage(item.imagePath)
            itemBinding.maskSeason.setMaskImageRes(item.maskResId)
            
            // 加载下方的图片
            val bitmap = BitmapFactory.decodeFile(item.imagePath)
            if (bitmap != null) {
                itemBinding.ivSeason.setImageBitmap(bitmap)
                itemBinding.ivSeason.scaleType = ImageView.ScaleType.FIT_XY
                itemBinding.ivSeason.adjustViewBounds = true
            }
        }
        
        // 设置点击事件
        itemBinding.root.setOnClickListener {
            val intent = Intent(this, SpecialDayTransitionActivity::class.java)
            startActivity(intent)
            finish()
        }
        
        // 添加到容器
        binding.llSeasonContainer.addView(itemBinding.root)
    }
}