package com.cyd.cyd_soft_competition.top20Activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.cyd.cyd_soft_competition.databinding.ActivityTopBinding
import com.cyd.cyd_soft_competition.databinding.ItemSpecialDayGridBinding
import com.cyd.cyd_soft_competition.dbmsg.Messages
import com.cyd.cyd_soft_competition.endingActivity.EndingVedioActivity
import java.io.File

class TopActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTopBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTopBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gridPaths = Messages(this).getTop20Paths()


        binding.rvGrid.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.rvGrid.adapter = SpecialDayGridAdapter(gridPaths)


        binding.ivBg.setOnClickListener {
            startActivity(Intent(this, EndingVedioActivity::class.java))
            finish()
        }
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
            params.height = (450..900).random() // Random height between 300 and 600 pixels
            holder.binding.ivItem.layoutParams = params
        }

        override fun getItemCount(): Int = paths.size
    }
}