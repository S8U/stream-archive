package com.github.s8u.streamarchive.video.repository.dto

import java.time.LocalDate

/**
 * 일자별 동영상 통계 투영
 *
 * 특정 날짜까지의 누적 동영상 수와 누적 스토리지 사용량을 담는다.
 */
data class VideoDailyStatProjection(
    val date: LocalDate,
    val videoCount: Long, // 누적 동영상 수
    val storageUsage: Long // 누적 스토리지 (바이트)
)
