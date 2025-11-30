package com.cyd.cyd_soft_competition.llm

object PromptBuilder {

    private val scenePrompts = mapOf(
        "year_review" to "请基于用户一整年的拍摄行为，洞察其年度主题、情绪轨迹、生活节奏与成长变化。",
        "stress_relief" to "请分析照片特征、拍摄时间段、地点分布，推断用户压力来源与自我疗愈方式。",
        "travel_memory" to "请从地点、caption 与体验碎片中，提炼旅行中的情绪、记忆、意义与故事线索。",
        "night_reflection" to "请从深夜拍照任务中推断情绪状态、独处特征、内心声音与反思模式。"
    )

    fun build(stats: Stats, scene: String): String {
        val baseStats = """
【用户图片行为统计】

- 全部照片数：${stats.totalRaw}
- 高质量（≥40分）照片数：${stats.total}
- 深夜拍摄次数：${stats.night}
- 清晨拍摄次数：${stats.morning}
- 黄昏拍摄次数：${stats.sunset}

- 常去地点 TOP5：${stats.topLocations}
- 标签 TOP10：${stats.topTags}
- 有效 Caption（过滤垃圾图）：共 ${stats.captions.size} 条
""".trimIndent()

        val jsonSchema = """
请严格输出以下 JSON（不能有多余文字、说明、注释、markdown）：

{
  "scene": "year_review | stress_relief | travel_memory | night_reflection",
  "summary": "一句话总结洞察",
  "user_portrait": {
    "life_theme": "用户生活主旋律",
    "emotional_traits": ["特征1", "特征2"],
    "behavior_patterns": ["模式1", "模式2"],
    "relationship_traits": ["特征1", "特征2"]
  },
  "story_points": [
    "洞察1",
    "洞察2",
    "洞察3"
  ],
  "suggestions": [
    "建议1",
    "建议2"
  ]
}
""".trimIndent()

        return """
你是一个高级“人格洞察 & 情绪叙事”模型，擅长从碎片化行为中构建深度用户画像。

场景：$scene
${scenePrompts[scene]}

输入数据如下：
$baseStats

要求：
1. 不要重复数据。
2. 要基于行为推断情绪、习惯、价值观。
3. 风格克制、人性化，不夸张。
4. 输出必须是合法 JSON。

$jsonSchema
""".trimIndent()
    }
}
