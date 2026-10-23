package com.github.s8u.streamarchive.platform.converter

import com.github.s8u.streamarchive.global.converter.EnumNameConverter
import com.github.s8u.streamarchive.platform.enums.PlatformType
import jakarta.persistence.Converter

/**
 * 플랫폼 유형 변환기
 */
@Converter
class PlatformTypeConverter : EnumNameConverter<PlatformType>(PlatformType::class.java)
