package com.cyd.cyd_soft_competition.mask

// 文件名：MaskImageHelper.kt（独立工具类）
import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout

/**
 * 掩码图片工具类：快速创建 MaskImageView 并配置掩码
 */
object MaskImageHelper {
    /**
     * 快速创建 MaskImageView（默认尺寸：match_parent + 300dp高度）
     */
    fun createMaskImageView(
        context: Context,
        targetImagePath: String, // 目标图本地路径
        maskImagePath: String? = null, // 掩码图本地路径
        maskImageRes: Int? = null // 掩码图drawable资源（二选一）
    ): MaskImageView {
        val maskImageView = MaskImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp2px(context, 300f) // 高度默认300dp，可自定义
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
            // 设置目标图
            setTargetImage(targetImagePath)
            // 设置掩码图（二选一）
            when {
                maskImagePath != null -> setMaskImage(maskImagePath)
                maskImageRes != null -> setMaskImageRes(maskImageRes)
            }
        }
        return maskImageView
    }

    /**
     * dp 转 px（工具方法）
     */
    private fun dp2px(context: Context, dpValue: Float): Int {
        val density = context.resources.displayMetrics.density
        return (dpValue * density + 0.5f).toInt()
    }
}