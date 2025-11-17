package com.cyd.cyd_soft_competition.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.ExifInterface
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import com.cyd.cyd_soft_competition.contentdb.BuildDB
import com.cyd.cyd_soft_competition.contentdb.DBBuildConfig

class DBActivity : ComponentActivity() {
    private val TAG = "DBActivity"
    // 新增：存储要解析 GPS 的照片路径（需根据实际场景赋值，如扫描后获取）
    private var photoPath: String = "/storage/emulated/0/DCIM/test/G0158758.JPG"
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 为简单起见写一个按钮触发扫描
        val button = Button(this).apply { text = "开始扫描照片并建库" }
        setContentView(button)

        // 点击开始执行 build()
        button.setOnClickListener {
            checkPermissionAndStart()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkPermissionAndStart() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), 1001)
            return
        }

        startBuildProcess()
    }


    /** 真正开始执行 build(context, cfg) */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startBuildProcess() {
        Toast.makeText(this, "开始扫描，请稍等…", Toast.LENGTH_SHORT).show()


        requestManageExternalStoragePermission(this)


        val dcim = File("/storage/emulated/0/DCIM/test")  // 扫描 DCIM 文件夹

        val cfg = DBBuildConfig(
            root = dcim,
            dbName = "photos.db",
            fastHash = false,
        )

        // 必须在后台线程，否则会卡住 UI
        Thread {
            try {
                BuildDB().build(applicationContext, cfg)
                runOnUiThread {
                    Toast.makeText(this, "扫描完成并写入数据库！", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "发生错误: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    fun requestManageExternalStoragePermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 检查是否已授予所有文件访问权限
            if (!Environment.isExternalStorageManager()) {
                // 跳转系统设置页面授权
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                activity.startActivityForResult(intent, 1002)
            } else {
                // 已授权，直接读取 GPS
                readGpsWithFullPermission(photoPath)
            }
        }
    }

    // 授权结果回调（Activity 中）
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1002 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                Toast.makeText(this, "所有文件访问权限已授予", Toast.LENGTH_SHORT).show()
                readGpsWithFullPermission(photoPath)
            } else {
                Toast.makeText(this, "未授予所有文件访问权限，无法读取 GPS", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun readGpsWithFullPermission(photoPath: String) {
        Thread {
            try {
                // 授权后，用原生 ExifInterface 即可读取完整 GPS
                val exif = ExifInterface(photoPath)
                val latitude = parseGpsCoordinate(exif, ExifInterface.TAG_GPS_LATITUDE, ExifInterface.TAG_GPS_LATITUDE_REF)
                val longitude = parseGpsCoordinate(exif, ExifInterface.TAG_GPS_LONGITUDE, ExifInterface.TAG_GPS_LONGITUDE_REF)

                runOnUiThread {
                    if (latitude != null && longitude != null) {
                        Toast.makeText(this, "GPS 解析成功：$latitude, $longitude", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "照片本身无 GPS 信息", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "读取 GPS 失败", e)
            }
        }.start()
    }
    // 新增：GPS 坐标解析方法（核心逻辑，之前验证过正确）
    private fun parseGpsCoordinate(
        exif: ExifInterface,
        coordinateTag: String,  // TAG_GPS_LATITUDE 或 TAG_GPS_LONGITUDE
        refTag: String          // TAG_GPS_LATITUDE_REF 或 TAG_GPS_LONGITUDE_REF
    ): Double? {
        try {
            // 读取度分秒字符串（如 "21/1, 18/1, 24.998/1"）
            val coordinateStr = exif.getAttribute(coordinateTag) ?: return null
            if (coordinateStr.isBlank()) return null

            // 拆分度、分、秒
            val parts = coordinateStr.split(",").map { it.trim() }
            if (parts.size != 3) {
                Log.w(TAG, "GPS 格式异常：$coordinateStr（需度,分,秒三部分）")
                return null
            }

            // 解析有理数字符串（如 "21/1" → 21.0）
            val degrees = parseExifRationalStr(parts[0]) ?: return null
            val minutes = parseExifRationalStr(parts[1]) ?: return null
            val seconds = parseExifRationalStr(parts[2]) ?: return null

            // 转换为十进制
            var coordinate = degrees + (minutes / 60.0) + (seconds / 3600.0)

            // 根据方向调整正负（N/E 正，S/W 负）
            val ref = exif.getAttribute(refTag)
            if (ref.equals("S", ignoreCase = true) || ref.equals("W", ignoreCase = true)) {
                coordinate = -coordinate
            }

            return coordinate
        } catch (e: Exception) {
            Log.e(TAG, "解析 GPS 坐标失败: ${e.message}")
            return null
        }
    }

    // 新增：解析 EXIF 有理数字符串（辅助方法）
    private fun parseExifRationalStr(str: String): Double? {
        return try {
            if (str.contains("/")) {
                val (numerator, denominator) = str.split("/").map { it.toDouble() }
                if (denominator == 0.0) return null  // 避免除零
                numerator / denominator
            } else {
                str.toDouble()
            }
        } catch (e: Exception) {
            Log.e(TAG, "解析有理数字符串失败：$str", e)
            null
        }
    }
}