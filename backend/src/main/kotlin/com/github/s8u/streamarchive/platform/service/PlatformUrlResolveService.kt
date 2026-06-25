package com.github.s8u.streamarchive.platform.service

import com.github.s8u.streamarchive.platform.service.dto.PlatformChannelIdResolveResult
import com.github.s8u.streamarchive.platform.strategy.PlatformStrategy
import org.springframework.stereotype.Service

/**
 * 플랫폼 채널 URL에서 플랫폼 종류와 채널 ID를 가려낸다.
 */
@Service
class PlatformUrlResolveService(
    private val strategies: List<PlatformStrategy>
) {

    /**
     * URL을 각 플랫폼 전략에 물어 알맞은 플랫폼과 채널 ID를 찾는다.
     *
     * 어느 플랫폼의 URL도 아니면 null을 반환한다.
     */
    fun resolve(url: String): PlatformChannelIdResolveResult? {
        val trimmed = url.trim()

        strategies.forEach { strategy ->
            val channelId = strategy.parseChannelId(trimmed)
            if (channelId != null) {
                return PlatformChannelIdResolveResult(
                    platformType = strategy.platformType,
                    platformChannelId = channelId
                )
            }
        }

        return null
    }

}
