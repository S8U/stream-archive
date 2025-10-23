package com.github.s8u.streamarchive.platform

import com.github.s8u.streamarchive.enums.PlatformType
import org.springframework.stereotype.Component

@Component
class PlatformStrategyFactory(
    private val strategies: List<PlatformStrategy>
) {

    fun getPlatformStrategy(platformType: PlatformType): PlatformStrategy {
        return strategies.find { it.platformType == platformType }
            ?: throw IllegalArgumentException("Platform strategy not found for type: $platformType")
    }

}