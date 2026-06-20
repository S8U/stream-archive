package com.github.s8u.streamarchive.dashboard.usecase.dto.result

/**
 * 대시보드 통계 조회 결과
 */
data class DashboardStatGetResult(
    val totalChannels: Long,
    val totalVideos: Long,
    val totalDuration: Long, // 초
    val totalStorage: Long // 바이트
)
