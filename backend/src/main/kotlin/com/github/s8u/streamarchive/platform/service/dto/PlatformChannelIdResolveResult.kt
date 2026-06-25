package com.github.s8u.streamarchive.platform.service.dto

import com.github.s8u.streamarchive.platform.enums.PlatformType

data class PlatformChannelIdResolveResult(
    val platformType: PlatformType,
    val platformChannelId: String
)
