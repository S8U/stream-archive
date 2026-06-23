package com.github.s8u.streamarchive.watchhistory.listener

import com.github.s8u.streamarchive.video.event.VideoDeletedEvent
import com.github.s8u.streamarchive.watchhistory.service.WatchHistoryDeleteService
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * 동영상이 삭제되면 그 동영상의 시청 기록을 삭제하는 리스너
 */
@Component
class WatchHistoryDeleteOnVideoDeletedListener(
    private val watchHistoryDeleteService: WatchHistoryDeleteService
) {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: VideoDeletedEvent) {
        watchHistoryDeleteService.deleteByVideoId(event.videoId)
    }

}
