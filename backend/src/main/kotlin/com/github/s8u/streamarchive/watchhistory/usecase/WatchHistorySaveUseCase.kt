package com.github.s8u.streamarchive.watchhistory.usecase

import com.github.s8u.streamarchive.auth.security.service.CurrentUserService
import com.github.s8u.streamarchive.video.service.VideoAccessAssertService
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.video.repository.VideoRepository
import com.github.s8u.streamarchive.watchhistory.entity.UserVideoWatchHistory
import com.github.s8u.streamarchive.watchhistory.repository.UserVideoWatchHistoryRepository
import com.github.s8u.streamarchive.watchhistory.usecase.dto.command.WatchHistorySaveCommand
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 동영상 시청 위치 저장
 *
 * 기존 기록이 있으면 위치를 갱신하고, 없으면 새로 만든다.
 */
@Service
class WatchHistorySaveUseCase(
    private val watchHistoryRepository: UserVideoWatchHistoryRepository,
    private val videoRepository: VideoRepository,
    private val currentUserService: CurrentUserService,
    private val videoAccessAssertService: VideoAccessAssertService
) {

    @Transactional
    fun save(videoUuid: String, command: WatchHistorySaveCommand) {
        val userId = currentUserService.getCurrentUserId()
            ?: throw BusinessException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED)

        val video = videoRepository.findByUuid(videoUuid) ?: throw BusinessException(
            "동영상을 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND
        )
        videoAccessAssertService.assertAccessible(video.contentPrivacy, video.channel?.contentPrivacy)

        val existing = watchHistoryRepository.findByUserIdAndVideoId(userId, video.id!!)
        if (existing != null) {
            // 기존 기록 업데이트
            existing.updatePosition(command.position)
        } else {
            // 새 기록 생성
            watchHistoryRepository.save(
                UserVideoWatchHistory(
                    userId = userId,
                    videoId = video.id!!,
                    lastPosition = command.position
                )
            )
        }
    }

}
