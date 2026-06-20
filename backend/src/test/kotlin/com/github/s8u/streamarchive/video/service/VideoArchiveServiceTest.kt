package com.github.s8u.streamarchive.video.service

import com.github.s8u.streamarchive.video.entity.Video
import com.github.s8u.streamarchive.video.entity.VideoArchiveHistory
import com.github.s8u.streamarchive.video.repository.VideoArchiveHistoryRepository
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class VideoArchiveServiceTest {

    private val videoArchiveHistoryRepository = mockk<VideoArchiveHistoryRepository>(relaxed = true)
    private val videoArchiveService = VideoArchiveService(videoArchiveHistoryRepository)

    @Nested
    inner class SetArchived {

        @Test
        fun `소장 처리하면 동영상을 소장하고 이력을 남긴다`() {
            val video = mockk<Video>()
            every { video.id } returns VIDEO_ID
            every { video.archive(USER_ID, CLIENT_IP) } just Runs

            val historySlot = slot<VideoArchiveHistory>()
            every { videoArchiveHistoryRepository.save(capture(historySlot)) } answers { firstArg() }

            videoArchiveService.setArchived(video, isArchived = true, userId = USER_ID, ip = CLIENT_IP)

            verify { video.archive(USER_ID, CLIENT_IP) }
            assertEquals(VIDEO_ID, historySlot.captured.videoId)
            assertEquals(true, historySlot.captured.isArchived)
            assertEquals(USER_ID, historySlot.captured.actionBy)
            assertEquals(CLIENT_IP, historySlot.captured.actionIp)
        }

        @Test
        fun `소장 해제하면 동영상을 해제하고 이력을 남긴다`() {
            val video = mockk<Video>()
            every { video.id } returns VIDEO_ID
            every { video.unarchive() } just Runs

            val historySlot = slot<VideoArchiveHistory>()
            every { videoArchiveHistoryRepository.save(capture(historySlot)) } answers { firstArg() }

            videoArchiveService.setArchived(video, isArchived = false, userId = USER_ID, ip = CLIENT_IP)

            verify { video.unarchive() }
            assertEquals(false, historySlot.captured.isArchived)
        }

        @Test
        fun `자동 소장이면 사용자와 IP 없이 이력을 남긴다`() {
            val video = mockk<Video>()
            every { video.id } returns VIDEO_ID
            every { video.archive(null, null) } just Runs

            val historySlot = slot<VideoArchiveHistory>()
            every { videoArchiveHistoryRepository.save(capture(historySlot)) } answers { firstArg() }

            videoArchiveService.setArchived(video, isArchived = true, userId = null, ip = null)

            verify { video.archive(null, null) }
            assertEquals(null, historySlot.captured.actionBy)
            assertEquals(null, historySlot.captured.actionIp)
        }
    }

    companion object {
        private const val VIDEO_ID = 1L
        private const val USER_ID = 99L
        private const val CLIENT_IP = "127.0.0.1"
    }

}
