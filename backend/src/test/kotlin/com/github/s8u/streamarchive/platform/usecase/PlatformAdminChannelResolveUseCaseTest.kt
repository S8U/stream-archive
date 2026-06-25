package com.github.s8u.streamarchive.platform.usecase

import com.github.s8u.streamarchive.platform.enums.PlatformType
import com.github.s8u.streamarchive.platform.service.PlatformUrlResolveService
import com.github.s8u.streamarchive.platform.service.dto.PlatformChannelIdResolveResult
import com.github.s8u.streamarchive.platform.strategy.PlatformStrategy
import com.github.s8u.streamarchive.platform.strategy.PlatformStrategyFactory
import com.github.s8u.streamarchive.platform.strategy.dto.PlatformChannelDto
import com.github.s8u.streamarchive.platform.usecase.dto.command.PlatformAdminChannelResolveCommand
import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PlatformAdminChannelResolveUseCaseTest {

    private val platformUrlResolveService = mockk<PlatformUrlResolveService>()
    private val platformStrategyFactory = mockk<PlatformStrategyFactory>()
    private val strategy = mockk<PlatformStrategy>()
    private val useCase = PlatformAdminChannelResolveUseCase(
        platformUrlResolveService = platformUrlResolveService,
        platformStrategyFactory = platformStrategyFactory
    )

    @Nested
    inner class Resolve {

        @Test
        fun `URL에서 추출한 플랫폼 채널 ID를 그대로 반환한다`() {
            every { platformUrlResolveService.resolve(URL) } returns PlatformChannelIdResolveResult(
                platformType = PlatformType.TWITCH,
                platformChannelId = LOGIN
            )
            every { platformStrategyFactory.getPlatformStrategy(PlatformType.TWITCH) } returns strategy
            every { strategy.getChannel(LOGIN) } returns PlatformChannelDto(
                platformDto = Any(),
                platformType = PlatformType.TWITCH,
                id = INTERNAL_ID,
                username = LOGIN,
                name = "테스트 채널",
                thumbnailUrl = "https://example.com/profile.png"
            )

            val result = useCase.resolve(PlatformAdminChannelResolveCommand(URL))

            assertEquals(PlatformType.TWITCH, result.platformType)
            assertEquals(LOGIN, result.platformChannelId)
            assertEquals("테스트 채널", result.name)
            assertEquals("https://example.com/profile.png", result.thumbnailUrl)
        }

    }

    companion object {
        private const val URL = "https://www.twitch.tv/test-login"
        private const val LOGIN = "test-login"
        private const val INTERNAL_ID = "123456789"
    }

}
