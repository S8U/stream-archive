package com.github.s8u.streamarchive.recording.listener

import com.github.s8u.streamarchive.detect.event.StreamDetectedEvent
import com.github.s8u.streamarchive.record.repository.RecordRepository
import com.github.s8u.streamarchive.video.service.VideoThumbnailSaveService
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

/**
 * 녹화 중인 동영상의 썸네일을 갱신하는 리스너 (스트리밍 감지 시)
 */
@Component
class RecordingVideoThumbnailSaveListener(
    private val recordRepository: RecordRepository,
    private val videoThumbnailSaveService: VideoThumbnailSaveService
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Async
    @EventListener
    fun handle(event: StreamDetectedEvent) {
        try {
            val record = recordRepository.findByPlatformTypeAndPlatformStreamIdAndIsEndedAndIsCancelled(
                platformType = event.stream.platformType,
                platformStreamId = event.stream.id,
                isEnded = false,
                isCancelled = false
            )

            if (record != null) {
                videoThumbnailSaveService.saveThumbnail(event.stream.thumbnailUrl, record.videoId)
                logger.debug(
                    "RecordingVideoThumbnailSaveListener: Updated thumbnail for active recording: recordId={}, videoId={}, streamId={}",
                    record.id,
                    record.videoId,
                    event.stream.id
                )
            }
        } catch (e: Exception) {
            logger.error(
                "RecordingVideoThumbnailSaveListener: Failed to update thumbnail for active recording: platformType={}, streamId={}",
                event.stream.platformType,
                event.stream.id,
                e
            )
        }
    }

}
