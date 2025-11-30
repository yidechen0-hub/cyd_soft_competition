package com.cyd.cyd_soft_competition.llm
data class ImageMeta(
    val year: Int?,
    val month: Int?,
    val day: Int?,
    val hour: Int?,
    val country: String?,
    val province: String?,
    val aestheticScore: Double?,
    val caption: String?,
    val tag: String?
)
