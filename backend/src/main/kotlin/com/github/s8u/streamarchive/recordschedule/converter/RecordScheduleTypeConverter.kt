package com.github.s8u.streamarchive.recordschedule.converter

import com.github.s8u.streamarchive.global.converter.EnumNameConverter
import com.github.s8u.streamarchive.recordschedule.enums.RecordScheduleType
import jakarta.persistence.Converter

/**
 * 녹화 스케줄 유형 변환기
 */
@Converter
class RecordScheduleTypeConverter : EnumNameConverter<RecordScheduleType>(RecordScheduleType::class.java)
