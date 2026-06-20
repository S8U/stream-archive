package com.github.s8u.streamarchive.channelplatform.controller.dto.request

import com.github.s8u.streamarchive.channelplatform.usecase.dto.command.ChannelPlatformAdminUpdateCommand
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "채널 플랫폼 수정 요청 (관리자)")
data class ChannelPlatformAdminUpdateRequest(
    @field:Schema(description = "플랫폼 채널 ID")
    val platformChannelId: String? = null,

    @field:Schema(description = "프로필 동기화 여부")
    val isSyncProfile: Boolean? = null
) {

    fun toCommand(): ChannelPlatformAdminUpdateCommand {
        return ChannelPlatformAdminUpdateCommand(
            platformChannelId = platformChannelId,
            isSyncProfile = isSyncProfile
        )
    }
}
