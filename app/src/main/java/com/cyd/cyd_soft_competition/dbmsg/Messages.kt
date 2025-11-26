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

}