package com.cyd.cyd_soft_competition.activity.tagActivity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.cyd.cyd_soft_competition.R
import com.cyd.cyd_soft_competition.activity.seasonActivity.SeasonTransitionActivity
import com.cyd.cyd_soft_competition.databinding.ActivitySmileVedioBinding
import java.io.File
import java.util.Timer
import java.util.TimerTask

class SmileVedioActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySmileVedioBinding
    private val imagePaths = mutableListOf<String>()
    private var currentPage = 0
    private var timer: Timer? = null
    private val DELAY_MS: Long = 200
    private val PERIOD_MS: Long = 3000
    private val SMILE_DIR = "/sdcard/taiyi/competition/smile/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySmileVedioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadImages()
        setupViewPager()

        binding.clRoot.setOnClickListener {
            startActivity(Intent(this, SeasonTransitionActivity::class.java))
        }
    }

    private fun loadImages() {
        val dir = File(SMILE_DIR)
        if (dir.exists() && dir.isDirectory) {
            val files = dir.listFiles { file ->
                val name = file.name.lowercase()
                name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".jpeg")
            }
            files?.forEach {
                imagePaths.add(it.absolutePath)
            }
        }
        
        // Fallback if no images found (avoid crash/empty)
        if (imagePaths.isEmpty()) {
             // Add some default or placeholder if needed, or just handle empty state
             // For now, let's assume there are images or just show nothing
        }
    }

    private fun setupViewPager() {
        if (imagePaths.isEmpty()) return

        val adapter = SmileImageAdapter(imagePaths)
        binding.vpSmile.adapter = adapter
        binding.vpSmile.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        // Auto scroll
        val handler = Handler(Looper.getMainLooper())
        val update = Runnable {
            if (currentPage == imagePaths.size) {
                currentPage = 0
            }
            binding.vpSmile.setCurrentItem(currentPage++, true)
        }

        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                handler.post(update)
            }
        }, DELAY_MS, PERIOD_MS)
        
        binding.vpSmile.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentPage = position
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }

    inner class SmileImageAdapter(private val paths: List<String>) : RecyclerView.Adapter<SmileImageAdapter.SmileViewHolder>() {

        inner class SmileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView: ImageView = itemView.findViewById(R.id.iv_smile)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmileViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_smile_image, parent, false)
            return SmileViewHolder(view)
        }

        override fun onBindViewHolder(holder: SmileViewHolder, position: Int) {
            val path = paths[position]
            holder.imageView.setImageURI(Uri.fromFile(File(path)))
        }

        override fun getItemCount(): Int = paths.size
    }
}