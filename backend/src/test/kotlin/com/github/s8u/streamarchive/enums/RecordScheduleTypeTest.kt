package com.github.s8u.streamarchive.enums

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class RecordScheduleTypeTest {

    @Nested
    @DisplayName("ONCE")
    inner class Once {

        @Test
        @DisplayName("항상 오늘로 판단한다")
        fun alwaysToday() {
            assertTrue(RecordScheduleType.ONCE.calculateIsToday(""))
            assertTrue(RecordScheduleType.ONCE.calculateIsToday("any_value"))
        }
    }

    @Nested
    @DisplayName("ALWAYS")
    inner class Always {

        @Test
        @DisplayName("항상 오늘로 판단한다")
        fun alwaysToday() {
            assertTrue(RecordScheduleType.ALWAYS.calculateIsToday(""))
            assertTrue(RecordScheduleType.ALWAYS.calculateIsToday("any_value"))
        }
    }

    @Nested
    @DisplayName("N_DAYS_OF_EVERY_WEEK")
    inner class NDaysOfEveryWeek {

        @Test
        @DisplayName("오늘 요일이 포함되어 있으면 true를 반환한다")
        fun todayIncluded() {
            val today = LocalDate.now().dayOfWeek.toString()
            val value = "[\"$today\"]"

            assertTrue(RecordScheduleType.N_DAYS_OF_EVERY_WEEK.calculateIsToday(value))
        }

        @Test
        @DisplayName("오늘 요일이 포함되지 않으면 false를 반환한다")
        fun todayNotIncluded() {
            // 오늘이 아닌 요일들만 선택
            val today = LocalDate.now().dayOfWeek
            val otherDays = java.time.DayOfWeek.entries.filter { it != today }
            val value = "[\"${otherDays.first()}\"]"

            assertFalse(RecordScheduleType.N_DAYS_OF_EVERY_WEEK.calculateIsToday(value))
        }

        @Test
        @DisplayName("여러 요일이 포함된 배열에서 오늘이 있으면 true")
        fun multipleeDaysWithToday() {
            val today = LocalDate.now().dayOfWeek.toString()
            val value = "[\"MONDAY\",\"$today\",\"FRIDAY\"]"

            assertTrue(RecordScheduleType.N_DAYS_OF_EVERY_WEEK.calculateIsToday(value))
        }

        @Test
        @DisplayName("단일 문자열 값으로도 판단할 수 있다")
        fun singleStringValue() {
            val today = LocalDate.now().dayOfWeek.toString()

            assertTrue(RecordScheduleType.N_DAYS_OF_EVERY_WEEK.calculateIsToday("\"$today\""))
        }
    }

    @Nested
    @DisplayName("SPECIFIC_DAY")
    inner class SpecificDay {

        @Test
        @DisplayName("오늘 날짜가 포함되어 있으면 true를 반환한다")
        fun todayIncluded() {
            val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val value = "[\"$today\"]"

            assertTrue(RecordScheduleType.SPECIFIC_DAY.calculateIsToday(value))
        }

        @Test
        @DisplayName("오늘 날짜가 포함되지 않으면 false를 반환한다")
        fun todayNotIncluded() {
            val yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val value = "[\"$yesterday\"]"

            assertFalse(RecordScheduleType.SPECIFIC_DAY.calculateIsToday(value))
        }

        @Test
        @DisplayName("여러 날짜 중 오늘이 있으면 true")
        fun multipleDatesWithToday() {
            val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val tomorrow = LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val value = "[\"$today\",\"$tomorrow\"]"

            assertTrue(RecordScheduleType.SPECIFIC_DAY.calculateIsToday(value))
        }

        @Test
        @DisplayName("단일 문자열 값으로도 판단할 수 있다")
        fun singleStringValue() {
            val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

            assertTrue(RecordScheduleType.SPECIFIC_DAY.calculateIsToday("\"$today\""))
        }
    }

    @Nested
    @DisplayName("enum 기본 속성")
    inner class EnumProperties {

        @Test
        @DisplayName("모든 스케줄 타입이 description을 가진다")
        fun allHaveDescriptions() {
            RecordScheduleType.entries.forEach { type ->
                assertNotNull(type.description)
                assertTrue(type.description.isNotBlank())
            }
        }

        @Test
        @DisplayName("4개의 스케줄 타입이 존재한다")
        fun fourTypes() {
            assertEquals(4, RecordScheduleType.entries.size)
        }
    }
}
