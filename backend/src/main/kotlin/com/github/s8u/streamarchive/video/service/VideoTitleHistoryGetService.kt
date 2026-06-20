package com.github.s8u.streamarchive.video.service

import com.github.s8u.streamarchive.video.repository.VideoMetadataTitleHistoryRepository
import org.springframework.stereotype.Service

/**
 * 동영상 제목 변경 이력 조회 서비스
 */
@Service
class VideoTitleHistoryGetService(
    private val titleHistoryRepository: VideoMetadataTitleHistoryRepository
) {

    /**
     * 해당 오프셋 시점 이전(포함)의 가장 마지막 제목을 반환한다.
     */
    fun findTitleAtOrBefore(videoId: Long, offsetMillis: Long): String? {
        return titleHistoryRepository.findTopByVideoIdAndOffsetMillisLessThanEqualOrderByOffsetMillisDesc(
            videoId = videoId,
            offsetMillis = offsetMillis
        )?.title
    }

}
