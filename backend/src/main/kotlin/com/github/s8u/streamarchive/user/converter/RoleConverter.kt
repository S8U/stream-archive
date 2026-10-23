package com.github.s8u.streamarchive.user.converter

import com.github.s8u.streamarchive.global.converter.EnumNameConverter
import com.github.s8u.streamarchive.user.enums.Role
import jakarta.persistence.Converter

/**
 * 역할 변환기
 */
@Converter
class RoleConverter : EnumNameConverter<Role>(Role::class.java)
