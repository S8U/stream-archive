package com.github.s8u.streamarchive.platform.converter

import com.github.s8u.streamarchive.global.converter.EnumNameConverter
import com.github.s8u.streamarchive.platform.enums.PlatformChatUserRole
import jakarta.persistence.Converter

/**
 * 플랫폼 채팅 작성자 역할 변환기
 */
@Converter
class PlatformChatUserRoleConverter : EnumNameConverter<PlatformChatUserRole>(PlatformChatUserRole::class.java)
