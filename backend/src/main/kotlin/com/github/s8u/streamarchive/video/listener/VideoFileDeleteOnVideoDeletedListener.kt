package com.github.s8u.streamarchive.video.listener

import com.github.s8u.streamarchive.video.event.VideoDeletedEvent
import com.github.s8u.streamarchive.video.service.VideoFileDeleteService
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * 동영상이 삭제되면 그 파일을 삭제하는 리스너
 */
@Component
class VideoFileDeleteOnVideoDeletedListener(
    private val videoFileDeleteService: VideoFileDeleteService
) {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: VideoDeletedEvent) {
        videoFileDeleteService.deleteFiles(event.videoId)
    }

}
