package com.github.s8u.streamarchive.dto

import com.github.s8u.streamarchive.entity.ChannelPlatform
import com.github.s8u.streamarchive.enums.PlatformType
import java.time.LocalDateTime

data class ChannelPlatformCreateRequest(
    val channelId: Long,
    val platformType: PlatformType,
    val platformChannelId: String,
    val isSyncProfile: Boolean = true
)

data class ChannelPlatformUpdateRequest(
    val isSyncProfile: Boolean?,
    val isActive: Boolean?
)

data class ChannelPlatformResponse(
    val id: Long,
    val channelId: Long,
    val platformType: PlatformType,
    val platformChannelId: String,
    val isSyncProfile: Boolean,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(channelPlatform: ChannelPlatform): ChannelPlatformResponse {
            return ChannelPlatformResponse(
                id = channelPlatform.id!!,
                channelId = channelPlatform.channelId,
                platformType = channelPlatform.platformType,
                platformChannelId = channelPlatform.platformChannelId,
                isSyncProfile = channelPlatform.isSyncProfile,
                isActive = channelPlatform.isActive,
                createdAt = channelPlatform.createdAt,
                updatedAt = channelPlatform.updatedAt
            )
        }
    }
}
