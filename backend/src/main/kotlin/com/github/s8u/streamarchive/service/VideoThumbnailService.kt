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
import java.util.concurrent.ConcurrentHashMap

@Service
class VideoThumbnailService(
    private val storageProperties: StorageProperties,
    private val recordRepository: RecordRepository
) {
    private val logger = LoggerFactory.getLogger(VideoThumbnailService::class.java)

    // recordId -> 녹화 중 관측된 최대 시청자 수
    private val peakViewerCache = ConcurrentHashMap<Long, Int>()

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
            ) ?: return

            saveThumbnail(event.stream.thumbnailUrl, record.videoId)
            logger.debug(
                "Updated thumbnail for active recording: recordId={}, videoId={}, streamId={}",
                record.id,
                record.videoId,
                event.stream.id
            )

            // 새로운 최고 시청자 수 갱신 시점이면 thumbnail-peak.png 도 함께 보존
            updatePeakThumbnailIfNewMax(record.id!!, record.videoId, event.stream.viewerCount)
        } catch (e: Exception) {
            logger.error(
                "Failed to update thumbnail for active recording: platformType={}, streamId={}",
                event.stream.platformType,
                event.stream.id,
                e
            )
        }
    }

    private fun updatePeakThumbnailIfNewMax(recordId: Long, videoId: Long, viewerCount: Int?) {
        if (viewerCount == null) return

        val newPeak = peakViewerCache.compute(recordId) { _, prev ->
            if (prev == null || viewerCount > prev) viewerCount else prev
        }
        if (newPeak != viewerCount) return

        try {
            val src = storageProperties.getVideoThumbnailPath(videoId)
            if (!Files.exists(src)) return
            val dst = storageProperties.getVideoPeakThumbnailPath(videoId)
            Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING)
            logger.debug(
                "Saved peak thumbnail: recordId={}, videoId={}, peakViewer={}",
                recordId, videoId, viewerCount
            )
        } catch (e: Exception) {
            logger.error(
                "Failed to save peak thumbnail: recordId={}, videoId={}, peakViewer={}",
                recordId, videoId, viewerCount, e
            )
        }
    }

    fun applyPeakThumbnail(videoId: Long) {
        val peak = storageProperties.getVideoPeakThumbnailPath(videoId)
        if (!Files.exists(peak)) {
            return
        }
        val main = storageProperties.getVideoThumbnailPath(videoId)
        try {
            Files.move(peak, main, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        } catch (e: Exception) {
            // ATOMIC_MOVE 가 파일시스템에서 지원되지 않을 수 있으므로 폴백
            Files.move(peak, main, StandardCopyOption.REPLACE_EXISTING)
        }
        logger.info("Applied peak thumbnail: videoId={}", videoId)
    }

    fun clearPeakCache(recordId: Long) {
        peakViewerCache.remove(recordId)
    }
}
