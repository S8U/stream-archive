package com.github.s8u.streamarchive.video.repository

import com.github.s8u.streamarchive.video.entity.VideoChatHistory
import org.springframework.data.jpa.repository.JpaRepository

interface VideoChatHistoryRepository : JpaRepository<VideoChatHistory, Long> {

    fun findByVideoIdAndOffsetMillisGreaterThanEqualAndOffsetMillisLessThanOrderByOffsetMillisAsc(
        videoId: Long,
        startOffset: Long,
        endOffset: Long
    ): List<VideoChatHistory>

}
