package com.github.s8u.streamarchive.video.usecase.dto.command

data class VideoSearchCommand(
    val title: String? = null,
    val description: String? = null,
    val channelName: String? = null,
    val channelUuid: String? = null
)
