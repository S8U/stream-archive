package com.github.s8u.streamarchive.video.service.dto

import java.time.LocalDateTime

/**
 * 동영상 자동 삭제 대상 (채널 한 곳에 적용할 기준)
 *
 * 이 채널에서 [createdBefore] 이전에 생성된 소장하지 않은 동영상이 삭제 대상이다.
 */
data class VideoAutoDeleteTarget(
    val channelId: Long,
    val deleteAfterDays: Int,
    val createdBefore: LocalDateTime
)
