package com.github.s8u.streamarchive.video.controller.dto.response

import com.github.s8u.streamarchive.video.usecase.dto.result.VideoAutoDeletePreviewResult
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "동영상 자동 삭제 미리보기 목록 응답 (관리자)")
data class VideoAutoDeletePreviewResponse(
    @field:Schema(description = "동영상 ID", example = "1")
    val id: Long,

    @field:Schema(description = "동영상 UUID", example = "550e8400-e29b-41d4-a716-446655440000")
    val uuid: String,

    @field:Schema(description = "채널 ID", example = "1")
    val channelId: Long,

    @field:Schema(description = "채널 이름", example = "우왁굳")
    val channelName: String,

    @field:Schema(description = "제목", example = "오늘의 방송")
    val title: String,

    @field:Schema(description = "파일 크기 (바이트)", example = "1073741824")
    val fileSize: Long,

    @field:Schema(description = "썸네일 URL", example = "/videos/550e8400-e29b-41d4-a716-446655440000/thumbnail")
    val thumbnailUrl: String,

    @field:Schema(description = "생성 일시", example = "2026-05-01T12:00:00")
    val createdAt: LocalDateTime,

    @field:Schema(description = "생성 후 경과 일수", example = "42")
    val ageDays: Int,

    @field:Schema(description = "보관 기준을 초과한 일수", example = "12")
    val overDays: Int
) {

    companion object {
        fun from(result: VideoAutoDeletePreviewResult): VideoAutoDeletePreviewResponse {
            return VideoAutoDeletePreviewResponse(
                id = result.id,
                uuid = result.uuid,
                channelId = result.channelId,
                channelName = result.channelName,
                title = result.title,
                fileSize = result.fileSize,
                thumbnailUrl = result.thumbnailUrl,
                createdAt = result.createdAt,
                ageDays = result.ageDays,
                overDays = result.overDays
            )
        }
    }

}
