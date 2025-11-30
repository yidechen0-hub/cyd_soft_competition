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

class GetTasksResActivity : AppCompatActivity() {
    private val TAG = "GetTasksResActivity"
    // TODO: Extract to a common configuration
    private val BACKEND_BASE_URL = "http://YOUR_SERVER_IP:10033/admin"
    private val client = OkHttpClient()
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
            text = "获取任务结果并更新数据库"
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
            getTasksResult()
        }
    }

    private fun getTasksResult() {
        val url = "$BACKEND_BASE_URL/server/getResult"
        appendLog("Requesting: $url")

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    appendLog("Request Failed: ${e.message}")
                    Toast.makeText(this@GetTasksResActivity, "Request Failed", Toast.LENGTH_SHORT).show()
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

                        val jsonObject = JSONObject(jsonString)
                        val code = jsonObject.optInt("code")
                        if (code == 0) {
                            val dataArray = jsonObject.optJSONArray("data")
                            if (dataArray != null) {
                                runOnUiThread { appendLog("Found ${dataArray.length()} results. Processing...") }
                                var updatedCount = 0
                                for (i in 0 until dataArray.length()) {
                                    val item = dataArray.getJSONObject(i)
                                    val filePath = item.optString("filePath")
                                    val result = item.optJSONObject("result")
                                    
                                    if (filePath.isNotEmpty() && result != null) {
                                        val score = result.optDouble("score", 0.0)
                                        val caption = result.optString("caption", "")
                                        val tag = result.optString("tag", "")
                                        
                                        databaseManager.updateImageAnalysisResult(filePath, score, caption, tag)
                                        updatedCount++
                                    }
                                }
                                runOnUiThread { appendLog("Updated $updatedCount items.") }
                            } else {
                                runOnUiThread { appendLog("No data found.") }
                            }
                        } else {
                            val msg = jsonObject.optString("msg")
                            runOnUiThread { appendLog("Server Error: $msg") }
                        }

                    } catch (e: Exception) {
                        runOnUiThread { appendLog("Parsing Error: ${e.message}") }
                        Log.e(TAG, "Parsing Error", e)
                    }
                }
            }
        })
    }

    private fun appendLog(msg: String) {
        logTextView.append("\n$msg")
        Log.i(TAG, msg)
    }
}