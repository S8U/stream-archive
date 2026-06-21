package com.github.s8u.streamarchive.video.usecase.dto.result

import com.github.s8u.streamarchive.video.entity.VideoAutoDeletePolicy

/**
 * 동영상 자동 삭제 정책 조회 결과
 *
 * 설정한 적 없으면 [isConfigured]가 false다.
 */
data class VideoAutoDeletePolicyGetResult(
    val channelId: Long?,
    val isConfigured: Boolean,
    val isEnabled: Boolean,
    val deleteAfterDays: Int?
) {

    companion object {
        fun from(
            channelId: Long?,
            policy: VideoAutoDeletePolicy?
        ): VideoAutoDeletePolicyGetResult {
            return VideoAutoDeletePolicyGetResult(
                channelId = channelId,
                isConfigured = policy != null,
                isEnabled = policy?.isEnabled ?: false,
                deleteAfterDays = policy?.deleteAfterDays
            )
        }
    }

}
