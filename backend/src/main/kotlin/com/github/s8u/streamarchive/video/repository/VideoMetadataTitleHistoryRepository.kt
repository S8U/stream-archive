package com.github.s8u.streamarchive.video.repository

import com.github.s8u.streamarchive.video.entity.VideoMetadataTitleHistory
import org.springframework.data.jpa.repository.JpaRepository

interface VideoMetadataTitleHistoryRepository : JpaRepository<VideoMetadataTitleHistory, Long> {

    fun findTopByVideoIdAndOffsetMillisLessThanEqualOrderByOffsetMillisDesc(
        videoId: Long,
        offsetMillis: Long
    ): VideoMetadataTitleHistory?

}
