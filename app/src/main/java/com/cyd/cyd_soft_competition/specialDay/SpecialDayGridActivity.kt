package com.cyd.cyd_soft_competition.specialDay

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.cyd.cyd_soft_competition.databinding.ActivitySpecialDayGridBinding
import com.cyd.cyd_soft_competition.databinding.ItemSpecialDayGridBinding
import com.cyd.cyd_soft_competition.dbmsg.Messages
import java.io.File

class SpecialDayGridActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySpecialDayGridBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpecialDayGridBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gridPaths = Messages(this).getSpecialDayGridData()

        // Get and display the date and photo count
        val (date, count) = Messages(this).getMostPhotoDayInfo()
        if (date.isNotEmpty()) {
            binding.tvTime.text = date
            binding.tvCount.text = "你拍摄了${count}张照片"
        }

        binding.rvGrid.layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        binding.rvGrid.adapter = SpecialDayGridAdapter(gridPaths)
    }

    inner class SpecialDayGridAdapter(private val paths: List<String>) :
        RecyclerView.Adapter<SpecialDayGridAdapter.ViewHolder>() {

        inner class ViewHolder(val binding: ItemSpecialDayGridBinding) :
            RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemSpecialDayGridBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val path = paths[position]
            
            // Use Glide for async image loading to prevent blocking main thread
            Glide.with(holder.itemView.context)
                .load(File(path))
                .centerCrop()
                .into(holder.binding.ivItem)
            
            // Random scaling for staggered effect
            val params = holder.binding.ivItem.layoutParams
            params.height = (300..600).random() // Random height between 300 and 600 pixels
            holder.binding.ivItem.layoutParams = params
        }

        override fun getItemCount(): Int = paths.size
    }
}
