package com.cyd.cyd_soft_competition.dbmsg

class Messages {
    // 从数据库中获取今年第一张图片的时间信息
    fun getFirstImgMsg(): String {
        return "25年1月1日"
    }
    // 从数据库中获取今年第一张图片的路径信息
    fun getFirstImgPath(): String {
        return "/sdcard/taiyi/competition/start/first.png"
    }
    // 从数据库中获取今年图片和视频的数量
    fun getImgAndVideoNum(): String {
        return "3424张照片和30段视频"
    }
    // 从数据库中获取今年平均拍摄次数
    fun getAvgShootNum(): String {
        return "平均每天拍摄10次"
    }
    // 从数据库中获取今年拍摄地点信息
    fun getLocationMsg(): String {
        return "你在三个国家，25个城市"
    }
    fun getFaceURL(): String {
        return "https://cdn.pixabay.com/photo/2015/04/23/22/00/tree-736885__480.jpg"
    }
    fun getFacePath(): List<String>  {
        return listOf("/sdcard/taiyi/competition/start/face1.JPG",
                    "/sdcard/taiyi/competition/start/face1.JPG",
                    "/sdcard/taiyi/competition/start/face1.JPG")
    }
    // 从数据库中获取人物标签，标签数量最多的前两个
    fun getTags(): List<String> {
        return listOf("猫咪","美食")
    }
    // 从数据库中获取人物标签数量最多的前两个
    fun getTagNums(): List<String> {
        return listOf("1000张","820张")
    }
    // 从数据库中获取笑脸数量
    fun getSmileCounts(): String {
        return "你的笑容出现了1000次"
    }
    fun getSmileVedioPath(): String{
        return "/sdcard/taiyi/competition/start/copywriting1.mp4"
    }

    // 默认有春夏秋冬
    fun getSeasonPath(): List<String>{
        return listOf("/sdcard/taiyi/competition/start/face1.JPG",
                        "/sdcard/taiyi/competition/start/face1.JPG",
                        "/sdcard/taiyi/competition/start/face1.JPG",
                        "/sdcard/taiyi/competition/start/face1.JPG")
    }



}