package com.github.s8u.streamarchive.dto

data class HighlightSegment(
    val startOffsetMs: Long,
    val endOffsetMs: Long,
    val chatCount: Int,
    val peakChatRate: Double
)

data class VideoHighlightsResponse(
    val videoUuid: String,
    val totalDurationMs: Long,
    val highlights: List<HighlightSegment>,
    val totalHighlightDurationMs: Long,
    val parameters: HighlightParameters
)

data class HighlightParameters(
    val windowSizeSeconds: Int = 10,
    val thresholdPercentile: Int = 80,
    val minSegmentSeconds: Int = 5,
    val mergeGapSeconds: Int = 15
)
