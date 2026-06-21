package com.github.s8u.streamarchive.video.controller.dto.response

import com.github.s8u.streamarchive.video.usecase.dto.result.VideoAutoDeleteHistorySearchResult
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "동영상 자동 삭제 이력 조회 응답 (관리자)")
data class VideoAutoDeleteHistorySearchResponse(
    @field:Schema(description = "이력 ID", example = "1")
    val id: Long,

    @field:Schema(description = "동영상 ID", example = "1")
    val videoId: Long,

    @field:Schema(description = "채널 ID", example = "1")
    val channelId: Long,

    @field:Schema(description = "채널 이름", example = "우왁굳")
    val channelName: String,

    @field:Schema(description = "삭제 시점 제목", example = "오늘의 방송")
    val title: String,

    @field:Schema(description = "삭제 시점 파일 크기 (바이트)", example = "1073741824")
    val fileSize: Long,

    @field:Schema(description = "동영상 생성 일시", example = "2026-05-01T12:00:00")
    val videoCreatedAt: LocalDateTime,

    @field:Schema(description = "삭제 일시", example = "2026-06-21T04:00:00")
    val deletedAt: LocalDateTime
) {

    companion object {
        fun from(result: VideoAutoDeleteHistorySearchResult): VideoAutoDeleteHistorySearchResponse {
            return VideoAutoDeleteHistorySearchResponse(
                id = result.id,
                videoId = result.videoId,
                channelId = result.channelId,
                channelName = result.channelName,
                title = result.title,
                fileSize = result.fileSize,
                videoCreatedAt = result.videoCreatedAt,
                deletedAt = result.deletedAt
            )
        }
    }

}
