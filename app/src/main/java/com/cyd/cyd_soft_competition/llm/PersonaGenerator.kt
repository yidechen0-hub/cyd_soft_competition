package com.cyd.cyd_soft_competition.llm

import org.json.JSONObject

class PersonaGenerator(
    private val dbPath: String,
    private val endpoint: String?,
    private val useOpenAI: Boolean = true
) {

    fun generate(scene: String = "year_review"): JSONObject? {

        val rows = DBReader(dbPath).loadMetadata()
        val stats = StatsBuilder.compute(rows)
        val prompt = PromptBuilder.build(stats, scene)

        val output = if (useOpenAI) {
            LLMClient.callAzure(prompt)
        } else {
            LLMClient.callDoubao(prompt, endpoint!!)
        }

        val result = JsonFixer.fix(output) { LLMClient.callAzure(it) }
        if (result == null) {
            println("❌ 无法解析 JSON：\n$output")
            return null
        }

        println("=== 用户画像（JSON） ===")
        println(result.toString(2))

        return result
    }
}
