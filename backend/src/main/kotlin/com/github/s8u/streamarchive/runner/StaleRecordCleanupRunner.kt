package com.github.s8u.streamarchive.runner

import com.github.s8u.streamarchive.repository.RecordRepository
import com.github.s8u.streamarchive.service.RecordService
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class StaleRecordCleanupRunner(
    private val recordRepository: RecordRepository,
    private val recordService: RecordService
) : ApplicationRunner {
    private val logger = LoggerFactory.getLogger(StaleRecordCleanupRunner::class.java)

    override fun run(args: ApplicationArguments) {
        val staleRecords = recordRepository.findByIsEndedFalseAndIsCancelledFalse()

        if (staleRecords.isEmpty()) {
            logger.info("No stale records to clean up on startup")
            return
        }

        logger.info("Found {} stale records to clean up on startup", staleRecords.size)

        staleRecords.forEach { record ->
            val recordId = record.id
            if (recordId == null) {
                logger.warn("Skipping stale record without ID")
                return@forEach
            }

            try {
                recordService.endRecording(recordId)
                logger.info("Cleaned up stale record on startup: recordId={}", recordId)
            } catch (e: Exception) {
                logger.error("Failed to clean up stale record on startup: recordId={}", recordId, e)
            }
        }
    }
}
