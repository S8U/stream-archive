package com.github.s8u.streamarchive.video.controller.dto.request

import com.github.s8u.streamarchive.video.usecase.dto.command.VideoAutoDeletePolicyUpdateCommand
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "동영상 자동 삭제 정책 설정 요청 (관리자)")
data class VideoAutoDeletePolicyUpdateRequest(
    @field:Schema(description = "자동 삭제 활성화 여부", example = "true")
    val isEnabled: Boolean,

    @field:Schema(description = "생성 후 며칠 지난 동영상을 삭제할지", example = "30")
    val deleteAfterDays: Int
) {

    fun toCommand(channelId: Long?): VideoAutoDeletePolicyUpdateCommand {
        return VideoAutoDeletePolicyUpdateCommand(
            channelId = channelId,
            isEnabled = isEnabled,
            deleteAfterDays = deleteAfterDays
        )
    }

}
