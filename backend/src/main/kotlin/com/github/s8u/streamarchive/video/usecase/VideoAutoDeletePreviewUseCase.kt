package com.github.s8u.streamarchive.video.usecase

import com.github.s8u.streamarchive.channel.repository.ChannelRepository
import com.github.s8u.streamarchive.global.service.UrlService
import com.github.s8u.streamarchive.video.entity.Video
import com.github.s8u.streamarchive.video.repository.VideoRepository
import com.github.s8u.streamarchive.video.service.VideoAutoDeletePolicyGetService
import com.github.s8u.streamarchive.video.service.VideoAutoDeleteTargetGetService
import com.github.s8u.streamarchive.video.service.dto.VideoAutoDeleteTarget
import com.github.s8u.streamarchive.video.usecase.dto.result.VideoAutoDeletePreviewResult
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * 동영상 자동 삭제 미리보기 목록 조회 (관리자)
 *
 * 현재 정책의 다음 자동 삭제 대상 목록을 보여준다.
 * 채널별 조회 결과를 합쳐 최근 생성순으로 정렬한다.
 * 채널 ID가 없으면 전체 채널을 대상으로 한다.
 */
@Service
class VideoAutoDeletePreviewUseCase(
    private val videoRepository: VideoRepository,
    private val channelRepository: ChannelRepository,
    private val videoAutoDeletePolicyGetService: VideoAutoDeletePolicyGetService,
    private val videoAutoDeleteTargetGetService: VideoAutoDeleteTargetGetService,
    private val urlService: UrlService
) {

    /**
     * 동영상 자동 삭제 미리보기 목록을 조회한다.
     */
    @Transactional(readOnly = true)
    fun preview(
        channelId: Long?,
        pageable: Pageable
    ): Page<VideoAutoDeletePreviewResult> {
        val now = LocalDateTime.now()
        val targets = getTargets(channelId, now)

        // 채널별 삭제 대상을 모아 최근 생성순으로 정렬
        val allTargets = targets
            .flatMap { target ->
                videoRepository.findAutoDeleteTargets(target.channelId, target.createdBefore)
                    .map { video -> video to target.deleteAfterDays }
            }
            .sortedByDescending { it.first.createdAt }

        // 현재 페이지만 잘라서 변환
        val pageContent = allTargets
            .drop(pageable.offset.toInt())
            .take(pageable.pageSize)

        val channelNames = findChannelNames(pageContent.map { it.first })
        val results = pageContent.map { (video, deleteAfterDays) ->
            val ageDays = ChronoUnit.DAYS.between(video.createdAt, now).toInt()
            VideoAutoDeletePreviewResult.from(
                video = video,
                channelName = channelNames[video.channelId] ?: "",
                thumbnailUrl = urlService.videoThumbnailUrl(video.uuid),
                ageDays = ageDays,
                overDays = ageDays - deleteAfterDays
            )
        }

        return PageImpl(results, pageable, allTargets.size.toLong())
    }

    // 미리보기 대상 채널과 기준일을 정한다 (단일 채널이면 그 채널만, 전체면 모든 채널)
    private fun getTargets(
        channelId: Long?,
        now: LocalDateTime
    ): List<VideoAutoDeleteTarget> {
        val channelIds = if (channelId == null) channelRepository.findAllIds() else listOf(channelId)

        return videoAutoDeleteTargetGetService.getTargets(
            globalPolicy = videoAutoDeletePolicyGetService.getGlobalPolicy(),
            channelPolicies = videoAutoDeletePolicyGetService.getAllChannelPolicies(),
            channelIds = channelIds,
            now = now
        )
    }

    // 현재 페이지 영상들의 채널 이름을 한 번에 조회한다
    private fun findChannelNames(videos: List<Video>): Map<Long, String> {
        val channelIds = videos.map { it.channelId }.distinct()
        return channelRepository.findAllById(channelIds).associate { it.id!! to it.name }
    }

}
