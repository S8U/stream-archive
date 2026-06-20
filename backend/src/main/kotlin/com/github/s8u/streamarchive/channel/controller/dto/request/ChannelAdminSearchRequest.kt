package com.github.s8u.streamarchive.channel.controller.dto.request

import com.github.s8u.streamarchive.channel.enums.ChannelContentPrivacy
import com.github.s8u.streamarchive.channel.usecase.dto.command.ChannelAdminSearchCommand
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "채널 검색 요청 (관리자)")
data class ChannelAdminSearchRequest(
    @field:Schema(description = "채널 ID", example = "1")
    val id: Long? = null,

    @field:Schema(description = "채널 UUID")
    val uuid: String? = null,

    @field:Schema(description = "채널 이름", example = "홍길동 채널")
    val name: String? = null,

    @field:Schema(description = "콘텐츠 공개 범위 (PUBLIC/UNLISTED/PRIVATE)")
    val contentPrivacy: ChannelContentPrivacy? = null
) {

    fun toCommand(): ChannelAdminSearchCommand {
        return ChannelAdminSearchCommand(
            id = id,
            uuid = uuid,
            name = name,
            contentPrivacy = contentPrivacy
        )
    }
}
