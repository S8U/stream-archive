package com.github.s8u.streamarchive.video.usecase

import com.github.s8u.streamarchive.video.repository.VideoAutoDeletePolicyRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 채널별 동영상 자동 삭제 정책 삭제 (관리자)
 *
 * 채널 정책을 지우면 그 채널은 전체 기본 정책을 따른다.
 */
@Service
class VideoAutoDeleteChannelPolicyDeleteUseCase(
    private val videoAutoDeletePolicyRepository: VideoAutoDeletePolicyRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun delete(channelId: Long) {
        videoAutoDeletePolicyRepository.deleteByChannelId(channelId)

        logger.info("VideoAutoDeleteChannelPolicyDeleteUseCase: Channel policy deleted: channelId={}", channelId)
    }

}
