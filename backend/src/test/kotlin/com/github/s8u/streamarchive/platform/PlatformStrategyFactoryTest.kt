package com.github.s8u.streamarchive.platform

import com.github.s8u.streamarchive.enums.PlatformType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class PlatformStrategyFactoryTest {

    @Nested
    @DisplayName("getPlatformStrategy")
    inner class GetPlatformStrategy {

        @Test
        @DisplayName("CHZZK 플랫폼에 해당하는 전략을 반환한다")
        fun getChzzkStrategy() {
            val chzzkStrategy = mock<PlatformStrategy>()
            whenever(chzzkStrategy.platformType).thenReturn(PlatformType.CHZZK)

            val factory = PlatformStrategyFactory(listOf(chzzkStrategy))

            val result = factory.getPlatformStrategy(PlatformType.CHZZK)

            assertEquals(PlatformType.CHZZK, result.platformType)
        }

        @Test
        @DisplayName("여러 전략 중 올바른 플랫폼 전략을 선택한다")
        fun selectCorrectStrategy() {
            val chzzkStrategy = mock<PlatformStrategy>()
            val twitchStrategy = mock<PlatformStrategy>()
            val soopStrategy = mock<PlatformStrategy>()
            whenever(chzzkStrategy.platformType).thenReturn(PlatformType.CHZZK)
            whenever(twitchStrategy.platformType).thenReturn(PlatformType.TWITCH)
            whenever(soopStrategy.platformType).thenReturn(PlatformType.SOOP)

            val factory = PlatformStrategyFactory(listOf(chzzkStrategy, twitchStrategy, soopStrategy))

            assertEquals(PlatformType.TWITCH, factory.getPlatformStrategy(PlatformType.TWITCH).platformType)
            assertEquals(PlatformType.SOOP, factory.getPlatformStrategy(PlatformType.SOOP).platformType)
        }

        @Test
        @DisplayName("등록되지 않은 플랫폼 유형은 예외를 발생시킨다")
        fun nonExistentPlatform() {
            val factory = PlatformStrategyFactory(emptyList())

            assertThrows(IllegalArgumentException::class.java) {
                factory.getPlatformStrategy(PlatformType.CHZZK)
            }
        }
    }
}
