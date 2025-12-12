package com.github.s8u.streamarchive.repository

import com.github.s8u.streamarchive.dto.AdminDashboardVideoHistoriesResponse
import com.github.s8u.streamarchive.dto.AdminVideoSearchRequest
import com.github.s8u.streamarchive.dto.PublicVideoSearchRequest
import com.github.s8u.streamarchive.entity.Video
import java.time.LocalDate
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface VideoRepositoryCustom {
    fun searchForAdmin(request: AdminVideoSearchRequest, pageable: Pageable): Page<Video>
    fun searchForPublic(request: PublicVideoSearchRequest, pageable: Pageable): Page<Video>

    // 대시보드 통계
    fun sumDuration(): Long?
    fun sumFileSize(): Long?
    fun getDailyVideoStats(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<AdminDashboardVideoHistoriesResponse.DailyStat>

    // 채널별 통계
    fun countByChannelId(channelId: Long): Long
    fun sumFileSizeByChannelId(channelId: Long): Long
}
