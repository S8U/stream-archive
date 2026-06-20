package com.github.s8u.streamarchive.channelplatform.usecase

import com.github.s8u.streamarchive.channelplatform.repository.ChannelPlatformRepository
import com.github.s8u.streamarchive.channelplatform.service.ChannelPlatformProfileSaveService
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.platform.enums.PlatformType
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 채널 플랫폼 프로필 동기화 (단건)
 */
@Service
class ChannelPlatformProfileSyncUseCase(
    private val channelPlatformRepository: ChannelPlatformRepository,
    private val channelPlatformProfileSaveService: ChannelPlatformProfileSaveService
) {

    @Transactional(readOnly = true)
    fun sync(channelId: Long, platformType: PlatformType) {
        val channelPlatform = channelPlatformRepository.findByChannelIdAndPlatformType(channelId, platformType)
            ?: throw BusinessException("채널 플랫폼을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)

        if (channelPlatform.isSyncProfile) {
            channelPlatformProfileSaveService.save(channelPlatform)
        }
    }

}
