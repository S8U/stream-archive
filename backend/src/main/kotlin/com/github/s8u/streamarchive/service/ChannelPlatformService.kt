package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.repository.ChannelPlatformRepository
import org.springframework.stereotype.Service

@Service
class ChannelPlatformService(
    private val channelPlatformRepository: ChannelPlatformRepository
) {
}