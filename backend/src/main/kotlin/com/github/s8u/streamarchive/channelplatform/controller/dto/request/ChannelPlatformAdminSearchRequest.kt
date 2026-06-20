package com.github.s8u.streamarchive.channelplatform.controller.dto.request

import com.github.s8u.streamarchive.channelplatform.usecase.dto.command.ChannelPlatformAdminSearchCommand
import com.github.s8u.streamarchive.platform.enums.PlatformType
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "채널 플랫폼 검색 요청 (관리자)")
data class ChannelPlatformAdminSearchRequest(
    @field:Schema(description = "채널 플랫폼 ID", example = "1")
    val id: Long? = null,

    @field:Schema(description = "채널 이름", example = "홍길동 채널")
    val channelName: String? = null,

    @field:Schema(description = "플랫폼 유형 (CHZZK/TWITCH/SOOP/YOUTUBE)")
    val platformType: PlatformType? = null,

    @field:Schema(description = "플랫폼 채널 ID")
    val platformChannelId: String? = null,

    @field:Schema(description = "프로필 동기화 여부")
    val isSyncProfile: Boolean? = null
) {

    fun toCommand(): ChannelPlatformAdminSearchCommand {
        return ChannelPlatformAdminSearchCommand(
            id = id,
            channelName = channelName,
            platformType = platformType,
            platformChannelId = platformChannelId,
            isSyncProfile = isSyncProfile
        )
    }
}
