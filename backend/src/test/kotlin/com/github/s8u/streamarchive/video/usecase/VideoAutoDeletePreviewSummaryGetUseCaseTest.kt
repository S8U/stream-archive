package com.github.s8u.streamarchive.video.usecase

import com.github.s8u.streamarchive.channel.repository.ChannelRepository
import com.github.s8u.streamarchive.video.repository.VideoRepository
import com.github.s8u.streamarchive.video.service.VideoAutoDeletePolicyGetService
import com.github.s8u.streamarchive.video.service.VideoAutoDeleteTargetGetService
import com.github.s8u.streamarchive.video.service.dto.VideoAutoDeleteTarget
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDateTime
import kotlin.test.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class VideoAutoDeletePreviewSummaryGetUseCaseTest {

    private val videoRepository = mockk<VideoRepository>()
    private val channelRepository = mockk<ChannelRepository>()
    private val videoAutoDeletePolicyGetService = mockk<VideoAutoDeletePolicyGetService>()
    private val videoAutoDeleteTargetGetService = mockk<VideoAutoDeleteTargetGetService>()
    private val videoAutoDeletePreviewSummaryGetUseCase = VideoAutoDeletePreviewSummaryGetUseCase(
        videoRepository,
        channelRepository,
        videoAutoDeletePolicyGetService,
        videoAutoDeleteTargetGetService
    )

    @Nested
    inner class GetSummary {

        @Test
        fun `전체 채널의 삭제 대상 개수와 파일 크기를 합산한다`() {
            val targets = listOf(target(channelId = 1L), target(channelId = 2L))
            every { channelRepository.findAllIds() } returns listOf(1L, 2L)
            every { videoAutoDeletePolicyGetService.getGlobalPolicy() } returns null
            every { videoAutoDeletePolicyGetService.getAllChannelPolicies() } returns emptyList()
            every {
                videoAutoDeleteTargetGetService.getTargets(null, emptyList(), listOf(1L, 2L), any())
            } returns targets
            every { videoRepository.countAutoDeleteTargets(1L, targets[0].createdBefore) } returns 2L
            every { videoRepository.countAutoDeleteTargets(2L, targets[1].createdBefore) } returns 3L
            every { videoRepository.sumFileSizeAutoDeleteTargets(1L, targets[0].createdBefore) } returns 1_000L
            every { videoRepository.sumFileSizeAutoDeleteTargets(2L, targets[1].createdBefore) } returns 2_000L

            val result = videoAutoDeletePreviewSummaryGetUseCase.getSummary(channelId = null)

            assertEquals(5L, result.targetCount)
            assertEquals(3_000L, result.totalFileSize)
        }

        @Test
        fun `채널 ID가 있으면 해당 채널의 요약만 조회한다`() {
            val target = target(channelId = 1L)
            every { videoAutoDeletePolicyGetService.getGlobalPolicy() } returns null
            every { videoAutoDeletePolicyGetService.getAllChannelPolicies() } returns emptyList()
            every {
                videoAutoDeleteTargetGetService.getTargets(null, emptyList(), listOf(1L), any())
            } returns listOf(target)
            every { videoRepository.countAutoDeleteTargets(1L, target.createdBefore) } returns 2L
            every { videoRepository.sumFileSizeAutoDeleteTargets(1L, target.createdBefore) } returns 1_000L

            val result = videoAutoDeletePreviewSummaryGetUseCase.getSummary(channelId = 1L)

            assertEquals(1L, result.channelId)
            assertEquals(2L, result.targetCount)
            assertEquals(1_000L, result.totalFileSize)
            verify(exactly = 0) { channelRepository.findAllIds() }
        }
    }

    private fun target(channelId: Long): VideoAutoDeleteTarget {
        return VideoAutoDeleteTarget(
            channelId = channelId,
            deleteAfterDays = 30,
            createdBefore = BASE_TIME.minusDays(30)
        )
    }

    companion object {
        private val BASE_TIME = LocalDateTime.of(2026, 6, 21, 12, 0)
    }

}
