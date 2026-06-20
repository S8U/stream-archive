package com.github.s8u.streamarchive.dashboard.usecase.dto.result

import java.time.LocalDate

/**
 * 대시보드 동영상 히스토리 조회 결과
 */
data class DashboardVideoHistoryGetResult(
    val dailyStats: List<DailyStat>
) {

    data class DailyStat(
        val date: LocalDate,
        val videoCount: Long, // 누적 동영상 수
        val storageUsage: Long // 누적 스토리지 (바이트)
    )
}
