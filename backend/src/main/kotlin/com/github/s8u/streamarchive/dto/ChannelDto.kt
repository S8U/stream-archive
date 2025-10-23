package com.github.s8u.streamarchive.dto

import com.github.s8u.streamarchive.entity.Channel
import com.github.s8u.streamarchive.enums.ContentPrivacy
import java.time.LocalDateTime

data class ChannelCreateRequest(
    val name: String,
    val contentPrivacy: ContentPrivacy
)

data class ChannelUpdateRequest(
    val name: String?,
    val contentPrivacy: ContentPrivacy?,
    val isActive: Boolean?
)

data class ChannelResponse(
    val id: Long,
    val uuid: String,
    val name: String,
    val contentPrivacy: ContentPrivacy,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(channel: Channel): ChannelResponse {
            return ChannelResponse(
                id = channel.id!!,
                uuid = channel.uuid,
                name = channel.name,
                contentPrivacy = channel.contentPrivacy,
                isActive = channel.isActive,
                createdAt = channel.createdAt,
                updatedAt = channel.updatedAt
            )
        }
    }
}
