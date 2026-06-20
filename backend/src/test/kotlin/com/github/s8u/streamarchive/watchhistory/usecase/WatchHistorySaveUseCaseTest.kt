package com.github.s8u.streamarchive.watchhistory.usecase

import com.github.s8u.streamarchive.auth.security.service.CurrentUserService
import com.github.s8u.streamarchive.channel.entity.Channel
import com.github.s8u.streamarchive.channel.enums.ChannelContentPrivacy
import com.github.s8u.streamarchive.video.service.VideoAccessAssertService
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.video.entity.Video
import com.github.s8u.streamarchive.video.enums.VideoContentPrivacy
import com.github.s8u.streamarchive.video.repository.VideoRepository
import com.github.s8u.streamarchive.watchhistory.entity.UserVideoWatchHistory
import com.github.s8u.streamarchive.watchhistory.repository.UserVideoWatchHistoryRepository
import com.github.s8u.streamarchive.watchhistory.usecase.dto.command.WatchHistorySaveCommand
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals

class WatchHistorySaveUseCaseTest {

    private val watchHistoryRepository = mockk<UserVideoWatchHistoryRepository>()
    private val videoRepository = mockk<VideoRepository>()
    private val currentUserService = mockk<CurrentUserService>()
    private val videoAccessAssertService = mockk<VideoAccessAssertService>()
    private val watchHistorySaveUseCase = WatchHistorySaveUseCase(
        watchHistoryRepository,
        videoRepository,
        currentUserService,
        videoAccessAssertService
    )

    @Nested
    inner class Save {

        @Test
        fun `로그인한 사용자가 없으면 예외를 던진다`() {
            every { currentUserService.getCurrentUserId() } returns null

            val exception = assertThrows<BusinessException> {
                watchHistorySaveUseCase.save(VIDEO_UUID, command())
            }

            assertEquals(HttpStatus.UNAUTHORIZED, exception.status)
            verify(exactly = 0) { videoRepository.findByUuid(any()) }
        }

        @Test
        fun `동영상을 찾을 수 없으면 예외를 던진다`() {
            every { currentUserService.getCurrentUserId() } returns USER_ID
            every { videoRepository.findByUuid(VIDEO_UUID) } returns null

            val exception = assertThrows<BusinessException> {
                watchHistorySaveUseCase.save(VIDEO_UUID, command())
            }

            assertEquals(HttpStatus.NOT_FOUND, exception.status)
        }

        @Test
        fun `접근 권한 검증에 실패하면 예외를 던지고 저장하지 않는다`() {
            every { currentUserService.getCurrentUserId() } returns USER_ID
            every { videoRepository.findByUuid(VIDEO_UUID) } returns video()
            every {
                videoAccessAssertService.assertAccessible(VideoContentPrivacy.PRIVATE, ChannelContentPrivacy.PUBLIC)
            } throws BusinessException("동영상을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)

            assertThrows<BusinessException> {
                watchHistorySaveUseCase.save(VIDEO_UUID, command())
            }

            verify(exactly = 0) { watchHistoryRepository.save(any()) }
        }

        @Test
        fun `기존 시청 기록이 있으면 위치를 갱신하고 새로 저장하지 않는다`() {
            every { currentUserService.getCurrentUserId() } returns USER_ID
            every { videoRepository.findByUuid(VIDEO_UUID) } returns video()
            every { videoAccessAssertService.assertAccessible(any(), any()) } just Runs
            val existing = mockk<UserVideoWatchHistory>()
            every { existing.updatePosition(POSITION) } just Runs
            every { watchHistoryRepository.findByUserIdAndVideoId(USER_ID, VIDEO_ID) } returns existing

            watchHistorySaveUseCase.save(VIDEO_UUID, command())

            verify { existing.updatePosition(POSITION) }
            verify(exactly = 0) { watchHistoryRepository.save(any()) }
        }

        @Test
        fun `기존 기록이 없으면 새 시청 기록을 저장한다`() {
            every { currentUserService.getCurrentUserId() } returns USER_ID
            every { videoRepository.findByUuid(VIDEO_UUID) } returns video()
            every { videoAccessAssertService.assertAccessible(any(), any()) } just Runs
            every { watchHistoryRepository.findByUserIdAndVideoId(USER_ID, VIDEO_ID) } returns null
            val savedSlot = slot<UserVideoWatchHistory>()
            every { watchHistoryRepository.save(capture(savedSlot)) } answers { savedSlot.captured }

            watchHistorySaveUseCase.save(VIDEO_UUID, command())

            verify { watchHistoryRepository.save(any()) }
            val saved = savedSlot.captured
            assertEquals(USER_ID, saved.userId)
            assertEquals(VIDEO_ID, saved.videoId)
            assertEquals(POSITION, saved.lastPosition)
        }
    }

    private fun command(): WatchHistorySaveCommand {
        return WatchHistorySaveCommand(position = POSITION)
    }

    private fun video(): Video {
        val channel = mockk<Channel>()
        every { channel.contentPrivacy } returns ChannelContentPrivacy.PUBLIC
        val video = mockk<Video>()
        every { video.id } returns VIDEO_ID
        every { video.contentPrivacy } returns VideoContentPrivacy.PRIVATE
        every { video.channel } returns channel
        return video
    }

    companion object {
        private const val USER_ID = 1L
        private const val VIDEO_ID = 10L
        private const val VIDEO_UUID = "video-uuid"
        private const val POSITION = 120
    }

}
