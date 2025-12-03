package com.cyd.cyd_soft_competition.remoteService

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cyd.cyd_soft_competition.buildDB.DatabaseManager
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Activity to fetch face analysis results
 */
class GetFaceResActivity : AppCompatActivity() {
    private val TAG = "GetFaceResActivity"
    private val BACKEND_BASE_URL = "http://staging-album-summary.srv"
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS) // 连接超时
        .readTimeout(60, TimeUnit.SECONDS)    // 读取超时
        .writeTimeout(60, TimeUnit.SECONDS)   // 写入超时
        .build()
    private lateinit var databaseManager: DatabaseManager
    private lateinit var logTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databaseManager = DatabaseManager(this)

        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val button = Button(this).apply {
            text = "获取人脸分析结果"
        }

        logTextView = TextView(this).apply {
            text = "Logs will appear here..."
        }
        val scrollView = ScrollView(this).apply {
            addView(logTextView)
        }

        layout.addView(button)
        layout.addView(scrollView)
        setContentView(layout)

        button.setOnClickListener {
            getFaceTaskId()
        }
    }

    private fun getFaceTaskId(): String? {
        // 获取之前保存的 taskId
        val taskId = CommitFaceTaskActivity.getFaceTaskId(this)

        if (taskId != null) {
            // 检查是否过期（例如：超过1天）
            val timestamp = CommitFaceTaskActivity.getFaceTaskTimestamp(this)
            val elapsedTime = System.currentTimeMillis() - timestamp
            val oneDay = 24 * 60 * 60 * 1000

            if (elapsedTime < oneDay) {
                // taskId 仍然有效，使用它
                appendLog("Found saved task ID: $taskId")
                getFaceAnalysisResult(taskId)
            } else {
                // taskId 已过期
                CommitFaceTaskActivity.clearFaceTaskId(this)
                Log.w(TAG, "Task ID expired")
                appendLog("Task ID expired, please submit a new task")
            }
        } else {
            appendLog("No saved task ID found")
        }
        return taskId
    }

    private fun getFaceAnalysisResult(taskID: String) {
        val url = "$BACKEND_BASE_URL/face-cluster/result/$taskID"
        appendLog("Requesting: $url")

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    appendLog("Request Failed: ${e.message}")
                    Toast.makeText(this@GetFaceResActivity, "Request Failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        runOnUiThread {
                            appendLog("Error: ${it.code}")
                        }
                        return
                    }

                    try {
                        val jsonString = it.body?.string()
                        if (jsonString == null) {
                            runOnUiThread { appendLog("Empty response") }
                            return
                        }

                        Log.i(TAG, "Raw Response: $jsonString")
                        if (!jsonString.trim().startsWith("{")) {
                            runOnUiThread { appendLog("Invalid JSON response: $jsonString") }
                            return
                        }
                        val jsonObject = JSONObject(jsonString)
                        
                        // Clear old data before inserting new results
                        databaseManager.clearUrlCount()
                        runOnUiThread { appendLog("Cleared old face analysis data.") }
                        
                        // Parse top-level fields
                        val taskId = jsonObject.optString("taskId")
                        val status = jsonObject.optString("status")
                        
                        runOnUiThread {
                            appendLog("Task ID: $taskId")
                            appendLog("Status: $status")
                        }
                        
                        // Parse result object
                        val result = jsonObject.optJSONObject("result")
                        if (result == null) {
                            runOnUiThread { appendLog("No result found") }
                            return
                        }
                        
                        val resultStatus = result.optString("status")
                        if (resultStatus != "finished"){
                            runOnUiThread { appendLog("Result status is not 'finished'") }
                            return
                        }
                        val error = result.optString("error", "null")
                        
                        runOnUiThread {
                            appendLog("Result Status: $resultStatus")
                            if (error != "null") {
                                appendLog("Error: $error")
                            }
                        }
                        
                        // Parse data object
                        val data = result.optJSONObject("data")
                        if (data == null) {
                            runOnUiThread { appendLog("No data found") }
                            return
                        }
                        
                        val joyFaces = data.optInt("joyFaces", 0)
                        runOnUiThread {
                            appendLog("Joy Faces: $joyFaces")
                        }
                        
                        // Process joyFaceSet
                        val joyFaceSet = data.optJSONArray("joyFaceSet")
                        if (joyFaceSet != null && joyFaceSet.length() > 0) {
                            runOnUiThread { appendLog("Processing ${joyFaceSet.length()} joy faces...") }
                            
                            for (i in 0 until joyFaceSet.length()) {
                                val joyFace = joyFaceSet.getJSONObject(i)
                                val downloadUrl = joyFace.optString("downloadUrl")
                                val filePath = joyFace.optString("filePath")
                                
                                Log.i(TAG, "Joy Face $i: downloadUrl=$downloadUrl, filePath=$filePath")
                                
                                // Mark joy faces with special count (-1)
                                if (downloadUrl.isNotEmpty()) {
                                    updateFaceUrlCount(downloadUrl, -1)
                                }
                            }
                            
                            runOnUiThread { appendLog("Processed ${joyFaceSet.length()} joy faces.") }
                        }
                        
                        // Process top3_faces
                        val top3Faces = data.optJSONObject("top3_faces")
                        if (top3Faces != null) {
                            runOnUiThread { appendLog("Processing top 3 faces...") }
                            
                            val keys = top3Faces.keys()
                            var processedCount = 0
                            
                            while (keys.hasNext()) {
                                val key = keys.next()
                                val faceCluster = top3Faces.getJSONObject(key)
                                
                                val clusterIdx = faceCluster.optInt("cluster_idx")
                                val clusterSize = faceCluster.optInt("cluster_size")
                                val url = faceCluster.optString("url")
                                val filePath = faceCluster.optString("filePath")
                                val faceImage = faceCluster.optString("faceImage")
                                
                                Log.i(TAG, "Top Face $key: cluster_idx=$clusterIdx, size=$clusterSize")
                                Log.i(TAG, "  url=$url")
                                Log.i(TAG, "  filePath=$filePath")
                                Log.i(TAG, "  faceImage=$faceImage")
                                
                                // Update url_count with cluster size
                                if (url.isNotEmpty()) {
                                    updateFaceUrlCount(faceImage, clusterSize)
                                    processedCount++
                                }
                            }
                            
                            runOnUiThread { appendLog("Processed $processedCount top face clusters.") }
                        }
                        
                        runOnUiThread { appendLog("✓ Face analysis complete!") }

                    } catch (e: Exception) {
                        runOnUiThread { appendLog("Parsing Error: ${e.message}") }
                        Log.e(TAG, "Parsing Error", e)
                    }
                }
            }
        })
    }

    private fun updateFaceUrlCount(url: String, count: Int) {
        databaseManager.updateFaceUrlCount(url, count)
    }

    private fun appendLog(msg: String) {
        logTextView.append("\n$msg")
        Log.i(TAG, msg)
    }
}