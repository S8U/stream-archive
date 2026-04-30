package com.github.s8u.streamarchive.repository

import com.github.s8u.streamarchive.entity.VideoMetadataViewerHistory
import org.springframework.data.jpa.repository.JpaRepository

interface VideoMetadataViewerHistoryRepository : JpaRepository<VideoMetadataViewerHistory, Long> {
    fun findTopByVideoIdOrderByViewerCountDescOffsetMillisAsc(videoId: Long): VideoMetadataViewerHistory?
}