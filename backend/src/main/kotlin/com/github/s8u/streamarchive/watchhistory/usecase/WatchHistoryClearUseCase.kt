package com.github.s8u.streamarchive.watchhistory.usecase

import com.github.s8u.streamarchive.auth.security.service.CurrentUserService
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.watchhistory.repository.UserVideoWatchHistoryRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 동영상 시청 기록 전체 삭제
 */
@Service
class WatchHistoryClearUseCase(
    private val watchHistoryRepository: UserVideoWatchHistoryRepository,
    private val currentUserService: CurrentUserService
) {

    @Transactional
    fun clear() {
        val userId = currentUserService.getCurrentUserId()
            ?: throw BusinessException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED)

        watchHistoryRepository.deleteAllByUserId(userId)
    }

}
