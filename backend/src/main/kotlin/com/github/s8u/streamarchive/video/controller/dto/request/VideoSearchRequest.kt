package com.github.s8u.streamarchive.video.controller.dto.request

import com.github.s8u.streamarchive.video.usecase.dto.command.VideoSearchCommand
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "동영상 검색 요청 (공개)")
data class VideoSearchRequest(
    @field:Schema(description = "제목", example = "오늘의 방송")
    val title: String? = null,

    @field:Schema(description = "설명")
    val description: String? = null,

    @field:Schema(description = "채널 이름", example = "홍길동 채널")
    val channelName: String? = null,

    @field:Schema(description = "채널 UUID")
    val channelUuid: String? = null
) {

    fun toCommand(): VideoSearchCommand {
        return VideoSearchCommand(
            title = title,
            description = description,
            channelName = channelName,
            channelUuid = channelUuid
        )
    }
}
