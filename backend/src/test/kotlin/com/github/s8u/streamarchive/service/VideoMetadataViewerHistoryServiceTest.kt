package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.entity.VideoMetadataViewerHistory
import com.github.s8u.streamarchive.recorder.RecordProcessManager
import com.github.s8u.streamarchive.repository.RecordRepository
import com.github.s8u.streamarchive.repository.VideoMetadataViewerHistoryRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*

@ExtendWith(MockitoExtension::class)
class VideoMetadataViewerHistoryServiceTest {

    @Mock lateinit var viewerHistoryRepository: VideoMetadataViewerHistoryRepository
    @Mock lateinit var recordRepository: RecordRepository
    @Mock lateinit var recordProcessManager: RecordProcessManager

    @InjectMocks
    lateinit var viewerHistoryService: VideoMetadataViewerHistoryService

    @Nested
    @DisplayName("saveViewerCount")
    inner class SaveViewerCount {

        @Test
        @DisplayName("시청자 수가 null이 아닌 경우 저장한다")
        fun saveNonNullViewerCount() {
            whenever(viewerHistoryRepository.save(any<VideoMetadataViewerHistory>())).thenAnswer { it.arguments[0] }

            viewerHistoryService.saveViewerCount(1L, 10L, 100, 5000L)

            verify(viewerHistoryRepository).save(argThat<VideoMetadataViewerHistory> {
                videoId == 10L && viewerCount == 100 && offsetMillis == 5000L
            })
        }

        @Test
        @DisplayName("시청자 수가 null인 경우 저장하지 않는다")
        fun skipNullViewerCount() {
            viewerHistoryService.saveViewerCount(1L, 10L, null, 5000L)

            verify(viewerHistoryRepository, never()).save(any())
        }

        @Test
        @DisplayName("시청자 수 0도 저장한다")
        fun saveZeroViewerCount() {
            whenever(viewerHistoryRepository.save(any<VideoMetadataViewerHistory>())).thenAnswer { it.arguments[0] }

            viewerHistoryService.saveViewerCount(1L, 10L, 0, 1000L)

            verify(viewerHistoryRepository).save(argThat<VideoMetadataViewerHistory> {
                viewerCount == 0
            })
        }
    }

    @Nested
    @DisplayName("clearCache")
    inner class ClearCache {

        @Test
        @DisplayName("캐시를 정리해도 예외가 발생하지 않는다")
        fun clearCacheNoException() {
            assertDoesNotThrow { viewerHistoryService.clearCache(1L) }
        }

        @Test
        @DisplayName("존재하지 않는 recordId의 캐시도 정리할 수 있다")
        fun clearNonExistentCache() {
            assertDoesNotThrow { viewerHistoryService.clearCache(999L) }
        }
    }
}
