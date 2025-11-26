package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.properties.StorageProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.nio.file.Files
import kotlin.io.path.isRegularFile

@Service
class VideoMetadataService(
    private val storageProperties: StorageProperties
) {
    private val logger = LoggerFactory.getLogger(VideoMetadataService::class.java)

    /**
     * 동영상 디렉토리의 전체 파일 크기 계산 (바이트)
     */
    fun calculateFileSize(videoId: Long): Long {
        val videoPath = storageProperties.getVideoPath(videoId)

        if (!Files.exists(videoPath)) {
            logger.warn("Video directory not found: videoId={}", videoId)
            return 0L
        }

        return try {
            Files.walk(videoPath)
                .filter { it.isRegularFile() }
                .mapToLong { Files.size(it) }
                .sum()
        } catch (e: Exception) {
            logger.error("Failed to calculate file size: videoId={}", videoId, e)
            0L
        }
    }

    /**
     * M3U8 플레이리스트 파일을 파싱하여 총 재생 시간 계산 (초)
     */
    fun calculateDuration(videoId: Long): Int {
        val playlistPath = storageProperties.getVideoPlaylistPath(videoId)

        if (!Files.exists(playlistPath)) {
            logger.warn("Playlist file not found: videoId={}", videoId)
            return 0
        }

        return try {
            val totalDuration = Files.readAllLines(playlistPath)
                .filter { it.startsWith("#EXTINF:") }
                .mapNotNull { line ->
                    // #EXTINF:3.000, 형식에서 duration 추출
                    val durationStr = line.substring(8).split(",")[0].trim()
                    durationStr.toDoubleOrNull()
                }
                .sum()

            totalDuration.toInt()
        } catch (e: Exception) {
            logger.error("Failed to calculate duration: videoId={}", videoId, e)
            0
        }
    }
}
