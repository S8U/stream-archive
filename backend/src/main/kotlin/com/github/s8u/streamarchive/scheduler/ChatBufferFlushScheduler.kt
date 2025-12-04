package com.github.s8u.streamarchive.scheduler

import com.github.s8u.streamarchive.service.VideoDataChatHistoryService
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@Profile("!test")
class ChatBufferFlushScheduler(
    private val videoDataChatHistoryService: VideoDataChatHistoryService
) {
    @Scheduled(fixedRate = 1000)
    fun flushChatBuffer() {
        videoDataChatHistoryService.flush()
    }
}