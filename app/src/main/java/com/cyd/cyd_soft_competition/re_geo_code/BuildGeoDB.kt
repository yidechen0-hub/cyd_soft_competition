package com.cyd.cyd_soft_competition.re_geo_code

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.widget.Toast
import com.cyd.cyd_soft_competition.contentdb.PhotoDbHelper


class BuildGeoDB {
    fun build(context: Context) {
        // 获取可读取的数据库实例（readOnly = true，性能更优）
        val helper = PhotoDbHelper(context, "photos.db")
        val db: SQLiteDatabase = helper.readableDatabase
        val geoDB = GeoDB(context, "geo.db")
        // 执行查询
        val photoLocations = PhotoDbQueryUtils.queryAllPhotoLocations(db)

        if (photoLocations.isEmpty()) {
            Toast.makeText(context, "未查询到照片数据", Toast.LENGTH_SHORT).show()
            return
        }
        // 遍历结果（根据业务需求处理：显示到列表、上传、反查地址等）
        for (location in photoLocations) {
            println(
                """
                    照片 ID：${location.id}
                    路径：${location.path}
                    经纬度：${location.latitude ?: "无"} , ${location.longitude ?: "无"}
                """.trimIndent()
            )

            // 如果你需要用经纬度反查地址（结合之前的 ReGeoCodeUtils）
            if (location.latitude != null && location.longitude != null) {
                // 调用逆地理编码 API（注意：需再次开启子线程，避免阻塞主线程）
                val res = BigDataCloudReGeoUtils.getGlobalAddress(location.latitude,location.longitude )
                var addressInfo = res.addressInfo

                val rec = GeoRecord(
                    location.id,
                    location.path,
                    addressInfo?.formattedAddress,
                    addressInfo?.country,
                    addressInfo?.countryCode,
                    addressInfo?.province,
                    addressInfo?.city,
                    addressInfo?.district,
                    addressInfo?.street,
                    addressInfo?.streetNumber,
                    addressInfo?.postalCode
                )
                geoDB.upsert(rec)
                Thread.sleep(100)
            }

        }
    }
}