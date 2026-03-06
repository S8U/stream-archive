package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.dto.AdminRecordScheduleCreateRequest
import com.github.s8u.streamarchive.dto.AdminRecordScheduleUpdateRequest
import com.github.s8u.streamarchive.entity.Channel
import com.github.s8u.streamarchive.entity.RecordSchedule
import com.github.s8u.streamarchive.enums.ContentPrivacy
import com.github.s8u.streamarchive.enums.PlatformType
import com.github.s8u.streamarchive.enums.RecordQuality
import com.github.s8u.streamarchive.enums.RecordScheduleType
import com.github.s8u.streamarchive.exception.BusinessException
import com.github.s8u.streamarchive.repository.ChannelRepository
import com.github.s8u.streamarchive.repository.RecordScheduleRepository
import com.github.s8u.streamarchive.util.UrlBuilder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.http.HttpStatus
import java.util.*

@ExtendWith(MockitoExtension::class)
class RecordScheduleServiceTest {

    @Mock lateinit var recordScheduleRepository: RecordScheduleRepository
    @Mock lateinit var channelRepository: ChannelRepository
    @Mock lateinit var urlBuilder: UrlBuilder

    @InjectMocks
    lateinit var recordScheduleService: RecordScheduleService

    private lateinit var testChannel: Channel

    @BeforeEach
    fun setUp() {
        testChannel = Channel(
            id = 1L,
            uuid = "channel-uuid",
            name = "Test Channel",
            contentPrivacy = ContentPrivacy.PUBLIC
        )
    }

    @Nested
    @DisplayName("getForAdmin")
    inner class GetForAdmin {

        @Test
        @DisplayName("존재하는 스케줄을 조회한다")
        fun getExistingSchedule() {
            val schedule = RecordSchedule(
                id = 1L,
                channel = testChannel,
                platformType = PlatformType.CHZZK,
                scheduleType = RecordScheduleType.ALWAYS,
                value = "",
                recordQuality = RecordQuality.BEST,
                priority = 0
            )

            whenever(recordScheduleRepository.findById(1L)).thenReturn(Optional.of(schedule))
            whenever(urlBuilder.channelProfileUrl("channel-uuid")).thenReturn("http://profile")

            val response = recordScheduleService.getForAdmin(1L)

            assertEquals(1L, response.id)
            assertEquals(PlatformType.CHZZK, response.platformType)
            assertEquals(RecordScheduleType.ALWAYS, response.scheduleType)
        }

        @Test
        @DisplayName("존재하지 않는 스케줄 조회 시 예외가 발생한다")
        fun getNonExistentSchedule() {
            whenever(recordScheduleRepository.findById(999L)).thenReturn(Optional.empty())

            assertThrows(BusinessException::class.java) {
                recordScheduleService.getForAdmin(999L)
            }
        }
    }

    @Nested
    @DisplayName("createForAdmin")
    inner class CreateForAdmin {

        @Test
        @DisplayName("ALWAYS 스케줄을 생성한다")
        fun createAlwaysSchedule() {
            val request = AdminRecordScheduleCreateRequest(
                channelId = 1L,
                platformType = PlatformType.CHZZK,
                scheduleType = RecordScheduleType.ALWAYS,
                value = "",
                recordQuality = RecordQuality.BEST,
                priority = 0
            )

            whenever(channelRepository.findById(1L)).thenReturn(Optional.of(testChannel))
            whenever(recordScheduleRepository.existsByChannelAndPlatformTypeAndScheduleType(
                testChannel, PlatformType.CHZZK, RecordScheduleType.ALWAYS
            )).thenReturn(false)
            whenever(recordScheduleRepository.save(any<RecordSchedule>())).thenAnswer {
                val rs = it.arguments[0] as RecordSchedule
                RecordSchedule(
                    id = 10L, channel = rs.channel, platformType = rs.platformType,
                    scheduleType = rs.scheduleType, value = rs.value,
                    recordQuality = rs.recordQuality, priority = rs.priority
                )
            }
            whenever(recordScheduleRepository.findById(10L)).thenReturn(Optional.of(
                RecordSchedule(
                    id = 10L, channel = testChannel, platformType = PlatformType.CHZZK,
                    scheduleType = RecordScheduleType.ALWAYS, value = "",
                    recordQuality = RecordQuality.BEST, priority = 0
                )
            ))
            whenever(urlBuilder.channelProfileUrl("channel-uuid")).thenReturn("http://profile")

            val response = recordScheduleService.createForAdmin(request)

            assertEquals(10L, response.id)
        }

        @Test
        @DisplayName("이미 ALWAYS 스케줄이 있을 때 중복 생성 시 예외가 발생한다")
        fun createDuplicateAlwaysSchedule() {
            val request = AdminRecordScheduleCreateRequest(
                channelId = 1L,
                platformType = PlatformType.CHZZK,
                scheduleType = RecordScheduleType.ALWAYS,
                value = "",
                recordQuality = RecordQuality.BEST,
                priority = 0
            )

            whenever(channelRepository.findById(1L)).thenReturn(Optional.of(testChannel))
            whenever(recordScheduleRepository.existsByChannelAndPlatformTypeAndScheduleType(
                testChannel, PlatformType.CHZZK, RecordScheduleType.ALWAYS
            )).thenReturn(true)

            val exception = assertThrows(BusinessException::class.java) {
                recordScheduleService.createForAdmin(request)
            }
            assertEquals(HttpStatus.CONFLICT, exception.status)
        }

        @Test
        @DisplayName("이미 ONCE 스케줄이 있을 때 중복 생성 시 예외가 발생한다")
        fun createDuplicateOnceSchedule() {
            val request = AdminRecordScheduleCreateRequest(
                channelId = 1L,
                platformType = PlatformType.TWITCH,
                scheduleType = RecordScheduleType.ONCE,
                value = "",
                recordQuality = RecordQuality.P1080,
                priority = 1
            )

            whenever(channelRepository.findById(1L)).thenReturn(Optional.of(testChannel))
            whenever(recordScheduleRepository.existsByChannelAndPlatformTypeAndScheduleType(
                testChannel, PlatformType.TWITCH, RecordScheduleType.ONCE
            )).thenReturn(true)

            val exception = assertThrows(BusinessException::class.java) {
                recordScheduleService.createForAdmin(request)
            }
            assertEquals(HttpStatus.CONFLICT, exception.status)
        }

        @Test
        @DisplayName("N_DAYS_OF_EVERY_WEEK 스케줄은 중복 체크 없이 생성할 수 있다")
        fun createWeeklyScheduleNoDuplicateCheck() {
            val request = AdminRecordScheduleCreateRequest(
                channelId = 1L,
                platformType = PlatformType.CHZZK,
                scheduleType = RecordScheduleType.N_DAYS_OF_EVERY_WEEK,
                value = "[\"MONDAY\",\"FRIDAY\"]",
                recordQuality = RecordQuality.P720,
                priority = 0
            )

            whenever(channelRepository.findById(1L)).thenReturn(Optional.of(testChannel))
            whenever(recordScheduleRepository.save(any<RecordSchedule>())).thenAnswer {
                val rs = it.arguments[0] as RecordSchedule
                RecordSchedule(
                    id = 11L, channel = rs.channel, platformType = rs.platformType,
                    scheduleType = rs.scheduleType, value = rs.value,
                    recordQuality = rs.recordQuality, priority = rs.priority
                )
            }
            whenever(recordScheduleRepository.findById(11L)).thenReturn(Optional.of(
                RecordSchedule(
                    id = 11L, channel = testChannel, platformType = PlatformType.CHZZK,
                    scheduleType = RecordScheduleType.N_DAYS_OF_EVERY_WEEK,
                    value = "[\"MONDAY\",\"FRIDAY\"]",
                    recordQuality = RecordQuality.P720, priority = 0
                )
            ))
            whenever(urlBuilder.channelProfileUrl("channel-uuid")).thenReturn("http://profile")

            val response = recordScheduleService.createForAdmin(request)

            verify(recordScheduleRepository, never()).existsByChannelAndPlatformTypeAndScheduleType(
                any(), any(), any()
            )
            assertEquals(11L, response.id)
        }

        @Test
        @DisplayName("존재하지 않는 채널에 스케줄 생성 시 예외가 발생한다")
        fun createScheduleWithNonExistentChannel() {
            val request = AdminRecordScheduleCreateRequest(
                channelId = 999L,
                platformType = PlatformType.CHZZK,
                scheduleType = RecordScheduleType.ALWAYS,
                value = "",
                recordQuality = RecordQuality.BEST,
                priority = 0
            )

            whenever(channelRepository.findById(999L)).thenReturn(Optional.empty())

            assertThrows(BusinessException::class.java) {
                recordScheduleService.createForAdmin(request)
            }
        }
    }

    @Nested
    @DisplayName("delete")
    inner class Delete {

        @Test
        @DisplayName("스케줄을 soft delete한다")
        fun softDeleteSchedule() {
            val schedule = RecordSchedule(
                id = 1L, channel = testChannel, platformType = PlatformType.CHZZK,
                scheduleType = RecordScheduleType.ALWAYS, value = "",
                recordQuality = RecordQuality.BEST, priority = 0
            )

            whenever(recordScheduleRepository.findById(1L)).thenReturn(Optional.of(schedule))

            recordScheduleService.delete(1L)

            assertFalse(schedule.isActive)
            assertNotNull(schedule.deletedAt)
        }

        @Test
        @DisplayName("존재하지 않는 스케줄 삭제 시 예외가 발생한다")
        fun deleteNonExistentSchedule() {
            whenever(recordScheduleRepository.findById(999L)).thenReturn(Optional.empty())

            assertThrows(BusinessException::class.java) {
                recordScheduleService.delete(999L)
            }
        }
    }

    @Nested
    @DisplayName("deleteAllByChannelId")
    inner class DeleteAllByChannelId {

        @Test
        @DisplayName("채널의 모든 스케줄을 soft delete한다")
        fun deleteAllSchedules() {
            val schedule1 = RecordSchedule(
                id = 1L, channel = testChannel, platformType = PlatformType.CHZZK,
                scheduleType = RecordScheduleType.ALWAYS, value = "",
                recordQuality = RecordQuality.BEST, priority = 0
            )
            val schedule2 = RecordSchedule(
                id = 2L, channel = testChannel, platformType = PlatformType.TWITCH,
                scheduleType = RecordScheduleType.ONCE, value = "",
                recordQuality = RecordQuality.P1080, priority = 1
            )

            whenever(channelRepository.findById(1L)).thenReturn(Optional.of(testChannel))
            whenever(recordScheduleRepository.findByChannel(testChannel)).thenReturn(listOf(schedule1, schedule2))

            recordScheduleService.deleteAllByChannelId(1L)

            assertFalse(schedule1.isActive)
            assertFalse(schedule2.isActive)
        }
    }
}
