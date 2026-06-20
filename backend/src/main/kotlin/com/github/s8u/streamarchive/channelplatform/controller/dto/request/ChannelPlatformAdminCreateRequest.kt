package com.github.s8u.streamarchive.channelplatform.controller.dto.request

import com.github.s8u.streamarchive.channelplatform.usecase.dto.command.ChannelPlatformAdminCreateCommand
import com.github.s8u.streamarchive.platform.enums.PlatformType
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "채널 플랫폼 생성 요청 (관리자)")
data class ChannelPlatformAdminCreateRequest(
    @field:Schema(description = "채널 ID", example = "1")
    val channelId: Long,

    @field:Schema(description = "플랫폼 유형 (CHZZK/TWITCH/SOOP/YOUTUBE)")
    val platformType: PlatformType,

    @field:Schema(description = "플랫폼 채널 ID")
    val platformChannelId: String,

    @field:Schema(description = "프로필 동기화 여부", example = "true")
    val isSyncProfile: Boolean = true
) {

    fun toCommand(): ChannelPlatformAdminCreateCommand {
        return ChannelPlatformAdminCreateCommand(
            channelId = channelId,
            platformType = platformType,
            platformChannelId = platformChannelId,
            isSyncProfile = isSyncProfile
        )
    }
}
