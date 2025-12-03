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

class CommitFaceTaskActivity : AppCompatActivity() {
    private val TAG = "CommitFaceTaskActivity"
    // Use HTTP as HTTPS certificate is not trusted
    private val BACKEND_BASE_URL = "http://non-planar-qwen-10032.kscn-tj5-cloudml.xiaomi.srv/caption_url"
    private val BACKEND_BASE_URL2 = "http://staging-album-summary.srv/server/faceRecognitionTask"
    private val client = OkHttpClient()
    private lateinit var databaseManager: DatabaseManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databaseManager = DatabaseManager(this)
        val button = Button(this).apply { text = "请求提交任务" }
        setContentView(button)
        button.setOnClickListener {

            Log.i(TAG, "Committing face recognition tasks...")

            // Use coroutine to add delay between requests
            CoroutineScope(Dispatchers.Main).launch {
                commitTask(BACKEND_BASE_URL2)
            }
        }
    }


    /**
     * Commit a task to the backend server
     * @param endpoint The API endpoint URL (defaults to BACKEND_BASE_URL)
     */
    private fun commitTask(
        endpoint: String = BACKEND_BASE_URL
    ) {
        Log.i(TAG, "Committing task to $endpoint")
        

        val json = JSONObject().apply {

        }

        val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(endpoint)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Commit Failed", e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (it.isSuccessful) {
                        val responseBody = it.body?.string()
                        Log.i(TAG, "  Response: $responseBody")
                        val jsonObject = JSONObject(responseBody)
                        val taskId = jsonObject.getString("data")
                        
                        // Save taskId to SharedPreferences for later use
                        saveFaceTaskId(taskId)
                        
                        Log.i(TAG, "Task committed successfully. Task ID: $taskId")

                    } else {
                        Log.e(TAG, "Commit Error: HTTP ${it.code}")
                    }
                }
            }
        })
    }
    
    /**
     * Save face recognition task ID to SharedPreferences
     */
    private fun saveFaceTaskId(taskId: String) {
        val prefs = getSharedPreferences("FaceTaskPrefs", MODE_PRIVATE)
        prefs.edit().apply {
            putString("face_task_id", taskId)
            putLong("face_task_timestamp", System.currentTimeMillis())
            apply()
        }
        Log.d(TAG, "Saved face task ID: $taskId")
    }
    
    companion object {
        /**
         * Retrieve the saved face recognition task ID from SharedPreferences
         * Call this from another activity to get the taskId
         */
        fun getFaceTaskId(context: android.content.Context): String? {
            val prefs = context.getSharedPreferences("FaceTaskPrefs", MODE_PRIVATE)
            return prefs.getString("face_task_id", null)
        }
        
        /**
         * Get the timestamp when the task ID was saved
         */
        fun getFaceTaskTimestamp(context: android.content.Context): Long {
            val prefs = context.getSharedPreferences("FaceTaskPrefs", MODE_PRIVATE)
            return prefs.getLong("face_task_timestamp", 0)
        }
        
        /**
         * Clear the saved face task ID
         */
        fun clearFaceTaskId(context: android.content.Context) {
            val prefs = context.getSharedPreferences("FaceTaskPrefs", MODE_PRIVATE)
            prefs.edit().clear().apply()
        }
    }
}
