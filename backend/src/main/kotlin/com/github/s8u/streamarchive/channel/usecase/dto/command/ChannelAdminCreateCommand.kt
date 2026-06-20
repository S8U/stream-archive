package com.github.s8u.streamarchive.channel.usecase.dto.command

import com.github.s8u.streamarchive.channel.enums.ChannelContentPrivacy

data class ChannelAdminCreateCommand(
    val name: String,
    val contentPrivacy: ChannelContentPrivacy
)
