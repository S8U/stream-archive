package com.github.s8u.streamarchive.channel.usecase

import com.github.s8u.streamarchive.channel.repository.ChannelRepository
import com.github.s8u.streamarchive.channel.service.ChannelAccessAssertService
import com.github.s8u.streamarchive.channel.usecase.dto.result.ChannelStatsResult
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.video.repository.VideoRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 채널 통계 조회 (공개)
 */
@Service
class ChannelStatsGetUseCase(
    private val channelRepository: ChannelRepository,
    private val videoRepository: VideoRepository,
    private val channelAccessAssertService: ChannelAccessAssertService
) {

    @Transactional(readOnly = true)
    fun getByUuid(uuid: String): ChannelStatsResult {
        val channel = channelRepository.findByUuid(uuid) ?: throw BusinessException(
            "채널을 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND
        )

        channelAccessAssertService.assertAccessible(channel.contentPrivacy)

        val videoCount = videoRepository.countByChannelId(channel.id!!)
        val totalFileSize = videoRepository.sumFileSizeByChannelId(channel.id!!)

        return ChannelStatsResult(
            videoCount = videoCount,
            totalFileSize = totalFileSize
        )
    }

}
