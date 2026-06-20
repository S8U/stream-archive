package com.github.s8u.streamarchive.record.controller.dto.request

import com.github.s8u.streamarchive.platform.enums.PlatformType
import com.github.s8u.streamarchive.record.usecase.dto.command.RecordAdminSearchCommand
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "녹화 기록 검색 요청 (관리자)")
data class RecordAdminSearchRequest(
    @field:Schema(description = "녹화 기록 ID", example = "1")
    val id: Long? = null,

    @field:Schema(description = "채널 이름", example = "홍길동 채널")
    val channelName: String? = null,

    @field:Schema(description = "제목", example = "오늘의 방송")
    val title: String? = null,

    @field:Schema(description = "플랫폼 스트림 ID")
    val platformStreamId: String? = null,

    @field:Schema(description = "플랫폼 유형 (CHZZK/SOOP/TWITCH/YOUTUBE)")
    val platformType: PlatformType? = null,

    @field:Schema(description = "종료 여부")
    val isEnded: Boolean? = null,

    @field:Schema(description = "취소 여부")
    val isCancelled: Boolean? = null,

    @field:Schema(description = "생성 일시 시작")
    val createdAtFrom: LocalDateTime? = null,

    @field:Schema(description = "생성 일시 끝")
    val createdAtTo: LocalDateTime? = null
) {

    fun toCommand(): RecordAdminSearchCommand {
        return RecordAdminSearchCommand(
            id = id,
            channelName = channelName,
            title = title,
            platformStreamId = platformStreamId,
            platformType = platformType,
            isEnded = isEnded,
            isCancelled = isCancelled,
            createdAtFrom = createdAtFrom,
            createdAtTo = createdAtTo
        )
    }
}
