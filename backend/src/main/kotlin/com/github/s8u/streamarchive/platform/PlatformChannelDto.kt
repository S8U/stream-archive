package com.github.s8u.streamarchive.platform

import com.github.s8u.streamarchive.enums.PlatformType

data class PlatformChannelDto(
    val platformDto: Any,
    val platformType: PlatformType,
    val id: String,
    val username: String,
    val name: String,
    val thumbnailUrl: String?
)