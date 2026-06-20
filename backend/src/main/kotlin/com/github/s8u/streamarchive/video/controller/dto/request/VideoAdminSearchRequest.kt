package com.github.s8u.streamarchive.video.controller.dto.request

import com.github.s8u.streamarchive.video.enums.VideoContentPrivacy
import com.github.s8u.streamarchive.video.usecase.dto.command.VideoAdminSearchCommand
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "동영상 검색 요청 (관리자)")
data class VideoAdminSearchRequest(
    @field:Schema(description = "동영상 ID", example = "1")
    val id: Long? = null,

    @field:Schema(description = "동영상 UUID")
    val uuid: String? = null,

    @field:Schema(description = "제목", example = "오늘의 방송")
    val title: String? = null,

    @field:Schema(description = "설명")
    val description: String? = null,

    @field:Schema(description = "채널 이름", example = "홍길동 채널")
    val channelName: String? = null,

    @field:Schema(description = "콘텐츠 공개 범위 (PUBLIC/UNLISTED/PRIVATE)")
    val contentPrivacy: VideoContentPrivacy? = null,

    @field:Schema(description = "소장 여부")
    val isArchived: Boolean? = null,

    @field:Schema(description = "생성 일시 시작")
    val createdAtFrom: LocalDateTime? = null,

    @field:Schema(description = "생성 일시 끝")
    val createdAtTo: LocalDateTime? = null
) {

    fun toCommand(): VideoAdminSearchCommand {
        return VideoAdminSearchCommand(
            id = id,
            uuid = uuid,
            title = title,
            description = description,
            channelName = channelName,
            contentPrivacy = contentPrivacy,
            isArchived = isArchived,
            createdAtFrom = createdAtFrom,
            createdAtTo = createdAtTo
        )
    }
}
