package com.github.s8u.streamarchive.repository

import com.github.s8u.streamarchive.entity.VideoMetadataCategoryHistory
import org.springframework.data.jpa.repository.JpaRepository

interface VideoMetadataCategoryHistoryRepository : JpaRepository<VideoMetadataCategoryHistory, Long> {
}