package com.github.s8u.streamarchive.watchhistory.usecase

import com.github.s8u.streamarchive.auth.security.service.CurrentUserService
import com.github.s8u.streamarchive.video.service.VideoAccessAssertService
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.video.repository.VideoRepository
import com.github.s8u.streamarchive.watchhistory.repository.UserVideoWatchHistoryRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 동영상 시청 기록 개별 삭제
 */
@Service
class WatchHistoryDeleteUseCase(
    private val watchHistoryRepository: UserVideoWatchHistoryRepository,
    private val videoRepository: VideoRepository,
    private val currentUserService: CurrentUserService,
    private val videoAccessAssertService: VideoAccessAssertService
) {

    @Transactional
    fun delete(videoUuid: String) {
        val userId = currentUserService.getCurrentUserId()
            ?: throw BusinessException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED)

        val video = videoRepository.findByUuid(videoUuid) ?: throw BusinessException(
            "동영상을 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND
        )
        videoAccessAssertService.assertAccessible(video.contentPrivacy, video.channel?.contentPrivacy)

        watchHistoryRepository.deleteByUserIdAndVideoId(userId, video.id!!)
    }

}
