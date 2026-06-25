package com.github.s8u.streamarchive.platform.controller.dto.request

import com.github.s8u.streamarchive.platform.usecase.dto.command.PlatformAdminChannelResolveCommand
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "플랫폼 채널 조회 요청 (관리자)")
data class PlatformAdminChannelResolveRequest(
    @field:Schema(
        description = "플랫폼 채널 URL",
        example = "https://chzzk.naver.com/live/abcdef0123456789abcdef0123456789"
    )
    val url: String
) {

    fun toCommand(): PlatformAdminChannelResolveCommand {
        return PlatformAdminChannelResolveCommand(
            url = url
        )
    }
}
