package com.github.s8u.streamarchive.platform.platforms.chzzk.client

data class ChzzkResponseDto<T>(
    val code: Int,
    val message: String?,
    val content: T?
)
