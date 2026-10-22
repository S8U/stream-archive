package com.github.s8u.streamarchive.user.usecase.dto.command

import com.github.s8u.streamarchive.user.enums.UserSettingKey

data class UserSettingUpdateCommand(
    val isShowUnlistedChannels: Boolean? = null,
    val isShowUnlistedVideos: Boolean? = null
) {

    /**
     * 바꾸려는 설정만 키와 저장할 값으로 바꿔 반환한다.
     *
     * null인 필드는 기존 값을 유지하므로 넣지 않는다.
     */
    fun toChangedValues(): Map<UserSettingKey, String> {
        val values = mapOf(
            UserSettingKey.SHOW_UNLISTED_CHANNELS to isShowUnlistedChannels,
            UserSettingKey.SHOW_UNLISTED_VIDEOS to isShowUnlistedVideos
        )

        return values
            .filterValues { it != null }
            .mapValues { it.value.toString() }
    }
}
