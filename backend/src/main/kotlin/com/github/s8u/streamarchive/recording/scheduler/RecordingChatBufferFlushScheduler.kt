package com.github.s8u.streamarchive.recording.scheduler

import com.github.s8u.streamarchive.recording.usecase.RecordingChatFlushUseCase
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * 채팅 버퍼 플러시 스케줄러
 *
 * 고빈도로 도는 경로라 시작/끝 로그는 남기지 않는다.
 */
@Component
@Profile("!test")
class RecordingChatBufferFlushScheduler(
    private val recordingChatFlushUseCase: RecordingChatFlushUseCase
) {

    @Scheduled(fixedRate = 1000)
    fun flushChatBuffer() {
        recordingChatFlushUseCase.flush()
    }

}
