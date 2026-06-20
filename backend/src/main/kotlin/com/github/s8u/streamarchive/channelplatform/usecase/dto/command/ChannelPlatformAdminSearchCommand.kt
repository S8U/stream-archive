package com.github.s8u.streamarchive.channelplatform.usecase.dto.command

import com.github.s8u.streamarchive.platform.enums.PlatformType

data class ChannelPlatformAdminSearchCommand(
    val id: Long? = null,
    val channelName: String? = null,
    val platformType: PlatformType? = null,
    val platformChannelId: String? = null,
    val isSyncProfile: Boolean? = null
)
