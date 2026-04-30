package com.github.s8u.streamarchive.repository

import com.github.s8u.streamarchive.entity.VideoMetadataTitleHistory
import org.springframework.data.jpa.repository.JpaRepository

interface VideoMetadataTitleHistoryRepository : JpaRepository<VideoMetadataTitleHistory, Long> {
    fun findTopByVideoIdAndOffsetMillisLessThanEqualOrderByOffsetMillisDesc(
        videoId: Long,
        offsetMillis: Long
    ): VideoMetadataTitleHistory?
}