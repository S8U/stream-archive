package com.github.s8u.streamarchive.video.usecase.dto.result

/**
 * 동영상 자동 삭제 미리보기 요약 결과
 *
 * 지금 정책대로라면 다음 자동 삭제에서 지워질 동영상의 개수와 총 용량이다.
 */
data class VideoAutoDeletePreviewSummaryGetResult(
    val channelId: Long?,
    val targetCount: Long,
    val totalFileSize: Long
)
