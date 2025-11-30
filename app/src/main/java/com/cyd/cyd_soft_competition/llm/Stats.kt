
package com.cyd.cyd_soft_competition.llm
data class Stats(
    val totalRaw: Int,
    val total: Int,
    val night: Int,
    val morning: Int,
    val sunset: Int,
    val topLocations: List<Pair<String, Int>>,
    val topTags: List<Pair<String, Int>>,
    val captions: List<String>
)

object StatsBuilder {

    fun compute(rows: List<ImageMeta>): Stats {
        // 1) 美学≥40过滤
        val filtered = rows.filter { (it.aestheticScore ?: 0.0) >= 40.0 }

        val totalRaw = rows.size
        val total = filtered.size

        var night = 0
        var morning = 0
        var sunset = 0

        val provinceCount = mutableMapOf<String, Int>()
        val tagCount = mutableMapOf<String, Int>()
        val captions = mutableListOf<String>()

        for (r in filtered) {
            val hour = r.hour
            if (hour != null) {
                if (hour in 0..5) night++
                if (hour in 6..9) morning++
                if (hour in 16..19) sunset++
            }

            r.province?.let { provinceCount[it] = (provinceCount[it] ?: 0) + 1 }
            r.tag?.let { tagCount[it] = (tagCount[it] ?: 0) + 1 }

            if (CaptionFilter.isValidCaption(r.caption, r.tag)) {
                captions.add(r.caption!!)
            }
        }

        val topLocations = provinceCount.entries
            .sortedByDescending { it.value }
            .take(5)
            .map { it.key to it.value }

        val topTags = tagCount.entries
            .sortedByDescending { it.value }
            .take(10)
            .map { it.key to it.value }

        return Stats(
            totalRaw = totalRaw,
            total = total,
            night = night,
            morning = morning,
            sunset = sunset,
            topLocations = topLocations,
            topTags = topTags,
            captions = captions
        )
    }
}
