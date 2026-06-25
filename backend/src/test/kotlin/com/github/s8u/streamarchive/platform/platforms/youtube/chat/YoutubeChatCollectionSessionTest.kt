package com.github.s8u.streamarchive.platform.platforms.youtube.chat

import com.github.s8u.streamarchive.platform.chat.dto.PlatformChatMessageDto
import com.github.s8u.streamarchive.platform.platforms.youtube.client.YoutubeApiClient
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeLiveChatMessage
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeLiveChatMessageAuthorDetails
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeLiveChatMessageSnippet
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
            every { apiClient.getLiveChatMessages(LIVE_CHAT_ID, null) } returns YoutubeLiveChatMessagesResponse(
                nextPageToken = "next-token",
                pollingIntervalMillis = 1000,
                offlineAt = "2026-05-13T11:01:00Z",
                items = listOf(
                    youtubeChatMessage(
                        messageText = "원본 채팅",
                        displayMessage = "표시 채팅 😀"
                    )
                )
            )

            val latch = CountDownLatch(1)
            val messages = mutableListOf<PlatformChatMessageDto>()
            val session = youtubeChatCollectionSession(
                messages = messages,
                latch = latch
            )

            session.start()

            assertTrue(latch.await(1, TimeUnit.SECONDS))

            val actual = messages.first()
            assertEquals("표시 채팅 😀", actual.message)

            session.stop()
        }

    }

    private fun youtubeChatCollectionSession(
        messages: MutableList<PlatformChatMessageDto>,
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
    }

}
