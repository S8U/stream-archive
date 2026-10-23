package com.github.s8u.streamarchive.record.converter

import com.github.s8u.streamarchive.global.converter.EnumNameConverter
import com.github.s8u.streamarchive.record.enums.RecordQuality
import jakarta.persistence.Converter

/**
 * 녹화 화질 변환기
 */
@Converter
class RecordQualityConverter : EnumNameConverter<RecordQuality>(RecordQuality::class.java)
