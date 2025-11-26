package com.cyd.cyd_soft_competition.mask

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatImageView
import java.io.File

class MaskImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {
    private val TAG = "MaskImageView"
    private val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        isFilterBitmap = true
        alpha = 255
    }
    private var targetBitmap: Bitmap? = null
    private var maskBitmap: Bitmap? = null
    private val drawRect = RectF()

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        scaleType = ScaleType.FIT_XY
        // 1. 关键修改：去掉自身背景（改为完全透明）
        setBackgroundColor(Color.TRANSPARENT)
        // 额外优化：禁用控件自身的绘制背景逻辑
        setWillNotDraw(false)
    }

    // 目标图加载方法（无修改）
    fun setTargetImage(localPath: String) {
        Log.d(TAG, "原生加载目标图：路径=$localPath，文件是否存在=${File(localPath).exists()}")
        Thread {
            val file = File(localPath)
            if (!file.exists()) {
                Log.e(TAG, "目标图文件不存在")
                post { clearTargetBitmap() }
                return@Thread
            }

            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
                BitmapFactory.decodeFile(localPath, this)
                val screenWidth = context.resources.displayMetrics.widthPixels
                inSampleSize = calculateInSampleSize(this, screenWidth, screenWidth * 2)
                inJustDecodeBounds = false
                inPreferredConfig = Bitmap.Config.ARGB_8888 // 确保目标图支持透明度
            }

            val bitmap = BitmapFactory.decodeFile(localPath, options)
            post {
                if (bitmap != null) {
                    Log.d(TAG, "目标图加载成功：宽=${bitmap.width}，高=${bitmap.height}")
                    targetBitmap = bitmap
                    requestLayout()
                    invalidate()
                } else {
                    Log.e(TAG, "目标图加载失败")
                    clearTargetBitmap()
                }
            }
        }.start()
    }

    fun setTargetImageRes(resId: Int) {
        Log.d(TAG, "原生加载目标图：resId=$resId")
        Thread {
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888 // 确保目标图支持透明度
                val screenWidth = context.resources.displayMetrics.widthPixels
                inJustDecodeBounds = true
                BitmapFactory.decodeResource(context.resources, resId, this)
                inSampleSize = calculateInSampleSize(this, screenWidth, screenWidth * 2)
                inJustDecodeBounds = false
            }
            val bitmap = BitmapFactory.decodeResource(context.resources, resId, options)
            post {
                if (bitmap != null) {
                    Log.d(TAG, "目标图加载成功：宽=${bitmap.width}，高=${bitmap.height}")
                    targetBitmap = bitmap
                    requestLayout()
                    invalidate()
                } else {
                    Log.e(TAG, "目标图加载失败")
                    clearTargetBitmap()
                }
            }
        }.start()
    }

    // 掩码图加载方法（无修改，保持非白色透明处理）
    fun setMaskImage(localPath: String) {
        Log.d(TAG, "原生加载掩码图：路径=$localPath")
        Thread {
            val file = File(localPath)
            if (!file.exists()) {
                Log.e(TAG, "掩码图文件不存在")
                post { clearMaskBitmap() }
                return@Thread
            }
            var rawMask = BitmapFactory.decodeFile(localPath)
            rawMask = processMaskBitmap(rawMask)
            post {
                if (rawMask != null) {
                    Log.d(TAG, "掩码图加载并处理成功：宽=${rawMask.width}，高=${rawMask.height}")
                    maskBitmap = rawMask
                    invalidate()
                } else {
                    Log.e(TAG, "掩码图加载失败")
                    clearMaskBitmap()
                }
            }
        }.start()
    }

    fun setMaskImageRes(resId: Int) {
        Log.d(TAG, "原生加载掩码图：resId=$resId")
        Thread {
            var rawMask = BitmapFactory.decodeResource(context.resources, resId)
            rawMask = processMaskBitmap(rawMask)
            post {
                if (rawMask != null) {
                    Log.d(TAG, "掩码图加载并处理成功：宽=${rawMask.width}，高=${rawMask.height}")
                    maskBitmap = rawMask
                    invalidate()
                } else {
                    Log.e(TAG, "掩码图加载失败")
                    clearMaskBitmap()
                }
            }
        }.start()
    }

    // 掩码图处理方法（无修改，非白色区域透明）
    private fun processMaskBitmap(rawMask: Bitmap?): Bitmap? {
        if (rawMask == null) return null

        val processedMask = Bitmap.createBitmap(
            rawMask.width, rawMask.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(processedMask)
        canvas.drawBitmap(rawMask, 0f, 0f, null)

        val pixels = IntArray(processedMask.width * processedMask.height)
        processedMask.getPixels(pixels, 0, processedMask.width, 0, 0, processedMask.width, processedMask.height)

        for (i in pixels.indices) {
            val color = pixels[i]
            val red = Color.red(color)
            val green = Color.green(color)
            val blue = Color.blue(color)

            val isWhite = red > 100 && green > 100 && blue > 100
            pixels[i] = if (isWhite) Color.WHITE else Color.TRANSPARENT
        }

        processedMask.setPixels(pixels, 0, processedMask.width, 0, 0, processedMask.width, processedMask.height)
        rawMask.recycle()
        return processedMask
    }

    // 工具方法（无修改）
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun clearTargetBitmap() {
        targetBitmap?.recycle()
        targetBitmap = null
        invalidate()
    }

    private fun clearMaskBitmap() {
        maskBitmap?.recycle()
        maskBitmap = null
        invalidate()
    }

    fun clear() {
        clearTargetBitmap()
        clearMaskBitmap()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        drawRect.set(0f, 0f, w.toFloat(), h.toFloat())
        Log.d(TAG, "onSizeChanged：宽=$w，高=$h")
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val screenWidth = context.resources.displayMetrics.widthPixels
        val paddingLeft = paddingLeft
        val paddingRight = paddingRight
        val finalWidth = screenWidth - paddingLeft - paddingRight

        val minHeight = dp2px(400f)
        var finalHeight = minHeight

        targetBitmap?.let {
            if (finalWidth > 0 && it.width > 0) {
                val scale = finalWidth.toFloat() / it.width
                finalHeight = (it.height * scale).toInt()
                finalHeight = Math.max(finalHeight, minHeight)
            }
        }

        setMeasuredDimension(finalWidth, finalHeight)
        Log.d(TAG, "onMeasure：宽=$finalWidth，高=$finalHeight，targetBitmap是否为空=${targetBitmap == null}")
    }

    // 2. 关键修改：去掉占位绘制（避免灰色挡住下方控件）
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        Log.d(TAG, "===== onDraw 执行了！===== 目标图=${targetBitmap != null}，掩码图=${maskBitmap != null}")

        // 去掉灰色占位绘制（否则会挡住下方控件）
        // val placeholderPaint = Paint()
        // placeholderPaint.color = Color.parseColor("#CCCCCC")
        // canvas.drawRect(drawRect, placeholderPaint)

        // 3. 关键优化：混合绘制时确保图层透明
        if (targetBitmap != null && maskBitmap != null) {
            // 保存图层时指定透明背景
            val saveCount = canvas.saveLayer(0f, 0f, drawRect.width(), drawRect.height(), null, Canvas.ALL_SAVE_FLAG)
            try {
                canvas.drawBitmap(targetBitmap!!, null, drawRect, null)
                val maskMatrix = Matrix().apply {
                    val scaleX = drawRect.width() / maskBitmap!!.width
                    val scaleY = drawRect.height() / maskBitmap!!.height
                    setScale(scaleX, scaleY)
                }
                canvas.drawBitmap(maskBitmap!!, maskMatrix, maskPaint)
            } finally {
                canvas.restoreToCount(saveCount)
            }
        }
    }

    private fun dp2px(dpValue: Float): Int {
        val density = context.resources.displayMetrics.density
        return (dpValue * density + 0.5f).toInt()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        clear()
    }
}