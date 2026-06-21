package com.github.s8u.streamarchive.video.usecase

import com.github.s8u.streamarchive.video.service.VideoAutoDeletePolicyGetService
import com.github.s8u.streamarchive.video.usecase.dto.result.VideoAutoDeleteChannelPolicySearchResult
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 채널별 동영상 자동 삭제 정책 목록 조회 (관리자)
 *
 * 채널별로 따로 설정한 정책만 조회한다.
 * 전체 기본 정책이 적용되는 채널은 빠진다.
 */
@Service
class VideoAutoDeleteChannelPolicySearchUseCase(
    private val videoAutoDeletePolicyGetService: VideoAutoDeletePolicyGetService
) {

    @Transactional(readOnly = true)
    fun search(): List<VideoAutoDeleteChannelPolicySearchResult> {
        return videoAutoDeletePolicyGetService.getAllChannelPolicies()
            .map { VideoAutoDeleteChannelPolicySearchResult.from(it) }
    }

}
