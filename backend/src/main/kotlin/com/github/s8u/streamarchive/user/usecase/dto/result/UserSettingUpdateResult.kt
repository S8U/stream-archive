package com.github.s8u.streamarchive.user.usecase.dto.result

import com.github.s8u.streamarchive.user.service.dto.UserSettingLoadResult

/**
 * 내 설정 수정 결과
 */
data class UserSettingUpdateResult(
    val isShowUnlistedChannels: Boolean,
    val isShowUnlistedVideos: Boolean
) {

    companion object {
        fun from(loadResult: UserSettingLoadResult): UserSettingUpdateResult {
            return UserSettingUpdateResult(
                isShowUnlistedChannels = loadResult.isShowUnlistedChannels,
                isShowUnlistedVideos = loadResult.isShowUnlistedVideos
            )
        }
    }
}
