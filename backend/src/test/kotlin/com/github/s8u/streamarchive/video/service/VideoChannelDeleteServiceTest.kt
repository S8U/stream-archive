package com.github.s8u.streamarchive.video.service

import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.video.entity.Video
import com.github.s8u.streamarchive.video.repository.VideoRepository
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals

class VideoChannelDeleteServiceTest {

    private val videoRepository = mockk<VideoRepository>()
    private val videoChannelDeleteService = VideoChannelDeleteService(videoRepository)

    @Nested
    inner class DeleteAllByChannelId {

        @Test
        fun `소장된 동영상이 있으면 예외를 던지고 조회·삭제하지 않는다`() {
            every { videoRepository.existsByChannelIdAndIsArchivedTrue(CHANNEL_ID) } returns true

            val exception = assertThrows<BusinessException> {
                videoChannelDeleteService.deleteAllByChannelId(CHANNEL_ID)
            }

            assertEquals(HttpStatus.CONFLICT, exception.status)
            verify(exactly = 0) { videoRepository.findByChannelId(any()) }
        }

        @Test
        fun `소장된 동영상이 없으면 동영상들을 소프트 삭제하고 삭제한 ID를 반환한다`() {
            val video1 = video(VIDEO_ID_1)
            val video2 = video(VIDEO_ID_2)
            every { videoRepository.existsByChannelIdAndIsArchivedTrue(CHANNEL_ID) } returns false
            every { videoRepository.findByChannelId(CHANNEL_ID) } returns listOf(video1, video2)

            val deletedIds = videoChannelDeleteService.deleteAllByChannelId(CHANNEL_ID)

            assertEquals(listOf(VIDEO_ID_1, VIDEO_ID_2), deletedIds)
            verify { video1.softDelete(userId = null, ip = null) }
            verify { video2.softDelete(userId = null, ip = null) }
        }

        @Test
        fun `삭제할 동영상이 없으면 빈 리스트를 반환한다`() {
            every { videoRepository.existsByChannelIdAndIsArchivedTrue(CHANNEL_ID) } returns false
            every { videoRepository.findByChannelId(CHANNEL_ID) } returns emptyList()

            val deletedIds = videoChannelDeleteService.deleteAllByChannelId(CHANNEL_ID)

            assertEquals(emptyList(), deletedIds)
        }
    }

    private fun video(id: Long): Video {
        val video = mockk<Video>()
        every { video.id } returns id
        every { video.softDelete(userId = null, ip = null) } just Runs
        return video
    }

    companion object {
        private const val CHANNEL_ID = 1L
        private const val VIDEO_ID_1 = 10L
        private const val VIDEO_ID_2 = 20L
    }

}
