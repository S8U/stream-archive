package com.github.s8u.streamarchive.platform.platforms.youtube.chat

import com.github.s8u.streamarchive.platform.chat.dto.PlatformChatMessageDto
import com.github.s8u.streamarchive.platform.chat.dto.PlatformChatRawMessageDto
import com.github.s8u.streamarchive.platform.enums.PlatformChatRawFormat
import com.github.s8u.streamarchive.platform.platforms.youtube.client.YoutubeApiClient
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeLiveChatMessage
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeLiveChatMessageAuthorDetails
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeLiveChatMessageSnippet
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeLiveChatMessagesFetchResult
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeLiveChatMessagesResponse
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeLiveChatTextMessageDetails
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class YoutubeChatCollectionSessionTest {

    private val apiClient = mockk<YoutubeApiClient>()

    @Nested
    inner class Start {

        @Test
        fun `표시 문자열이 있으면 유튜브 채팅 메시지로 우선 사용한다`() {
            every { apiClient.getLiveChatMessages(LIVE_CHAT_ID, null) } returns YoutubeLiveChatMessagesFetchResult(
                response = YoutubeLiveChatMessagesResponse(
                    nextPageToken = "next-token",
                    pollingIntervalMillis = 1000,
                    offlineAt = "2026-05-13T11:01:00Z",
                    items = listOf(
                        youtubeChatMessage(
                            messageText = "원본 채팅",
                            displayMessage = "표시 채팅 😀"
                        )
                    )
                ),
                rawBody = RAW_BODY
            )

            val latch = CountDownLatch(1)
            val messages = mutableListOf<PlatformChatMessageDto>()
            val session = youtubeChatCollectionSession(
                messages = messages,
                rawMessages = mutableListOf(),
                latch = latch
            )

            session.start()

            assertTrue(latch.await(1, TimeUnit.SECONDS))

            val actual = messages.first()
            assertEquals("표시 채팅 😀", actual.message)

            session.stop()
        }

        @Test
        fun `조회한 응답 본문을 원본 메시지로 그대로 넘긴다`() {
            every { apiClient.getLiveChatMessages(LIVE_CHAT_ID, null) } returns YoutubeLiveChatMessagesFetchResult(
                response = YoutubeLiveChatMessagesResponse(
                    nextPageToken = "next-token",
                    pollingIntervalMillis = 1000,
                    offlineAt = "2026-05-13T11:01:00Z",
                    items = listOf(
                        youtubeChatMessage(
                            messageText = "원본 채팅",
                            displayMessage = null
                        )
                    )
                ),
                rawBody = RAW_BODY
            )

            val latch = CountDownLatch(1)
            val rawMessages = mutableListOf<PlatformChatRawMessageDto>()
            val session = youtubeChatCollectionSession(
                messages = mutableListOf(),
                rawMessages = rawMessages,
                latch = latch
            )

            session.start()

            assertTrue(latch.await(1, TimeUnit.SECONDS))

            val actual = rawMessages.single()
            assertEquals(PlatformChatRawFormat.TEXT, actual.format)
            assertEquals(RAW_BODY, actual.payload)

            session.stop()
        }

        @Test
        fun `응답 본문을 해석하지 못해도 원본을 넘긴 뒤 연결 종료를 알린다`() {
            every { apiClient.getLiveChatMessages(LIVE_CHAT_ID, null) } returns YoutubeLiveChatMessagesFetchResult(
                response = null,
                rawBody = RAW_BODY
            )

            val closedLatch = CountDownLatch(1)
            val rawMessages = mutableListOf<PlatformChatRawMessageDto>()
            val session = YoutubeChatCollectionSession(
                apiClient = apiClient,
                recordId = RECORD_ID,
                videoId = VIDEO_ID,
                liveChatId = LIVE_CHAT_ID,
                recordStartedAt = LocalDateTime.of(2026, 5, 13, 20, 0, 0),
                onChat = {},
                onRaw = { rawMessages.add(it) },
                onClosed = { closedLatch.countDown() }
            )

            session.start()

            assertTrue(closedLatch.await(1, TimeUnit.SECONDS))
            assertEquals(RAW_BODY, rawMessages.single().payload)

            session.stop()
        }

    }

    private fun youtubeChatCollectionSession(
        messages: MutableList<PlatformChatMessageDto>,
        rawMessages: MutableList<PlatformChatRawMessageDto>,
        latch: CountDownLatch
    ): YoutubeChatCollectionSession {
        return YoutubeChatCollectionSession(
            apiClient = apiClient,
            recordId = RECORD_ID,
            videoId = VIDEO_ID,
            liveChatId = LIVE_CHAT_ID,
            recordStartedAt = LocalDateTime.of(2026, 5, 13, 20, 0, 0),
            onChat = {
                messages.add(it)
                latch.countDown()
            },
            onRaw = { rawMessages.add(it) },
            onClosed = {}
        )
    }

    private fun youtubeChatMessage(
        messageText: String,
        displayMessage: String?
    ): YoutubeLiveChatMessage {
        return YoutubeLiveChatMessage(
            id = "chat-message-1",
            snippet = YoutubeLiveChatMessageSnippet(
                type = "textMessageEvent",
                publishedAt = "2026-05-13T11:00:05Z",
                displayMessage = displayMessage,
                textMessageDetails = YoutubeLiveChatTextMessageDetails(
                    messageText = messageText
                )
            ),
            authorDetails = YoutubeLiveChatMessageAuthorDetails(
                displayName = "테스트 사용자"
            )
        )
    }

    companion object {
        private const val RECORD_ID = 1L
        private const val VIDEO_ID = 2L
        private const val LIVE_CHAT_ID = "live-chat-123"
        private const val RAW_BODY = "{\"kind\":\"youtube#liveChatMessageListResponse\"}"
    }

}
