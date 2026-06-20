package com.github.s8u.streamarchive.video.service

import com.github.s8u.streamarchive.global.properties.StorageProperties
import com.github.s8u.streamarchive.global.util.ImageDownloadUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/**
 * 동영상 썸네일 저장 서비스
 */
@Service
class VideoThumbnailSaveService(
    private val storageProperties: StorageProperties
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun saveThumbnail(thumbnailUrl: String?, videoId: Long) {
        saveThumbnailToPath(thumbnailUrl, videoId, storageProperties.getVideoThumbnailPath(videoId))
    }

    fun savePeakThumbnail(thumbnailUrl: String?, videoId: Long) {
        saveThumbnailToPath(thumbnailUrl, videoId, storageProperties.getVideoPeakThumbnailPath(videoId))
    }

    /**
     * 피크 시점 썸네일을 대표 썸네일로 적용한다.
     *
     * 피크 썸네일이 없으면 아무것도 하지 않는다.
     */
    fun applyPeakThumbnail(videoId: Long) {
        val peakThumbnailPath = storageProperties.getVideoPeakThumbnailPath(videoId)
        if (!Files.exists(peakThumbnailPath)) {
            logger.debug("VideoThumbnailSaveService: Peak thumbnail does not exist: videoId={}", videoId)
            return
        }

        val thumbnailPath = storageProperties.getVideoThumbnailPath(videoId)
        Files.createDirectories(thumbnailPath.parent)
        Files.copy(peakThumbnailPath, thumbnailPath, StandardCopyOption.REPLACE_EXISTING)
        Files.deleteIfExists(peakThumbnailPath)

        logger.info("VideoThumbnailSaveService: Applied peak thumbnail: videoId={}", videoId)
    }

    private fun saveThumbnailToPath(thumbnailUrl: String?, videoId: Long, filePath: Path) {
        if (thumbnailUrl == null) {
            logger.warn("VideoThumbnailSaveService: Thumbnail URL is null for videoId: {}", videoId)
            return
        }

        try {
            val inputStream = ImageDownloadUtils.downloadImage(thumbnailUrl) ?: return

            Files.createDirectories(filePath.parent)

            inputStream.use {
                Files.copy(it, filePath, StandardCopyOption.REPLACE_EXISTING)
            }

            logger.debug("VideoThumbnailSaveService: Saved video thumbnail: videoId={}, url={}", videoId, thumbnailUrl)
        } catch (e: Exception) {
            logger.error("VideoThumbnailSaveService: Failed to save thumbnail for videoId: {}, url: {}", videoId, thumbnailUrl, e)
        }
    }

}
