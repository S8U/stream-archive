package com.github.s8u.streamarchive.video.service

import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.video.entity.Video
import com.github.s8u.streamarchive.video.event.VideoDeletedEvent
import com.github.s8u.streamarchive.video.repository.VideoRepository
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpStatus
import java.util.Optional
import kotlin.test.assertEquals

class VideoDeleteServiceTest {

    private val videoRepository = mockk<VideoRepository>()
    private val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
    private val videoDeleteService = VideoDeleteService(videoRepository, eventPublisher)

    @Nested
    inner class Delete {

        @Test
        fun `동영상을 찾을 수 없으면 예외를 던지고 이벤트를 발행하지 않는다`() {
            every { videoRepository.findById(VIDEO_ID) } returns Optional.empty()

            val exception = assertThrows<BusinessException> {
                videoDeleteService.delete(VIDEO_ID)
            }

            assertEquals(HttpStatus.NOT_FOUND, exception.status)
            verify(exactly = 0) { eventPublisher.publishEvent(any()) }
        }

        @Test
        fun `소장된 동영상이면 예외를 던지고 삭제·이벤트 발행을 하지 않는다`() {
            val video = mockk<Video>()
            every { video.isArchived } returns true
            every { videoRepository.findById(VIDEO_ID) } returns Optional.of(video)

            val exception = assertThrows<BusinessException> {
                videoDeleteService.delete(VIDEO_ID)
            }

            assertEquals(HttpStatus.CONFLICT, exception.status)
            verify(exactly = 0) { video.softDelete(any(), any()) }
            verify(exactly = 0) { eventPublisher.publishEvent(any()) }
        }

        @Test
        fun `삭제에 성공하면 소프트 삭제하고 삭제 이벤트를 발행한다`() {
            val video = mockk<Video>()
            every { video.isArchived } returns false
            every { video.id } returns VIDEO_ID
            every { video.softDelete(userId = null, ip = null) } just Runs
            every { videoRepository.findById(VIDEO_ID) } returns Optional.of(video)

            val eventSlot = slot<VideoDeletedEvent>()
            every { eventPublisher.publishEvent(capture(eventSlot)) } just Runs

            videoDeleteService.delete(VIDEO_ID)

            verify { video.softDelete(userId = null, ip = null) }
            assertEquals(VIDEO_ID, eventSlot.captured.videoId)
        }
    }

    companion object {
        private const val VIDEO_ID = 1L
    }

}
