package com.github.s8u.streamarchive.channel.controller.dto.response

import com.github.s8u.streamarchive.channel.enums.ChannelContentPrivacy
import com.github.s8u.streamarchive.channel.usecase.dto.result.ChannelGetResult
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "채널 단건 응답 (공개)")
data class ChannelGetResponse(
    @field:Schema(description = "채널 UUID")
    val uuid: String,

    @field:Schema(description = "채널 이름", example = "홍길동 채널")
    val name: String,

    @field:Schema(description = "프로필 이미지 URL")
    val profileUrl: String,

    @field:Schema(description = "콘텐츠 공개 범위 (PUBLIC/UNLISTED/PRIVATE)")
    val contentPrivacy: ChannelContentPrivacy,

    @field:Schema(description = "생성 일시")
    val createdAt: LocalDateTime,

    @field:Schema(description = "수정 일시")
    val updatedAt: LocalDateTime
) {

    companion object {
        fun from(result: ChannelGetResult): ChannelGetResponse {
            return ChannelGetResponse(
                uuid = result.uuid,
                name = result.name,
                profileUrl = result.profileUrl,
                contentPrivacy = result.contentPrivacy,
                createdAt = result.createdAt,
                updatedAt = result.updatedAt
            )
        }
    }
}
