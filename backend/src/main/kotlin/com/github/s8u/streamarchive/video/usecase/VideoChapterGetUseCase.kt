package com.github.s8u.streamarchive.video.usecase

import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.video.entity.VideoMetadataCategoryHistory
import com.github.s8u.streamarchive.video.repository.VideoMetadataCategoryHistoryRepository
import com.github.s8u.streamarchive.video.repository.VideoRepository
import com.github.s8u.streamarchive.video.service.VideoAccessAssertService
import com.github.s8u.streamarchive.video.service.VideoTitleHistoryGetService
import com.github.s8u.streamarchive.video.usecase.dto.result.VideoChapterGetResult
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 동영상 챕터 조회 (공개)
 *
 * 카테고리 변경 이력을 챕터 경계로 변환한다.
 * 각 챕터의 제목은 그 챕터 시작 시점의 제목을 함께 담는다.
 */
@Service
class VideoChapterGetUseCase(
    private val categoryHistoryRepository: VideoMetadataCategoryHistoryRepository,
    private val videoRepository: VideoRepository,
    private val videoTitleHistoryGetService: VideoTitleHistoryGetService,
    private val videoAccessAssertService: VideoAccessAssertService
) {

    @Transactional(readOnly = true)
    fun getByVideoUuid(uuid: String): List<VideoChapterGetResult> {
        val video = videoRepository.findByUuid(uuid)
            ?: throw BusinessException("동영상을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)

        videoAccessAssertService.assertAccessible(video.contentPrivacy, video.channel?.contentPrivacy)

        val videoId = video.id!!
        val categoryHistories = categoryHistoryRepository.findByVideoIdOrderByOffsetMillisAsc(videoId)

        return toChapters(videoId, categoryHistories)
    }

    /**
     * 카테고리 변경 이력을 챕터 목록으로 변환한다.
     *
     * 첫 챕터는 항상 0:00에서 시작하도록 오프셋을 당긴다.
     * 각 챕터 제목은 그 챕터 시작 시점(이전 포함)의 마지막 제목을 매칭한다.
     * 자잘한 구간이나 깜빡임 노이즈를 거를 경우 이 변환 지점에서 병합한다 (현재는 이력 그대로).
     */
    private fun toChapters(
        videoId: Long,
        categoryHistories: List<VideoMetadataCategoryHistory>
    ): List<VideoChapterGetResult> {
        return categoryHistories.mapIndexed { index, history ->
            // 첫 챕터는 0:00로 당긴다 (첫 카테고리 기록이 0보다 뒤에 잡혀도 영상 처음부터 챕터가 있게)
            val offsetMillis = if (index == 0) 0L else history.offsetMillis
            VideoChapterGetResult(
                offsetMillis = offsetMillis,
                category = history.category,
                title = videoTitleHistoryGetService.findTitleAtOrBefore(videoId, offsetMillis)
            )
        }
    }

}
