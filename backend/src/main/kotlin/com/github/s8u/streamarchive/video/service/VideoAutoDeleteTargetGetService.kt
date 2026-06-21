package com.github.s8u.streamarchive.video.service

import com.github.s8u.streamarchive.video.entity.VideoAutoDeletePolicy
import com.github.s8u.streamarchive.video.service.dto.VideoAutoDeleteTarget
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * 동영상 자동 삭제 대상 조회 서비스
 *
 * 전체 정책과 채널별 정책으로 각 채널에 적용할 삭제 기준을 정한다.
 * 채널별 정책이 있으면 그 정책을, 없으면 전체 정책을 적용한다.
 * 활성화되지 않은 정책이 적용되는 채널은 대상에서 빠진다.
 */
@Service
class VideoAutoDeleteTargetGetService {

    /**
     * 채널별 적용 정책에 따른 삭제 대상 목록을 조회한다.
     *
     * 채널별 정책이 없는 채널은 [globalPolicy]를 적용한다.
     * 적용할 정책이 없거나 비활성이면 그 채널은 결과에서 빠진다.
     */
    fun getTargets(
        globalPolicy: VideoAutoDeletePolicy?,
        channelPolicies: List<VideoAutoDeletePolicy>,
        channelIds: List<Long>,
        now: LocalDateTime
    ): List<VideoAutoDeleteTarget> {
        val channelPolicyMap = channelPolicies.associateBy { it.channelId }

        return channelIds.mapNotNull { channelId ->
            val policy = channelPolicyMap[channelId] ?: globalPolicy

            if (policy == null || !policy.isEnabled) {
                return@mapNotNull null
            }

            VideoAutoDeleteTarget(
                channelId = channelId,
                deleteAfterDays = policy.deleteAfterDays,
                createdBefore = now.minusDays(policy.deleteAfterDays.toLong())
            )
        }
    }

}
