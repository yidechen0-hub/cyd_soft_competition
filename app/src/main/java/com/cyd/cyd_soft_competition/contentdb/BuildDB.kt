package com.cyd.cyd_android.contentdb

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.cyd.cyd_android.contentdb.util.iterImages

class BuildDB {
    @RequiresApi(Build.VERSION_CODES.O)
    fun build(context: Context, cfg: DBBuildConfig) {
        val extractor = MetaExtractor(cfg.fastHash,context)
        val db = PhotoDb(context, cfg.dbName)

        try {
            for (file in iterImages(cfg.root)) {
                val baseRec = extractor.extractOne(file) ?: continue

                // 插件扩展
                var rec = baseRec
                for (enricher in cfg.enrichers) {
                    try {
                        val patch = enricher.enrich(rec)
                        if (patch.isNotEmpty()) {
                            rec = rec.copy(
                                caption = (patch["caption"] as? String) ?: rec.caption,
                                aestheticScore = (patch["aestheticScore"] as? Number)?.toDouble()
                                    ?: rec.aestheticScore,
                                clipQuery = (patch["clipQuery"] as? String) ?: rec.clipQuery,
                                clipVector = (patch["clipVector"] as? ByteArray) ?: rec.clipVector
                            )
                        }
                    } catch (e: Exception) {
                        // 这里可以打 log
                        e.printStackTrace()
                    }
                }

                db.upsert(rec)
            }
        } finally {
            db.close()
        }
    }
}