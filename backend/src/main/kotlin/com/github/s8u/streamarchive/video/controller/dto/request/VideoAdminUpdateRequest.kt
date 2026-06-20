package com.github.s8u.streamarchive.video.controller.dto.request

import com.github.s8u.streamarchive.video.enums.VideoContentPrivacy
import com.github.s8u.streamarchive.video.usecase.dto.command.VideoAdminUpdateCommand
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "동영상 수정 요청 (관리자)")
data class VideoAdminUpdateRequest(
    @field:Schema(description = "제목", example = "오늘의 방송")
    val title: String? = null,

    @field:Schema(description = "설명")
    val description: String? = null,

    @field:Schema(description = "콘텐츠 공개 범위 (PUBLIC/UNLISTED/PRIVATE)")
    val contentPrivacy: VideoContentPrivacy? = null,

    @field:Schema(description = "채팅 싱크 오프셋 (밀리초)", example = "0")
    val chatSyncOffsetMillis: Long? = null
) {

    fun toCommand(): VideoAdminUpdateCommand {
        return VideoAdminUpdateCommand(
            title = title,
            description = description,
            contentPrivacy = contentPrivacy,
            chatSyncOffsetMillis = chatSyncOffsetMillis
        )
    }
}
