package com.cyd.cyd_soft_competition.llm

import org.json.JSONObject

object JsonFixer {

    private val jsonBlockRegex = Regex("\\{[\\s\\S]*\\}")

    fun fix(raw: String, llmFixer: (String) -> String): JSONObject? {
        // 1 直接正则提取
        jsonBlockRegex.find(raw)?.let {
            try { return JSONObject(it.value) } catch (_: Exception) {}
        }

        // 2 清理 markdown
        val cleaned = raw
            .replace("```json", "")
            .replace("```", "")
            .trim()
        try { return JSONObject(cleaned) } catch (_: Exception) {}

        // 3 使用 LLM 修复
        val repaired = llmFixer("""
请修复下面内容为合法 JSON，只能输出 JSON：
$raw
""".trimIndent())

        return try { JSONObject(repaired) } catch (_: Exception) { null }
    }
}
