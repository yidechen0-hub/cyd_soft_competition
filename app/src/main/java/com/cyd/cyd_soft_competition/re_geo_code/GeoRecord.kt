package com.cyd.cyd_soft_competition.re_geo_code

data class GeoRecord(
    val id : String,
    val path: String,
    val formattedAddress: String?, // 完整结构化地址（如：北京市朝阳区阜通东大街6号）
    val province: String?, // 省份（如：北京市）
    val city: String?, // 城市（如：北京市）
    val district: String?, // 区/县（如：朝阳区）
    val township: String?, // 乡镇（如：望京街道）
    val street: String?, // 街道（如：阜通东大街）
    val streetNumber: String? // 门牌号（如：6号）
)

