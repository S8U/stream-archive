package com.github.s8u.streamarchive.watchhistory.usecase

import com.github.s8u.streamarchive.auth.security.service.CurrentUserService
import com.github.s8u.streamarchive.video.service.VideoAccessAssertService
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.video.repository.VideoRepository
import com.github.s8u.streamarchive.watchhistory.repository.UserVideoWatchHistoryRepository
import com.github.s8u.streamarchive.watchhistory.usecase.dto.result.WatchHistoryGetResult
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 동영상 시청 기록 단건 조회
 */
@Service
class WatchHistoryGetUseCase(
    private val watchHistoryRepository: UserVideoWatchHistoryRepository,
    private val videoRepository: VideoRepository,
    private val currentUserService: CurrentUserService,
    private val videoAccessAssertService: VideoAccessAssertService
) {

    @Transactional(readOnly = true)
    fun getByVideoUuid(videoUuid: String): WatchHistoryGetResult? {
        val userId = currentUserService.getCurrentUserId()
            ?: throw BusinessException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED)

        val video = videoRepository.findByUuid(videoUuid) ?: throw BusinessException(
            "동영상을 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND
        )
        videoAccessAssertService.assertAccessible(video.contentPrivacy, video.channel?.contentPrivacy)

        val history = watchHistoryRepository.findByUserIdAndVideoId(userId, video.id!!)
        return history?.let { WatchHistoryGetResult.from(it) }
    }

}
