package com.cyd.cyd_soft_competition.mask

import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule

// 必须添加 @GlideModule 注解，Glide 会自动识别
@GlideModule
class AppGlideModule : AppGlideModule() {
    // 无需重写任何方法，仅用于触发 Glide 注解处理器
}