package com.github.s8u.streamarchive.dto

/**
 * 하이라이트 구간 응답 DTO
 */
data class HighlightResponse(
    val startOffsetMillis: Long,   // 하이라이트 시작 시점 (밀리초)
    val endOffsetMillis: Long,     // 하이라이트 종료 시점 (밀리초)
    val chatCount: Int,            // 해당 구간의 총 채팅 개수
    val intensity: Double,         // 강도 (0.0 ~ 1.0, 정규화된 점수)
    val peakOffsetMillis: Long     // 피크 지점 (가장 채팅이 많은 시점)
)
