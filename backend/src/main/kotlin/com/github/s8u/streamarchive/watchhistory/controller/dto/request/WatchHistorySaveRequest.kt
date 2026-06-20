package com.github.s8u.streamarchive.watchhistory.controller.dto.request

import com.github.s8u.streamarchive.watchhistory.usecase.dto.command.WatchHistorySaveCommand
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "시청 위치 저장 요청")
data class WatchHistorySaveRequest(
    @field:Schema(description = "재생 위치 (초)", example = "120")
    val position: Int
) {

    fun toCommand(): WatchHistorySaveCommand {
        return WatchHistorySaveCommand(position = position)
    }
}
