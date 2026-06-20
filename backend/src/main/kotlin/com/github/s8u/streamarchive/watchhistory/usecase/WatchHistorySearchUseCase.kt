package com.github.s8u.streamarchive.watchhistory.usecase

import com.github.s8u.streamarchive.auth.security.service.CurrentUserService
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.global.service.UrlService
import com.github.s8u.streamarchive.video.repository.VideoRepository
import com.github.s8u.streamarchive.watchhistory.repository.UserVideoWatchHistoryRepository
import com.github.s8u.streamarchive.watchhistory.usecase.dto.result.WatchHistorySearchResult
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 동영상 시청 기록 목록 조회
 */
@Service
class WatchHistorySearchUseCase(
    private val watchHistoryRepository: UserVideoWatchHistoryRepository,
    private val videoRepository: VideoRepository,
    private val currentUserService: CurrentUserService,
    private val urlService: UrlService
) {

    @Transactional(readOnly = true)
    fun search(pageable: Pageable): Page<WatchHistorySearchResult> {
        val userId = currentUserService.getCurrentUserId()
            ?: throw BusinessException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED)

        val histories = watchHistoryRepository.findByUserIdOrderByWatchedAtDesc(userId, pageable)

        // 페이지 내 동영상들을 채널과 함께 한 번에 조회 (N+1 방지)
        val videosById = videoRepository.findAllByIdInWithChannel(histories.content.map { it.videoId })
            .associateBy { it.id }

        return histories.map { history ->
            val video = videosById[history.videoId]
                ?: throw BusinessException("동영상을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)

            WatchHistorySearchResult.from(
                history = history,
                video = video,
                channelProfileUrl = urlService.channelProfileUrl(video.channel?.uuid!!),
                videoThumbnailUrl = urlService.videoThumbnailUrl(video.uuid)
            )
        }
    }

}
