package com.github.s8u.streamarchive.user.service.dto

import com.github.s8u.streamarchive.user.enums.UserSettingKey

/**
 * 사용자 설정 불러오기 결과 (적용할 값)
 */
data class UserSettingLoadResult(
    val isShowUnlistedChannels: Boolean,
    val isShowUnlistedVideos: Boolean
) {

    companion object {
        fun from(values: Map<UserSettingKey, String>): UserSettingLoadResult {
            return UserSettingLoadResult(
                isShowUnlistedChannels = values.getValue(UserSettingKey.SHOW_UNLISTED_CHANNELS).toBoolean(),
                isShowUnlistedVideos = values.getValue(UserSettingKey.SHOW_UNLISTED_VIDEOS).toBoolean()
            )
        }
    }
}
