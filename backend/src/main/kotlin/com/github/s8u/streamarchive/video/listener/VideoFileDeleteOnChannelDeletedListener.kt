package com.github.s8u.streamarchive.video.listener

import com.github.s8u.streamarchive.channel.event.ChannelDeletedEvent
import com.github.s8u.streamarchive.video.service.VideoFileDeleteService
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * 채널이 삭제되면 그 채널의 동영상 파일을 삭제하는 리스너
 */
@Component
class VideoFileDeleteOnChannelDeletedListener(
    private val videoFileDeleteService: VideoFileDeleteService
) {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: ChannelDeletedEvent) {
        event.videoIds.forEach { videoId ->
            videoFileDeleteService.deleteFiles(videoId)
        }
    }

}
