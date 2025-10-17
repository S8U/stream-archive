package com.github.s8u.streamarchive.client.chzzk

data class ChzzkResponseDto<T>(
    val code: Int,
    val message: String?,
    val content: T?
)