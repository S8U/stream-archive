package com.github.s8u.streamarchive.dashboard.controller.dto.response

import com.github.s8u.streamarchive.dashboard.usecase.dto.result.DashboardVideoHistoryGetResult
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "대시보드 동영상 히스토리 응답 (관리자)")
data class DashboardVideoHistoryResponse(
    @field:Schema(description = "일자별 누적 통계")
    val dailyStats: List<DailyStat>
) {

    data class DailyStat(
        val date: LocalDate,
        val videoCount: Long, // 누적 동영상 수
        val storageUsage: Long // 누적 스토리지 (바이트)
    )

    companion object {
        fun from(result: DashboardVideoHistoryGetResult): DashboardVideoHistoryResponse {
            return DashboardVideoHistoryResponse(
                dailyStats = result.dailyStats.map { dailyStat ->
                    DailyStat(
                        date = dailyStat.date,
                        videoCount = dailyStat.videoCount,
                        storageUsage = dailyStat.storageUsage
                    )
                }
            )
        }
    }
}
