package com.cyd.cyd_soft_competition.mask

// 文件名：MaskImageView.kt（独立文件，放在任意包下）
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import java.io.File

/**
 * 独立的掩码ImageView：白色像素显示目标图，非白色像素透明
 * 用法：setTargetImage（设置目标图） + setMaskImage（设置掩码图）
 */
class MaskImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {
    // 混合模式画笔（核心：目标图仅在掩码白色区域显示）
    private val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
    }
    // 目标图片（需要被掩码控制的图片）
    private var targetBitmap: Bitmap? = null
    // 掩码图片（黑白PNG，白色=显示，非白色=透明）
    private var maskBitmap: Bitmap? = null
    // 绘制区域（铺满控件）
    private val drawRect = RectF()
    private val TAG = "MaskImageView"

    init {
        // 开启硬件加速，提升绘制性能（可选，部分机型需关闭）
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    /**
     * 设置目标图片（本地绝对路径）
     */
    fun setTargetImage(localPath: String) {
        if (!File(localPath).exists()) {
            targetBitmap = null
            invalidate()
            return
        }
        Glide.with(context)
            .asBitmap()
            .load(File(localPath))
            .into(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    targetBitmap = resource
                    invalidate() // 刷新绘制
                }
            })
    }

    /**
     * 设置目标图片（drawable资源ID）
     */
    fun setTargetImageRes(resId: Int) {
        Glide.with(context)
            .asBitmap()
            .load(resId)
            .into(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    targetBitmap = resource
                    invalidate()
                }
            })
    }

    /**
     * 设置掩码图片（本地绝对路径）
     */
    fun setMaskImage(localPath: String) {
        if (!File(localPath).exists()) {
            maskBitmap = null
            invalidate()
            return
        }
        Glide.with(context)
            .asBitmap()
            .load(File(localPath))
            .into(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    maskBitmap = resource
                    invalidate()
                }
            })
    }

    /**
     * 设置掩码图片（drawable资源ID）
     */
    fun setMaskImageRes(resId: Int) {
        Glide.with(context)
            .asBitmap()
            .load(resId)
            .into(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    maskBitmap = resource
                    invalidate()
                }
            })
    }

    /**
     * 清除图片和掩码（释放资源）
     */
    fun clear() {
        targetBitmap?.recycle()
        maskBitmap?.recycle()
        targetBitmap = null
        maskBitmap = null
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        drawRect.set(0f, 0f, w.toFloat(), h.toFloat()) // 更新绘制区域
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 目标图或掩码图为空，直接返回（不绘制）
        if (targetBitmap == null || maskBitmap == null) return

        // 1. 保存画布状态（避免影响其他绘制）
        val saveCount = canvas.saveLayer(drawRect, null, Canvas.ALL_SAVE_FLAG)

        // 2. 绘制目标图（先画，作为混合的"目标"）
        canvas.drawBitmap(targetBitmap!!, null, drawRect, null)

        // 3. 绘制掩码图（后画，作为混合的"源"，通过混合模式筛选显示区域）
        canvas.drawBitmap(
            maskBitmap!!,
            Matrix().apply {
                // 自动缩放掩码图至控件尺寸（与目标图匹配）
                val scaleX = drawRect.width() / maskBitmap!!.width
                val scaleY = drawRect.height() / maskBitmap!!.height
                setScale(scaleX, scaleY)
            },
            maskPaint
        )

        // 4. 恢复画布状态
        canvas.restoreToCount(saveCount)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        clear() // 页面销毁时释放资源，避免内存泄漏
    }
    // 关键修复：重写 onMeasure，适配 wrap_content
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        // 1. 获取父容器给的宽度模式和尺寸（match_parent 时 width 为屏幕宽度）
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        // 2. 若目标图片已加载，且高度为 wrap_content，根据图片尺寸计算高度
        if (targetBitmap != null && MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
            val imageWidth = targetBitmap!!.width
            val imageHeight = targetBitmap!!.height

            if (imageWidth > 0 && widthSize > 0) {
                // 3. 按宽度比例缩放高度（保持图片宽高比，避免变形）
                val scale = widthSize.toFloat() / imageWidth
                val measuredHeight = (imageHeight * scale).toInt()

                // 4. 设置最终测量尺寸（宽度=父容器给的尺寸，高度=按比例计算的尺寸）
                setMeasuredDimension(widthSize, measuredHeight)
            }
        }
    }
}