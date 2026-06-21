package com.github.s8u.streamarchive.video.service

import com.github.s8u.streamarchive.video.entity.Video
import com.github.s8u.streamarchive.video.entity.VideoAutoDeleteHistory
import com.github.s8u.streamarchive.video.repository.VideoAutoDeleteHistoryRepository
import org.springframework.stereotype.Service

/**
 * 동영상 자동 삭제 이력 저장 서비스
 *
 * 파일이 사라져도 무엇이 지워졌는지 알 수 있게 삭제 시점 정보를 저장한다.
 */
@Service
class VideoAutoDeleteHistorySaveService(
    private val videoAutoDeleteHistoryRepository: VideoAutoDeleteHistoryRepository
) {

    /**
     * 자동 삭제 이력을 저장한다.
     */
    fun save(video: Video) {
        videoAutoDeleteHistoryRepository.save(
            VideoAutoDeleteHistory(
                videoId = video.id!!,
                channelId = video.channelId,
                title = video.title,
                fileSize = video.fileSize,
                videoCreatedAt = video.createdAt
            )
        )
    }

}
