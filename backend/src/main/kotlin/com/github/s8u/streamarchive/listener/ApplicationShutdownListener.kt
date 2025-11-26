package com.github.s8u.streamarchive.listener

import com.github.s8u.streamarchive.recorder.RecordProcessManager
import com.github.s8u.streamarchive.service.RecordService
import org.slf4j.LoggerFactory
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class ApplicationShutdownListener(
    private val recordProcessManager: RecordProcessManager,
    private val recordService: RecordService
) {
    private val logger = LoggerFactory.getLogger(ApplicationShutdownListener::class.java)

    @EventListener(ContextClosedEvent::class)
    fun onApplicationShutdown() {
        logger.info("Application shutdown initiated, cleaning up recording processes")

        recordProcessManager.getActiveRecordIds().forEach { recordId ->
            try {
                recordService.endRecording(recordId)
            } catch (e: Exception) {
                logger.error("Failed to clean up recording: recordId={}", recordId, e)
            }
        }

        logger.info("Shutdown cleanup completed")
    }
}
