package com.github.s8u.streamarchive.video.service

import com.github.s8u.streamarchive.video.entity.VideoAutoDeletePolicy
import com.github.s8u.streamarchive.video.repository.VideoAutoDeletePolicyRepository
import org.springframework.stereotype.Service

/**
 * 동영상 자동 삭제 정책 조회 서비스
 *
 * 전체 정책과 채널별 정책을 조회한다.
 */
@Service
class VideoAutoDeletePolicyGetService(
    private val videoAutoDeletePolicyRepository: VideoAutoDeletePolicyRepository
) {

    /**
     * 전체 기본 정책을 조회한다.
     *
     * 아직 설정한 적 없으면 null을 반환한다.
     */
    fun getGlobalPolicy(): VideoAutoDeletePolicy? {
        return videoAutoDeletePolicyRepository.findByChannelIdIsNull()
    }

    /**
     * 채널별 정책을 조회한다.
     *
     * 채널에 설정한 적 없으면 null을 반환한다.
     */
    fun getChannelPolicy(channelId: Long): VideoAutoDeletePolicy? {
        return videoAutoDeletePolicyRepository.findByChannelId(channelId)
    }

    /**
     * 채널별 정책 전체를 조회한다.
     */
    fun getAllChannelPolicies(): List<VideoAutoDeletePolicy> {
        return videoAutoDeletePolicyRepository.findAllByChannelIdIsNotNull()
    }

}
