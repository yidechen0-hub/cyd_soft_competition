package com.cyd.cyd_soft_competition.contentdb

// Kotlin 里你可以用接口 + DI 实现“插件”
interface PhotoEnricher {
    /**
     * @param record 当前记录，可读
     * @return 要 merge 进 record 的字段，例如 mapOf("caption" to "...", "aestheticScore" to 0.81)
     */
    fun enrich(record: PhotoRecord): Map<String, Any?>
}