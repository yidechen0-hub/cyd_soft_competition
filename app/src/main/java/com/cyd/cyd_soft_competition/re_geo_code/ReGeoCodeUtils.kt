package com.cyd.cyd_soft_competition.re_geo_code

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * 高德经纬度反查地址工具类（逆地理编码）
 * 核心：调用高德 Web 服务 API，传入高德坐标系（GCJ02）经纬度，返回详细地址
 */
object ReGeoCodeUtils {
    // 替换为你的高德 Web 服务 API Key（必须是 Web 服务类型）
    private const val AMAP_API_KEY = "625e1e4e91fb0d5952fabb0415d0ed35"
    // 高德逆地理编码 API 地址
    private const val RE_GEO_CODE_URL = "https://restapi.amap.com/v3/geocode/regeo"

    /**
     * 经纬度反查地址（同步方法，需在子线程调用）
     * @param longitude 经度（高德 GCJ02 坐标系）
     * @param latitude 纬度（高德 GCJ02 坐标系）
     * @return ReGeoResult 地址结果封装类（成功/失败信息）
     */
    fun getAddressByLatLng(longitude: Double, latitude: Double): ReGeoResult {
        // 1. 校验经纬度合法性
        if (longitude < -180 || longitude > 180 || latitude < -90 || latitude > 90) {
            return ReGeoResult(false, "经纬度格式非法", null)
        }

        // 2. 拼接请求参数
        val params = StringBuilder()
        try {
            params.append("location=").append(URLEncoder.encode("$longitude,$latitude", "UTF-8"))
            params.append("&key=").append(URLEncoder.encode(AMAP_API_KEY, "UTF-8"))
            params.append("&extensions=base") // base：基础地址（精简）；all：包含 POI 等详细信息
            params.append("&output=json") // 返回格式：json（默认）
            params.append("&radius=1000") // 搜索半径（米），默认 1000
        } catch (e: Exception) {
            return ReGeoResult(false, "参数编码失败：${e.message}", null)
        }

        // 3. 发起网络请求
        val fullUrl = "$RE_GEO_CODE_URL?$params"
        var connection: HttpURLConnection? = null
        var reader: BufferedReader? = null
        try {
            val url = URL(fullUrl)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000 // 连接超时 5 秒
            connection.readTimeout = 5000 // 读取超时 5 秒

            // 4. 读取响应数据
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                // 5. 解析 JSON 响应
                return parseReGeoJson(response.toString())
            } else {
                return ReGeoResult(false, "请求失败，响应码：$responseCode", null)
            }
        } catch (e: IOException) {
            return ReGeoResult(false, "网络异常：${e.message}", null)
        } finally {
            reader?.close()
            connection?.disconnect()
        }
    }

    /**
     * 解析高德逆地理编码 JSON 响应
     */
    private fun parseReGeoJson(json: String): ReGeoResult {
        try {
            val jsonObject = JSONObject(json)
            val status = jsonObject.getString("status") // 1：成功，0：失败
            if (status != "1") {
                val info = jsonObject.getString("info") // 错误信息
                return ReGeoResult(false, "API 调用失败：$info", null)
            }

            // 解析核心地址信息
            val reGeoCodeObj = jsonObject.getJSONObject("regeocode")
            // 用 optString() 解析字段：不存在时返回空字符串，而非抛出异常
            val formattedAddress = reGeoCodeObj.optString("formatted_address", "")
            val addressComponent = reGeoCodeObj.optJSONObject("addressComponent") // 地址组件可能不存在

            // 解析详细地址组件（容错：addressComponent 为 null 时所有子字段返回空）
            val province = addressComponent?.optString("province", "") ?: ""
            val city = addressComponent?.optString("city", "") ?: ""
            val district = addressComponent?.optString("district", "") ?: ""
            val township = addressComponent?.optString("township", "") ?: ""
            val street = addressComponent?.optString("street", "") ?: ""
            val streetNumber = addressComponent?.optString("streetNumber", "") ?: ""

            // 封装结果（空字符串不影响显示，避免 NullPointException）
            val addressInfo = AddressInfo(
                formattedAddress = formattedAddress.takeIf { it.isNotEmpty() },
                province = province.takeIf { it.isNotEmpty() },
                city = city.takeIf { it.isNotEmpty() },
                district = district.takeIf { it.isNotEmpty() },
                township = township.takeIf { it.isNotEmpty() },
                street = street.takeIf { it.isNotEmpty() },
                streetNumber = streetNumber.takeIf { it.isNotEmpty() }
            )
            return ReGeoResult(true, "查询成功", addressInfo)
        } catch (e: JSONException) {
            return ReGeoResult(false, "JSON 解析失败：${e.message}", null)
        }
    }

    /**
     * 检查网络是否可用（辅助方法）
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false
            return networkInfo.isConnected
        }
    }

    /**
     * 地址结果封装类
     * @param isSuccess 是否查询成功
     * @param message 提示信息（成功/失败原因）
     * @param addressInfo 地址详情（成功时非空）
     */
    data class ReGeoResult(
        val isSuccess: Boolean,
        val message: String,
        val addressInfo: AddressInfo?
    )

    /**
     * 详细地址信息类
     */
    data class AddressInfo(
        val formattedAddress: String?, // 完整结构化地址（如：北京市朝阳区阜通东大街6号）
        val province: String?, // 省份（如：北京市）
        val city: String?, // 城市（如：北京市）
        val district: String?, // 区/县（如：朝阳区）
        val township: String?, // 乡镇（如：望京街道）
        val street: String?, // 街道（如：阜通东大街）
        val streetNumber: String? // 门牌号（如：6号）
    )
}