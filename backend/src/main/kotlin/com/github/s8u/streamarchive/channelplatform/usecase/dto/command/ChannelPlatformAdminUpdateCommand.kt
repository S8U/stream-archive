package com.github.s8u.streamarchive.channelplatform.usecase.dto.command

data class ChannelPlatformAdminUpdateCommand(
    val platformChannelId: String? = null,
    val isSyncProfile: Boolean? = null
)
