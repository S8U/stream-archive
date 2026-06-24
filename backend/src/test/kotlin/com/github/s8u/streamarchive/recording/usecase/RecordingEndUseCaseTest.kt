package com.github.s8u.streamarchive.recording.usecase

import com.github.s8u.streamarchive.global.service.TransactionRunner
import com.github.s8u.streamarchive.record.entity.Record
import com.github.s8u.streamarchive.record.repository.RecordRepository
import com.github.s8u.streamarchive.recording.manager.RecordingChatCollectManager
import com.github.s8u.streamarchive.recording.manager.RecordingEndStateManager
import com.github.s8u.streamarchive.recording.manager.RecordingVideoCategoryChangeDetectManager
import com.github.s8u.streamarchive.recording.manager.RecordingVideoProcessManager
import com.github.s8u.streamarchive.recording.manager.RecordingVideoTitleChangeDetectManager
import com.github.s8u.streamarchive.recording.manager.RecordingVideoViewerChangeDetectManager
import com.github.s8u.streamarchive.video.entity.Video
import com.github.s8u.streamarchive.video.repository.VideoRepository
import com.github.s8u.streamarchive.video.service.VideoDeleteService
import com.github.s8u.streamarchive.video.service.VideoMetadataCalculateService
import com.github.s8u.streamarchive.video.service.VideoThumbnailSaveService
import com.github.s8u.streamarchive.video.service.VideoTitleHistoryGetService
import com.github.s8u.streamarchive.video.service.VideoViewerHistoryGetService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.Optional

class RecordingEndUseCaseTest {

    // 트랜잭션 경계는 TransactionRunnerTest가 검증하므로, 여기서는 람다만 실행한다
    private val transactionRunner = mockk<TransactionRunner>()

    private val recordRepository = mockk<RecordRepository>(relaxed = true)
    private val videoRepository = mockk<VideoRepository>(relaxed = true)
    private val recordingVideoProcessManager = mockk<RecordingVideoProcessManager>(relaxed = true)
    private val recordingChatCollectManager = mockk<RecordingChatCollectManager>(relaxed = true)
    private val videoMetadataCalculateService = mockk<VideoMetadataCalculateService>(relaxed = true)
    private val videoThumbnailSaveService = mockk<VideoThumbnailSaveService>(relaxed = true)
    private val videoDeleteService = mockk<VideoDeleteService>(relaxed = true)
    private val viewerHistoryGetService = mockk<VideoViewerHistoryGetService>(relaxed = true)
    private val titleHistoryGetService = mockk<VideoTitleHistoryGetService>(relaxed = true)
    private val recordingVideoViewerChangeDetectManager = mockk<RecordingVideoViewerChangeDetectManager>(relaxed = true)
    private val recordingVideoTitleChangeDetectManager = mockk<RecordingVideoTitleChangeDetectManager>(relaxed = true)
    private val recordingVideoCategoryChangeDetectManager =
        mockk<RecordingVideoCategoryChangeDetectManager>(relaxed = true)

    // 메모리 상태 매니저는 순수 로직이라 실제 객체를 쓴다
    private val recordingEndStateManager = RecordingEndStateManager()

    private val recordingEndUseCase = RecordingEndUseCase(
        transactionRunner = transactionRunner,
        recordRepository = recordRepository,
        videoRepository = videoRepository,
        recordingVideoProcessManager = recordingVideoProcessManager,
        recordingChatCollectManager = recordingChatCollectManager,
        videoMetadataCalculateService = videoMetadataCalculateService,
        videoThumbnailSaveService = videoThumbnailSaveService,
        videoDeleteService = videoDeleteService,
        viewerHistoryGetService = viewerHistoryGetService,
        titleHistoryGetService = titleHistoryGetService,
        recordingVideoViewerChangeDetectManager = recordingVideoViewerChangeDetectManager,
        recordingVideoTitleChangeDetectManager = recordingVideoTitleChangeDetectManager,
        recordingVideoCategoryChangeDetectManager = recordingVideoCategoryChangeDetectManager,
        recordingEndStateManager = recordingEndStateManager
    )

    @BeforeEach
    fun setUp() {
        // 트랜잭션 안에서 람다가 실행된다고 보고, 넘어온 람다를 그대로 실행한다
        every { transactionRunner.run<Any?>(any()) } answers { firstArg<() -> Any?>().invoke() }
        every { transactionRunner.runWithRetry<Any?>(any()) } answers { firstArg<() -> Any?>().invoke() }

        // 제네릭 save는 저장한 엔티티를 그대로 돌려준다
        every { recordRepository.save(any()) } answers { firstArg() }
        every { videoRepository.save(any()) } answers { firstArg() }
    }

    @Nested
    inner class End {

        @Test
        fun `이미 종료된 녹화면 동영상 삭제도 메타데이터 확정도 하지 않는다`() {
            val record = endedRecord()
            every { recordRepository.findById(RECORD_ID) } returns Optional.of(record)

            recordingEndUseCase.end(RECORD_ID)

            verify(exactly = 0) { recordRepository.save(any()) }
            verify(exactly = 0) { videoDeleteService.delete(any()) }
            verify(exactly = 0) { videoMetadataCalculateService.calculateFileSize(any()) }
        }

        @Test
        fun `충분히 긴 녹화면 종료를 확정하고 메타데이터를 확정한다`() {
            val record = activeRecord(durationSeconds = 60)
            every { recordRepository.findById(RECORD_ID) } returns Optional.of(record)
            every { videoRepository.findById(VIDEO_ID) } returns Optional.of(mockk<Video>(relaxed = true))

            recordingEndUseCase.end(RECORD_ID)

            verify { record.end(false) }
            verify { recordRepository.save(record) }
            verify { videoMetadataCalculateService.calculateFileSize(VIDEO_ID) }
            verify(exactly = 0) { videoDeleteService.delete(any()) }
        }

        @Test
        fun `너무 짧은 녹화는 동영상을 지우고 시작 실패로 표시한다`() {
            val record = activeRecord(durationSeconds = 5)
            every { recordRepository.findById(RECORD_ID) } returns Optional.of(record)

            recordingEndUseCase.end(RECORD_ID, isCancelled = false)

            verify { videoDeleteService.delete(VIDEO_ID) }
            verify { record.markFailed() }
            verify(exactly = 0) { videoMetadataCalculateService.calculateFileSize(any()) }
        }

        @Test
        fun `짧은 녹화라도 수동 취소면 시작 실패로 표시하지 않는다`() {
            val record = activeRecord(durationSeconds = 5)
            every { recordRepository.findById(RECORD_ID) } returns Optional.of(record)

            recordingEndUseCase.end(RECORD_ID, isCancelled = true)

            verify { videoDeleteService.delete(VIDEO_ID) }
            verify(exactly = 0) { record.markFailed() }
        }

        @Test
        fun `메타데이터 확정이 실패해도 종료는 되돌려지지 않는다`() {
            val record = activeRecord(durationSeconds = 60)
            every { recordRepository.findById(RECORD_ID) } returns Optional.of(record)
            every { videoMetadataCalculateService.calculateFileSize(VIDEO_ID) } throws RuntimeException("계산 실패")

            // 예외가 전파되지 않고 종료 확정은 이미 이뤄졌다
            recordingEndUseCase.end(RECORD_ID)

            verify { record.end(false) }
            verify { recordRepository.save(record) }
        }

        @Test
        fun `이미 종료 처리 중이면 아무것도 하지 않는다`() {
            recordingEndStateManager.markEnding(RECORD_ID)

            recordingEndUseCase.end(RECORD_ID)

            verify(exactly = 0) { recordRepository.findById(any()) }
        }
    }

    private fun activeRecord(durationSeconds: Long): Record {
        val record = mockk<Record>(relaxed = true)
        every { record.isEnded } returns false
        every { record.videoId } returns VIDEO_ID
        every { record.createdAt } returns BASE_TIME
        every { record.endedAt } returns BASE_TIME.plusSeconds(durationSeconds)
        return record
    }

    private fun endedRecord(): Record {
        val record = mockk<Record>(relaxed = true)
        every { record.isEnded } returns true
        return record
    }

    companion object {
        private const val RECORD_ID = 1L
        private const val VIDEO_ID = 10L
        private val BASE_TIME: LocalDateTime = LocalDateTime.of(2026, 1, 1, 0, 0, 0)
    }

}
