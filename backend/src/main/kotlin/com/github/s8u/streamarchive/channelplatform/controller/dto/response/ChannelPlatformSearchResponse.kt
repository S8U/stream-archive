package com.github.s8u.streamarchive.channelplatform.controller.dto.response

import com.github.s8u.streamarchive.channelplatform.usecase.dto.result.ChannelPlatformSearchResult
import com.github.s8u.streamarchive.platform.enums.PlatformType
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "채널 플랫폼 목록 응답 (공개)")
data class ChannelPlatformSearchResponse(
    @field:Schema(description = "플랫폼 유형 (CHZZK/TWITCH/SOOP/YOUTUBE)")
    val platformType: PlatformType,

    @field:Schema(description = "플랫폼 채널 ID")
    val platformChannelId: String,

    @field:Schema(description = "방송 URL")
    val streamUrl: String,

    @field:Schema(description = "프로필 동기화 여부")
    val isSyncProfile: Boolean
) {

    companion object {
        fun from(result: ChannelPlatformSearchResult): ChannelPlatformSearchResponse {
            return ChannelPlatformSearchResponse(
                platformType = result.platformType,
                platformChannelId = result.platformChannelId,
                streamUrl = result.streamUrl,
                isSyncProfile = result.isSyncProfile
            )
        }
    }
}
