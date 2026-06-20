package com.github.s8u.streamarchive.watchhistory.controller.dto.response

import com.github.s8u.streamarchive.watchhistory.usecase.dto.result.WatchHistoryGetResult
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "개별 동영상 시청 기록 응답")
data class WatchHistoryGetResponse(
    @field:Schema(description = "마지막 재생 위치 (초)", example = "120")
    val lastPosition: Int,

    @field:Schema(description = "시청 일시")
    val watchedAt: LocalDateTime
) {

    companion object {
        fun from(result: WatchHistoryGetResult): WatchHistoryGetResponse {
            return WatchHistoryGetResponse(
                lastPosition = result.lastPosition,
                watchedAt = result.watchedAt
            )
        }
    }
}
