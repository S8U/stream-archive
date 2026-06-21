package com.github.s8u.streamarchive.video.usecase.dto.result

import com.github.s8u.streamarchive.video.entity.VideoAutoDeletePolicy

/**
 * 채널별 동영상 자동 삭제 정책 목록 조회 결과 (한 건)
 */
data class VideoAutoDeleteChannelPolicySearchResult(
    val channelId: Long,
    val isEnabled: Boolean,
    val deleteAfterDays: Int
) {

    companion object {
        fun from(policy: VideoAutoDeletePolicy): VideoAutoDeleteChannelPolicySearchResult {
            return VideoAutoDeleteChannelPolicySearchResult(
                channelId = policy.channelId!!,
                isEnabled = policy.isEnabled,
                deleteAfterDays = policy.deleteAfterDays
            )
        }
    }

}
