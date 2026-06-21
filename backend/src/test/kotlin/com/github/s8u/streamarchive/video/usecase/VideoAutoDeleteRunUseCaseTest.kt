package com.github.s8u.streamarchive.video.usecase

import com.github.s8u.streamarchive.channel.repository.ChannelRepository
import com.github.s8u.streamarchive.global.service.TransactionRunner
import com.github.s8u.streamarchive.video.entity.Video
import com.github.s8u.streamarchive.video.repository.VideoRepository
import com.github.s8u.streamarchive.video.service.VideoAutoDeleteHistorySaveService
import com.github.s8u.streamarchive.video.service.VideoAutoDeletePolicyGetService
import com.github.s8u.streamarchive.video.service.VideoAutoDeleteTargetGetService
import com.github.s8u.streamarchive.video.service.VideoDeleteService
import com.github.s8u.streamarchive.video.service.dto.VideoAutoDeleteTarget
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDateTime
import kotlin.test.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class VideoAutoDeleteRunUseCaseTest {

    private val videoRepository = mockk<VideoRepository>()
    private val channelRepository = mockk<ChannelRepository>()
    private val videoAutoDeletePolicyGetService = mockk<VideoAutoDeletePolicyGetService>()
    private val videoAutoDeleteTargetGetService = mockk<VideoAutoDeleteTargetGetService>()
    private val videoDeleteService = mockk<VideoDeleteService>(relaxed = true)
    private val videoAutoDeleteHistorySaveService = mockk<VideoAutoDeleteHistorySaveService>(relaxed = true)
    private val transactionRunner = mockk<TransactionRunner>()
    private val videoAutoDeleteRunUseCase = VideoAutoDeleteRunUseCase(
        videoRepository,
        channelRepository,
        videoAutoDeletePolicyGetService,
        videoAutoDeleteTargetGetService,
        videoDeleteService,
        videoAutoDeleteHistorySaveService,
        transactionRunner
    )

    @BeforeEach
    fun setUp() {
        every { transactionRunner.run<Any?>(any()) } answers { firstArg<() -> Any?>().invoke() }
        every { channelRepository.findAllIds() } returns listOf(CHANNEL_ID)
        every { videoAutoDeletePolicyGetService.getGlobalPolicy() } returns null
        every { videoAutoDeletePolicyGetService.getAllChannelPolicies() } returns emptyList()
    }

    @Nested
    inner class Run {

        @Test
        fun `삭제와 이력 저장에 성공한 건수를 반환한다`() {
            val target = target()
            val firstVideo = video(1L)
            val secondVideo = video(2L)
            every {
                videoAutoDeleteTargetGetService.getTargets(null, emptyList(), listOf(CHANNEL_ID), any())
            } returns listOf(target)
            every { videoRepository.findAutoDeleteTargets(CHANNEL_ID, target.createdBefore) } returns
                listOf(firstVideo, secondVideo)

            val result = videoAutoDeleteRunUseCase.run()

            assertEquals(2, result.deletedCount)
            verify { videoDeleteService.delete(1L) }
            verify { videoDeleteService.delete(2L) }
            verify { videoAutoDeleteHistorySaveService.save(firstVideo) }
            verify { videoAutoDeleteHistorySaveService.save(secondVideo) }
        }

        @Test
        fun `한 건 삭제가 실패해도 다음 동영상을 계속 삭제한다`() {
            val target = target()
            val failedVideo = video(1L)
            val succeededVideo = video(2L)
            every {
                videoAutoDeleteTargetGetService.getTargets(null, emptyList(), listOf(CHANNEL_ID), any())
            } returns listOf(target)
            every { videoRepository.findAutoDeleteTargets(CHANNEL_ID, target.createdBefore) } returns
                listOf(failedVideo, succeededVideo)
            every { videoDeleteService.delete(1L) } throws RuntimeException("삭제 실패")

            val result = videoAutoDeleteRunUseCase.run()

            assertEquals(1, result.deletedCount)
            verify(exactly = 0) { videoAutoDeleteHistorySaveService.save(failedVideo) }
            verify { videoDeleteService.delete(2L) }
            verify { videoAutoDeleteHistorySaveService.save(succeededVideo) }
        }

        @Test
        fun `적용 대상 채널이 없으면 동영상을 조회하지 않는다`() {
            every {
                videoAutoDeleteTargetGetService.getTargets(null, emptyList(), listOf(CHANNEL_ID), any())
            } returns emptyList()

            val result = videoAutoDeleteRunUseCase.run()

            assertEquals(0, result.deletedCount)
            verify(exactly = 0) { videoRepository.findAutoDeleteTargets(any(), any()) }
        }
    }

    private fun video(id: Long): Video {
        val video = mockk<Video>()
        every { video.id } returns id
        return video
    }

    private fun target(): VideoAutoDeleteTarget {
        return VideoAutoDeleteTarget(
            channelId = CHANNEL_ID,
            deleteAfterDays = 30,
            createdBefore = BASE_TIME.minusDays(30)
        )
    }

    companion object {
        private const val CHANNEL_ID = 1L
        private val BASE_TIME = LocalDateTime.of(2026, 6, 21, 12, 0)
    }

}
