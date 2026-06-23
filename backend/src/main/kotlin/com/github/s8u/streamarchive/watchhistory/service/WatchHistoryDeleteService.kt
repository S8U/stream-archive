package com.github.s8u.streamarchive.watchhistory.service

import com.github.s8u.streamarchive.watchhistory.repository.UserVideoWatchHistoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 동영상 시청 기록 삭제 서비스
 *
 * 동영상이 삭제될 때 그 동영상을 가리키는 모든 사용자의 시청 기록을 삭제한다.
 */
@Service
class WatchHistoryDeleteService(
    private val watchHistoryRepository: UserVideoWatchHistoryRepository
) {

    /**
     * 해당 동영상의 시청 기록을 모두 삭제한다.
     */
    @Transactional
    fun deleteByVideoId(videoId: Long) {
        watchHistoryRepository.deleteAllByVideoId(videoId)
    }

}
