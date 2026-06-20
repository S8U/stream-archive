package com.github.s8u.streamarchive.recording.usecase

import com.github.s8u.streamarchive.channelplatform.entity.ChannelPlatform
import com.github.s8u.streamarchive.channelplatform.repository.ChannelPlatformRepository
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.platform.chat.PlatformChatStrategyFactory
import com.github.s8u.streamarchive.platform.enums.PlatformType
import com.github.s8u.streamarchive.platform.strategy.PlatformStrategy
import com.github.s8u.streamarchive.platform.strategy.PlatformStrategyFactory
import com.github.s8u.streamarchive.platform.strategy.dto.PlatformStreamDto
import com.github.s8u.streamarchive.record.entity.Record
import com.github.s8u.streamarchive.record.enums.RecordQuality
import com.github.s8u.streamarchive.record.repository.RecordRepository
import com.github.s8u.streamarchive.recording.manager.RecordingChatCollectManager
import com.github.s8u.streamarchive.recording.manager.RecordingVideoCategoryChangeDetectManager
import com.github.s8u.streamarchive.recording.manager.RecordingVideoProcessManager
import com.github.s8u.streamarchive.recording.manager.RecordingVideoTitleChangeDetectManager
import com.github.s8u.streamarchive.recording.manager.RecordingVideoViewerChangeDetectManager
import com.github.s8u.streamarchive.recording.usecase.dto.command.RecordingStartCommand
import com.github.s8u.streamarchive.video.entity.Video
import com.github.s8u.streamarchive.video.repository.VideoRepository
import com.github.s8u.streamarchive.video.service.VideoArchiveService
import com.github.s8u.streamarchive.video.service.VideoCategoryHistoryAppendService
import com.github.s8u.streamarchive.video.service.VideoThumbnailSaveService
import com.github.s8u.streamarchive.video.service.VideoTitleHistoryAppendService
import com.github.s8u.streamarchive.video.service.VideoViewerHistoryAppendService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertNull

class RecordingStartUseCaseTest {

    private val recordRepository = mockk<RecordRepository>(relaxed = true)
    private val videoRepository = mockk<VideoRepository>(relaxed = true)
    private val videoArchiveService = mockk<VideoArchiveService>(relaxed = true)
    private val recordingVideoProcessManager = mockk<RecordingVideoProcessManager>(relaxed = true)
    private val recordingChatCollectManager = mockk<RecordingChatCollectManager>(relaxed = true)
    private val platformStrategyFactory = mockk<PlatformStrategyFactory>()
    private val platformChatStrategyFactory = mockk<PlatformChatStrategyFactory>(relaxed = true)
    private val channelPlatformRepository = mockk<ChannelPlatformRepository>()
    private val videoThumbnailSaveService = mockk<VideoThumbnailSaveService>(relaxed = true)
    private val viewerHistoryAppendService = mockk<VideoViewerHistoryAppendService>(relaxed = true)
    private val titleHistoryAppendService = mockk<VideoTitleHistoryAppendService>(relaxed = true)
    private val categoryHistoryAppendService = mockk<VideoCategoryHistoryAppendService>(relaxed = true)
    private val recordingVideoViewerChangeDetectManager = mockk<RecordingVideoViewerChangeDetectManager>(relaxed = true)
    private val recordingVideoTitleChangeDetectManager = mockk<RecordingVideoTitleChangeDetectManager>(relaxed = true)
    private val recordingVideoCategoryChangeDetectManager = mockk<RecordingVideoCategoryChangeDetectManager>(relaxed = true)

    private val recordingStartUseCase = RecordingStartUseCase(
        recordRepository,
        videoRepository,
        videoArchiveService,
        recordingVideoProcessManager,
        recordingChatCollectManager,
        platformStrategyFactory,
        platformChatStrategyFactory,
        channelPlatformRepository,
        videoThumbnailSaveService,
        viewerHistoryAppendService,
        titleHistoryAppendService,
        categoryHistoryAppendService,
        recordingVideoViewerChangeDetectManager,
        recordingVideoTitleChangeDetectManager,
        recordingVideoCategoryChangeDetectManager
    )

    private val savedVideo = mockk<Video>(relaxed = true).apply {
        every { id } returns VIDEO_ID
    }
    private val record = mockk<Record>(relaxed = true).apply {
        every { id } returns RECORD_ID
    }

    @BeforeEach
    fun setUp() {
        // save 결과로 ID가 부여된 엔티티가 돌아온다고 본다
        every { videoRepository.save(any()) } returns savedVideo
        every { recordRepository.save(any()) } returns record
    }

    @Nested
    inner class Start {

        @Test
        fun `수동 취소된 스트림이면 녹화를 시작하지 않는다`() {
            every {
                recordRepository.existsByPlatformTypeAndPlatformStreamIdAndIsCancelled(any(), any(), true)
            } returns true

            val result = recordingStartUseCase.start(command())

            assertNull(result)
            verify(exactly = 0) { videoRepository.save(any()) }
        }

        @Test
        fun `이미 녹화 중인 스트림이면 녹화를 시작하지 않는다`() {
            stubGuards(wasCancelled = false, isAlreadyRecording = true, failedCount = 0)

            val result = recordingStartUseCase.start(command())

            assertNull(result)
            verify(exactly = 0) { videoRepository.save(any()) }
        }

        @Test
        fun `연속 시작 실패가 한도를 넘으면 녹화를 시작하지 않는다`() {
            stubGuards(wasCancelled = false, isAlreadyRecording = false, failedCount = 3)

            val result = recordingStartUseCase.start(command())

            assertNull(result)
            verify(exactly = 0) { videoRepository.save(any()) }
        }

        @Test
        fun `정상이면 동영상과 녹화 기록을 만들고 프로세스와 채팅 수집을 시작한다`() {
            stubGuards(wasCancelled = false, isAlreadyRecording = false, failedCount = 0)
            stubStartSuccess()

            recordingStartUseCase.start(command())

            verify { videoRepository.save(any()) }
            verify { recordRepository.save(any()) }
            verify { recordingVideoProcessManager.startRecording(RECORD_ID, STREAM_URL, "best", VIDEO_ID, emptyList()) }
            verify { recordingChatCollectManager.startCollecting(RECORD_ID, VIDEO_ID, PlatformType.CHZZK, PLATFORM_CHANNEL_ID, any()) }
        }

        @Test
        fun `자동 소장 스케줄이면 동영상을 소장 처리한다`() {
            stubGuards(wasCancelled = false, isAlreadyRecording = false, failedCount = 0)
            stubStartSuccess()

            recordingStartUseCase.start(command(autoArchive = true))

            verify { videoArchiveService.setArchived(savedVideo, true, null, null) }
        }

        @Test
        fun `자동 소장 스케줄이 아니면 소장 처리하지 않는다`() {
            stubGuards(wasCancelled = false, isAlreadyRecording = false, failedCount = 0)
            stubStartSuccess()

            recordingStartUseCase.start(command(autoArchive = false))

            verify(exactly = 0) { videoArchiveService.setArchived(any(), any(), any(), any()) }
        }

        @Test
        fun `프로세스 시작에 실패하면 시작 실패로 표시하고 예외를 던진다`() {
            stubGuards(wasCancelled = false, isAlreadyRecording = false, failedCount = 0)
            stubStartSuccess()
            every { recordingVideoProcessManager.startRecording(any(), any(), any(), any(), any()) } throws RuntimeException("프로세스 실패")

            assertThrows<BusinessException> {
                recordingStartUseCase.start(command())
            }

            verify { record.failToStart() }
            verify { recordRepository.save(record) }
        }

        @Test
        fun `채널 플랫폼을 찾을 수 없으면 시작 실패로 표시하고 예외를 던진다`() {
            stubGuards(wasCancelled = false, isAlreadyRecording = false, failedCount = 0)
            every { platformStrategyFactory.getPlatformStrategy(PlatformType.CHZZK) } returns mockk(relaxed = true)
            every { channelPlatformRepository.findByChannelIdAndPlatformType(CHANNEL_ID, PlatformType.CHZZK) } returns null

            assertThrows<BusinessException> {
                recordingStartUseCase.start(command())
            }

            verify { record.failToStart() }
        }
    }

    private fun stubGuards(wasCancelled: Boolean, isAlreadyRecording: Boolean, failedCount: Long) {
        every { recordRepository.existsByPlatformTypeAndPlatformStreamIdAndIsCancelled(any(), any(), true) } returns wasCancelled
        every {
            recordRepository.existsByPlatformTypeAndPlatformStreamIdAndIsEndedAndIsCancelled(any(), any(), false, false)
        } returns isAlreadyRecording
        every { recordRepository.countByPlatformTypeAndPlatformStreamIdAndIsFailed(any(), any(), true) } returns failedCount
    }

    private fun stubStartSuccess() {
        val strategy = mockk<PlatformStrategy>()
        every { strategy.getStreamUrl(PLATFORM_CHANNEL_ID) } returns STREAM_URL
        every { strategy.getStreamlinkArgs() } returns emptyList()
        every { platformStrategyFactory.getPlatformStrategy(PlatformType.CHZZK) } returns strategy

        val channelPlatform = mockk<ChannelPlatform>(relaxed = true)
        every { channelPlatform.platformChannelId } returns PLATFORM_CHANNEL_ID
        every { channelPlatformRepository.findByChannelIdAndPlatformType(CHANNEL_ID, PlatformType.CHZZK) } returns channelPlatform
    }

    private fun command(autoArchive: Boolean = false): RecordingStartCommand {
        return RecordingStartCommand(
            channelId = CHANNEL_ID,
            stream = stream(),
            recordQuality = RecordQuality.BEST,
            autoArchive = autoArchive
        )
    }

    private fun stream(): PlatformStreamDto {
        return PlatformStreamDto(
            platformType = PlatformType.CHZZK,
            id = STREAM_ID,
            username = "스트리머",
            title = "방송 제목",
            category = "게임",
            viewerCount = 100,
            thumbnailUrl = "http://thumb",
            startedAt = null,
            platformDto = Any()
        )
    }

    companion object {
        private const val CHANNEL_ID = 1L
        private const val VIDEO_ID = 10L
        private const val RECORD_ID = 20L
        private const val STREAM_ID = "stream-1"
        private const val PLATFORM_CHANNEL_ID = "platform-channel-1"
        private const val STREAM_URL = "https://chzzk.naver.com/live/platform-channel-1"
    }

}
