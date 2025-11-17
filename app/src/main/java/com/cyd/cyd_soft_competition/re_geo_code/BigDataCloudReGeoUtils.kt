package com.cyd.cyd_soft_competition.re_geo_code

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
 * BigDataCloud 全球逆地理编码工具类（支持国内外地址，无需科学上网）
 * 核心：调用 BigDataCloud Reverse Geocode Client API，解析 WGS84 经纬度
 */
object BigDataCloudReGeoUtils {
    // 替换为你的 BigDataCloud API Key（从官网控制台复制）
    private const val BIGDATA_API_KEY = "bdc_fc9ccde59c67415f873114a080980c66"
    // BigDataCloud 逆地理编码 API 地址（免费版）
    private const val BIGDATA_RE_GEO_URL = "https://api.bigdatacloud.net/data/reverse-geocode-client"

    /**
     * 经纬度反查全球地址（同步方法，需在子线程调用）
     * @param latitude 纬度（WGS84 坐标系，如 34.0522 或 -34.0522）
     * @param longitude 经度（WGS84 坐标系，如 -118.2437 或 116.480881）
     * @return ReGeoResult 地址结果封装类（成功/失败信息 + 地址详情）
     */
    fun getGlobalAddress(latitude: Double, longitude: Double): ReGeoResult {
        // 1. 校验经纬度合法性
        if (longitude < -180 || longitude > 180 || latitude < -90 || latitude > 90) {
            Log.e("BigDataReGeo", "经纬度格式非法：lat=$latitude, lng=$longitude")
            return ReGeoResult(false, "经纬度格式非法", null)
        }

        // 2. 拼接请求参数（BigDataCloud API 参数说明：https://www.bigdatacloud.com/docs/api/reverse-geocode-client）
        val params = StringBuilder()
        try {
            // 核心参数：纬度、经度（顺序：latitude 在前，longitude 在后）
            params.append("latitude=").append(URLEncoder.encode(latitude.toString(), "UTF-8"))
            params.append("&longitude=").append(URLEncoder.encode(longitude.toString(), "UTF-8"))
            params.append("&key=").append(BIGDATA_API_KEY)
            // 可选参数：语言（默认英文，中文为 zh-CN，支持多语言）
            params.append("&localityLanguage=").append(URLEncoder.encode("zh-CN", "UTF-8"))
            // 可选参数：是否返回详细地址组件（true=返回街道、门牌号等）
            params.append("&returnFullResponse=").append(true)
        } catch (e: Exception) {
            Log.e("BigDataReGeo", "参数拼接失败", e)
            return ReGeoResult(false, "参数编码失败：${e.message}", null)
        }

        // 3. 发起网络请求
        val fullUrl = "$BIGDATA_RE_GEO_URL?$params"
        Log.d("BigDataReGeo", "请求 URL：$fullUrl") // 调试用：查看最终请求地址
        var connection: HttpURLConnection? = null
        var reader: BufferedReader? = null
        try {
            val url = URL(fullUrl)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 8000 // 超时时间设为 8 秒（适配国际网络）
            connection.readTimeout = 8000

            // 4. 读取响应数据（处理成功/失败响应）
            val responseCode = connection.responseCode
            Log.d("BigDataReGeo", "响应码：$responseCode")
            val inputStream = if (responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream
            } else {
                connection.errorStream // 读取错误响应（如配额超限、Key 无效）
            }

            reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
            val response = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            val responseJson = response.toString()
            Log.d("BigDataReGeo", "响应 JSON：$responseJson") // 调试用：查看 API 返回详情

            // 5. 解析响应结果
            return if (responseCode == HttpURLConnection.HTTP_OK) {
                parseReGeoJson(responseJson)
            } else {
                ReGeoResult(false, "请求失败，响应码：$responseCode，错误信息：$responseJson", null)
            }
        } catch (e: IOException) {
            Log.e("BigDataReGeo", "网络异常", e)
            return ReGeoResult(false, "网络异常：${e.message}（请检查网络连接）", null)
        } finally {
            // 关闭资源，避免内存泄漏
            reader?.close()
            connection?.disconnect()
        }
    }

    /**
     * 解析 BigDataCloud API 返回的 JSON 响应
     */
    private fun parseReGeoJson(json: String): ReGeoResult {
        try {
            val jsonObject = JSONObject(json)

            // 检查 API 返回的错误信息（免费版配额超限、Key 无效等）
            if (jsonObject.has("error")) {
                val errorMsg = jsonObject.getString("error")
                Log.e("BigDataReGeo", "API 错误：$errorMsg")
                return ReGeoResult(false, "查询失败：$errorMsg", null)
            }

            // 解析核心地址字段（字段说明参考官方文档）
            val formattedAddress = jsonObject.optString("formattedAddress", "") // 完整地址（如：美国加利福尼亚州山景城 Amphitheatre Parkway 1600 号）
            val country = jsonObject.optString("countryName", "") // 国家（如：美国）
            val countryCode = jsonObject.optString("countryCode", "") // 国家代码（如：US）
            val province = jsonObject.optString("principalSubdivision", "") // 省/州（如：加利福尼亚州）
            val city = jsonObject.optString("city", "") // 城市（如：山景城）
            val district = jsonObject.optString("locality", "") // 区/县（如：圣克拉拉县）
            val street = jsonObject.optString("street", "") // 街道（如：Amphitheatre Parkway）
            val streetNumber = jsonObject.optString("streetNumber", "") // 门牌号（如：1600）
            val postalCode = jsonObject.optString("postalCode", "") // 邮编（如：94043）

            // 封装地址信息（空字符串转为 null，避免冗余）
            val addressInfo = AddressInfo(
                formattedAddress = formattedAddress.takeIf { it.isNotEmpty() },
                country = country.takeIf { it.isNotEmpty() },
                countryCode = countryCode.takeIf { it.isNotEmpty() },
                province = province.takeIf { it.isNotEmpty() },
                city = city.takeIf { it.isNotEmpty() },
                district = district.takeIf { it.isNotEmpty() },
                street = street.takeIf { it.isNotEmpty() },
                streetNumber = streetNumber.takeIf { it.isNotEmpty() },
                postalCode = postalCode.takeIf { it.isNotEmpty() }
            )

            return ReGeoResult(true, "查询成功", addressInfo)
        } catch (e: JSONException) {
            Log.e("BigDataReGeo", "JSON 解析失败", e)
            return ReGeoResult(false, "JSON 解析失败：${e.message}", null)
        }
    }





}