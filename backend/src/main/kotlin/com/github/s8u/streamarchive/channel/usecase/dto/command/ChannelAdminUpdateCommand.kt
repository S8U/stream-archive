package com.github.s8u.streamarchive.channel.usecase.dto.command

import com.github.s8u.streamarchive.channel.enums.ChannelContentPrivacy

data class ChannelAdminUpdateCommand(
    val name: String? = null,
    val contentPrivacy: ChannelContentPrivacy? = null
)
