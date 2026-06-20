package com.github.s8u.streamarchive.channelplatform.usecase.dto.result

import com.github.s8u.streamarchive.channelplatform.entity.ChannelPlatform
import com.github.s8u.streamarchive.platform.enums.PlatformType
import java.time.LocalDateTime

/**
 * 채널 플랫폼 생성 결과 (관리자)
 */
data class ChannelPlatformAdminCreateResult(
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
        fun from(
            channelPlatform: ChannelPlatform,
            platformUrl: String,
            channelProfileUrl: String
        ): ChannelPlatformAdminCreateResult {
            return ChannelPlatformAdminCreateResult(
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
