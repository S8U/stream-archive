package com.github.s8u.streamarchive.channel.usecase.dto.command

import com.github.s8u.streamarchive.channel.enums.ChannelContentPrivacy

data class ChannelAdminSearchCommand(
    val id: Long? = null,
    val uuid: String? = null,
    val name: String? = null,
    val contentPrivacy: ChannelContentPrivacy? = null
)
