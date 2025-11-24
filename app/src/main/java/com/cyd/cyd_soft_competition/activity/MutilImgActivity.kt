package com.cyd.cyd_soft_competition.activity


import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cyd.cyd_soft_competition.databinding.ActivityMutilImgBinding
import java.io.File
import com.cyd.cyd_soft_competition.R

class MutilImgActivity : AppCompatActivity() {
    private val autoScrollHandler = Handler(Looper.getMainLooper())
    private val scrollInterval = 30L // 滚动间隔（越小越流畅，30ms最佳）
    private val scrollStep = 10 // 每次滚动步长（像素），越大越快
    private var totalScrollHeight = 0 // 总滚动高度（所有有效图片+间距）
    private var isManualScrolling = false // 是否在手动滑动（暂停自动滚动）

    // View Binding
    private lateinit var binding: ActivityMutilImgBinding

    // 本地图片绝对路径列表（替换为你的实际路径！）
    private val selectedImages: MutableList<String> = mutableListOf(
        "/sdcard/taiyi/test/AlbumSearchPipelineAbility/1.jpg",
        "/sdcard/taiyi/test/AlbumSearchPipelineAbility/2.JPG",
        "/sdcard/taiyi/test/AlbumSearchPipelineAbility/3.JPG"
    )

    // 图片View列表
    private val imageViewList by lazy {
        listOf(binding.ivImage1, binding.ivImage2, binding.ivImage3)
    }

    // 有效图片路径列表
    private val validImagePaths by lazy {
        selectedImages.filter { File(it).exists() && File(it).isFile }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMutilImgBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupImages() // 动态显示有效图片
        setupAutoScroll() // 初始化持续滚动
        setupManualScrollListener() // 监听手动滑动（暂停/恢复自动滚动）
    }

    /**
     * 动态加载有效图片
     */
    private fun setupImages() {
        validImagePaths.forEachIndexed { index, imagePath ->
            if (index < imageViewList.size) {
                val imageView = imageViewList[index]
                imageView.visibility = android.view.View.VISIBLE

                Glide.with(this)
                    .load(File(imagePath))
//                    .placeholder(R.drawable.ic_placeholder)
//                    .error(R.drawable.ic_error)
                    .into(imageView)

                imageView.setOnClickListener {
                    Toast.makeText(this, "点击第${index+1}张图", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 隐藏多余的ImageView
        for (index in validImagePaths.size until imageViewList.size) {
            imageViewList[index].visibility = android.view.View.GONE
        }
    }

    /**
     * 计算总滚动高度，启动持续滚动
     */
    private fun setupAutoScroll() {
        binding.llImageContainer.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.llImageContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)

                // 有效图片≥2张才启动持续滚动
                if (validImagePaths.size >= 2) {
                    calculateTotalScrollHeight() // 计算总滚动高度
                    startContinuousScroll() // 启动持续滚动
                }
            }
        })
    }

    /**
     * 计算总滚动高度：所有有效图片高度 + 间距总和
     */
    private fun calculateTotalScrollHeight() {
        val imageSpacing = resources.getDimensionPixelSize(R.dimen.image_spacing)
        var imageTotalHeight = 0

        // 计算所有有效图片的总高度
        imageViewList.filter { it.visibility == android.view.View.VISIBLE }
            .forEach { imageView -> imageTotalHeight += imageView.height }

        // 计算间距总和（有效图片数-1 个间距）
        val spacingTotal = imageSpacing * (validImagePaths.size - 1)

        // 总滚动高度 = 图片总高度 + 间距总和（滑动到这里就是末尾）
        totalScrollHeight = imageTotalHeight + spacingTotal
    }

    /**
     * 持续滚动核心逻辑：每次滚动 scrollStep 像素，循环推进
     */
    private fun startContinuousScroll() {
        autoScrollHandler.postDelayed(object : Runnable {
            override fun run() {
                // 手动滑动时暂停自动滚动
                if (isManualScrolling) {
                    autoScrollHandler.postDelayed(this, scrollInterval)
                    return
                }

                val currentScrollY = binding.scrollView.scrollY
                val nextScrollY = currentScrollY + scrollStep

                // 滑动到末尾：瞬间回弹到顶部，继续滚动
                if (nextScrollY >= totalScrollHeight) {
                    binding.scrollView.scrollTo(0, 0) // 瞬间回弹（无动画）
                } else {
                    binding.scrollView.smoothScrollBy(0, scrollStep) // 持续平滑滚动
                }

                // 重复执行，实现持续滚动
                autoScrollHandler.postDelayed(this, scrollInterval)
            }
        }, scrollInterval)
    }

    /**
     * 监听手动滑动：手动滑动时暂停自动滚动，松手后恢复
     */
    private fun setupManualScrollListener() {
        var lastTouchY = 0f

        // 触摸开始：标记为手动滑动，暂停自动滚动
        binding.scrollView.setOnTouchListener { _, event ->
            when (event.action) {
                // 手指按下：记录初始位置，暂停自动滚动
                android.view.MotionEvent.ACTION_DOWN -> {
                    lastTouchY = event.y
                    isManualScrolling = true
                }
                // 手指松开/取消：恢复自动滚动
                android.view.MotionEvent.ACTION_UP,
                android.view.MotionEvent.ACTION_CANCEL -> {
                    isManualScrolling = false
                }
            }
            false // 不拦截触摸事件，保留ScrollView原生滑动
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 停止滚动，避免内存泄漏
        autoScrollHandler.removeCallbacksAndMessages(null)
    }
}