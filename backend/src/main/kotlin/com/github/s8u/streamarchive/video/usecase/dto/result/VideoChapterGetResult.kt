package com.github.s8u.streamarchive.video.usecase.dto.result

/**
 * 동영상 챕터 조회 결과
 *
 * 카테고리 변경 이력을 챕터 경계로 변환한 결과다.
 */
data class VideoChapterGetResult(
    val offsetMillis: Long,
    val category: String?,
    val title: String?
)
