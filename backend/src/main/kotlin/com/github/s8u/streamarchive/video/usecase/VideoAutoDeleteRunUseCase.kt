package com.github.s8u.streamarchive.video.usecase

import com.github.s8u.streamarchive.channel.repository.ChannelRepository
import com.github.s8u.streamarchive.global.service.TransactionRunner
import com.github.s8u.streamarchive.video.entity.Video
import com.github.s8u.streamarchive.video.repository.VideoRepository
import com.github.s8u.streamarchive.video.service.VideoAutoDeleteHistorySaveService
import com.github.s8u.streamarchive.video.service.VideoAutoDeletePolicyGetService
import com.github.s8u.streamarchive.video.service.VideoAutoDeleteTargetGetService
import com.github.s8u.streamarchive.video.service.VideoDeleteService
import com.github.s8u.streamarchive.video.usecase.dto.result.VideoAutoDeleteRunResult
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * 동영상 자동 삭제 실행
 *
 * 채널별 적용 정책을 정해 기준일 이전의 소장하지 않은 동영상을 삭제한다.
 * 한 건이 실패해도 나머지는 계속 삭제한다.
 */
@Service
class VideoAutoDeleteRunUseCase(
    private val videoRepository: VideoRepository,
    private val channelRepository: ChannelRepository,
    private val videoAutoDeletePolicyGetService: VideoAutoDeletePolicyGetService,
    private val videoAutoDeleteTargetGetService: VideoAutoDeleteTargetGetService,
    private val videoDeleteService: VideoDeleteService,
    private val videoAutoDeleteHistorySaveService: VideoAutoDeleteHistorySaveService,
    private val transactionRunner: TransactionRunner
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * 동영상 자동 삭제를 실행한다.
     */
    fun run(): VideoAutoDeleteRunResult {
        val now = LocalDateTime.now()

        // 채널별 적용 정책 결정
        val targets = videoAutoDeleteTargetGetService.getTargets(
            globalPolicy = videoAutoDeletePolicyGetService.getGlobalPolicy(),
            channelPolicies = videoAutoDeletePolicyGetService.getAllChannelPolicies(),
            channelIds = channelRepository.findAllIds(),
            now = now
        )

        var deletedCount = 0
        for (target in targets) {
            val videos = videoRepository.findAutoDeleteTargets(target.channelId, target.createdBefore)
            for (video in videos) {
                if (deleteOne(video)) {
                    deletedCount++
                }
            }
        }

        logger.info("VideoAutoDeleteRunUseCase: Auto-deleted videos: deletedCount={}", deletedCount)

        return VideoAutoDeleteRunResult(deletedCount = deletedCount)
    }

    // 동영상 한 건 삭제 및 이력 저장 (실패 시 다음 건 계속)
    private fun deleteOne(video: Video): Boolean {
        return try {
            transactionRunner.run {
                videoDeleteService.delete(video.id!!)
                videoAutoDeleteHistorySaveService.save(video)
            }
            true
        } catch (e: Exception) {
            logger.error("VideoAutoDeleteRunUseCase: Failed to auto-delete video: videoId={}", video.id, e)
            false
        }
    }

}
