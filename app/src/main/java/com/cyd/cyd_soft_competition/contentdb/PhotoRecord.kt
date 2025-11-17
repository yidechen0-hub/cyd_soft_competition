package com.cyd.cyd_soft_competition.contentdb

data class PhotoRecord(
    // Core identity
    val path: String,
    val md5: String?,

    // Dimensions & EXIF basics
    val width: Int?,
    val height: Int?,
    val orientation: Int?,
    val cameraMake: String?,
    val cameraModel: String?,

    // Time
    val takenAtUtc: Long?,        // epoch seconds UTC
    val takenAtSrc: String?,      // exif|filename|file_mtime|unknown
    val tzOffsetMin: Int?,        // minutes offset from UTC (if known)

    // Location
    val latitude: Double?,
    val longitude: Double?,
    val locationSrc: String?,     // exif|sidecar|unknown
    val locationAccuracyM: Double?,

    // File system
    val fileMtime: Long?,
    val fileCtime: Long?,
    val fileSize: Long?,
    val mimeType: String?,

    // Extensible user/model fields (nullable; plugins may fill these)
    val caption: String? = null,
    val aestheticScore: Double? = null,
    val clipQuery: String? = null,
    val clipVector: ByteArray? = null  // e.g., serialized floats
)