package com.github.s8u.streamarchive.video.usecase

import com.github.s8u.streamarchive.channel.repository.ChannelRepository
import com.github.s8u.streamarchive.video.repository.VideoRepository
import com.github.s8u.streamarchive.video.service.VideoAutoDeletePolicyGetService
import com.github.s8u.streamarchive.video.service.VideoAutoDeleteTargetGetService
import com.github.s8u.streamarchive.video.service.dto.VideoAutoDeleteTarget
import com.github.s8u.streamarchive.video.usecase.dto.result.VideoAutoDeletePreviewSummaryGetResult
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * 동영상 자동 삭제 미리보기 요약 조회 (관리자)
 *
 * 지금 정책대로라면 다음 자동 삭제에서 지워질 동영상의 개수와 총 용량을 센다.
 * 채널 ID가 없으면 전체 채널의 적용 정책을 합산한다.
 */
@Service
class VideoAutoDeletePreviewSummaryGetUseCase(
    private val videoRepository: VideoRepository,
    private val channelRepository: ChannelRepository,
    private val videoAutoDeletePolicyGetService: VideoAutoDeletePolicyGetService,
    private val videoAutoDeleteTargetGetService: VideoAutoDeleteTargetGetService
) {

    /**
     * 동영상 자동 삭제 미리보기 요약을 조회한다.
     */
    @Transactional(readOnly = true)
    fun getSummary(channelId: Long?): VideoAutoDeletePreviewSummaryGetResult {
        val targets = getTargets(channelId)

        var targetCount = 0L
        var totalFileSize = 0L
        for (target in targets) {
            targetCount += videoRepository.countAutoDeleteTargets(target.channelId, target.createdBefore)
            totalFileSize += videoRepository.sumFileSizeAutoDeleteTargets(target.channelId, target.createdBefore)
        }

        return VideoAutoDeletePreviewSummaryGetResult(
            channelId = channelId,
            targetCount = targetCount,
            totalFileSize = totalFileSize
        )
    }

    // 미리보기 대상 채널과 기준일을 정한다 (단일 채널이면 그 채널만, 전체면 모든 채널)
    private fun getTargets(channelId: Long?): List<VideoAutoDeleteTarget> {
        val channelIds = if (channelId == null) channelRepository.findAllIds() else listOf(channelId)

        return videoAutoDeleteTargetGetService.getTargets(
            globalPolicy = videoAutoDeletePolicyGetService.getGlobalPolicy(),
            channelPolicies = videoAutoDeletePolicyGetService.getAllChannelPolicies(),
            channelIds = channelIds,
            now = LocalDateTime.now()
        )
    }

}
