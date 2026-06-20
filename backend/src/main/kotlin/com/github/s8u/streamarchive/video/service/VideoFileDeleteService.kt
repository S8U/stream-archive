package com.github.s8u.streamarchive.video.service

import com.github.s8u.streamarchive.global.properties.StorageProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.util.Comparator

/**
 * 동영상 파일 삭제 서비스
 *
 * 동영상 디렉토리의 모든 파일을 삭제한다.
 */
@Service
class VideoFileDeleteService(
    private val storageProperties: StorageProperties
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun deleteFiles(videoId: Long) {
        val videoDir = storageProperties.getVideoPath(videoId)
        if (Files.exists(videoDir)) {
            try {
                Files.walk(videoDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach { Files.delete(it) }
                logger.info("VideoFileDeleteService: Video files deleted: videoId={}", videoId)
            } catch (e: Exception) {
                logger.error("VideoFileDeleteService: Failed to delete video files: videoId={}", videoId, e)
            }
        }
    }

}
