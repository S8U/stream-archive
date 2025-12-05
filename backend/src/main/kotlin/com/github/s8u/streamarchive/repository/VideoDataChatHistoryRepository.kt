package com.github.s8u.streamarchive.repository

import com.github.s8u.streamarchive.entity.VideoDataChatHistory
import org.springframework.data.jpa.repository.JpaRepository

interface VideoDataChatHistoryRepository : JpaRepository<VideoDataChatHistory, Long> {
    fun findByVideoIdAndOffsetMillisGreaterThanEqualAndOffsetMillisLessThanOrderByOffsetMillisAsc(
        videoId: Long,
        startOffset: Long,
        endOffset: Long
    ): List<VideoDataChatHistory>
}