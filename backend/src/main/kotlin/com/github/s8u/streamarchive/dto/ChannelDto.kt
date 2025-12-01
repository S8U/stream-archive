package com.github.s8u.streamarchive.dto

import com.github.s8u.streamarchive.entity.Channel
import com.github.s8u.streamarchive.enums.ContentPrivacy
import java.time.LocalDateTime

data class AdminChannelCreateRequest(
    val name: String,
    val contentPrivacy: ContentPrivacy
)

data class AdminChannelUpdateRequest(
    val name: String?,
    val contentPrivacy: ContentPrivacy?
)

data class AdminChannelSearchRequest(
    val id: Long? = null,
    val uuid: String? = null,
    val name: String? = null,
    val contentPrivacy: ContentPrivacy? = null
)

data class AdminChannelResponse(
    val id: Long,
    val uuid: String,
    val name: String,
    val profileUrl: String,
    val contentPrivacy: ContentPrivacy,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(channel: Channel, profileUrl: String): AdminChannelResponse {
            return AdminChannelResponse(
                id = channel.id!!,
                uuid = channel.uuid,
                name = channel.name,
                profileUrl = profileUrl,
                contentPrivacy = channel.contentPrivacy,
                createdAt = channel.createdAt,
                updatedAt = channel.updatedAt
            )
        }
    }
}

data class PublicChannelSearchRequest(
    val name: String? = null
)

data class PublicChannelResponse(
    val uuid: String,
    val name: String,
    val profileUrl: String,
    val contentPrivacy: ContentPrivacy,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(channel: Channel, profileUrl: String): PublicChannelResponse {
            return PublicChannelResponse(
                uuid = channel.uuid,
                name = channel.name,
                profileUrl = profileUrl,
                contentPrivacy = channel.contentPrivacy,
                createdAt = channel.createdAt,
                updatedAt = channel.updatedAt
            )
        }
    }
}