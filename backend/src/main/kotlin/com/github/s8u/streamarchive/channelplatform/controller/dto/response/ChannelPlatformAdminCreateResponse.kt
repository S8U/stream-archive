package com.github.s8u.streamarchive.channelplatform.controller.dto.response

import com.github.s8u.streamarchive.channelplatform.usecase.dto.result.ChannelPlatformAdminCreateResult
import com.github.s8u.streamarchive.platform.enums.PlatformType
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "채널 플랫폼 생성 응답 (관리자)")
data class ChannelPlatformAdminCreateResponse(
    @field:Schema(description = "채널 플랫폼 ID", example = "1")
    val id: Long,

    @field:Schema(description = "채널 정보")
    val channel: ChannelInfo,

    @field:Schema(description = "플랫폼 유형 (CHZZK/TWITCH/SOOP/YOUTUBE)")
    val platformType: PlatformType,

    @field:Schema(description = "플랫폼 채널 ID")
    val platformChannelId: String,

    @field:Schema(description = "플랫폼 방송 URL")
    val platformUrl: String,

    @field:Schema(description = "프로필 동기화 여부")
    val isSyncProfile: Boolean,

    @field:Schema(description = "생성 일시")
    val createdAt: LocalDateTime,

    @field:Schema(description = "수정 일시")
    val updatedAt: LocalDateTime
) {

    @Schema(description = "채널 정보")
    data class ChannelInfo(
        @field:Schema(description = "채널 ID", example = "1")
        val id: Long,

        @field:Schema(description = "채널 이름", example = "홍길동 채널")
        val name: String,

        @field:Schema(description = "프로필 이미지 URL")
        val profileUrl: String
    )

    companion object {
        fun from(result: ChannelPlatformAdminCreateResult): ChannelPlatformAdminCreateResponse {
            return ChannelPlatformAdminCreateResponse(
                id = result.id,
                channel = ChannelInfo(
                    id = result.channel.id,
                    name = result.channel.name,
                    profileUrl = result.channel.profileUrl
                ),
                platformType = result.platformType,
                platformChannelId = result.platformChannelId,
                platformUrl = result.platformUrl,
                isSyncProfile = result.isSyncProfile,
                createdAt = result.createdAt,
                updatedAt = result.updatedAt
            )
        }
    }
}
