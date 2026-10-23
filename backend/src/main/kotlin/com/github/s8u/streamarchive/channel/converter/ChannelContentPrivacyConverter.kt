package com.github.s8u.streamarchive.channel.converter

import com.github.s8u.streamarchive.channel.enums.ChannelContentPrivacy
import com.github.s8u.streamarchive.global.converter.EnumNameConverter
import jakarta.persistence.Converter

/**
 * 채널 콘텐츠 공개 범위 변환기
 */
@Converter
class ChannelContentPrivacyConverter : EnumNameConverter<ChannelContentPrivacy>(ChannelContentPrivacy::class.java)
