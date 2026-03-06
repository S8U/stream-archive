package com.github.s8u.streamarchive.enums

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RecordQualityTest {

    @Nested
    @DisplayName("streamlinkValue")
    inner class StreamlinkValue {

        @Test
        @DisplayName("BEST는 'best' 값을 가진다")
        fun bestValue() {
            assertEquals("best", RecordQuality.BEST.streamlinkValue)
        }

        @Test
        @DisplayName("WORST는 'worst' 값을 가진다")
        fun worstValue() {
            assertEquals("worst", RecordQuality.WORST.streamlinkValue)
        }

        @Test
        @DisplayName("P1080_60은 '1080p60' 값을 가진다")
        fun p1080_60Value() {
            assertEquals("1080p60", RecordQuality.P1080_60.streamlinkValue)
        }

        @Test
        @DisplayName("P720은 '720p' 값을 가진다")
        fun p720Value() {
            assertEquals("720p", RecordQuality.P720.streamlinkValue)
        }

        @Test
        @DisplayName("모든 품질 옵션이 streamlink 값을 가진다")
        fun allHaveValues() {
            RecordQuality.entries.forEach { quality ->
                assertNotNull(quality.streamlinkValue)
                assertTrue(quality.streamlinkValue.isNotBlank())
            }
        }
    }

    @Nested
    @DisplayName("buildFallbackString")
    inner class BuildFallbackString {

        @Test
        @DisplayName("BEST는 모든 품질을 포함하는 fallback 문자열을 생성한다")
        fun bestFallback() {
            val fallback = RecordQuality.BEST.buildFallbackString()

            assertTrue(fallback.startsWith("best,"))
            assertTrue(fallback.contains("1080p60"))
            assertTrue(fallback.contains("720p"))
            assertTrue(fallback.endsWith("worst"))
        }

        @Test
        @DisplayName("WORST는 자기 자신만 포함한다")
        fun worstFallback() {
            val fallback = RecordQuality.WORST.buildFallbackString()

            assertEquals("worst", fallback)
        }

        @Test
        @DisplayName("P1080은 1080p부터 worst까지 포함한다")
        fun p1080Fallback() {
            val fallback = RecordQuality.P1080.buildFallbackString()

            assertTrue(fallback.startsWith("1080p,"))
            assertFalse(fallback.contains("1080p60"))
            assertFalse(fallback.contains("1440p"))
            assertTrue(fallback.contains("720p"))
            assertTrue(fallback.endsWith("worst"))
        }

        @Test
        @DisplayName("P720_60은 720p60부터 worst까지 포함한다")
        fun p720_60Fallback() {
            val fallback = RecordQuality.P720_60.buildFallbackString()

            assertTrue(fallback.startsWith("720p60,"))
            assertFalse(fallback.contains("1080p"))
            assertTrue(fallback.contains("720p"))
            assertTrue(fallback.contains("480p"))
            assertTrue(fallback.endsWith("worst"))
        }

        @Test
        @DisplayName("fallback 문자열은 쉼표로 구분된다")
        fun commaDelimited() {
            val fallback = RecordQuality.P1080.buildFallbackString()
            val parts = fallback.split(",")

            assertTrue(parts.size > 1)
            parts.forEach { part ->
                assertTrue(part.isNotBlank())
            }
        }
    }

    @Nested
    @DisplayName("enum 기본 속성")
    inner class EnumProperties {

        @Test
        @DisplayName("13개의 품질 옵션이 존재한다")
        fun thirteenOptions() {
            assertEquals(13, RecordQuality.entries.size)
        }

        @Test
        @DisplayName("BEST가 첫 번째, WORST가 마지막이다")
        fun orderCheck() {
            assertEquals(0, RecordQuality.BEST.ordinal)
            assertEquals(12, RecordQuality.WORST.ordinal)
        }
    }
}
