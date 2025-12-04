package com.github.s8u.streamarchive.repository

import com.github.s8u.streamarchive.entity.VideoDataChatHistory
import org.springframework.data.jpa.repository.JpaRepository

interface VideoMetadataChatHistoryRepository : JpaRepository<VideoDataChatHistory, Long> {
}