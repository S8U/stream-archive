package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.dto.AdminDashboardStatsResponse
import com.github.s8u.streamarchive.dto.AdminDashboardVideoHistoriesResponse
import com.github.s8u.streamarchive.repository.ChannelRepository
import com.github.s8u.streamarchive.repository.VideoRepository
import java.time.LocalDate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DashboardService(
    private val channelRepository: ChannelRepository,
    private val videoRepository: VideoRepository
) {

    @Transactional(readOnly = true)
    fun getStats(): AdminDashboardStatsResponse {
        val totalChannels = channelRepository.count()
        val totalVideos = videoRepository.count()
        val totalDuration = videoRepository.sumDuration() ?: 0L
        val totalStorage = videoRepository.sumFileSize() ?: 0L

        return AdminDashboardStatsResponse(
            totalChannels = totalChannels,
            totalVideos = totalVideos,
            totalDuration = totalDuration,
            totalStorage = totalStorage
        )
    }

    @Transactional(readOnly = true)
    fun getVideoHistories(): AdminDashboardVideoHistoriesResponse {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(29)

        val dailyStats = videoRepository.getDailyVideoStats(startDate, endDate)

        return AdminDashboardVideoHistoriesResponse(dailyStats = dailyStats)
    }
}
