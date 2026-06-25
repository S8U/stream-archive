package com.github.s8u.streamarchive.platform.controller.dto.response

import com.github.s8u.streamarchive.platform.enums.PlatformType
import com.github.s8u.streamarchive.platform.usecase.dto.result.PlatformAdminChannelResolveResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "플랫폼 채널 조회 응답 (관리자)")
data class PlatformAdminChannelResolveResponse(
    @field:Schema(description = "플랫폼 유형 (CHZZK/TWITCH/SOOP/YOUTUBE)")
    val platformType: PlatformType,

    @field:Schema(description = "플랫폼 채널 ID")
    val platformChannelId: String,

    @field:Schema(description = "채널 이름", example = "홍길동")
    val name: String,

    @field:Schema(description = "프로필 썸네일 URL")
    val thumbnailUrl: String?
) {

    companion object {
        fun from(result: PlatformAdminChannelResolveResult): PlatformAdminChannelResolveResponse {
            return PlatformAdminChannelResolveResponse(
                platformType = result.platformType,
                platformChannelId = result.platformChannelId,
                name = result.name,
                thumbnailUrl = result.thumbnailUrl
            )
        }
    }
}
