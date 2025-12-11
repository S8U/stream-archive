package com.github.s8u.streamarchive.scheduler

import com.github.s8u.streamarchive.recorder.RecordProcessManager
import com.github.s8u.streamarchive.repository.RecordRepository
import com.github.s8u.streamarchive.repository.VideoRepository
import com.github.s8u.streamarchive.service.VideoMetadataService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 녹화 중인 동영상의 용량과 길이를 주기적으로 업데이트하는 스케줄러
 */
@Component
@Profile("!test")
class RecordingMetadataUpdateScheduler(
    private val recordProcessManager: RecordProcessManager,
    private val recordRepository: RecordRepository,
    private val videoRepository: VideoRepository,
    private val videoMetadataService: VideoMetadataService
) {
    private val logger = LoggerFactory.getLogger(RecordingMetadataUpdateScheduler::class.java)

    /**
     * 10초마다 활성 녹화의 메타데이터 업데이트
     */
    @Scheduled(fixedRate = 10000)
    @Transactional
    fun updateActiveRecordingMetadata() {
        val activeRecordIds = recordProcessManager.getActiveRecordIds()
        if (activeRecordIds.isEmpty()) {
            return
        }

        logger.debug("Updating metadata for {} active recordings", activeRecordIds.size)

        for (recordId in activeRecordIds) {
            try {
                val record = recordRepository.findById(recordId).orElse(null) ?: continue
                val video = videoRepository.findById(record.videoId).orElse(null) ?: continue

                val newFileSize = videoMetadataService.calculateFileSize(video.id!!)
                val newDuration = videoMetadataService.calculateDuration(video.id!!)

                video.fileSize = newFileSize
                video.duration = newDuration
                videoRepository.save(video)

                logger.debug(
                    "Updated metadata for active recording: recordId={}, videoId={}, fileSize={}, duration={}",
                    recordId,
                    video.id,
                    newFileSize,
                    newDuration
                )
            } catch (e: Exception) {
                logger.error("Failed to update metadata for active recording: recordId={}", recordId, e)
            }
        }
    }
}
