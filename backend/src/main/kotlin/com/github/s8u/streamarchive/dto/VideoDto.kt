package com.github.s8u.streamarchive.dto

import com.github.s8u.streamarchive.entity.Video
import com.github.s8u.streamarchive.enums.ContentPrivacy
import java.time.LocalDateTime

data class AdminVideoSearchRequest(
    val title: String? = null,
    val channelName: String? = null,
    val contentPrivacy: ContentPrivacy? = null,
    val createdAtFrom: LocalDateTime? = null,
    val createdAtTo: LocalDateTime? = null
)

data class AdminVideoUpdateRequest(
    val title: String?,
    val contentPrivacy: ContentPrivacy?
)

data class AdminVideoResponse(
    val id: Long,
    val uuid: String,
    val channelId: Long,
    val title: String,
    val duration: Int,
    val fileSize: Long,
    val contentPrivacy: ContentPrivacy,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(video: Video): AdminVideoResponse {
            return AdminVideoResponse(
                id = video.id!!,
                uuid = video.uuid,
                channelId = video.channelId,
                title = video.title,
                duration = video.duration,
                fileSize = video.fileSize,
                contentPrivacy = video.contentPrivacy,
                createdAt = video.createdAt,
                updatedAt = video.updatedAt
            )
        }
    }
}
