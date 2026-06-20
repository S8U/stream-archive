package com.github.s8u.streamarchive.platform.chat

import com.github.s8u.streamarchive.platform.enums.PlatformType
import org.springframework.stereotype.Component

@Component
class PlatformChatStrategyFactory(
    private val strategies: List<PlatformChatStrategy>
) {

    /**
     * 채팅 전략을 찾는다.
     *
     * 채팅을 지원하지 않는 플랫폼은 null을 반환한다.
     */
    fun findPlatformChatStrategy(platformType: PlatformType): PlatformChatStrategy? {
        return strategies.find { it.platformType == platformType }
    }

}
