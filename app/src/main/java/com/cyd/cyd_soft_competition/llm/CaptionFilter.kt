package com.cyd.cyd_soft_competition.llm

object CaptionFilter {

    private val badTags = setOf("文档", "截图", "截屏", "屏幕", "OCR", "说明书")
    private val badKeywords = listOf("screenshot", "screen", "pdf", "doc", "excel", "file", "button", "ui")

    fun isValidCaption(caption: String?, tag: String?): Boolean {
        if (caption.isNullOrBlank()) return false
        if (tag in badTags) return false

        val lower = caption.lowercase()
        if (badKeywords.any { lower.contains(it) }) return false

        if (caption.trim().length < 5) return false
        return true
    }
}
