package com.github.s8u.streamarchive.user.enums

/**
 * 사용자 설정 키
 *
 * 값은 문자열로 저장한다.
 * 키를 없애도 그 키로 저장된 행은 읽지 않는다. 남은 행은 따로 지운다.
 */
enum class UserSettingKey(
    val defaultValue: String,
    val isAdminOnly: Boolean
) {
    SHOW_UNLISTED_CHANNELS("false", true),  // 일부공개 채널 보기
    SHOW_UNLISTED_VIDEOS("false", true);    // 일부공개 동영상 보기

    /**
     * 이 설정을 쓸 수 있는 역할인지 확인한다.
     *
     * 관리자 전용 설정은 관리자만 쓸 수 있다.
     */
    fun isAvailableTo(isAdmin: Boolean): Boolean {
        return !isAdminOnly || isAdmin
    }

    /**
     * 저장된 값과 사용자 역할로 적용할 값을 정한다.
     *
     * 저장된 값이 없으면 기본값을 쓴다.
     * 쓸 수 없는 역할이면 저장된 값과 상관없이 기본값을 쓴다.
     */
    fun resolveValue(savedValue: String?, isAdmin: Boolean): String {
        if (!isAvailableTo(isAdmin)) {
            return defaultValue
        }

        return savedValue ?: defaultValue
    }

}
