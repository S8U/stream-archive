package com.github.s8u.streamarchive.video.usecase.dto.result

import com.github.s8u.streamarchive.video.entity.VideoAutoDeletePolicy

/**
 * 동영상 자동 삭제 정책 설정 결과
 */
data class VideoAutoDeletePolicyUpdateResult(
    val channelId: Long?,
    val isEnabled: Boolean,
    val deleteAfterDays: Int
) {

    companion object {
        fun from(policy: VideoAutoDeletePolicy): VideoAutoDeletePolicyUpdateResult {
            return VideoAutoDeletePolicyUpdateResult(
                channelId = policy.channelId,
                isEnabled = policy.isEnabled,
                deleteAfterDays = policy.deleteAfterDays
            )
        }
    }

}
