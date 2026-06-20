package com.github.s8u.streamarchive.channelplatform.usecase

import com.github.s8u.streamarchive.channelplatform.repository.ChannelPlatformRepository
import com.github.s8u.streamarchive.channelplatform.service.ChannelPlatformProfileSaveService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 채널 플랫폼 프로필 동기화 (전체)
 */
@Service
class ChannelPlatformProfileSyncAllUseCase(
    private val channelPlatformRepository: ChannelPlatformRepository,
    private val channelPlatformProfileSaveService: ChannelPlatformProfileSaveService
) {

    @Transactional(readOnly = true)
    fun syncAll() {
        val channelPlatforms = channelPlatformRepository.findByIsSyncProfile(true)
        channelPlatforms.forEach { channelPlatformProfileSaveService.save(it) }
    }

}
