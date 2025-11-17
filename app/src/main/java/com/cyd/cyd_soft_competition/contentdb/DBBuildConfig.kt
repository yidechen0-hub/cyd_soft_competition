package com.cyd.cyd_android.contentdb

import java.io.File

data class DBBuildConfig(
    val root: File,
    val dbName: String,
    val fastHash: Boolean = false,
    val enrichers: List<PhotoEnricher> = emptyList()
)
