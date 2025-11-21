package com.github.s8u.streamarchive.event

import com.github.s8u.streamarchive.entity.ChannelPlatform
import com.github.s8u.streamarchive.platform.PlatformStreamDto
import java.time.LocalDateTime

data class StreamDetectedEvent(
    val channelPlatform: ChannelPlatform,
    val stream: PlatformStreamDto,
    val detectedAt: LocalDateTime = LocalDateTime.now()
)
