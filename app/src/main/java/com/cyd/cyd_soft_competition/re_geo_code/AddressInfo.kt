package com.cyd.cyd_soft_competition.re_geo_code

/**
 * 全球地址详情类（适配国内外地址结构）
 */
data class AddressInfo(
    val formattedAddress: String?, // 完整结构化地址
    val country: String?, // 国家（如：美国、中国）
    val countryCode: String?, // 国家代码（如：US、CN）
    val province: String?, // 省/州（如：加利福尼亚州、北京市）
    val city: String?, // 城市（如：山景城、北京市）
    val district: String?, // 区/县（如：圣克拉拉县、朝阳区）
    val street: String?, // 街道（如：Amphitheatre Parkway、阜通东大街）
    val streetNumber: String?, // 门牌号（如：1600、6号）
    val postalCode: String? // 邮编（如：94043、100102）
)
