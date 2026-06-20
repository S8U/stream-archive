package com.github.s8u.streamarchive.channelplatform.usecase.dto.command

import com.github.s8u.streamarchive.platform.enums.PlatformType

data class ChannelPlatformAdminCreateCommand(
    val channelId: Long,
    val platformType: PlatformType,
    val platformChannelId: String,
    val isSyncProfile: Boolean = true
)
