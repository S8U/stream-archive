package com.github.s8u.streamarchive.channelplatform.service

import com.github.s8u.streamarchive.channelplatform.repository.ChannelPlatformRepository
import org.springframework.stereotype.Service

/**
 * 채널에 속한 모든 채널 플랫폼을 삭제한다.
 */
@Service
class ChannelPlatformDeleteService(
    private val channelPlatformRepository: ChannelPlatformRepository
) {

    fun deleteAllByChannelId(channelId: Long) {
        val channelPlatforms = channelPlatformRepository.findByChannelId(channelId)

        channelPlatforms.forEach { channelPlatform ->
            channelPlatform.softDelete(userId = null, ip = null)
        }
    }

}
