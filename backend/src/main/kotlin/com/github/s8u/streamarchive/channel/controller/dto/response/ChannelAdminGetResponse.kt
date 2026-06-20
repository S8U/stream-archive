package com.github.s8u.streamarchive.channel.controller.dto.response

import com.github.s8u.streamarchive.channel.enums.ChannelContentPrivacy
import com.github.s8u.streamarchive.channel.usecase.dto.result.ChannelAdminGetResult
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "채널 단건 응답 (관리자)")
data class ChannelAdminGetResponse(
    @field:Schema(description = "채널 ID", example = "1")
    val id: Long,

    @field:Schema(description = "채널 UUID")
    val uuid: String,

    @field:Schema(description = "채널 이름", example = "홍길동 채널")
    val name: String,

    @field:Schema(description = "프로필 이미지 URL")
    val profileUrl: String,

    @field:Schema(description = "전체 동영상 파일 크기 (바이트)", example = "1073741824")
    val totalVideoFileSize: Long,

    @field:Schema(description = "콘텐츠 공개 범위 (PUBLIC/UNLISTED/PRIVATE)")
    val contentPrivacy: ChannelContentPrivacy,

    @field:Schema(description = "생성 일시")
    val createdAt: LocalDateTime,

    @field:Schema(description = "수정 일시")
    val updatedAt: LocalDateTime
) {

    companion object {
        fun from(result: ChannelAdminGetResult): ChannelAdminGetResponse {
            return ChannelAdminGetResponse(
                id = result.id,
                uuid = result.uuid,
                name = result.name,
                profileUrl = result.profileUrl,
                totalVideoFileSize = result.totalVideoFileSize,
                contentPrivacy = result.contentPrivacy,
                createdAt = result.createdAt,
                updatedAt = result.updatedAt
            )
        }
    }
}
