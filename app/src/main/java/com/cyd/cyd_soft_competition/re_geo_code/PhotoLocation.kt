package com.cyd.cyd_soft_competition.re_geo_code

data class PhotoLocation(
    val id: String, // 照片唯一标识（对应表中 id 字段，通常为主键）
    val path: String, // 照片路径（对应 path 字段）
    val latitude: Double?, // 纬度（可能为 null，无定位时）
    val longitude: Double? // 经度（可能为 null，无定位时）
)