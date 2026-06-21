package com.github.s8u.streamarchive.video.usecase.dto.command

/**
 * 동영상 자동 삭제 정책 설정 명령
 *
 * [channelId]가 없으면 전체 기본 정책을 설정한다.
 */
data class VideoAutoDeletePolicyUpdateCommand(
    val channelId: Long?,
    val isEnabled: Boolean,
    val deleteAfterDays: Int
)
