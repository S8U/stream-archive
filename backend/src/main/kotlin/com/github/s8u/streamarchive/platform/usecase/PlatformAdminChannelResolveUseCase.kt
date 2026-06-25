package com.github.s8u.streamarchive.platform.usecase

import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.platform.service.PlatformUrlResolveService
import com.github.s8u.streamarchive.platform.strategy.PlatformStrategyFactory
import com.github.s8u.streamarchive.platform.usecase.dto.command.PlatformAdminChannelResolveCommand
import com.github.s8u.streamarchive.platform.usecase.dto.result.PlatformAdminChannelResolveResult
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 플랫폼 채널 조회 (관리자)
 *
 * URL에서 플랫폼과 채널 ID를 가려낸 뒤 플랫폼 API로 채널 이름·썸네일을 가져온다.
 * 간편 채널 추가 화면이 저장 전에 미리보기를 채우는 용도다.
 */
@Service
class PlatformAdminChannelResolveUseCase(
    private val platformUrlResolveService: PlatformUrlResolveService,
    private val platformStrategyFactory: PlatformStrategyFactory
) {

    @Transactional(readOnly = true)
    fun resolve(command: PlatformAdminChannelResolveCommand): PlatformAdminChannelResolveResult {
        // URL에서 플랫폼·채널 ID 추출
        val resolved = platformUrlResolveService.resolve(command.url)
            ?: throw BusinessException(
                "지원하지 않는 URL이거나 채널을 인식할 수 없습니다.",
                HttpStatus.BAD_REQUEST
            )

        // 플랫폼 API로 채널 정보 조회
        val strategy = platformStrategyFactory.getPlatformStrategy(resolved.platformType)
        val channel = strategy.getChannel(resolved.platformChannelId)
            ?: throw BusinessException("플랫폼에서 채널을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)

        return PlatformAdminChannelResolveResult(
            platformType = resolved.platformType,
            platformChannelId = resolved.platformChannelId,
            name = channel.name,
            thumbnailUrl = channel.thumbnailUrl
        )
    }

}
