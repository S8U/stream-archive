package com.github.s8u.streamarchive.video.converter

import com.github.s8u.streamarchive.global.converter.EnumNameConverter
import com.github.s8u.streamarchive.video.enums.VideoContentPrivacy
import jakarta.persistence.Converter

/**
 * 동영상 콘텐츠 공개 범위 변환기
 */
@Converter
class VideoContentPrivacyConverter : EnumNameConverter<VideoContentPrivacy>(VideoContentPrivacy::class.java)
