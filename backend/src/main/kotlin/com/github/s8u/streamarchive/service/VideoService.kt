package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.dto.AdminVideoResponse
import com.github.s8u.streamarchive.dto.AdminVideoSearchRequest
import com.github.s8u.streamarchive.dto.AdminVideoUpdateRequest
import com.github.s8u.streamarchive.entity.Video
import com.github.s8u.streamarchive.exception.BusinessException
import com.github.s8u.streamarchive.properties.StorageProperties
import com.github.s8u.streamarchive.repository.VideoRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.file.Files
import java.time.LocalDateTime
import java.util.Comparator

@Service
class VideoService(
    private val videoRepository: VideoRepository,
    private val storageProperties: StorageProperties
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional(readOnly = true)
    fun searchForAdmin(request: AdminVideoSearchRequest, pageable: Pageable): Page<AdminVideoResponse> {
        return videoRepository.searchForAdmin(request, pageable)
            .map { AdminVideoResponse.from(it) }
    }

    @Transactional(readOnly = true)
    fun getForAdmin(id: Long): AdminVideoResponse {
        val video = videoRepository.findById(id).orElseThrow {
            BusinessException("동영상을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }
        return AdminVideoResponse.from(video)
    }

    @Transactional
    fun updateForAdmin(id: Long, request: AdminVideoUpdateRequest): AdminVideoResponse {
        val video = videoRepository.findById(id).orElseThrow {
            BusinessException("동영상을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        request.title?.let { video.title = it }
        request.contentPrivacy?.let { video.contentPrivacy = it }

        return AdminVideoResponse.from(video)
    }

    @Transactional
    fun delete(id: Long) {
        val video = videoRepository.findById(id).orElseThrow {
            BusinessException("동영상을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        deleteVideoFiles(video)

        video.isActive = false
        video.deletedAt = LocalDateTime.now()
    }

    @Transactional
    fun deleteAllByChannelId(channelId: Long) {
        val videos = videoRepository.findByChannelId(channelId)

        videos.forEach { video ->
            deleteVideoFiles(video)

            video.isActive = false
            video.deletedAt = LocalDateTime.now()
        }
    }

    private fun deleteVideoFiles(video: Video) {
        val videoDir = storageProperties.videosPath.resolve(video.uuid)
        if (Files.exists(videoDir)) {
            try {
                Files.walk(videoDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach { Files.delete(it) }
                logger.info("Video files deleted: videoId={}, uuid={}", video.id, video.uuid)
            } catch (e: Exception) {
                logger.error("Failed to delete video files: videoId={}, uuid={}", video.id, video.uuid, e)
            }
        }
    }

}