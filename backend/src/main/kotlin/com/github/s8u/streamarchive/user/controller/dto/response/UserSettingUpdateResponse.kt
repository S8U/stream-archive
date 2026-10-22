package com.github.s8u.streamarchive.user.controller.dto.response

import com.github.s8u.streamarchive.user.usecase.dto.result.UserSettingUpdateResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "내 설정 수정 응답")
data class UserSettingUpdateResponse(
    @field:Schema(description = "일부공개 채널 보기 (관리자 전용)", example = "true")
    val isShowUnlistedChannels: Boolean,

    @field:Schema(description = "일부공개 동영상 보기 (관리자 전용)", example = "true")
    val isShowUnlistedVideos: Boolean
) {

    companion object {
        fun from(result: UserSettingUpdateResult): UserSettingUpdateResponse {
            return UserSettingUpdateResponse(
                isShowUnlistedChannels = result.isShowUnlistedChannels,
                isShowUnlistedVideos = result.isShowUnlistedVideos
            )
        }
    }
}
