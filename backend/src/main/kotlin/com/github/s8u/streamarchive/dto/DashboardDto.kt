package com.github.s8u.streamarchive.dto

import java.time.LocalDate

// 대시보드 통계 응답
data class AdminDashboardStatsResponse(
    val totalChannels: Long,
    val totalVideos: Long,
    val totalDuration: Long, // 초
    val totalStorage: Long // 바이트
)

// 동영상 히스토리 응답
data class AdminDashboardVideoHistoriesResponse(val dailyStats: List<DailyStat>) {
    data class DailyStat(
        val date: LocalDate,
        val videoCount: Long, // 누적 동영상 수
        val storageUsage: Long // 누적 스토리지 (바이트)
    )
}
