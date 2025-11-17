package com.cyd.cyd_soft_competition

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cyd.cyd_soft_competition.databinding.ActivityMainBinding
import com.chaquo.python.Python
import android.util.Log
import android.widget.Toast



class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding // 视图绑定
    private val TAG = "ChaquopyKotlinDemo"
    private lateinit var pythonModule: com.chaquo.python.PyObject // Python 模块实例

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化 Python 模块（my_python.py 对应模块名 "my_python"）
        val python = Python.getInstance()
        pythonModule = python.getModule("photo_exif_reader")


        binding.btnAdd.setOnClickListener {
            try {
                pythonModule.callAttr("test_get_geo")
            } catch (e: Exception) {
                handleError(e)
            }
        }



    }

    // 错误处理
    private fun handleError(e: Exception) {
        val errorMsg = "调用失败：${e.message}"
        binding.tvResult.text = errorMsg
        Log.e(TAG, errorMsg, e)
        Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
    }
}