package com.github.s8u.streamarchive.video.repository

import com.github.s8u.streamarchive.video.entity.VideoMetadataCategoryHistory
import org.springframework.data.jpa.repository.JpaRepository

interface VideoMetadataCategoryHistoryRepository : JpaRepository<VideoMetadataCategoryHistory, Long> {

    fun findByVideoIdOrderByOffsetMillisAsc(videoId: Long): List<VideoMetadataCategoryHistory>

}
