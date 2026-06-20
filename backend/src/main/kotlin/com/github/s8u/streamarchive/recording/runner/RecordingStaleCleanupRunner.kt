package com.github.s8u.streamarchive.recording.runner

import com.github.s8u.streamarchive.record.repository.RecordRepository
import com.github.s8u.streamarchive.recording.usecase.RecordingEndUseCase
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/**
 * 부팅 시 미종료 녹화 정리 러너
 *
 * 애플리케이션이 종료될 때 미처 끝내지 못한 녹화를 다음 부팅에서 종료 처리한다.
 */
@Component
@Profile("!test")
class RecordingStaleCleanupRunner(
    private val recordRepository: RecordRepository,
    private val recordingEndUseCase: RecordingEndUseCase
) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments) {
        val staleRecords = recordRepository.findByIsEndedFalseAndIsCancelledFalse()

        if (staleRecords.isEmpty()) {
            logger.info("RecordingStaleCleanupRunner: No stale records to clean up on startup")
            return
        }

        logger.info("RecordingStaleCleanupRunner: Found {} stale records to clean up on startup", staleRecords.size)

        staleRecords.forEach { record ->
            val recordId = record.id
            if (recordId == null) {
                logger.warn("RecordingStaleCleanupRunner: Skipping stale record without ID")
                return@forEach
            }

            try {
                recordingEndUseCase.end(recordId)
                logger.info("RecordingStaleCleanupRunner: Cleaned up stale record on startup: recordId={}", recordId)
            } catch (e: Exception) {
                logger.error("RecordingStaleCleanupRunner: Failed to clean up stale record on startup: recordId={}", recordId, e)
            }
        }
    }

}
