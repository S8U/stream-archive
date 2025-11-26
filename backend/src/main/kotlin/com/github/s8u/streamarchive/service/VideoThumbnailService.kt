package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.event.StreamDetectedEvent
import com.github.s8u.streamarchive.properties.StorageProperties
import com.github.s8u.streamarchive.repository.RecordRepository
import com.github.s8u.streamarchive.util.ImageDownloadUtil
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.StandardCopyOption

@Service
class VideoThumbnailService(
    private val storageProperties: StorageProperties,
    private val recordRepository: RecordRepository
) {
    private val logger = LoggerFactory.getLogger(VideoThumbnailService::class.java)

    fun saveThumbnail(thumbnailUrl: String?, videoId: Long) {
        if (thumbnailUrl == null) {
            logger.warn("Thumbnail URL is null for videoId: {}", videoId)
            return
        }

        try {
            val inputStream = ImageDownloadUtil.downloadImage(thumbnailUrl) ?: return

            val filePath = storageProperties.getVideoThumbnailPath(videoId)
            Files.createDirectories(filePath.parent)

            inputStream.use {
                Files.copy(it, filePath, StandardCopyOption.REPLACE_EXISTING)
            }

            logger.debug("Saved video thumbnail: videoId={}, url={}", videoId, thumbnailUrl)
        } catch (e: Exception) {
            logger.error("Failed to save thumbnail for videoId: {}, url: {}", videoId, thumbnailUrl, e)
        }
    }

    @Async
    @EventListener
    fun updateThumbnailForActiveRecording(event: StreamDetectedEvent) {
        try {
            val record = recordRepository.findByPlatformTypeAndPlatformStreamIdAndIsEndedAndIsCancelled(
                platformType = event.stream.platformType,
                platformStreamId = event.stream.id,
                isEnded = false,
                isCancelled = false
            )

            if (record != null) {
                saveThumbnail(event.stream.thumbnailUrl, record.videoId)
                logger.debug(
                    "Updated thumbnail for active recording: recordId={}, videoId={}, streamId={}",
                    record.id,
                    record.videoId,
                    event.stream.id
                )
            }
        } catch (e: Exception) {
            logger.error(
                "Failed to update thumbnail for active recording: platformType={}, streamId={}",
                event.stream.platformType,
                event.stream.id,
                e
            )
        }
    }
}
