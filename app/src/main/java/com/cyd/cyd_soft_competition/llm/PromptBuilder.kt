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

  "summary": "可直接用于年度总结的文案（如：“在清晨公园与深夜书桌间，藏着你努力生活的模样”）",

  "user_portrait": {

    "life_theme": "可直接用于年度总结的文案，用户生活主旋律（结合常去地点、拍摄时段提炼，如“如享受美景与美食与宠物”）",

    "emotional_traits": ["特征1（具体，如“深夜感性”）", "特征2（如“清晨充满活力”）"],

    "behavior_patterns": ["模式1（如“周末常去湿地公园拍摄”）", "模式2（如“压力大时会拍街头美食”）"],

    "relationship_traits": ["特征1（从标签/地点推断，如“重视家庭聚会”）", "特征2（如“享受独处时光”）"]

  },

  "story_points": [

    "洞察1（带细节，如“黄昏拍摄的12次日落，多在通勤路上，是你一天中最放松的碎片时刻”）",

    "洞察2（如“常去的老书店出现在TOP1地点，搭配‘治愈’标签，是你的精神栖息地”）",

    "洞察3（如“深夜拍摄多为书桌灯光，说明你有挑灯奋进的习惯”）"

  ],

  "suggestions": [

    "建议1（贴合需求，如“下次旅行时，可多拍当地街头的烟火气，延续你的记忆收藏习惯”）",

    "建议2（如“清晨拍摄的公园晨景很有活力，不妨整理成电子相册，作为心情充电站”）"

  ]

}
""".trimIndent()

        return """
你是一位专业的「记忆导演 & 视觉策展人」，更是懂生活的「情绪捕手」。擅长从用户碎片化的拍摄行为中，打捞那些藏在光影里的真实心境，用克制而温暖的笔触，构建有温度的深度用户画像，串联起打动人心的生活轨迹。

场景：$scene
${scenePrompts[scene]}

输入数据如下：
$baseStats

创作准则:
- 基于数据但不止于数据，用拍摄时间、地点、标签的细节，推断用户没说出口的情绪、习惯与价值观，比如“常去的老公园+清晨拍摄”或许藏着TA的独处疗愈时刻。
- 风格温柔有生活味，拒绝空洞套话，多捕捉具体意象——不说“热爱生活”，而是从“黄昏拍的街角落叶”联想到“TA习惯记录季节的小变化”。

- 洞察真实克制，不夸张放大情绪，让用户看到时能会心一笑，或想起某段被遗忘的小片段。

- 不重复罗列统计数据，所有结论都要扎根于上述行为信息。

$jsonSchema
""".trimIndent()
    }
}
