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
    val isSyncProfile: Boolean?,
    val platformChannelId: String?
)

data class AdminChannelPlatformSearchRequest(
    val id: Long? = null,
    val channelName: String? = null,
    val platformType: PlatformType? = null,
    val platformChannelId: String? = null,
    val isSyncProfile: Boolean? = null
)

data class AdminChannelPlatformResponse(
    val id: Long,
    val channel: ChannelInfo,
    val platformType: PlatformType,
    val platformChannelId: String,
    val platformUrl: String,
    val isSyncProfile: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    data class ChannelInfo(
        val id: Long,
        val name: String,
        val profileUrl: String
    )

    companion object {
        fun from(channelPlatform: ChannelPlatform, platformUrl: String, channelProfileUrl: String): AdminChannelPlatformResponse {
            return AdminChannelPlatformResponse(
                id = channelPlatform.id!!,
                channel = ChannelInfo(
                    id = channelPlatform.channel?.id!!,
                    name = channelPlatform.channel?.name!!,
                    profileUrl = channelProfileUrl
                ),
                platformType = channelPlatform.platformType,
                platformChannelId = channelPlatform.platformChannelId,
                platformUrl = platformUrl,
                isSyncProfile = channelPlatform.isSyncProfile,
                createdAt = channelPlatform.createdAt,
                updatedAt = channelPlatform.updatedAt
            )
        }
    }
}

data class ChannelPlatformResponse(
    val platformType: PlatformType,
    val platformChannelId: String,
    val streamUrl: String,
    val isSyncProfile: Boolean
) {
    companion object {
        fun from(channelPlatform: ChannelPlatform, streamUrl: String): ChannelPlatformResponse {
            return ChannelPlatformResponse(
                platformType = channelPlatform.platformType,
                platformChannelId = channelPlatform.platformChannelId,
                streamUrl = streamUrl,
                isSyncProfile = channelPlatform.isSyncProfile
            )
        }
    }
}
