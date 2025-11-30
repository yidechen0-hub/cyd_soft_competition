package com.cyd.cyd_soft_competition.remoteService

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import com.cyd.cyd_soft_competition.buildDB.DatabaseManager

class TestFdsActivity: AppCompatActivity() {
    private val TAG = "TestFdsActivity"
    private val PATH = "/sdcard/DCIM/test"
    private lateinit var databaseManager: DatabaseManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databaseManager = DatabaseManager(this)

        val button = Button(this).apply { text = "开始向fds上传文件,并将url写入数据库" }
        setContentView(button)

        // 点击开始执行 build()
        button.setOnClickListener {
            val files = scanDirectory()
            for (file in files) {
                test(file)
            }
//            test(files[0])
        }


    }


    private fun test(myFile: File){
//        val myFile = File( "/sdcard/taiyi/competition/start/face1.JPG") // Ensure this file exists
        Log.i(TAG, "========== Starting upload for: ${myFile.absolutePath} ==========")
        FdsUploader.uploadFile(myFile, object : FdsUploader.UploadCallback {
            override fun onSuccess(key: String) {
                Log.i(TAG, "Upload Success: ${myFile.absolutePath} -> $key")
                Log.i(TAG, "Now fetching download URL for key: $key")
                // Now you can submit the task to /server/photoCommit with this key/url
                FdsUploader.getDownUrl(key){url, error ->
                    if (error != null) {
                        Log.e(TAG, "Failed to get download URL: ${error.message}", error)
                        return@getDownUrl
                    }
                    if (url == null){
                        Log.e(TAG, "Failed to get download URL: null")
                        return@getDownUrl
                    }
                    Log.i(TAG, "Got download URL: $url")
                    Log.i(TAG, "Updating database for path: ${myFile.absolutePath}")
                    databaseManager.updateImageUrl(myFile.absolutePath, url)
                    Log.i(TAG, "========== Upload and DB update complete ==========")
                }

            }
            override fun onError(e: Exception) {
                Log.e(TAG, "Upload Error: ${myFile.absolutePath}: ${e.message}", e)
            }
        })
    }


    fun scanDirectory(path: String = PATH): List<File> {
        val root = File(path)
        if (!root.exists() || !root.isDirectory) {
            Log.i(TAG,"Invalid directory: $path")
            return emptyList()
        }

        return root.walk()
            .filter { it.isFile && isImage(it) }
            .toList()
    }

    private fun isImage(file: File): Boolean {
        val extensions = listOf("jpg", "jpeg", "png", "webp", "heic")
        return extensions.any { file.extension.equals(it, ignoreCase = true) }
    }
}