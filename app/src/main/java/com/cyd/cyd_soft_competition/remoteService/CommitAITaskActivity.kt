package com.cyd.cyd_soft_competition.remoteService

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.cyd.cyd_soft_competition.buildDB.DatabaseManager
import com.cyd.cyd_soft_competition.dbmsg.Messages
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import org.json.JSONObject
import kotlinx.coroutines.*

class CommitAITaskActivity : AppCompatActivity() {
    private val TAG = "CommitAITaskActivity"
    // Use HTTP as HTTPS certificate is not trusted
    private val BACKEND_BASE_URL = "http://non-planar-qwen-10032.kscn-tj5-cloudml.xiaomi.srv/caption_url"
    private val client = OkHttpClient()
    private lateinit var databaseManager: DatabaseManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databaseManager = DatabaseManager(this)
        val button = Button(this).apply { text = "请求提交任务" }
        setContentView(button)
        button.setOnClickListener {
            val list = getUrlPath()
            if (list.isEmpty()) {
                Log.e(TAG, "No tasks to commit")
                return@setOnClickListener
            }
            
            Log.i(TAG, "Committing ${list.size} tasks with rate limit (5 requests/second)")
            
            // Use coroutine to add delay between requests
            CoroutineScope(Dispatchers.Main).launch {
                for ((index, pair) in list.withIndex()) {
                    val (path, url) = pair
                    commitTask(url, path)
                    
                    // Add 200ms delay between requests (5 requests per second)
                    if (index < list.size - 1) {
                        delay(200)
                    }
                    break
                }
            }
        }
    }

    private fun getUrlPath(): List<Pair<String, String>>{
        return Messages(this).getUrlPath()
    }

    /**
     * Commit a task to the backend server
     * @param downloadUrl The FDS download URL of the uploaded file
     * @param filePath The local file path
     * @param endpoint The API endpoint URL (defaults to BACKEND_BASE_URL)
     */
    private fun commitTask(
        downloadUrl: String, 
        filePath: String,
        endpoint: String = BACKEND_BASE_URL
    ) {
        Log.i(TAG, "Committing task to $endpoint")
        Log.i(TAG, "  File: $filePath")
        Log.i(TAG, "  Download URL: $downloadUrl")
        
        val json = JSONObject().apply {
            put("downloadUrl", downloadUrl)
//            put("filePath", filePath)
        }

        val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(endpoint)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Commit Failed for $filePath: ${e.message}", e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (it.isSuccessful) {
                        val responseBody = it.body?.string()
                        Log.i(TAG, "Commit Success for $filePath")
                        Log.i(TAG, "  Response: $responseBody")
                    } else {
                        Log.e(TAG, "Commit Error for $filePath: HTTP ${it.code}")
                    }
                }
            }
        })
    }
}