package com.github.s8u.streamarchive.dto

import com.github.s8u.streamarchive.entity.ChannelPlatform
import com.github.s8u.streamarchive.enums.PlatformType
import java.time.LocalDateTime

data class AdminChannelPlatformCreateRequest(
    val channelId: Long,
    val platformType: PlatformType,
    val platformChannelId: String,
    val isSyncProfile: Boolean = true
)

data class AdminChannelPlatformUpdateRequest(
    val isSyncProfile: Boolean?
)

data class AdminChannelPlatformSearchRequest(
    val channelName: String? = null,
    val platformType: PlatformType? = null,
    val platformChannelId: String? = null,
    val isSyncProfile: Boolean? = null
)

data class AdminChannelPlatformResponse(
    val id: Long,
    val channelId: Long,
    val platformType: PlatformType,
    val platformChannelId: String,
    val isSyncProfile: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(channelPlatform: ChannelPlatform): AdminChannelPlatformResponse {
            return AdminChannelPlatformResponse(
                id = channelPlatform.id!!,
                channelId = channelPlatform.channelId,
                platformType = channelPlatform.platformType,
                platformChannelId = channelPlatform.platformChannelId,
                isSyncProfile = channelPlatform.isSyncProfile,
                createdAt = channelPlatform.createdAt,
                updatedAt = channelPlatform.updatedAt
            )
        }
    }
}
