package com.cyd.cyd_soft_competition.dbmsg

import android.content.Context
import android.util.Log
import com.cyd.cyd_soft_competition.buildDB.DatabaseManager

class Messages(context: Context) {
    private val dbManager = DatabaseManager(context)
    private val context = context

    // 从数据库中获取今年第一张图片的时间信息
    fun getFirstImgMsg(): String {
        val metadata = dbManager.getFirstImageMetadata()
        return if (metadata != null && metadata.year != null) {
            "${metadata.year}年${metadata.month}月${metadata.day}日"
        } else {
            "暂无数据"
        }
//         return "25年1月1日"
    }

    // 从数据库中获取今年第一张图片的路径信息
    fun getFirstImgPath(): String {
        val metadata = dbManager.getFirstImageMetadata()
        return metadata?.path ?: ""
//         return "/sdcard/taiyi/competition/start/first.png"
    }

    // 从数据库中获取今年图片和视频的数量
    fun getImgAndVideoNum(): String {
        val imgCount = dbManager.getImageCount()
        val videoCount = dbManager.getVideoCount()
        return "${imgCount}张照片"
//         return "3424张照片和30段视频"
    }

    // 从数据库中获取今年平均拍摄次数
    fun getAvgShootNum(): String {
        val totalImages = dbManager.getImageCount()
//        val distinctDays = dbManager.getDistinctDayCount()
//        val avg = if (distinctDays > 0) totalImages / distinctDays else 0
        val avg = totalImages / 365
        return "平均每天拍摄${avg}次"
//         return "平均每天拍摄10次"
    }

    // 从数据库中获取今年拍摄地点信息
    fun getLocationMsg(): String {
        val (countries, provinces) = dbManager.getLocationCounts()
        return "你在${countries}个国家，${provinces}个省份"
//         return "你在三个国家，25个城市"
    }

    fun getFaceURL(): List<String> {
        return dbManager.getFaceUrl()
        // return "https://cdn.pixabay.com/photo/2015/04/23/22/00/tree-736885__480.jpg"
    }

    fun getFacePath(): List<String>  {
        val paths = dbManager.getFacePaths()
        val res = mutableListOf<String>()
        for (path in paths){
            Log.i("getFacePath", "path: $path")
            val localPath = ImageDownloader.downloadImage(path, context)
            res.add(localPath ?: "")
        }
        return if (res.isNotEmpty()) res else emptyList()

//        return listOf("/sdcard/taiyi/competition/start/face1.JPG",
//                    "/sdcard/taiyi/competition/start/face1.JPG",
//                    "/sdcard/taiyi/competition/start/face1.JPG")

    }

    // 从数据库中获取人物标签，标签数量最多的前两个
    fun getTags(): List<String> {
        val topTags = dbManager.getTopTags(2)
        return topTags.map { it.first }
//         return listOf("猫咪","美食")
    }

    // 从数据库中获取人物标签数量最多的前两个
    fun getTagNums(): List<String> {
        val topTags = dbManager.getTopTags(2)
        return topTags.map { "${it.second}张" }
//         return listOf("1000张","820张")
    }

    // 从数据库中获取笑脸数量
    fun getSmileCounts(): String {
        val count = dbManager.getSmileCount()
        return "你们的笑容出现了${count}次"
//        return "你的笑容出现了1000次"
    }

    fun getSmileVedioPath(): String{
//        return dbManager.getSmileVideoPath() ?: ""
         return "/sdcard/taiyi/competition/start/copywriting1.mp4"
    }

    // 默认有春夏秋冬
    fun getSeasonPath(): List<String>{
        val paths = dbManager.getSeasonPaths()
        return if (paths.isNotEmpty()) paths else emptyList()

//        return listOf("/sdcard/taiyi/competition/start/face1.JPG",
//                        "/sdcard/taiyi/competition/start/face1.JPG",
//                        "/sdcard/taiyi/competition/start/face1.JPG",
//                        "/sdcard/taiyi/competition/start/face1.JPG")

    }

    // 从数据库中获取特殊日子的单张照片路径(凌晨0-4点,越晚越特别)
    fun getSpecialDaySinglePath(): String {
        return dbManager.getSpecialDaySinglePath() ?: ""
//        return "/sdcard/taiyi/competition/start/face1.JPG"
    }

    // 从数据库中获取特殊日子的日期(如:8月5日)
    fun getSpecialDayDate(): String {
        return dbManager.getSpecialDayDate() ?: ""
    }

    // 从数据库中获取特殊日子的网格数据(拍摄照片最多的一天的所有照片)
    fun getSpecialDayGridData(): List<String> {
//        val res = mutableListOf<String>()
//        for(i in 0..50){
//            res.add("/sdcard/taiyi/competition/start/face1.JPG")
//        }
        return dbManager.getSpecialDayGridData()
//        return res
    }

    // 从数据库中获取拍摄照片最多的一天的信息(日期和数量)
    fun getMostPhotoDayInfo(): Pair<String, Int> {
        return dbManager.getMostPhotoDayInfo()
    }

    // 从数据库中获取aesthetic_score最高的20张图片路径
    fun getTop20Paths(): List<String> {
        return dbManager.getTop20Paths()
//        val res = mutableListOf<String>()
//        for(i in 0..50){
//            res.add("/sdcard/taiyi/competition/start/face1.JPG")
//        }
//        return res
    }

    fun getUrlPath(): List<Pair<String, String>> {
        return dbManager.getAllImagePathsAndUrls()
    }
}