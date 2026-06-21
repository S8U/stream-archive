package com.github.s8u.streamarchive.video.usecase

import com.github.s8u.streamarchive.channel.entity.Channel
import com.github.s8u.streamarchive.channel.repository.ChannelRepository
import com.github.s8u.streamarchive.global.service.UrlService
import com.github.s8u.streamarchive.video.entity.Video
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
import org.springframework.data.domain.PageRequest

class VideoAutoDeletePreviewUseCaseTest {

    private val videoRepository = mockk<VideoRepository>()
    private val channelRepository = mockk<ChannelRepository>()
    private val videoAutoDeletePolicyGetService = mockk<VideoAutoDeletePolicyGetService>()
    private val videoAutoDeleteTargetGetService = mockk<VideoAutoDeleteTargetGetService>()
    private val urlService = mockk<UrlService>()
    private val videoAutoDeletePreviewUseCase = VideoAutoDeletePreviewUseCase(
        videoRepository,
        channelRepository,
        videoAutoDeletePolicyGetService,
        videoAutoDeleteTargetGetService,
        urlService
    )

    @Nested
    inner class Preview {

        @Test
        fun `전체 채널 대상을 최근 생성순으로 합쳐 페이징한다`() {
            val oldVideo = video(id = 1L, channelId = 1L, createdAt = BASE_TIME.minusDays(30))
            val newestVideo = video(id = 2L, channelId = 2L, createdAt = BASE_TIME.minusDays(10))
            val middleVideo = video(id = 3L, channelId = 1L, createdAt = BASE_TIME.minusDays(20))
            val targets = listOf(
                target(channelId = 1L, deleteAfterDays = 7),
                target(channelId = 2L, deleteAfterDays = 14)
            )
            every { channelRepository.findAllIds() } returns listOf(1L, 2L)
            every { videoAutoDeletePolicyGetService.getGlobalPolicy() } returns null
            every { videoAutoDeletePolicyGetService.getAllChannelPolicies() } returns emptyList()
            every {
                videoAutoDeleteTargetGetService.getTargets(null, emptyList(), listOf(1L, 2L), any())
            } returns targets
            every { videoRepository.findAutoDeleteTargets(1L, targets[0].createdBefore) } returns
                listOf(oldVideo, middleVideo)
            every { videoRepository.findAutoDeleteTargets(2L, targets[1].createdBefore) } returns listOf(newestVideo)
            every { channelRepository.findAllById(any<Iterable<Long>>()) } returns listOf(
                channel(id = 1L, name = "첫 번째 채널"),
                channel(id = 2L, name = "두 번째 채널")
            )
            every { urlService.videoThumbnailUrl(any()) } answers { "/thumbnail/${firstArg<String>()}" }

            val result = videoAutoDeletePreviewUseCase.preview(
                channelId = null,
                pageable = PageRequest.of(0, 2)
            )

            assertEquals(3, result.totalElements)
            assertEquals(listOf(2L, 3L), result.content.map { it.id })
            assertEquals(listOf("두 번째 채널", "첫 번째 채널"), result.content.map { it.channelName })
            assertEquals(listOf("/thumbnail/video-2", "/thumbnail/video-3"), result.content.map { it.thumbnailUrl })
            assertEquals(listOf(10, 20), result.content.map { it.ageDays })
            // overDays = ageDays - deleteAfterDays (음수면 아직 기준일 전, 정책보다 일찍 잡힌 대상)
            assertEquals(listOf(-4, 13), result.content.map { it.overDays })
        }

        @Test
        fun `채널 ID가 있으면 해당 채널만 대상으로 조회한다`() {
            val target = target(channelId = 1L, deleteAfterDays = 7)
            every { videoAutoDeletePolicyGetService.getGlobalPolicy() } returns null
            every { videoAutoDeletePolicyGetService.getAllChannelPolicies() } returns emptyList()
            every {
                videoAutoDeleteTargetGetService.getTargets(null, emptyList(), listOf(1L), any())
            } returns listOf(target)
            every { videoRepository.findAutoDeleteTargets(1L, target.createdBefore) } returns emptyList()
            every { channelRepository.findAllById(any<Iterable<Long>>()) } returns emptyList()

            val result = videoAutoDeletePreviewUseCase.preview(
                channelId = 1L,
                pageable = PageRequest.of(0, 20)
            )

            assertEquals(0, result.totalElements)
            verify(exactly = 0) { channelRepository.findAllIds() }
        }
    }

    private fun video(
        id: Long,
        channelId: Long,
        createdAt: LocalDateTime
    ): Video {
        val video = mockk<Video>()
        every { video.id } returns id
        every { video.uuid } returns "video-$id"
        every { video.channelId } returns channelId
        every { video.title } returns "동영상 $id"
        every { video.fileSize } returns id * 1000
        every { video.createdAt } returns createdAt
        return video
    }

    private fun channel(
        id: Long,
        name: String
    ): Channel {
        val channel = mockk<Channel>()
        every { channel.id } returns id
        every { channel.name } returns name
        return channel
    }

    private fun target(
        channelId: Long,
        deleteAfterDays: Int
    ): VideoAutoDeleteTarget {
        return VideoAutoDeleteTarget(
            channelId = channelId,
            deleteAfterDays = deleteAfterDays,
            createdBefore = BASE_TIME.minusDays(deleteAfterDays.toLong())
        )
    }

    companion object {
        // ageDays는 UseCase 내부의 LocalDateTime.now() 기준으로 계산되므로,
        // 결정적으로 단언하려면 데이터의 createdAt도 현재 시각을 기준으로 잡는다.
        private val BASE_TIME = LocalDateTime.now()
    }

}
