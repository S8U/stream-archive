package com.github.s8u.streamarchive.channel.usecase.dto.result

/**
 * 채널 통계 조회 결과
 */
data class ChannelStatsResult(
    val videoCount: Long,
    val totalFileSize: Long
)
