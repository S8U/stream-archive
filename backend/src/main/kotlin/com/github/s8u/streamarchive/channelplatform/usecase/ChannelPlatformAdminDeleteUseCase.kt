package com.github.s8u.streamarchive.channelplatform.usecase

import com.github.s8u.streamarchive.channelplatform.repository.ChannelPlatformRepository
import com.github.s8u.streamarchive.global.exception.BusinessException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 채널 플랫폼 삭제 (관리자)
 */
@Service
class ChannelPlatformAdminDeleteUseCase(
    private val channelPlatformRepository: ChannelPlatformRepository
) {

    @Transactional
    fun delete(id: Long) {
        val channelPlatform = channelPlatformRepository.findById(id).orElseThrow {
            BusinessException("채널 플랫폼을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        channelPlatform.softDelete(userId = null, ip = null)
    }

}
