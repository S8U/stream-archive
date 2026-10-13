package com.github.s8u.streamarchive.recording.usecase

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.s8u.streamarchive.global.service.TransactionRunner
import com.github.s8u.streamarchive.platform.chat.dto.PlatformChatMessageDto
import com.github.s8u.streamarchive.platform.enums.PlatformChatMessageType
import com.github.s8u.streamarchive.platform.enums.PlatformChatUserRole
import com.github.s8u.streamarchive.recording.manager.RecordingChatBufferManager
import com.github.s8u.streamarchive.video.entity.VideoChatHistory
import com.github.s8u.streamarchive.video.repository.VideoChatHistoryBulkRepository
import com.github.s8u.streamarchive.video.service.VideoChatEmojiSaveService
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RecordingChatFlushUseCaseTest {

    private val recordingChatBufferManager = mockk<RecordingChatBufferManager>()
    private val videoChatEmojiSaveService = mockk<VideoChatEmojiSaveService>()
    private val videoChatHistoryBulkRepository = mockk<VideoChatHistoryBulkRepository>(relaxed = true)
    private val transactionRunner = mockk<TransactionRunner>()

    private val recordingChatFlushUseCase = RecordingChatFlushUseCase(
        recordingChatBufferManager = recordingChatBufferManager,
        videoChatEmojiSaveService = videoChatEmojiSaveService,
        videoChatHistoryBulkRepository = videoChatHistoryBulkRepository,
        transactionRunner = transactionRunner,
        objectMapper = jacksonObjectMapper()
    )

    @BeforeEach
    fun setUp() {
        every { transactionRunner.run<Any?>(any()) } answers { firstArg<() -> Any?>().invoke() }
        every { videoChatEmojiSaveService.saveAll(any(), any()) } returns emptyList()
    }

    @Nested
    inner class Flush {

        @Test
        fun `버퍼의 채팅을 종류·역할·후원 금액까지 그대로 채팅 이력으로 저장한다`() {
            every { recordingChatBufferManager.drain() } returns listOf(
                chatMessage(
                    messageType = PlatformChatMessageType.CHAT,
                    userRole = PlatformChatUserRole.MANAGER,
                    donationAmount = null
                ),
                chatMessage(
                    messageType = PlatformChatMessageType.DONATION,
                    userRole = null,
                    donationAmount = 1000
                )
            )
            val savedChatHistories = slot<List<VideoChatHistory>>()
            every { videoChatHistoryBulkRepository.bulkInsert(capture(savedChatHistories)) } returns Unit

            recordingChatFlushUseCase.flush()

            val (chat, donation) = savedChatHistories.captured
            assertEquals(PlatformChatMessageType.CHAT, chat.messageType)
            assertEquals(PlatformChatUserRole.MANAGER, chat.userRole)
            assertNull(chat.donationAmount)
            assertEquals(PlatformChatMessageType.DONATION, donation.messageType)
            assertNull(donation.userRole)
            assertEquals(1000, donation.donationAmount)
        }

        @Test
        fun `버퍼가 비어 있으면 저장하지 않는다`() {
            every { recordingChatBufferManager.drain() } returns emptyList()

            recordingChatFlushUseCase.flush()

            verify(exactly = 0) { videoChatHistoryBulkRepository.bulkInsert(any()) }
        }

    }

    private fun chatMessage(
        messageType: PlatformChatMessageType,
        userRole: PlatformChatUserRole?,
        donationAmount: Int?
    ): PlatformChatMessageDto {
        return PlatformChatMessageDto(
            recordId = RECORD_ID,
            videoId = VIDEO_ID,
            messageType = messageType,
            username = "작성자",
            userRole = userRole,
            message = "채팅",
            donationAmount = donationAmount,
            offsetMillis = 1000,
            createdAt = LocalDateTime.of(2026, 10, 14, 20, 0, 1)
        )
    }

    companion object {
        private const val RECORD_ID = 1L
        private const val VIDEO_ID = 2L
    }

}
