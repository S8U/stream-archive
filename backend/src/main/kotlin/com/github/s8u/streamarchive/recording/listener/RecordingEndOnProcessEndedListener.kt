package com.github.s8u.streamarchive.recording.listener

import com.github.s8u.streamarchive.recording.event.RecordingProcessEndedEvent
import com.github.s8u.streamarchive.recording.usecase.RecordingEndUseCase
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

/**
 * 녹화 프로세스 종료 시 녹화를 종료 처리하는 리스너
 */
@Component
class RecordingEndOnProcessEndedListener(
    private val recordingEndUseCase: RecordingEndUseCase
) {

    @EventListener
    fun handle(event: RecordingProcessEndedEvent) {
        recordingEndUseCase.end(event.recordId, isCancel = false)
    }

}
