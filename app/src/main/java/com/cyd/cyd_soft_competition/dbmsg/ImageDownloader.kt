package com.cyd.cyd_soft_competition.dbmsg

import android.content.Context
import android.os.Environment
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

object ImageDownloader {
    // OkHttpClient 实例（全局复用）
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * 下载图片到指定目录
     * @param imageUrl 图片网络 URL
     * @param context 上下文（用于获取存储路径）
     * @param targetDir 目标目录（如 "Download/MyAppImages"，相对于外部存储根目录或 App 私有目录）
     * @param isExternal 是否下载到外部存储（true=外部存储，false=App 私有目录）
     * @return 本地绝对路径（成功）/ null（失败）
     */
    fun downloadImage(
        imageUrl: String,
        context: Context,
        targetDir: String = "/sdcard/taiyi/competition/test",
        isExternal: Boolean = true
    ): String? {
        return try {
            // 1. 生成目标文件（文件名从 URL 提取，避免重复）
            val fileName = imageUrl.substringAfterLast("/") // 提取文件名：306d8ec0-bd9c-4ccc-91fc-1bed12d82368.jpg
            val targetFile = getTargetFile(context, targetDir, fileName, isExternal) ?: return null

            if (targetFile.exists() && targetFile.length() > 0) { // 额外检查文件大小，避免空文件
                return targetFile.absolutePath
            }

            // 2. 发起网络请求下载图片
            val request = Request.Builder().url(imageUrl).build()
            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                println("下载失败：响应码 ${response.code}")
                return null
            }

            // 3. 写入文件
            val inputStream: InputStream = response.body?.byteStream() ?: return null
            val outputStream = FileOutputStream(targetFile)
            val buffer = ByteArray(1024 * 8) // 8KB 缓冲区
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }

            // 4. 关闭流，返回绝对路径
            outputStream.flush()
            outputStream.close()
            inputStream.close()
            targetFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            println("下载异常：$e")
            null
        }
    }

    /**
     * 获取目标文件（创建目录 + 生成文件实例）
     */
    private fun getTargetFile(
        context: Context,
        targetDir: String,
        fileName: String,
        isExternal: Boolean
    ): File? {
        return try {
            val dir = if (isExternal) {
                // 外部存储目录（Android 10+ 推荐用 MediaStore，此处兼容旧版本）
                if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
                    File(Environment.getExternalStorageDirectory(), targetDir)
                } else {
                    println("外部存储不可用")
                    return null
                }
            } else {
                // App 私有目录（无需权限，推荐）
                File(context.filesDir, targetDir) // 路径：/data/data/包名/files/Download/MyAppImages
                // 或缓存目录（适合临时文件）：File(context.cacheDir, targetDir)
            }

            // 创建目录（若不存在）
            if (!dir.exists()) {
                dir.mkdirs() // 递归创建多级目录
            }

            File(dir, fileName)
        } catch (e: Exception) {
            println("创建目标文件失败：${e.message}")
            null
        }
    }
}