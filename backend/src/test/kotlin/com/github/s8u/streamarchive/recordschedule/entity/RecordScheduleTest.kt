package com.github.s8u.streamarchive.recordschedule.entity

import com.github.s8u.streamarchive.channel.entity.Channel
import com.github.s8u.streamarchive.channel.enums.ChannelContentPrivacy
import com.github.s8u.streamarchive.platform.enums.PlatformType
import com.github.s8u.streamarchive.record.enums.RecordQuality
import com.github.s8u.streamarchive.recordschedule.enums.RecordScheduleType
import kotlin.test.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RecordScheduleTest {

    private fun channel(): Channel {
        return Channel(
            uuid = "channel-uuid",
            name = "채널",
            contentPrivacy = ChannelContentPrivacy.PUBLIC
        )
    }

    private fun recordSchedule(): RecordSchedule {
        return RecordSchedule(
            channel = channel(),
            platformType = PlatformType.CHZZK,
            scheduleType = RecordScheduleType.ALWAYS,
            value = "원래 값",
            recordQuality = RecordQuality.BEST,
            priority = 0
        )
    }

    @Nested
    inner class Update {

        @Test
        fun `모든 인자를 넘기면 전부 갱신된다`() {
            val schedule = recordSchedule()

            schedule.update(
                platformType = PlatformType.TWITCH,
                scheduleType = RecordScheduleType.ONCE,
                value = "새 값",
                recordQuality = RecordQuality.WORST,
                priority = 5,
                autoArchive = true
            )

            assertEquals(PlatformType.TWITCH, schedule.platformType)
            assertEquals(RecordScheduleType.ONCE, schedule.scheduleType)
            assertEquals("새 값", schedule.value)
            assertEquals(RecordQuality.WORST, schedule.recordQuality)
            assertEquals(5, schedule.priority)
            assertEquals(true, schedule.autoArchive)
        }

        @Test
        fun `value만 넘기면 value만 바뀌고 나머지는 유지된다`() {
            val schedule = recordSchedule()

            schedule.update(
                platformType = null,
                scheduleType = null,
                value = "새 값",
                recordQuality = null,
                priority = null,
                autoArchive = null
            )

            assertEquals("새 값", schedule.value)
            assertEquals(PlatformType.CHZZK, schedule.platformType)
            assertEquals(RecordScheduleType.ALWAYS, schedule.scheduleType)
            assertEquals(RecordQuality.BEST, schedule.recordQuality)
            assertEquals(0, schedule.priority)
            assertEquals(false, schedule.autoArchive)
        }

        @Test
        fun `모든 인자가 null이면 아무 값도 바뀌지 않는다`() {
            val schedule = recordSchedule()

            schedule.update(
                platformType = null,
                scheduleType = null,
                value = null,
                recordQuality = null,
                priority = null,
                autoArchive = null
            )

            assertEquals(PlatformType.CHZZK, schedule.platformType)
            assertEquals(RecordScheduleType.ALWAYS, schedule.scheduleType)
            assertEquals("원래 값", schedule.value)
            assertEquals(RecordQuality.BEST, schedule.recordQuality)
            assertEquals(0, schedule.priority)
            assertEquals(false, schedule.autoArchive)
        }
    }
}
