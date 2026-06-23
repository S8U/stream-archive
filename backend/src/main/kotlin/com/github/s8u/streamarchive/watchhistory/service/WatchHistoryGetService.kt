package com.github.s8u.streamarchive.watchhistory.service

import com.github.s8u.streamarchive.auth.security.service.CurrentUserService
import com.github.s8u.streamarchive.watchhistory.repository.UserVideoWatchHistoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 동영상 시청 기록 조회 서비스
 *
 * 다른 도메인(동영상 목록 등)이 현재 사용자의 시청 위치를 함께 보여줄 때 쓴다.
 */
@Service
class WatchHistoryGetService(
    private val watchHistoryRepository: UserVideoWatchHistoryRepository,
    private val currentUserService: CurrentUserService
) {

    /**
     * 현재 로그인 사용자의 동영상별 마지막 재생 위치(초)를 조회한다.
     *
     * 비로그인이거나 시청 기록이 없으면 빈 맵을 돌려준다.
     */
    @Transactional(readOnly = true)
    fun findLastPositionsByVideoIds(videoIds: Collection<Long>): Map<Long, Int> {
        val userId = currentUserService.getCurrentUserId() ?: return emptyMap()
        if (videoIds.isEmpty()) {
            return emptyMap()
        }

        return watchHistoryRepository.findByUserIdAndVideoIdIn(userId, videoIds)
            .associate { it.videoId to it.lastPosition }
    }

}
