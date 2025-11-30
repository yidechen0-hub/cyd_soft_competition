package com.cyd.cyd_soft_competition.llm

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

object LLMClient {

    private val client = OkHttpClient()

    var openaiKey: String = ""
    var doubaoKey: String = ""
    var baseUrl: String = "https://api.openai.com/v1/chat/completions"

    fun callOpenAI(prompt: String): String {
        // Mock mode check
        if (openaiKey == "mock") {
            return mockOpenAI()
        }

        // 正确写法：List 转 JSONArray 后再 put
        val json = JSONObject()
            .put("model", "gpt-5-nano")
            .put("max_completion_tokens", 5000)
            .put("reasoning_effort", "minimal")
            .put("messages", JSONArray().apply {
                // 向 JSONArray 中添加 JSONObject 元素
                put(JSONObject().put("role", "user").put("content", prompt))
            })

        val requestbody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val req = Request.Builder()
            .url(baseUrl)
            .addHeader("Authorization", "Bearer $openaiKey")
            .post(requestbody)
            .build()

        val resp = client.newCall(req).execute().body!!.string()
        val root = JSONObject(resp)
        return root.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
    }

    private fun mockOpenAI(): String {
        Thread.sleep(1500) // Simulate network delay
        return """
            {
              "scene": "year_review",
              "summary": "Mock Data: User is a night owl who loves photography.",
              "user_portrait": {
                "life_theme": "Exploring the Unknown",
                "emotional_traits": ["Curious", "Introspective"],
                "behavior_patterns": ["Night Photography", "Urban Exploration"],
                "relationship_traits": ["Independent", "Observant"]
              },
              "story_points": [
                "Captured the silence of the city at 3 AM.",
                "Found beauty in the chaos of rush hour.",
                "Documented the changing seasons from the same window."
              ],
              "suggestions": [
                "Try capturing the sunrise for a change.",
                "Experiment with long exposure shots."
              ]
            }
        """.trimIndent()
    }

    // Azure Config
    private const val AZURE_ENDPOINT = "https://secalgo-azure-openai.openai.azure.com/"
    private const val AZURE_API_KEY = "6be983d6c96647808c36a64e131ecfb6"
    private const val AZURE_API_VERSION = "2025-03-01-preview"

    fun callAzure(prompt: String, model: String = "gpt-5-nano"): String {
        val deploymentId = model // Assuming deployment name matches model name
        val url = "${AZURE_ENDPOINT}openai/deployments/$deploymentId/chat/completions?api-version=$AZURE_API_VERSION"

        val json = JSONObject()
            .put("messages", JSONArray().apply {
                put(JSONObject().put("role", "user").put("content", prompt))
            })
            .put("max_completion_tokens", 5000)
            .put("reasoning_effort", "minimal")

        val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val req = Request.Builder()
            .url(url)
            .addHeader("api-key", AZURE_API_KEY) // Azure uses 'api-key', not 'Authorization'
            .post(requestBody)
            .build()

        val resp = client.newCall(req).execute().body!!.string()
        val root = JSONObject(resp)
        return root.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
    }

    fun callDoubao(prompt: String, endpoint: String): String {
        val json = JSONObject()
            .put("model", endpoint)
            .put("temperature", 0.4)
            .put("messages", JSONArray().apply {
                put(JSONObject().put("role", "user").put("content", prompt))
            })

        val requestbody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val req = Request.Builder()
            .url("https://ark.cn-beijing.volces.com/api/v3/chat/completions")
            .addHeader("Authorization", "Bearer $doubaoKey")
            .post(requestbody)
            .build()

        val respStr = client.newCall(req).execute().body!!.string()
        val root = JSONObject(respStr)

        val content = root.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .get("content")

        return when (content) {
            is String -> content
            is org.json.JSONArray ->
                (0 until content.length()).joinToString("") { content.getJSONObject(it).optString("text") }
            else -> content.toString()
        }
    }
}
