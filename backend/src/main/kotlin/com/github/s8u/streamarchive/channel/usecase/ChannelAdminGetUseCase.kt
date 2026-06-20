package com.github.s8u.streamarchive.channel.usecase

import com.github.s8u.streamarchive.channel.repository.ChannelRepository
import com.github.s8u.streamarchive.channel.usecase.dto.result.ChannelAdminGetResult
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.global.service.UrlService
import com.github.s8u.streamarchive.video.repository.VideoRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 채널 상세 조회 (관리자)
 */
@Service
class ChannelAdminGetUseCase(
    private val channelRepository: ChannelRepository,
    private val videoRepository: VideoRepository,
    private val urlService: UrlService
) {

    @Transactional(readOnly = true)
    fun get(id: Long): ChannelAdminGetResult {
        val channel = channelRepository.findById(id).orElseThrow {
            BusinessException("채널을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }
        val totalVideoFileSize = videoRepository.sumFileSizeByChannelIds(listOf(id))[id] ?: 0L

        return ChannelAdminGetResult.from(
            channel = channel,
            profileUrl = urlService.channelProfileUrl(channel.uuid),
            totalVideoFileSize = totalVideoFileSize
        )
    }

}
