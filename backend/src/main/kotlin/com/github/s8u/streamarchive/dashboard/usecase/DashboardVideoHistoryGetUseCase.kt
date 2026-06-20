package com.github.s8u.streamarchive.dashboard.usecase

import com.github.s8u.streamarchive.dashboard.usecase.dto.result.DashboardVideoHistoryGetResult
import com.github.s8u.streamarchive.video.repository.VideoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

/**
 * 대시보드 동영상 히스토리 조회 (관리자)
 */
@Service
class DashboardVideoHistoryGetUseCase(
    private val videoRepository: VideoRepository
) {

    @Transactional(readOnly = true)
    fun get(): DashboardVideoHistoryGetResult {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(29)

        // video 투영 결과를 대시보드 결과로 변환
        val dailyStats = videoRepository.getDailyVideoStats(startDate, endDate)
            .map {
                DashboardVideoHistoryGetResult.DailyStat(
                    date = it.date,
                    videoCount = it.videoCount,
                    storageUsage = it.storageUsage
                )
            }

        return DashboardVideoHistoryGetResult(dailyStats = dailyStats)
    }

}
