package com.github.s8u.streamarchive.channelplatform.usecase.dto.result

import com.github.s8u.streamarchive.channelplatform.entity.ChannelPlatform
import com.github.s8u.streamarchive.platform.enums.PlatformType

/**
 * 채널 플랫폼 목록 조회 결과 (공개)
 */
data class ChannelPlatformSearchResult(
    val platformType: PlatformType,
    val platformChannelId: String,
    val streamUrl: String,
    val isSyncProfile: Boolean
) {

    companion object {
        fun from(channelPlatform: ChannelPlatform, streamUrl: String): ChannelPlatformSearchResult {
            return ChannelPlatformSearchResult(
                platformType = channelPlatform.platformType,
                platformChannelId = channelPlatform.platformChannelId,
                streamUrl = streamUrl,
                isSyncProfile = channelPlatform.isSyncProfile
            )
        }
    }
}
