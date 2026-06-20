package com.github.s8u.streamarchive.channel.controller.dto.request

import com.github.s8u.streamarchive.channel.enums.ChannelContentPrivacy
import com.github.s8u.streamarchive.channel.usecase.dto.command.ChannelAdminCreateCommand
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "채널 생성 요청 (관리자)")
data class ChannelAdminCreateRequest(
    @field:Schema(description = "채널 이름", example = "홍길동 채널")
    val name: String,

    @field:Schema(description = "콘텐츠 공개 범위 (PUBLIC/UNLISTED/PRIVATE)")
    val contentPrivacy: ChannelContentPrivacy
) {

    fun toCommand(): ChannelAdminCreateCommand {
        return ChannelAdminCreateCommand(
            name = name,
            contentPrivacy = contentPrivacy
        )
    }
}
