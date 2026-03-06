package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.dto.SaveWatchHistoryRequest
import com.github.s8u.streamarchive.entity.UserVideoWatchHistory
import com.github.s8u.streamarchive.entity.Video
import com.github.s8u.streamarchive.enums.ContentPrivacy
import com.github.s8u.streamarchive.exception.BusinessException
import com.github.s8u.streamarchive.repository.UserVideoWatchHistoryRepository
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

@ExtendWith(MockitoExtension::class)
class WatchHistoryServiceTest {

    @Mock lateinit var watchHistoryRepository: UserVideoWatchHistoryRepository
    @Mock lateinit var videoRepository: VideoRepository
    @Mock lateinit var authenticationService: AuthenticationService
    @Mock lateinit var urlBuilder: UrlBuilder

    @InjectMocks
    lateinit var watchHistoryService: WatchHistoryService

    private lateinit var testVideo: Video

    @BeforeEach
    fun setUp() {
        testVideo = Video(
            id = 1L,
            uuid = "video-uuid",
            channelId = 1L,
            title = "Test Video",
            duration = 3600,
            contentPrivacy = ContentPrivacy.PUBLIC
        )
    }

    @Nested
    @DisplayName("getWatchHistory")
    inner class GetWatchHistory {

        @Test
        @DisplayName("시청 기록이 있으면 반환한다")
        fun getExistingHistory() {
            val history = UserVideoWatchHistory(
                id = 1L, userId = 1L, videoId = 1L,
                lastPosition = 300
            )

            whenever(authenticationService.getCurrentUserId()).thenReturn(1L)
            whenever(videoRepository.findByUuid("video-uuid")).thenReturn(testVideo)
            whenever(watchHistoryRepository.findByUserIdAndVideoId(1L, 1L)).thenReturn(history)

            val response = watchHistoryService.getWatchHistory("video-uuid")

            assertNotNull(response)
            assertEquals(300, response!!.lastPosition)
        }

        @Test
        @DisplayName("시청 기록이 없으면 null을 반환한다")
        fun getNoHistory() {
            whenever(authenticationService.getCurrentUserId()).thenReturn(1L)
            whenever(videoRepository.findByUuid("video-uuid")).thenReturn(testVideo)
            whenever(watchHistoryRepository.findByUserIdAndVideoId(1L, 1L)).thenReturn(null)

            val response = watchHistoryService.getWatchHistory("video-uuid")

            assertNull(response)
        }

        @Test
        @DisplayName("로그인하지 않은 상태에서 조회 시 예외가 발생한다")
        fun getHistoryWithoutLogin() {
            whenever(authenticationService.getCurrentUserId()).thenReturn(null)

            val exception = assertThrows(BusinessException::class.java) {
                watchHistoryService.getWatchHistory("video-uuid")
            }
            assertEquals(HttpStatus.UNAUTHORIZED, exception.status)
        }

        @Test
        @DisplayName("존재하지 않는 동영상의 시청 기록 조회 시 예외가 발생한다")
        fun getHistoryForNonExistentVideo() {
            whenever(authenticationService.getCurrentUserId()).thenReturn(1L)
            whenever(videoRepository.findByUuid("nonexistent")).thenReturn(null)

            assertThrows(BusinessException::class.java) {
                watchHistoryService.getWatchHistory("nonexistent")
            }
        }
    }

    @Nested
    @DisplayName("saveWatchHistory")
    inner class SaveWatchHistory {

        @Test
        @DisplayName("새 시청 기록을 생성한다")
        fun createNewHistory() {
            val request = SaveWatchHistoryRequest(position = 120)

            whenever(authenticationService.getCurrentUserId()).thenReturn(1L)
            whenever(videoRepository.findByUuid("video-uuid")).thenReturn(testVideo)
            whenever(watchHistoryRepository.findByUserIdAndVideoId(1L, 1L)).thenReturn(null)
            whenever(watchHistoryRepository.save(any<UserVideoWatchHistory>())).thenAnswer { it.arguments[0] }

            assertDoesNotThrow { watchHistoryService.saveWatchHistory("video-uuid", request) }
            verify(watchHistoryRepository).save(argThat<UserVideoWatchHistory> {
                userId == 1L && videoId == 1L && lastPosition == 120
            })
        }

        @Test
        @DisplayName("기존 시청 기록을 업데이트한다")
        fun updateExistingHistory() {
            val request = SaveWatchHistoryRequest(position = 500)
            val existing = UserVideoWatchHistory(
                id = 1L, userId = 1L, videoId = 1L, lastPosition = 120
            )

            whenever(authenticationService.getCurrentUserId()).thenReturn(1L)
            whenever(videoRepository.findByUuid("video-uuid")).thenReturn(testVideo)
            whenever(watchHistoryRepository.findByUserIdAndVideoId(1L, 1L)).thenReturn(existing)

            watchHistoryService.saveWatchHistory("video-uuid", request)

            assertEquals(500, existing.lastPosition)
            verify(watchHistoryRepository, never()).save(any())
        }

        @Test
        @DisplayName("로그인하지 않은 상태에서 저장 시 예외가 발생한다")
        fun saveHistoryWithoutLogin() {
            whenever(authenticationService.getCurrentUserId()).thenReturn(null)

            assertThrows(BusinessException::class.java) {
                watchHistoryService.saveWatchHistory("video-uuid", SaveWatchHistoryRequest(100))
            }
        }
    }

    @Nested
    @DisplayName("deleteWatchHistory")
    inner class DeleteWatchHistory {

        @Test
        @DisplayName("특정 동영상의 시청 기록을 삭제한다")
        fun deleteHistory() {
            whenever(authenticationService.getCurrentUserId()).thenReturn(1L)
            whenever(videoRepository.findByUuid("video-uuid")).thenReturn(testVideo)

            watchHistoryService.deleteWatchHistory("video-uuid")

            verify(watchHistoryRepository).deleteByUserIdAndVideoId(1L, 1L)
        }

        @Test
        @DisplayName("로그인하지 않은 상태에서 삭제 시 예외가 발생한다")
        fun deleteWithoutLogin() {
            whenever(authenticationService.getCurrentUserId()).thenReturn(null)

            assertThrows(BusinessException::class.java) {
                watchHistoryService.deleteWatchHistory("video-uuid")
            }
        }
    }

    @Nested
    @DisplayName("deleteAllWatchHistories")
    inner class DeleteAllWatchHistories {

        @Test
        @DisplayName("모든 시청 기록을 삭제한다")
        fun deleteAll() {
            whenever(authenticationService.getCurrentUserId()).thenReturn(1L)

            watchHistoryService.deleteAllWatchHistories()

            verify(watchHistoryRepository).deleteAllByUserId(1L)
        }

        @Test
        @DisplayName("로그인하지 않은 상태에서 전체 삭제 시 예외가 발생한다")
        fun deleteAllWithoutLogin() {
            whenever(authenticationService.getCurrentUserId()).thenReturn(null)

            assertThrows(BusinessException::class.java) {
                watchHistoryService.deleteAllWatchHistories()
            }
        }
    }
}
