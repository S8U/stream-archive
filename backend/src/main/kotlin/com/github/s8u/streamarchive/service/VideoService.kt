package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.entity.Video
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class VideoService(
    private val videoRepository: com.github.s8u.streamarchive.repository.VideoRepository
) {
    @Transactional
    fun createVideo(video: Video): Video {
        return videoRepository.save(video)
    }

    @Transactional(readOnly = true)
    fun getVideoById(videoId: Long): Video? {
        return videoRepository.findById(videoId).orElse(null)
    }

    @Transactional
    fun deleteVideo(videoId: Long) {
        val video = videoRepository.findById(videoId).orElse(null)
        if (video != null) {
            video.isActive = false
            video.deletedAt = LocalDateTime.now()
            videoRepository.save(video)
        }
    }
}