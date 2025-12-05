package com.github.s8u.streamarchive.enums

import com.fasterxml.jackson.databind.ObjectMapper
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class RecordScheduleType(
    val description: String,
    val calculateIsToday: (value: String) -> Boolean
) {

    ONCE("한 번만", {
        true
    }),

    ALWAYS("항상", {
        true
    }),

    N_DAYS_OF_EVERY_WEEK("매주 N요일", { value ->
        val objectMapper = ObjectMapper()
        val jsonNode = objectMapper.readTree(value)

        if (jsonNode.isArray) {
            jsonNode.any { it.asText() == LocalDate.now().dayOfWeek.toString() }
        } else {
            value == LocalDate.now().dayOfWeek.toString()
        }
    }),

    SPECIFIC_DAY("날짜 지정", { value ->
        val objectMapper = ObjectMapper()
        val jsonNode = objectMapper.readTree(value)

        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        if (jsonNode.isArray) {
            jsonNode.any { it.asText() == today }
        } else {
            value == today
        }
    });

}