package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.entity.ChannelPlatform
import com.github.s8u.streamarchive.entity.Record
import com.github.s8u.streamarchive.entity.Video
import com.github.s8u.streamarchive.enums.ContentPrivacy
import com.github.s8u.streamarchive.enums.PlatformType
import com.github.s8u.streamarchive.enums.RecordQuality
import com.github.s8u.streamarchive.exception.BusinessException
import com.github.s8u.streamarchive.platform.PlatformStrategy
import com.github.s8u.streamarchive.platform.PlatformStrategyFactory
import com.github.s8u.streamarchive.platform.PlatformStreamDto
import com.github.s8u.streamarchive.recorder.ChatRecordWebSocketManager
import com.github.s8u.streamarchive.recorder.RecordProcessManager
import com.github.s8u.streamarchive.repository.ChannelPlatformRepository
import com.github.s8u.streamarchive.repository.RecordRepository
import com.github.s8u.streamarchive.repository.RecordScheduleRepository
import com.github.s8u.streamarchive.repository.VideoRepository
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
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class RecordServiceTest {

    @Mock lateinit var recordRepository: RecordRepository
    @Mock lateinit var videoRepository: VideoRepository
    @Mock lateinit var recordProcessManager: RecordProcessManager
    @Mock lateinit var chatRecordWebSocketManager: ChatRecordWebSocketManager
    @Mock lateinit var platformStrategyFactory: PlatformStrategyFactory
    @Mock lateinit var channelPlatformRepository: ChannelPlatformRepository
    @Mock lateinit var videoMetadataService: VideoMetadataService
    @Mock lateinit var recordScheduleRepository: RecordScheduleRepository
    @Mock lateinit var recordScheduleService: RecordScheduleService
    @Mock lateinit var videoThumbnailService: VideoThumbnailService
    @Mock lateinit var videoService: VideoService
    @Mock lateinit var urlBuilder: UrlBuilder
    @Mock lateinit var viewerHistoryService: VideoMetadataViewerHistoryService
    @Mock lateinit var titleHistoryService: VideoMetadataTitleHistoryService
    @Mock lateinit var categoryHistoryService: VideoMetadataCategoryHistoryService

    @InjectMocks
    lateinit var recordService: RecordService

    private lateinit var testStream: PlatformStreamDto

    @BeforeEach
    fun setUp() {
        testStream = PlatformStreamDto(
            platformType = PlatformType.CHZZK,
            id = "stream-123",
            username = "streamer",
            title = "Test Stream",
            category = "Game",
            viewerCount = 100,
            thumbnailUrl = "http://thumb.jpg",
            startedAt = LocalDateTime.now(),
            platformDto = Object()
        )
    }

    @Nested
    @DisplayName("startRecording")
    inner class StartRecording {

        @Test
        @DisplayName("취소된 방송은 녹화를 건너뛴다")
        fun skipCancelledStream() {
            whenever(recordRepository.existsByPlatformTypeAndPlatformStreamIdAndIsCancelled(
                PlatformType.CHZZK, "stream-123", true
            )).thenReturn(true)

            val result = recordService.startRecording(1L, testStream, RecordQuality.BEST)

            assertNull(result)
            verify(videoRepository, never()).save(any())
        }

        @Test
        @DisplayName("이미 녹화 중인 스트림은 건너뛴다")
        fun skipAlreadyRecording() {
            whenever(recordRepository.existsByPlatformTypeAndPlatformStreamIdAndIsCancelled(
                PlatformType.CHZZK, "stream-123", true
            )).thenReturn(false)
            whenever(recordRepository.existsByPlatformTypeAndPlatformStreamIdAndIsEndedAndIsCancelled(
                PlatformType.CHZZK, "stream-123", false, false
            )).thenReturn(true)

            val result = recordService.startRecording(1L, testStream, RecordQuality.BEST)

            assertNull(result)
            verify(videoRepository, never()).save(any())
        }

        @Test
        @DisplayName("새 녹화를 성공적으로 시작한다")
        fun startRecordingSuccess() {
            val strategy = mock<PlatformStrategy>()
            val channelPlatform = mock<ChannelPlatform>()

            whenever(recordRepository.existsByPlatformTypeAndPlatformStreamIdAndIsCancelled(
                any(), any(), any()
            )).thenReturn(false)
            whenever(recordRepository.existsByPlatformTypeAndPlatformStreamIdAndIsEndedAndIsCancelled(
                any(), any(), any(), any()
            )).thenReturn(false)
            whenever(platformStrategyFactory.getPlatformStrategy(PlatformType.CHZZK)).thenReturn(strategy)
            whenever(strategy.getChatSyncOffsetMillis()).thenReturn(5000L)
            whenever(videoRepository.save(any<Video>())).thenAnswer {
                val v = it.arguments[0] as Video
                Video(
                    id = 10L, uuid = v.uuid, channelId = v.channelId,
                    title = v.title, contentPrivacy = v.contentPrivacy,
                    chatSyncOffsetMillis = v.chatSyncOffsetMillis
                )
            }
            whenever(recordRepository.save(any<Record>())).thenAnswer {
                val r = it.arguments[0] as Record
                Record(
                    id = 20L, channelId = r.channelId, videoId = r.videoId,
                    platformType = r.platformType, platformStreamId = r.platformStreamId,
                    recordQuality = r.recordQuality
                )
            }
            whenever(channelPlatformRepository.findByChannelIdAndPlatformType(1L, PlatformType.CHZZK))
                .thenReturn(channelPlatform)
            whenever(channelPlatform.platformChannelId).thenReturn("chzzk-channel-id")
            whenever(strategy.getStreamUrl("chzzk-channel-id")).thenReturn("https://chzzk.naver.com/live/...")
            whenever(strategy.getStreamlinkArgs()).thenReturn(emptyList())

            val result = recordService.startRecording(1L, testStream, RecordQuality.BEST)

            assertNotNull(result)
            verify(videoRepository).save(any<Video>())
            verify(recordRepository, atLeast(1)).save(any<Record>())
            verify(recordProcessManager).startRecording(
                recordId = 20L,
                streamUrl = "https://chzzk.naver.com/live/...",
                quality = "best",
                videoId = 10L,
                streamlinkArgs = emptyList()
            )
            verify(viewerHistoryService).saveViewerCount(20L, 10L, 100, 0)
            verify(titleHistoryService).saveTitle(20L, 10L, "Test Stream", 0)
            verify(categoryHistoryService).saveCategory(20L, 10L, "Game", 0)
        }

        @Test
        @DisplayName("프로세스 시작 실패 시 Record를 취소 상태로 변경한다")
        fun startRecordingProcessFail() {
            val strategy = mock<PlatformStrategy>()
            val channelPlatform = mock<ChannelPlatform>()

            whenever(recordRepository.existsByPlatformTypeAndPlatformStreamIdAndIsCancelled(
                any(), any(), any()
            )).thenReturn(false)
            whenever(recordRepository.existsByPlatformTypeAndPlatformStreamIdAndIsEndedAndIsCancelled(
                any(), any(), any(), any()
            )).thenReturn(false)
            whenever(platformStrategyFactory.getPlatformStrategy(PlatformType.CHZZK)).thenReturn(strategy)
            whenever(strategy.getChatSyncOffsetMillis()).thenReturn(5000L)
            whenever(videoRepository.save(any<Video>())).thenAnswer {
                val v = it.arguments[0] as Video
                Video(id = 10L, uuid = v.uuid, channelId = v.channelId,
                    title = v.title, contentPrivacy = v.contentPrivacy)
            }
            whenever(recordRepository.save(any<Record>())).thenAnswer {
                val r = it.arguments[0] as Record
                Record(id = 20L, channelId = r.channelId, videoId = r.videoId,
                    platformType = r.platformType, platformStreamId = r.platformStreamId,
                    recordQuality = r.recordQuality)
            }
            whenever(channelPlatformRepository.findByChannelIdAndPlatformType(1L, PlatformType.CHZZK))
                .thenReturn(channelPlatform)
            whenever(channelPlatform.platformChannelId).thenReturn("chzzk-channel-id")
            whenever(strategy.getStreamUrl("chzzk-channel-id")).thenReturn("https://stream-url")
            whenever(strategy.getStreamlinkArgs()).thenReturn(emptyList())
            whenever(recordProcessManager.startRecording(any(), any(), any(), any(), any()))
                .thenThrow(RuntimeException("Process failed"))

            assertThrows(BusinessException::class.java) {
                recordService.startRecording(1L, testStream, RecordQuality.BEST)
            }

            // Record가 취소 상태로 저장되었는지 확인
            verify(recordRepository, atLeast(2)).save(argThat<Record> {
                isEnded && isCancelled
            })
        }
    }

    @Nested
    @DisplayName("endRecording")
    inner class EndRecording {

        @Test
        @DisplayName("녹화를 정상 종료한다")
        fun endRecordingSuccess() {
            val record = Record(
                id = 1L, channelId = 1L, videoId = 10L,
                platformType = PlatformType.CHZZK,
                platformStreamId = "stream-123",
                recordQuality = "best",
                createdAt = LocalDateTime.now().minusMinutes(30)
            )
            val video = Video(
                id = 10L, uuid = "video-uuid", channelId = 1L,
                title = "Test", contentPrivacy = ContentPrivacy.PUBLIC
            )

            whenever(recordRepository.findById(1L)).thenReturn(Optional.of(record))
            whenever(videoRepository.findById(10L)).thenReturn(Optional.of(video))
            whenever(videoMetadataService.calculateFileSize(10L)).thenReturn(5000000L)
            whenever(videoMetadataService.calculateDuration(10L)).thenReturn(1800)
            whenever(videoRepository.save(any<Video>())).thenReturn(video)
            whenever(recordRepository.save(any<Record>())).thenReturn(record)

            recordService.endRecording(1L)

            assertTrue(record.isEnded)
            assertFalse(record.isCancelled)
            assertNotNull(record.endedAt)
            verify(chatRecordWebSocketManager).stopRecording(1L)
            verify(viewerHistoryService).clearCache(1L)
            verify(titleHistoryService).clearCache(1L)
            verify(categoryHistoryService).clearCache(1L)
        }

        @Test
        @DisplayName("이미 종료된 녹화는 무시한다")
        fun endAlreadyEndedRecording() {
            val record = Record(
                id = 1L, channelId = 1L, videoId = 10L,
                platformType = PlatformType.CHZZK,
                platformStreamId = "stream-123",
                recordQuality = "best"
            ).apply { isEnded = true }

            whenever(recordRepository.findById(1L)).thenReturn(Optional.of(record))

            recordService.endRecording(1L)

            verify(chatRecordWebSocketManager, never()).stopRecording(any())
        }

        @Test
        @DisplayName("취소 녹화 시 프로세스를 강제 종료한다")
        fun cancelRecording() {
            val record = Record(
                id = 1L, channelId = 1L, videoId = 10L,
                platformType = PlatformType.CHZZK,
                platformStreamId = "stream-123",
                recordQuality = "best",
                createdAt = LocalDateTime.now().minusMinutes(30)
            )
            val video = Video(
                id = 10L, uuid = "video-uuid", channelId = 1L,
                title = "Test", contentPrivacy = ContentPrivacy.PUBLIC
            )

            whenever(recordRepository.findById(1L)).thenReturn(Optional.of(record))
            whenever(videoRepository.findById(10L)).thenReturn(Optional.of(video))
            whenever(videoMetadataService.calculateFileSize(10L)).thenReturn(5000000L)
            whenever(videoMetadataService.calculateDuration(10L)).thenReturn(1800)
            whenever(videoRepository.save(any<Video>())).thenReturn(video)
            whenever(recordRepository.save(any<Record>())).thenReturn(record)

            recordService.endRecording(1L, isCancel = true)

            assertTrue(record.isEnded)
            assertTrue(record.isCancelled)
            verify(recordProcessManager).stopRecording(1L)
        }

        @Test
        @DisplayName("10초 미만 녹화는 비디오를 삭제한다")
        fun deleteShortRecording() {
            val record = Record(
                id = 1L, channelId = 1L, videoId = 10L,
                platformType = PlatformType.CHZZK,
                platformStreamId = "stream-123",
                recordQuality = "best",
                createdAt = LocalDateTime.now().minusSeconds(5)
            )

            whenever(recordRepository.findById(1L)).thenReturn(Optional.of(record))
            whenever(recordRepository.save(any<Record>())).thenReturn(record)

            recordService.endRecording(1L)

            assertTrue(record.isEnded)
            verify(videoService).delete(10L)
        }

        @Test
        @DisplayName("존재하지 않는 녹화 종료 시 예외가 발생한다")
        fun endNonExistentRecording() {
            whenever(recordRepository.findById(999L)).thenReturn(Optional.empty())

            assertThrows(BusinessException::class.java) {
                recordService.endRecording(999L)
            }
        }
    }

    @Nested
    @DisplayName("isEndingRecord")
    inner class IsEndingRecord {

        @Test
        @DisplayName("종료 처리 중이 아닌 녹화는 false를 반환한다")
        fun notEndingRecord() {
            assertFalse(recordService.isEndingRecord(999L))
        }
    }

    @Nested
    @DisplayName("getForAdmin")
    inner class GetForAdmin {

        @Test
        @DisplayName("존재하지 않는 녹화 조회 시 예외가 발생한다")
        fun getNonExistentRecord() {
            whenever(recordRepository.findById(999L)).thenReturn(Optional.empty())

            assertThrows(BusinessException::class.java) {
                recordService.getForAdmin(999L)
            }
        }
    }
}
