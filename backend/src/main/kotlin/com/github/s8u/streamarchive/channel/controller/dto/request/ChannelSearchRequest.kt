package com.github.s8u.streamarchive.channel.controller.dto.request

import com.github.s8u.streamarchive.channel.usecase.dto.command.ChannelSearchCommand
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "채널 검색 요청 (공개)")
data class ChannelSearchRequest(
    @field:Schema(description = "채널 이름", example = "홍길동 채널")
    val name: String? = null
) {

    fun toCommand(): ChannelSearchCommand {
        return ChannelSearchCommand(name = name)
    }
}
