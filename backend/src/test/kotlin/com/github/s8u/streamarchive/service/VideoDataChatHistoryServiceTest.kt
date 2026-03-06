package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.chat.ChatMessageDto
import com.github.s8u.streamarchive.entity.Video
import com.github.s8u.streamarchive.entity.VideoDataChatHistory
import com.github.s8u.streamarchive.enums.ContentPrivacy
import com.github.s8u.streamarchive.exception.BusinessException
import com.github.s8u.streamarchive.repository.VideoDataChatHistoryBulkRepository
import com.github.s8u.streamarchive.repository.VideoDataChatHistoryRepository
import com.github.s8u.streamarchive.repository.VideoRepository
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

@ExtendWith(MockitoExtension::class)
class VideoDataChatHistoryServiceTest {

    @Mock lateinit var videoDataChatHistoryRepository: VideoDataChatHistoryRepository
    @Mock lateinit var videoDataChatHistoryBulkRepository: VideoDataChatHistoryBulkRepository
    @Mock lateinit var videoRepository: VideoRepository
    @Mock lateinit var authenticationService: AuthenticationService

    @InjectMocks
    lateinit var chatHistoryService: VideoDataChatHistoryService

    @Nested
    @DisplayName("addBuffer")
    inner class AddBuffer {

        @Test
        @DisplayName("채팅 메시지를 버퍼에 추가한다")
        fun addMessageToBuffer() {
            val message = ChatMessageDto(
                recordId = 1L,
                videoId = 1L,
                username = "user1",
                message = "Hello",
                offsetMillis = 5000L,
                createdAt = LocalDateTime.now()
            )

            assertDoesNotThrow { chatHistoryService.addBuffer(message) }
        }
    }

    @Nested
    @DisplayName("flush")
    inner class Flush {

        @Test
        @DisplayName("버퍼가 비어있으면 아무 것도 하지 않는다")
        fun flushEmptyBuffer() {
            chatHistoryService.flush()

            verify(videoDataChatHistoryBulkRepository, never()).bulkInsert(any())
        }

        @Test
        @DisplayName("버퍼의 메시지를 일괄 저장하고 비운다")
        fun flushWithMessages() {
            val message1 = ChatMessageDto(1L, 1L, "user1", "msg1", 1000L, LocalDateTime.now())
            val message2 = ChatMessageDto(1L, 1L, "user2", "msg2", 2000L, LocalDateTime.now())

            chatHistoryService.addBuffer(message1)
            chatHistoryService.addBuffer(message2)

            chatHistoryService.flush()

            verify(videoDataChatHistoryBulkRepository).bulkInsert(argThat<List<ChatMessageDto>> {
                size == 2
            })

            // 두 번째 flush에서는 빈 버퍼
            chatHistoryService.flush()
            verify(videoDataChatHistoryBulkRepository, times(1)).bulkInsert(any())
        }
    }

    @Nested
    @DisplayName("getChatHistoriesByVideoIdForPublic")
    inner class GetChatHistories {

        @Test
        @DisplayName("offset 범위로 채팅 이력을 조회한다")
        fun getChatHistoriesInRange() {
            val video = Video(
                id = 1L, uuid = "video-uuid", channelId = 1L,
                title = "Test", contentPrivacy = ContentPrivacy.PUBLIC
            )
            val chatHistories = listOf(
                VideoDataChatHistory(id = 1L, videoId = 1L, username = "user1", message = "hi", offsetMillis = 1000L),
                VideoDataChatHistory(id = 2L, videoId = 1L, username = "user2", message = "hello", offsetMillis = 2000L)
            )

            whenever(videoRepository.findByUuid("video-uuid")).thenReturn(video)
            whenever(videoDataChatHistoryRepository.findByVideoIdAndOffsetMillisGreaterThanEqualAndOffsetMillisLessThanOrderByOffsetMillisAsc(
                1L, 0L, 5000L
            )).thenReturn(chatHistories)

            val result = chatHistoryService.getChatHistoriesByVideoIdForPublic("video-uuid", 0L, 5000L)

            assertEquals(2, result.size)
            assertEquals("user1", result[0].username)
            assertEquals("hi", result[0].message)
            assertEquals(1000L, result[0].offsetMillis)
        }

        @Test
        @DisplayName("음수 offset 시 예외가 발생한다")
        fun negativeOffset() {
            val exception = assertThrows(BusinessException::class.java) {
                chatHistoryService.getChatHistoriesByVideoIdForPublic("video-uuid", -1L, 5000L)
            }
            assertEquals(HttpStatus.BAD_REQUEST, exception.status)
        }

        @Test
        @DisplayName("offsetStart가 offsetEnd보다 클 때 예외가 발생한다")
        fun startGreaterThanEnd() {
            val exception = assertThrows(BusinessException::class.java) {
                chatHistoryService.getChatHistoriesByVideoIdForPublic("video-uuid", 5000L, 1000L)
            }
            assertEquals(HttpStatus.BAD_REQUEST, exception.status)
        }

        @Test
        @DisplayName("존재하지 않는 동영상 조회 시 예외가 발생한다")
        fun nonExistentVideo() {
            whenever(videoRepository.findByUuid("nonexistent")).thenReturn(null)

            assertThrows(BusinessException::class.java) {
                chatHistoryService.getChatHistoriesByVideoIdForPublic("nonexistent", 0L, 5000L)
            }
        }

        @Test
        @DisplayName("비공개 동영상의 채팅을 일반 사용자가 조회 시 예외가 발생한다")
        fun privateVideoChatAsUser() {
            val video = Video(
                id = 1L, uuid = "private-uuid", channelId = 1L,
                title = "Private", contentPrivacy = ContentPrivacy.PRIVATE
            )
            whenever(videoRepository.findByUuid("private-uuid")).thenReturn(video)
            whenever(authenticationService.isAdmin()).thenReturn(false)

            assertThrows(BusinessException::class.java) {
                chatHistoryService.getChatHistoriesByVideoIdForPublic("private-uuid", 0L, 5000L)
            }
        }

        @Test
        @DisplayName("비공개 동영상의 채팅을 관리자는 조회할 수 있다")
        fun privateVideoChatAsAdmin() {
            val video = Video(
                id = 1L, uuid = "private-uuid", channelId = 1L,
                title = "Private", contentPrivacy = ContentPrivacy.PRIVATE
            )
            whenever(videoRepository.findByUuid("private-uuid")).thenReturn(video)
            whenever(authenticationService.isAdmin()).thenReturn(true)
            whenever(videoDataChatHistoryRepository.findByVideoIdAndOffsetMillisGreaterThanEqualAndOffsetMillisLessThanOrderByOffsetMillisAsc(
                1L, 0L, 5000L
            )).thenReturn(emptyList())

            val result = chatHistoryService.getChatHistoriesByVideoIdForPublic("private-uuid", 0L, 5000L)

            assertEquals(0, result.size)
        }
    }
}
