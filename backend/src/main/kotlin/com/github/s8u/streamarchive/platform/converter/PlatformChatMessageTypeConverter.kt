package com.github.s8u.streamarchive.platform.converter

import com.github.s8u.streamarchive.global.converter.EnumNameConverter
import com.github.s8u.streamarchive.platform.enums.PlatformChatMessageType
import jakarta.persistence.Converter

/**
 * 플랫폼 채팅 메시지 종류 변환기
 */
@Converter
class PlatformChatMessageTypeConverter : EnumNameConverter<PlatformChatMessageType>(PlatformChatMessageType::class.java)
