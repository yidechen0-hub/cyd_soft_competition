package com.cyd.cyd_soft_competition.buildDB


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
 * BigDataCloud Global Reverse Geocoding Utility
 */
object ReverseGeoCoder {
    private val TAG = "ReverseGeoCoder"
    private const val BIGDATA_API_KEY = "bdc_8f581bb5f07646bc8cbdb42208203b2f"
    private const val BIGDATA_RE_GEO_URL = "https://api.bigdatacloud.net/data/reverse-geocode-client"

    fun getGlobalAddress(latitude: Double, longitude: Double): ReGeoResult {
        if (longitude < -180 || longitude > 180 || latitude < -90 || latitude > 90) {
            Log.i(TAG,"Invalid coordinates: lat=$latitude, lng=$longitude")
            return ReGeoResult(false, "Invalid coordinates", null)
        }

        val params = StringBuilder()
        try {
            params.append("latitude=").append(URLEncoder.encode(latitude.toString(), "UTF-8"))
            params.append("&longitude=").append(URLEncoder.encode(longitude.toString(), "UTF-8"))
            params.append("&key=").append(BIGDATA_API_KEY)
            params.append("&localityLanguage=").append(URLEncoder.encode("zh-CN", "UTF-8"))
            params.append("&returnFullResponse=").append(true)
        } catch (e: Exception) {
            Log.e(TAG,"Error building params: ${e.message}")
            return ReGeoResult(false, "Encoding error: ${e.message}", null)
        }

        val fullUrl = "$BIGDATA_RE_GEO_URL?$params"
        // Log.i(TAG,"Request URL: $fullUrl") 

        var connection: HttpURLConnection? = null
        var reader: BufferedReader? = null
        try {
            val url = URL(fullUrl)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 8000
            connection.readTimeout = 8000

            val responseCode = connection.responseCode
            val inputStream = if (responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
            val response = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            val responseJson = response.toString()

            return if (responseCode == HttpURLConnection.HTTP_OK) {
                parseReGeoJson(responseJson)
            } else {
                ReGeoResult(false, "Request failed: $responseCode, $responseJson", null)
            }
        } catch (e: IOException) {
            Log.e(TAG,"Network error: ${e.message}")
            return ReGeoResult(false, "Network error: ${e.message}", null)
        } finally {
            reader?.close()
            connection?.disconnect()
        }
    }

    private fun parseReGeoJson(json: String): ReGeoResult {
        try {
            val jsonObject = JSONObject(json)

            if (jsonObject.has("error")) {
                val errorMsg = jsonObject.getString("error")
                Log.i(TAG,"API Error: $errorMsg")
                return ReGeoResult(false, "API Error: $errorMsg", null)
            }

            val formattedAddress = jsonObject.optString("formattedAddress", "")
            val country = jsonObject.optString("countryName", "")
            val countryCode = jsonObject.optString("countryCode", "")
            val province = jsonObject.optString("principalSubdivision", "")
            val city = jsonObject.optString("city", "")
            val district = jsonObject.optString("locality", "")
            val street = jsonObject.optString("street", "")
            val streetNumber = jsonObject.optString("streetNumber", "")
            val postalCode = jsonObject.optString("postalCode", "")

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

            return ReGeoResult(true, "Success", addressInfo)
        } catch (e: JSONException) {
            Log.i(TAG,"JSON Parse Error: ${e.message}")
            return ReGeoResult(false, "JSON Parse Error: ${e.message}", null)
        }
    }
}

data class ReGeoResult(
    val isSuccess: Boolean,
    val msg: String,
    val addressInfo: AddressInfo?
)

data class AddressInfo(
    val formattedAddress: String?,
    val country: String?,
    val countryCode: String?,
    val province: String?,
    val city: String?,
    val district: String?,
    val street: String?,
    val streetNumber: String?,
    val postalCode: String?
)
