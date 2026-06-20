package com.github.s8u.streamarchive.recordschedule.usecase

import com.github.s8u.streamarchive.channel.entity.Channel
import com.github.s8u.streamarchive.channel.repository.ChannelRepository
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.global.service.UrlService
import com.github.s8u.streamarchive.platform.enums.PlatformType
import com.github.s8u.streamarchive.record.enums.RecordQuality
import com.github.s8u.streamarchive.recordschedule.entity.RecordSchedule
import com.github.s8u.streamarchive.recordschedule.enums.RecordScheduleType
import com.github.s8u.streamarchive.recordschedule.repository.RecordScheduleRepository
import com.github.s8u.streamarchive.recordschedule.usecase.dto.command.RecordScheduleAdminCreateCommand
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import java.time.LocalDateTime
import java.util.Optional
import kotlin.test.assertEquals

class RecordScheduleAdminCreateUseCaseTest {

    private val recordScheduleRepository = mockk<RecordScheduleRepository>()
    private val channelRepository = mockk<ChannelRepository>()
    private val urlService = mockk<UrlService>()
    private val recordScheduleAdminCreateUseCase = RecordScheduleAdminCreateUseCase(
        recordScheduleRepository,
        channelRepository,
        urlService
    )

    @Nested
    inner class Create {

        @Test
        fun `채널을 찾을 수 없으면 예외를 던지고 저장하지 않는다`() {
            every { channelRepository.findById(CHANNEL_ID) } returns Optional.empty()

            val exception = assertThrows<BusinessException> {
                recordScheduleAdminCreateUseCase.create(command(RecordScheduleType.ALWAYS))
            }

            assertEquals(HttpStatus.NOT_FOUND, exception.status)
            verify(exactly = 0) { recordScheduleRepository.save(any()) }
        }

        @Test
        fun `ONCE 스케줄이 이미 존재하면 예외를 던지고 저장하지 않는다`() {
            val channel = mockk<Channel>()
            every { channelRepository.findById(CHANNEL_ID) } returns Optional.of(channel)
            every {
                recordScheduleRepository.existsByChannelAndPlatformTypeAndScheduleType(
                    channel = channel,
                    platformType = PlatformType.CHZZK,
                    scheduleType = RecordScheduleType.ONCE
                )
            } returns true

            val exception = assertThrows<BusinessException> {
                recordScheduleAdminCreateUseCase.create(command(RecordScheduleType.ONCE))
            }

            assertEquals(HttpStatus.CONFLICT, exception.status)
            verify(exactly = 0) { recordScheduleRepository.save(any()) }
        }

        @Test
        fun `ALWAYS 스케줄이 이미 존재하면 예외를 던진다`() {
            val channel = mockk<Channel>()
            every { channelRepository.findById(CHANNEL_ID) } returns Optional.of(channel)
            every {
                recordScheduleRepository.existsByChannelAndPlatformTypeAndScheduleType(
                    channel = channel,
                    platformType = PlatformType.CHZZK,
                    scheduleType = RecordScheduleType.ALWAYS
                )
            } returns true

            val exception = assertThrows<BusinessException> {
                recordScheduleAdminCreateUseCase.create(command(RecordScheduleType.ALWAYS))
            }

            assertEquals(HttpStatus.CONFLICT, exception.status)
        }

        @Test
        fun `중복 검사를 통과하면 스케줄을 저장하고 결과를 반환한다`() {
            val channel = mockk<Channel>()
            every { channelRepository.findById(CHANNEL_ID) } returns Optional.of(channel)
            every { channel.uuid } returns CHANNEL_UUID
            every {
                recordScheduleRepository.existsByChannelAndPlatformTypeAndScheduleType(
                    channel = channel,
                    platformType = PlatformType.CHZZK,
                    scheduleType = RecordScheduleType.ONCE
                )
            } returns false
            every { recordScheduleRepository.save(any()) } returns savedSchedule(channel)
            every { urlService.channelProfileUrl(CHANNEL_UUID) } returns PROFILE_URL

            val result = recordScheduleAdminCreateUseCase.create(command(RecordScheduleType.ONCE))

            assertEquals(SCHEDULE_ID, result.id)
            assertEquals(CHANNEL_ID, result.channel.id)
            assertEquals(PROFILE_URL, result.channel.profileUrl)
            assertEquals(PlatformType.CHZZK, result.platformType)
            assertEquals(RecordScheduleType.ONCE, result.scheduleType)
            verify { recordScheduleRepository.save(any()) }
        }

        @Test
        fun `SPECIFIC_DAY 스케줄은 중복 검사 없이 저장한다`() {
            val channel = mockk<Channel>()
            every { channelRepository.findById(CHANNEL_ID) } returns Optional.of(channel)
            every { channel.uuid } returns CHANNEL_UUID
            every { recordScheduleRepository.save(any()) } returns savedSchedule(channel)
            every { urlService.channelProfileUrl(CHANNEL_UUID) } returns PROFILE_URL

            recordScheduleAdminCreateUseCase.create(command(RecordScheduleType.SPECIFIC_DAY))

            verify(exactly = 0) { recordScheduleRepository.existsByChannelAndPlatformTypeAndScheduleType(any(), any(), any()) }
            verify { recordScheduleRepository.save(any()) }
        }
    }

    private fun command(scheduleType: RecordScheduleType): RecordScheduleAdminCreateCommand {
        return RecordScheduleAdminCreateCommand(
            channelId = CHANNEL_ID,
            platformType = PlatformType.CHZZK,
            scheduleType = scheduleType,
            value = SCHEDULE_VALUE
        )
    }

    private fun savedSchedule(channel: Channel): RecordSchedule {
        every { channel.id } returns CHANNEL_ID
        every { channel.name } returns CHANNEL_NAME
        val schedule = mockk<RecordSchedule>()
        every { schedule.id } returns SCHEDULE_ID
        every { schedule.channel } returns channel
        every { schedule.platformType } returns PlatformType.CHZZK
        every { schedule.scheduleType } returns RecordScheduleType.ONCE
        every { schedule.value } returns SCHEDULE_VALUE
        every { schedule.recordQuality } returns RecordQuality.BEST
        every { schedule.priority } returns 0
        every { schedule.createdAt } returns NOW
        every { schedule.updatedAt } returns NOW
        return schedule
    }

    companion object {
        private const val CHANNEL_ID = 1L
        private const val SCHEDULE_ID = 100L
        private const val CHANNEL_UUID = "channel-uuid"
        private const val CHANNEL_NAME = "테스트 채널"
        private const val SCHEDULE_VALUE = "value"
        private const val PROFILE_URL = "https://example.com/channels/channel-uuid/profile"
        private val NOW = LocalDateTime.now()
    }

}
