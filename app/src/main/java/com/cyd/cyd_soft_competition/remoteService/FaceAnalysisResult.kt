package com.cyd.cyd_soft_competition.remoteService

import com.google.gson.annotations.SerializedName

/**
 * 人脸聚类分析总结果
 */
data class FaceAnalysisResult(
    // 人脸聚类列表
    @SerializedName("faceList") val faceList: List<FaceCluster>,
    // 带愉悦表情的人脸数量
    @SerializedName("joyFaces") val joyFaces: Int,
    // 带愉悦表情的人脸集合
    @SerializedName("joyFaceSet") val joyFaceSet: List<JoyFace>,
    // 检测到的人脸总数
    @SerializedName("totalFaces") val totalFaces: Int,
    // 聚类数量
    @SerializedName("clusterCount") val clusterCount: Int
)

/**
 * 人脸聚类信息
 */
data class FaceCluster(
    // 聚类ID
    @SerializedName("clusterId") val clusterId: Int,
    // 聚类包含的人脸数量
    @SerializedName("clusterSize") val clusterSize: Int,
    // 聚类代表图URL
    @SerializedName("representativeImage") val representativeImage: String,
    // 聚类下的所有图片
    @SerializedName("images") val images: List<ClusterImage>
)

/**
 * 聚类内的图片信息
 */
data class ClusterImage(
    // 图片原始URL
    @SerializedName("url") val url: String,
    // 图片处理后URL
    @SerializedName("image") val image: String
)

/**
 * 愉悦表情人脸信息
 */
data class JoyFace(
    // 图片下载URL
    @SerializedName("downloadUrl") val downloadUrl: String,
    // 图片本地文件路径
    @SerializedName("filePath") val filePath: String
)
