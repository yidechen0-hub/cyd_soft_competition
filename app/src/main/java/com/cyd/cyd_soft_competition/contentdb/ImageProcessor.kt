package com.cyd.cyd_soft_competition.contentdb

// 在 Activity 或 Fragment 中使用
import android.content.Context
import android.util.Log
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class ImageProcessor(private val context: Context) {

    /**
     * 给定外部存储的绝对路径（如 "/sdcard/DCIM/test.jpg"），
     * 将其复制到私有目录，并调用 Python 脚本处理。
     *
     * @param externalPath 外部存储的完整路径（必须存在且可读）
     * @return Python 脚本的返回值（如 (lat, lon) 元组），或 null 表示失败
     */
//    fun processExternalImage(externalPath: String): String? {
//        // 1. 检查 Chaquopy 是否初始化
//        if (!Python.isStarted()) {
//            Python.start(AndroidPlatform(context))
//        }
//
//        // 2. 构造 Uri（兼容 SAF 和传统路径）
//        val uri = try {
//            if (externalPath.startsWith("file://")) {
//                Uri.parse(externalPath)
//            } else {
//                // 注意：直接 file:/// 路径在 Android 10+ 可能无效！
//                // 更安全的方式是通过 MediaStore 获取 Uri，但这里假设你已有合法路径
//                File(externalPath).toUri()
//            }
//        } catch (e: Exception) {
//            Log.e("ImageProcessor", "无效路径: $externalPath", e)
//            return null
//        }
//
//        // 3. 生成目标文件名（保留扩展名）
//        val fileName = File(externalPath).name
//        val privateFile = File(context.filesDir, "temp_$fileName")
//
//        // 4. 从外部路径读取并复制到私有目录
//        return try {
//            context.contentResolver.openInputStream(uri)?.use { input ->
//                FileOutputStream(privateFile).use { output ->
//                    input.copyTo(output)
//                }
//            } ?: run {
//                Log.e("ImageProcessor", "无法打开输入流: $uri")
//                return null
//            }
//
//            Log.d("ImageProcessor", "已复制到私有目录: ${privateFile.absolutePath}")
//
//
////
////            // 可选：处理完后删除临时文件
////            privateFile.delete()
////
////            result
//            privateFile.absolutePath
//        } catch (e: Exception) {
//            Log.e("ImageProcessor", "处理图片失败: $externalPath", e)
//            privateFile.delete() // 清理
//            null
//        }
//    }
    fun processExternalImage(externalPath: String): String? {
        // 1. 检查 Chaquopy 是否初始化
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context))
        }

        // 2. 优先处理真实文件路径（避免 ContentResolver 导致的元数据丢失）
        val externalFile = File(externalPath)
        if (!externalFile.exists() || !externalFile.isFile) {
            Log.e("ImageProcessor", "文件不存在或不是有效文件: $externalPath")
            return null
        }

        // 3. 生成目标文件名（保留扩展名，避免冲突）
        val fileName = "temp_${System.currentTimeMillis()}_${externalFile.name}"
        val privateFile = File(context.filesDir, fileName)

        // 4. 直接用 File 流复制（关键：保留完整元数据）
        return try {
            FileInputStream(externalFile).use { input ->
                FileOutputStream(privateFile).use { output ->
                    // 缓冲区复制，提升效率且保留元数据
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                    output.flush()
                }
            }

            Log.d("ImageProcessor", "已复制到私有目录（元数据保留）: ${privateFile.absolutePath}")
            privateFile.absolutePath
        } catch (e: SecurityException) {
            // 若直接文件复制失败（权限问题），降级为 ContentResolver 复制
            Log.w("ImageProcessor", "直接复制权限不足，尝试降级方案", e)
//            copyWithContentResolver(externalFile.toUri(), fileName)
            null
        } catch (e: Exception) {
            Log.e("ImageProcessor", "文件复制失败", e)
            privateFile.delete() // 清理失败的临时文件
            null
        }
    }

    fun processImageGpsPython(private_path: String): Pair<Double, Double>?  {
            // 5. 调用 Python 脚本
            val py = Python.getInstance()
            val module = py.getModule("photo_exif_reader") // 对应 src/main/python/exif_reader.py
            val result = module.callAttr("get_geo", private_path)
        return try {
            // 1. 确保 result 是 List<*> 类型
            val list = result as? List<*> ?: return null

            // 2. 确保至少有两个元素
            if (list.size < 2) return null

            // 3. 安全转换为 Double（支持 Int/Float/String）
            val lat = list[0]?.let { toDoubleOrNull(it) } ?: return null
            val lon = list[1]?.let { toDoubleOrNull(it) } ?: return null

            Pair(lat, lon)
        } catch (e: Exception) {
            Log.e("EXIF", "解析 Python 返回值失败", e)
            null
        }
    }
    // 辅助函数：安全转 Double
    private fun toDoubleOrNull(value: Any?): Double? {
        return when (value) {
            is Double -> value
            is Float -> value.toDouble()
            is Int -> value.toDouble()
            is Long -> value.toDouble()
            is String -> value.toDoubleOrNull()
            else -> null
        }
    }
}