package com.cyd.cyd_soft_competition.remoteService

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import com.google.gson.Gson
import com.google.gson.JsonObject

object FdsUploader {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private const val TAG = "FDSUploader"
    // Replace with your actual backend server address
    private const val BACKEND_BASE_URL = "http://staging-album-summary.srv"
    
    // Authentication cookie
    private const val AUTH_COOKIE = "_aegis_cas=eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJuYmYiOjE3NjQ1MDk2ODIsImRlcGlkIjoiK1x1MDAxYkxpMVx1MDAwMXtjbV9cXFx1MDAxYSFcdTAwMTFWXHUwMDBiXHUwMDA1XHUwMDA0a1cuXHUwMDA1Rlx1MDAxMVdcdTAwMGJcdTAwMDJcZmMlMWJGXHUwMDEwVlx1MDAwZlx1MDAwMiIsImF1ZCI6InN0YWdpbmctdGFpeWkuZW5naW5lLm1pdWkuY29tIiwiYyI6MCwiZGV0YWlsIjoioPvLYUg6z33jXHUwMDAw1cS1XC8nV77g5qwoOdYpUsvimfBcdTAwMDNftOPjZWxzXHUwMDE2KWvZoTCkre8gXHUwMDA3XHUwMDAz1CBp8XbWluHNNWxcdTAwMWH4Z8NcdTAwMTLsW1h73DtTOPiuUz9FmP0tV1pcZmFxwJVaiLdcXLZiMlZqQY38Kuxd82EkadD6no_qVINDN-DHKOSQXHUwMDA2Xz5seN6dczgo0W9ptVqCx1Y_S35Enam8nm_EXHUwMDE4_Fx1MDAwMTuM9ihcdTAwMDZMXHUwMDBm5SxXWlx1MDAxM2LWksVeg_dcdTAwMWL5WPc7-n3pY8-9t1tI-bim4myVPlx1MDAxY1x1MDAwNlx1MDAxYij99NuC2DxV5GhWdZtQafqoT8N5U0bnKdZA8jj3wTBcdTAwMGLbW3FcdTAwMTPidO7U3Sl1IUk4OHa2opqOUFxyXHUwMDA2VHJcdTAwMGVjg59q8r3r6WJcdTAwMTa3s7pemliNS1VcdTAwMWJ3XpTUYeyfcY5cdTAwN2b1XHUwMDFhXHUwMDFiR5NcdTAwMTHd8rZEI1_RSujuWXLX4yr0flx1MDAwNawuy61cdTAwMDWESmAmprDkS9OPMyDwayt-XHUwMDA0XHUwMDFjU28kXeja-WdcdTAwMWFf9H36UGvDYGvT0OkzXGI6dcJcdTAwMTC9ipNW41x1MDAwNP1riFGvV4PAmvCJoGeaNeH-JDhcdTAwMDDflFmKsZjEm1ZDkP1cdTAwMTjNbkZcZqbJXHUwMDA3LCFu9Lksz1x1MDAxOF5D7lVtbFlDefQpqeNdTopzyohzvlxckzsmTJmtXHUwMDE470W6YMVcdTAwMDEhXHUwMDAwJpT2zuRVj_uoVTdHXHUwMDFlMTx9RyXsXHUwMDdminKBXHUwMDAy0Plcbt06XHUwMDEw3iQ2Rb51dd5nI3C3O6bvUCNtJTJ4YkXfK0RqaVx1MDAxM-o5Xlx1MDAwZSnnaSa83fVcXFenZl6I0dZLOlx1MDAxMWy602bwXHUwMDE5aHOE15wnXrBcdTAwMTQjM6ZcIrlOOUZmZXdJgFwiNdSa3mU6XHUwMDE2X1VcdTAwMTK8noSt3Vx1MDAwMZ9cdTAwMTaLOVmRXCJcdL_kRoyiIFtcdTAwMGVIbSjsOVx1MDAxMlx1MDAwNCdcdTAwMDPg1vQp0oIoXHUwMDFi4Vx1MDAxY2pcdTAwMTn1eoD2epVCk8uVdr0nXHUwMDFjXHUwMDE5OyHWXHUwMDAyXFyVsbOV8ipcIlxinHYkq29a0Vt96YW3kVSJxHZMLqiKiolX2pxcdTAwMDUlMFx1MDAxZMKzTFx1MDAxOZnANeTPNmnkj2PfLOI6bSIsInN1YiI6ImNoZW55aWRlIiwidCI6ImZhbHNlIiwidXQiOiJcdTAwMDM_XHUwMDA2TVx0QlZRIiwiZXhwIjoxNzY0NTk5NjgyLCJkIjoiNjg3NWJhMzE4YjhhMmYzYmZhMGVlODVmN2RhNzRkNTIiLCJpc3MiOiJNSS1JTkZPU0VDIiwibCI6IiVcdTAwMWE4XHUwMDExVlxuIiwidHlwIjoiY2FzIn0.k2SUuFx8dmJ8U8gEwRM6CiCD5niPyrc7GbjWtr0b-NhhH_r1DtX57Wbi-my3om0Hbehyfp8nHjXwpdjQtZCUVQ"
    
    interface UploadCallback {
        fun onSuccess(key: String)
        fun onError(e: Exception)
    }
    
    /**
     * Main entry point to upload a file
     */
    fun uploadFile(file: File, callback: UploadCallback) {
        // Step 1: Get Presigned URL from your backend
        getPresignedUrl(file.name) { presignedUrl, key, error ->
            if (error != null || presignedUrl == null) {
                callback.onError(error ?: Exception("Failed to get presigned URL"))
                return@getPresignedUrl
            }
            // Step 2: Upload file to FDS using the presigned URL
            uploadToFds(presignedUrl, file, key!!, callback)
        }
    }
    
    fun getDownUrl(key: String, callback: (String?, Exception?) -> Unit) {
        // Construct the URL: /fdsServer/getDownUrl?key=xxx
        val url = "$BACKEND_BASE_URL/server/getDownloadUrl?filename=$key"
        val url2 = "https://staging-taiyi.engine.miui.com/admin/deploy/resources/version/download?filename=$key"
        val request = Request.Builder()
            .url(url)
            .addHeader("Cookie", AUTH_COOKIE)
            .get()
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "getDownUrl network failure: ${e.message}", e)
                callback(null, e)
            }
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        Log.e(TAG, "getDownUrl failed: ${it.code}")
                        callback(null, IOException("Backend error: ${it.code}"))
                        return
                    }
                    try {
                        val jsonString = it.body?.string()
                        Log.i(TAG, "getDownUrl response: $jsonString")
                        val jsonObject = gson.fromJson(jsonString, JsonObject::class.java)
                        // Fix: use .get() instead of .getAsJsonObject() for primitive values
                        val downUrl = jsonObject.get("data").asString
                        Log.i(TAG, "Extracted download URL: $downUrl")
                        callback(downUrl, null)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse getDownUrl response: ${e.message}", e)
                        callback(null, e)
                    }
                }
            }
        })
    }
    
    private fun getPresignedUrl(filename: String, onResult: (String?, String?, Exception?) -> Unit) {
        // Construct the URL: /fdsServer/generatePresignedUri?filename=xxx
        val url = "$BACKEND_BASE_URL/server/fdsUrl?filename=$filename"
        val url2 = "https://staging-taiyi.engine.miui.com/admin/deploy/fdsServer/generatePresignedUri?filename=$filename"
        val request = Request.Builder()
            .url(url)
            .addHeader("Cookie", AUTH_COOKIE)
            .get()
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "getPresignedUrl network failure: ${e.message}", e)
                onResult(null, null, e)
            }
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        Log.e(TAG, "getPresignedUrl failed: ${it.code}")
                        onResult(null, null, IOException("Backend error: ${it.code}"))
                        return
                    }
                    try {
                        val jsonString = it.body?.string()
                        Log.i(TAG, "getPresignedUrl response: $jsonString")
                        
                        if (jsonString.isNullOrEmpty()) {
                            Log.e(TAG, "getPresignedUrl response is empty")
                            onResult(null, null, IOException("Empty response"))
                            return
                        }
                        
                        val jsonObject = gson.fromJson(jsonString, JsonObject::class.java)
                        Log.i(TAG, "Parsed JSON: $jsonObject")

                        // Parse the response structure
                        // Server returns: { "code": 0, "data": { "preSignedUrl": "...", "key": "..." } }
                        val data = jsonObject.getAsJsonObject("data")
                        val presignedUrl = data.get("preSignedUrl").asString
                        val key = data.get("key").asString
                        
                        Log.i(TAG, "Extracted presignedUrl: $presignedUrl")
                        Log.i(TAG, "Extracted key: $key")

                        onResult(presignedUrl, key, null)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse getPresignedUrl response: ${e.message}", e)
                        onResult(null, null, e)
                    }
                }
            }
        })
    }
    
    private fun uploadToFds(presignedUrl: String, file: File, key: String, callback: UploadCallback) {
        // FDS expects multipart/form-data
        // The "file" part name is standard, but FDS presigned URLs usually ignore the part name
        // as long as the content is multipart. However, "file" is a safe default.
        val fileBody = file.asRequestBody("application/octet-stream".toMediaType())

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, fileBody)
            .addFormDataPart("key", key)
            // Note: If your FDS config required other form fields (like GalaxyAccessKeyId),
            // they are already embedded in the presigned URL query parameters,
            // so we usually just need to send the file in the body.
            .build()
        
        val request = Request.Builder()
            .url(presignedUrl)
            .post(requestBody)
            .build()
        
        Log.i(TAG, "========== FDS Upload Start ==========")
        Log.i(TAG, "File: ${file.name}")
        Log.i(TAG, "File size: ${file.length()} bytes")
        Log.i(TAG, "Key: $key")
        Log.i(TAG, "Upload URL: $presignedUrl")
        
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "========== FDS Upload FAILED (Network) ==========")
                Log.e(TAG, "Error: ${e.message}", e)
                callback.onError(e)
            }
            
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    Log.i(TAG, "========== FDS Upload Response ==========")
                    Log.i(TAG, "Response code: ${it.code}")
                    Log.i(TAG, "Response message: ${it.message}")
                    
                    if (it.isSuccessful) {
                        val jsonString = it.body?.string()
                        Log.i(TAG, "Response body: $jsonString")
                        Log.i(TAG, "Response body length: ${jsonString?.length ?: 0}")
                        Log.i(TAG, "Response body is null: ${jsonString == null}")
                        Log.i(TAG, "Response body is empty: ${jsonString?.isEmpty()}")
                        
                        try {
                            if (jsonString.isNullOrEmpty()) {
                                Log.w(TAG, "Response is empty, using original key: $key")
                                callback.onSuccess(key)
                                return
                            }
                            
                            Log.i(TAG, "Attempting to parse JSON...")
                            val jsonObject = gson.fromJson(jsonString, JsonObject::class.java)
                            Log.i(TAG, "Parsed JSON object: $jsonObject")
                            Log.i(TAG, "JSON object type: ${jsonObject.javaClass.name}")
                            Log.i(TAG, "Has 'objectName' field: ${jsonObject.has("objectName")}")
                            
                            if (jsonObject.has("objectName")) {
                                val objectName = jsonObject.get("objectName").asString
                                Log.i(TAG, "Extracted objectName: $objectName")
                                Log.i(TAG, "========== FDS Upload SUCCESS ==========")
                                callback.onSuccess(objectName)
                            } else {
                                Log.w(TAG, "No 'objectName' field in response, using original key: $key")
                                Log.w(TAG, "Available fields: ${jsonObject.keySet()}")
                                Log.i(TAG, "========== FDS Upload SUCCESS (fallback) ==========")
                                callback.onSuccess(key)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "========== JSON Parsing FAILED ==========")
                            Log.e(TAG, "Exception type: ${e.javaClass.name}")
                            Log.e(TAG, "Exception message: ${e.message}", e)
                            Log.w(TAG, "Falling back to original key: $key")
                            Log.i(TAG, "========== FDS Upload SUCCESS (fallback after error) ==========")
                            callback.onSuccess(key)
                        }
                    } else {
                        val errorBody = it.body?.string()
                        Log.e(TAG, "========== FDS Upload FAILED (HTTP) ==========")
                        Log.e(TAG, "Error code: ${it.code}")
                        Log.e(TAG, "Error message: ${it.message}")
                        Log.e(TAG, "Error body: $errorBody")
                        callback.onError(IOException("FDS Upload failed: ${it.code} - ${it.message}"))
                    }
                }
            }
        })
    }
}