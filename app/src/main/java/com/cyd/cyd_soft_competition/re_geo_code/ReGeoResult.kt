package com.cyd.cyd_soft_competition.re_geo_code



/**
 * 地址结果封装类（扩展全球地址字段）
 */
data class ReGeoResult(
    val isSuccess: Boolean,
    val message: String,
    val addressInfo: AddressInfo?
)

