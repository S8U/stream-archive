package com.github.s8u.streamarchive.video.usecase

import com.github.s8u.streamarchive.video.service.VideoAutoDeletePolicyGetService
import com.github.s8u.streamarchive.video.usecase.dto.result.VideoAutoDeletePolicyGetResult
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 동영상 자동 삭제 정책 조회 (관리자)
 */
@Service
class VideoAutoDeletePolicyGetUseCase(
    private val videoAutoDeletePolicyGetService: VideoAutoDeletePolicyGetService
) {

    /**
     * 전체 기본 정책을 조회한다.
     */
    @Transactional(readOnly = true)
    fun getGlobal(): VideoAutoDeletePolicyGetResult {
        val policy = videoAutoDeletePolicyGetService.getGlobalPolicy()
        return VideoAutoDeletePolicyGetResult.from(channelId = null, policy = policy)
    }

    /**
     * 채널별 정책을 조회한다.
     */
    @Transactional(readOnly = true)
    fun getChannel(channelId: Long): VideoAutoDeletePolicyGetResult {
        val policy = videoAutoDeletePolicyGetService.getChannelPolicy(channelId)
        return VideoAutoDeletePolicyGetResult.from(channelId = channelId, policy = policy)
    }

}
