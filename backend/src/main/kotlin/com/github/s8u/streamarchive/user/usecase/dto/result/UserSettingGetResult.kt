package com.github.s8u.streamarchive.user.usecase.dto.result

import com.github.s8u.streamarchive.user.service.dto.UserSettingLoadResult

/**
 * 내 설정 조회 결과
 */
data class UserSettingGetResult(
    val isShowUnlistedChannels: Boolean,
    val isShowUnlistedVideos: Boolean
) {

    companion object {
        fun from(loadResult: UserSettingLoadResult): UserSettingGetResult {
            return UserSettingGetResult(
                isShowUnlistedChannels = loadResult.isShowUnlistedChannels,
                isShowUnlistedVideos = loadResult.isShowUnlistedVideos
            )
        }
    }
}
