package com.cyd.cyd_soft_competition.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cyd.cyd_soft_competition.buildDB.DatabaseManager
import com.cyd.cyd_soft_competition.buildDB.ReverseGeoCoder
import com.cyd.cyd_soft_competition.databinding.ActivityReGeoCodeBinding
import com.cyd.cyd_soft_competition.re_geo_code.BuildGeoDB
import com.cyd.cyd_soft_competition.re_geo_code.ReGeoCodeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReGeoCodeActivity : AppCompatActivity(), View.OnClickListener {

    // 控件引用
    private lateinit var etLongitude: EditText
    private lateinit var etLatitude: EditText
    private lateinit var btnQuery: Button
    private lateinit var btnBuildGeo: Button
    private lateinit var btnBatchUpdate: Button
    private lateinit var tvResult: TextView
    private val buildGeoDB: BuildGeoDB by lazy { BuildGeoDB() }
    private lateinit var databaseManager: DatabaseManager

    private lateinit var binding: ActivityReGeoCodeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReGeoCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseManager = DatabaseManager(this)

        // 初始化控件
        initView()
        // 设置监听
        setListener()
        // 预设示例经纬度（方便测试）
        setDefaultLatLng()
        btnBuildGeo.setOnClickListener {
            Thread {
                try {
                    buildGeoDB.build(applicationContext)
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
        
        btnBatchUpdate.setOnClickListener {
            startBatchUpdate()
        }
    }

    /**
     * 初始化控件
     */
    private fun initView() {
        etLongitude = binding.etLongitude
        etLatitude = binding.etLatitude
        btnQuery = binding.btnQuery
        tvResult = binding.tvResult
        btnBuildGeo = binding.btnBuildGeo
        btnBatchUpdate = binding.btnBatchUpdate
    }

    /**
     * 设置监听（按钮点击、输入框文本变化）
     */
    private fun setListener() {
        btnQuery.setOnClickListener(this)

        // 输入框文本变化时，清空之前的结果
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tvResult.text = "查询结果将显示在这里..."
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        etLongitude.addTextChangedListener(textWatcher)
        etLatitude.addTextChangedListener(textWatcher)
    }

    /**
     * 预设示例经纬度（高德GCJ02坐标系，北京市朝阳区望京SOHO）
     */
    private fun setDefaultLatLng() {
        etLongitude.setText("116.480881")
        etLatitude.setText("39.989410")
    }
    
    private fun startBatchUpdate() {
        if (!ReGeoCodeUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "网络不可用，请检查网络设置", Toast.LENGTH_SHORT).show()
            return
        }

        tvResult.text = "开始批量更新..."
        btnBatchUpdate.isEnabled = false
        btnQuery.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            val images = databaseManager.getImagesWithoutLocation()
            withContext(Dispatchers.Main) {
                tvResult.text = "找到 ${images.size} 张需要更新位置的照片..."
            }

            if (images.isEmpty()) {
                withContext(Dispatchers.Main) {
                    tvResult.append("\n没有需要更新的照片。")
                    btnBatchUpdate.isEnabled = true
                    btnQuery.isEnabled = true
                }
                return@launch
            }

            var successCount = 0
            var failCount = 0

            for ((index, image) in images.withIndex()) {
                withContext(Dispatchers.Main) {
                    tvResult.text = "正在处理第 ${index + 1}/${images.size} 张...\n路径: ${image.path}"
                }

                if (image.latitude == null || image.longitude == null){
                    continue
                }

                // Use ReverseGeoCoder instead of ReGeoCodeUtils
                val result = ReverseGeoCoder.getGlobalAddress(image.latitude, image.longitude)
                
                if (result.isSuccess && result.addressInfo != null) {
                    val info = result.addressInfo
                    val country = info.country ?: "中国" // Default or from result
                    val province = info.province ?: ""
                    
                    // If province is empty but city is not, use city as province (common for municipalities)
                    val finalProvince = if (province.isEmpty()) info.city ?: "" else province
                    
                    databaseManager.updateLocation(image.path, country, finalProvince)
                    successCount++
                    withContext(Dispatchers.Main) {
                        tvResult.append("\n成功: $finalProvince")
                    }
                } else {
                    failCount++
                    withContext(Dispatchers.Main) {
                        tvResult.append("\n失败: ${result.msg}")
                    }
                }
                
                // Rate limiting to avoid API ban
                delay(300) 
            }

            withContext(Dispatchers.Main) {
                tvResult.append("\n\n更新完成！\n成功: $successCount\n失败: $failCount")
                btnBatchUpdate.isEnabled = true
                btnQuery.isEnabled = true
                Toast.makeText(this@ReGeoCodeActivity, "批量更新完成", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * 按钮点击事件
     */
    override fun onClick(v: View?) {
        when (v?.id) {
            btnQuery.id -> {
                // 1. 检查网络
                if (!ReGeoCodeUtils.isNetworkAvailable(this)) {
                    Toast.makeText(this, "网络不可用，请检查网络设置", Toast.LENGTH_SHORT).show()
                    return
                }

                // 2. 获取输入的经纬度
                val longitudeStr = etLongitude.text.toString().trim()
                val latitudeStr = etLatitude.text.toString().trim()

                // 3. 校验输入不为空
                if (TextUtils.isEmpty(longitudeStr) || TextUtils.isEmpty(latitudeStr)) {
                    Toast.makeText(this, "经度和纬度不能为空", Toast.LENGTH_SHORT).show()
                    return
                }

                // 4. 校验经纬度格式（转换为Double）
                val longitude = try {
                    longitudeStr.toDouble()
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "经度格式错误（请输入数字）", Toast.LENGTH_SHORT).show()
                    return
                }

                val latitude = try {
                    latitudeStr.toDouble()
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "纬度格式错误（请输入数字）", Toast.LENGTH_SHORT).show()
                    return
                }

                // 5. 校验经纬度范围（经度：-180~180，纬度：-90~90）
                if (longitude < -180 || longitude > 180) {
                    Toast.makeText(this, "经度范围错误（应在-180~180之间）", Toast.LENGTH_SHORT).show()
                    return
                }
                if (latitude < -90 || latitude > 90) {
                    Toast.makeText(this, "纬度范围错误（应在-90~90之间）", Toast.LENGTH_SHORT).show()
                    return
                }

                // 6. 显示加载状态
                tvResult.text = "查询中..."
                btnQuery.isEnabled = false // 防止重复点击

                // 7. 子线程调用反查接口（Coroutine实现）
                CoroutineScope(Dispatchers.IO).launch {
                    val result = ReGeoCodeUtils.getAddressByLatLng(longitude, latitude)
                    // 8. 切换到主线程更新UI
                    withContext(Dispatchers.Main) {
                        btnQuery.isEnabled = true // 恢复按钮可用
                        if (result.isSuccess) {
                            val address = result.addressInfo
                            address?.let {
                                // 格式化显示结果
                                val resultText = """
                                    查询成功！
                                    完整结构化地址：${it.formattedAddress}
                                    省份：${it.province}
                                    城市：${it.city}
                                    区/县：${it.district}
                                    乡镇：${it.township}
                                    街道：${it.street}
                                    门牌号：${it.streetNumber}
                                """.trimIndent()
                                tvResult.text = resultText
                            }
                        } else {
                            tvResult.text = "查询失败：${result.message}"
                            Toast.makeText(this@ReGeoCodeActivity, "查询失败：${result.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}