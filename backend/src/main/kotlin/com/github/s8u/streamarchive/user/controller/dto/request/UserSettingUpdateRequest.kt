package com.github.s8u.streamarchive.user.controller.dto.request

import com.github.s8u.streamarchive.user.usecase.dto.command.UserSettingUpdateCommand
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "내 설정 수정 요청 (보낸 필드만 바꾼다)")
data class UserSettingUpdateRequest(
    @field:Schema(description = "일부공개 채널 보기 (관리자 전용)", example = "true")
    val isShowUnlistedChannels: Boolean? = null,

    @field:Schema(description = "일부공개 동영상 보기 (관리자 전용)", example = "true")
    val isShowUnlistedVideos: Boolean? = null
) {

    fun toCommand(): UserSettingUpdateCommand {
        return UserSettingUpdateCommand(
            isShowUnlistedChannels = isShowUnlistedChannels,
            isShowUnlistedVideos = isShowUnlistedVideos
        )
    }
}
