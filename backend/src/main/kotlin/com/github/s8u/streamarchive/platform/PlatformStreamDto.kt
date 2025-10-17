package com.github.s8u.streamarchive.platform

import com.github.s8u.streamarchive.enums.PlatformType
import java.time.LocalDateTime

data class PlatformStreamDto(
    val platformType: PlatformType,
    val id: String,
    val username: String,
    val title: String?,
    val category: String?,
    val viewerCount: Int?,
    val thumbnailUrl: String?,
    val startedAt: LocalDateTime?,
    val platformDto: Any,
)