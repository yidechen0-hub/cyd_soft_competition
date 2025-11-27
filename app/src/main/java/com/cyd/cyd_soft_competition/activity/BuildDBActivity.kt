package com.cyd.cyd_soft_competition.activity


import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import com.cyd.cyd_soft_competition.buildDB.DatabaseManager
import com.cyd.cyd_soft_competition.buildDB.ImageScanner
import com.cyd.cyd_soft_competition.buildDB.ReverseGeoCoder
import java.io.File


class BuildDBActivity : ComponentActivity() {
    private val TAG = "BuildDBActivity"
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 为简单起见写一个按钮触发扫描
        val button = Button(this).apply { text = "开始扫描照片并建库" }
        setContentView(button)

        // 点击开始执行 build()
        button.setOnClickListener {
            requestAllFilesPermission()
        }
    }

    private var isBuilding = false

    fun buildDB() {
        if (isBuilding) {
            Log.w(TAG, "Build already in progress")
            return
        }
        isBuilding = true

        Thread {
            var dbManager: DatabaseManager? = null
            try {
                dbManager = DatabaseManager(this@BuildDBActivity)
                dbManager.initializeTables()

                val imageFiles = ImageScanner().scanDirectory("/sdcard/DCIM/test")
                Log.i(TAG, "Found ${imageFiles.size} images.")

                for (file in imageFiles) {
                    var metadata = ImageScanner().extractMetadata(file)
                    if (metadata.latitude != null && metadata.longitude != null) {
                        Log.i(TAG, "Fetching address for ${file.name}...")
                        val result = ReverseGeoCoder.getGlobalAddress(
                            metadata.latitude,
                            metadata.longitude
                        )
                        if (result.isSuccess && result.addressInfo != null) {
                            metadata = metadata.copy(
                                country = result.addressInfo.country,
                                province = result.addressInfo.province
                            )
                            Log.i(TAG, "Location: ${result.addressInfo.country}, ${result.addressInfo.province}")
                        } else {
                            Log.w(TAG, "Failed to get address: ${result.msg}")
                        }
                    }
                    dbManager.insertImageMetadata(metadata)
                }

                Handler(Looper.getMainLooper()).post {
                    if (!isFinishing && !isDestroyed) {
                        Log.i(TAG,"Database built successfully!")
                        Toast.makeText(this@BuildDBActivity, "Database built successfully!", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error during DB build", e)
                Handler(Looper.getMainLooper()).post {
                    if (!isFinishing && !isDestroyed) {
                        Toast.makeText(this@BuildDBActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } finally {
                dbManager?.close()
                isBuilding = false
            }
        }.start()
    }

    private fun requestAllFilesPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:${packageName}")
                startActivity(intent)
            } else {
                // 已授权，开始扫描
                buildDB()
            }
        }
    }


}