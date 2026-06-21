package com.github.s8u.streamarchive.video.usecase

import com.github.s8u.streamarchive.auth.security.service.CurrentUserService
import com.github.s8u.streamarchive.channel.repository.ChannelRepository
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.global.util.RequestUtils
import com.github.s8u.streamarchive.video.entity.VideoAutoDeletePolicy
import com.github.s8u.streamarchive.video.repository.VideoAutoDeletePolicyRepository
import com.github.s8u.streamarchive.video.usecase.dto.command.VideoAutoDeletePolicyUpdateCommand
import com.github.s8u.streamarchive.video.usecase.dto.result.VideoAutoDeletePolicyUpdateResult
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 동영상 자동 삭제 정책 설정 (관리자)
 *
 * 전체 또는 채널별 정책을 설정한다.
 * 해당 정책이 없으면 새로 만들고, 있으면 값을 바꾼다.
 */
@Service
class VideoAutoDeletePolicyUpdateUseCase(
    private val videoAutoDeletePolicyRepository: VideoAutoDeletePolicyRepository,
    private val channelRepository: ChannelRepository,
    private val currentUserService: CurrentUserService
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * 동영상 자동 삭제 정책을 설정한다.
     */
    @Transactional
    fun update(command: VideoAutoDeletePolicyUpdateCommand): VideoAutoDeletePolicyUpdateResult {
        // 검증 (일수 하한은 엔티티 불변식으로 강제하므로 여기선 채널 존재만 본다)
        if (command.channelId != null && !channelRepository.existsById(command.channelId))
            throw BusinessException("채널을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)

        val userId = currentUserService.getCurrentUserId()
        val clientIp = RequestUtils.getClientIp()

        // 없으면 생성, 있으면 수정
        val policy = findPolicy(command.channelId)
        if (policy == null) {
            val newPolicy = VideoAutoDeletePolicy(
                channelId = command.channelId,
                isEnabled = command.isEnabled,
                deleteAfterDays = command.deleteAfterDays
            )
            newPolicy.recordCreator(userId, clientIp)

            videoAutoDeletePolicyRepository.save(newPolicy)

            logger.info(
                "VideoAutoDeletePolicyUpdateUseCase: Auto-delete policy created: channelId={} userId={}",
                command.channelId,
                userId
            )

            return VideoAutoDeletePolicyUpdateResult.from(newPolicy)
        }

        policy.update(isEnabled = command.isEnabled, deleteAfterDays = command.deleteAfterDays)
        policy.recordUpdater(userId, clientIp)

        logger.info(
            "VideoAutoDeletePolicyUpdateUseCase: Auto-delete policy updated: policyId={} channelId={} userId={}",
            policy.id,
            command.channelId,
            userId
        )

        return VideoAutoDeletePolicyUpdateResult.from(policy)
    }

    private fun findPolicy(channelId: Long?): VideoAutoDeletePolicy? {
        return if (channelId == null) {
            videoAutoDeletePolicyRepository.findByChannelIdIsNull()
        } else {
            videoAutoDeletePolicyRepository.findByChannelId(channelId)
        }
    }

}
