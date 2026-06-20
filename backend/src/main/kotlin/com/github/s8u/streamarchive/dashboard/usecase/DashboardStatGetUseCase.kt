package com.github.s8u.streamarchive.dashboard.usecase

import com.github.s8u.streamarchive.channel.repository.ChannelRepository
import com.github.s8u.streamarchive.dashboard.usecase.dto.result.DashboardStatGetResult
import com.github.s8u.streamarchive.video.repository.VideoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 대시보드 통계 조회 (관리자)
 */
@Service
class DashboardStatGetUseCase(
    private val channelRepository: ChannelRepository,
    private val videoRepository: VideoRepository
) {

    @Transactional(readOnly = true)
    fun get(): DashboardStatGetResult {
        val totalChannels = channelRepository.count()
        val totalVideos = videoRepository.count()
        val totalDuration = videoRepository.sumDuration() ?: 0L
        val totalStorage = videoRepository.sumFileSize() ?: 0L

        return DashboardStatGetResult(
            totalChannels = totalChannels,
            totalVideos = totalVideos,
            totalDuration = totalDuration,
            totalStorage = totalStorage
        )
    }

}
