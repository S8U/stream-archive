package com.github.s8u.streamarchive.watchhistory.usecase.dto.result

import com.github.s8u.streamarchive.watchhistory.entity.UserVideoWatchHistory
import java.time.LocalDateTime

/**
 * 개별 동영상 시청 기록 조회 결과
 */
data class WatchHistoryGetResult(
    val lastPosition: Int,
    val watchedAt: LocalDateTime
) {

    companion object {
        fun from(history: UserVideoWatchHistory): WatchHistoryGetResult {
            return WatchHistoryGetResult(
                lastPosition = history.lastPosition,
                watchedAt = history.watchedAt
            )
        }
    }
}
