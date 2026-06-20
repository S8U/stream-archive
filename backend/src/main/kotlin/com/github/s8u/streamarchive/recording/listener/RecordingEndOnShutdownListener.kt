package com.github.s8u.streamarchive.recording.listener

import com.github.s8u.streamarchive.recording.manager.RecordingVideoProcessManager
import com.github.s8u.streamarchive.recording.usecase.RecordingEndUseCase
import org.slf4j.LoggerFactory
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

/**
 * 애플리케이션 종료 시 녹화를 종료하는 리스너
 *
 * 애플리케이션이 내려갈 때 진행 중인 녹화를 모두 종료 처리한다.
 */
@Component
class RecordingEndOnShutdownListener(
    private val recordingVideoProcessManager: RecordingVideoProcessManager,
    private val recordingEndUseCase: RecordingEndUseCase
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @EventListener(ContextClosedEvent::class)
    fun onApplicationShutdown() {
        logger.info("RecordingEndOnShutdownListener: Application shutdown initiated, cleaning up recording processes")

        recordingVideoProcessManager.getActiveRecordIds().forEach { recordId ->
            try {
                recordingEndUseCase.end(recordId)
            } catch (e: Exception) {
                logger.error("RecordingEndOnShutdownListener: Failed to clean up recording: recordId={}", recordId, e)
            }
        }

        logger.info("RecordingEndOnShutdownListener: Shutdown cleanup completed")
    }

}
