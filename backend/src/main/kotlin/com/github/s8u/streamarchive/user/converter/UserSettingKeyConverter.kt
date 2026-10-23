package com.github.s8u.streamarchive.user.converter

import com.github.s8u.streamarchive.global.converter.EnumNameConverter
import com.github.s8u.streamarchive.user.enums.UserSettingKey
import jakarta.persistence.Converter

/**
 * 사용자 설정 키 변환기
 */
@Converter
class UserSettingKeyConverter : EnumNameConverter<UserSettingKey>(UserSettingKey::class.java)
