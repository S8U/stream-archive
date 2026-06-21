package com.github.s8u.streamarchive.video.usecase

import com.github.s8u.streamarchive.channel.entity.Channel
import com.github.s8u.streamarchive.channel.repository.ChannelRepository
import com.github.s8u.streamarchive.video.entity.VideoAutoDeleteHistory
import com.github.s8u.streamarchive.video.repository.VideoAutoDeleteHistoryRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDateTime
import kotlin.test.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

class VideoAutoDeleteHistorySearchUseCaseTest {

    private val videoAutoDeleteHistoryRepository = mockk<VideoAutoDeleteHistoryRepository>()
    private val channelRepository = mockk<ChannelRepository>()
    private val videoAutoDeleteHistorySearchUseCase = VideoAutoDeleteHistorySearchUseCase(
        videoAutoDeleteHistoryRepository,
        channelRepository
    )

    @Nested
    inner class Search {

        @Test
        fun `채널 ID가 없으면 전체 이력을 조회하고 채널 이름을 매핑한다`() {
            val pageable = PageRequest.of(0, 20)
            val firstHistory = history(id = 1L, videoId = 10L, channelId = 1L)
            val secondHistory = history(id = 2L, videoId = 20L, channelId = 2L)
            every { videoAutoDeleteHistoryRepository.findAllByOrderByCreatedAtDescVideoIdDesc(pageable) } returns
                PageImpl(listOf(firstHistory, secondHistory), pageable, 2)
            every { channelRepository.findAllById(any<Iterable<Long>>()) } returns listOf(
                channel(id = 1L, name = "첫 번째 채널"),
                channel(id = 2L, name = "두 번째 채널")
            )

            val result = videoAutoDeleteHistorySearchUseCase.search(channelId = null, pageable = pageable)

            assertEquals(listOf(10L, 20L), result.content.map { it.videoId })
            assertEquals(listOf("첫 번째 채널", "두 번째 채널"), result.content.map { it.channelName })
            verify(exactly = 0) {
                videoAutoDeleteHistoryRepository.findAllByChannelIdOrderByCreatedAtDescVideoIdDesc(any(), any())
            }
        }

        @Test
        fun `채널 ID가 있으면 해당 채널의 이력만 조회한다`() {
            val pageable = PageRequest.of(0, 20)
            val channelHistory = history(id = 1L, videoId = 10L, channelId = 1L)
            every {
                videoAutoDeleteHistoryRepository.findAllByChannelIdOrderByCreatedAtDescVideoIdDesc(1L, pageable)
            } returns PageImpl(listOf(channelHistory), pageable, 1)
            every { channelRepository.findAllById(any<Iterable<Long>>()) } returns listOf(
                channel(id = 1L, name = "첫 번째 채널")
            )

            val result = videoAutoDeleteHistorySearchUseCase.search(channelId = 1L, pageable = pageable)

            assertEquals(listOf(10L), result.content.map { it.videoId })
            assertEquals(listOf("첫 번째 채널"), result.content.map { it.channelName })
            verify(exactly = 0) {
                videoAutoDeleteHistoryRepository.findAllByOrderByCreatedAtDescVideoIdDesc(any())
            }
        }

        @Test
        fun `채널이 삭제돼 이름을 찾지 못하면 빈 문자열을 채운다`() {
            val pageable = PageRequest.of(0, 20)
            val orphanHistory = history(id = 1L, videoId = 10L, channelId = 99L)
            every { videoAutoDeleteHistoryRepository.findAllByOrderByCreatedAtDescVideoIdDesc(pageable) } returns
                PageImpl(listOf(orphanHistory), pageable, 1)
            every { channelRepository.findAllById(any<Iterable<Long>>()) } returns emptyList()

            val result = videoAutoDeleteHistorySearchUseCase.search(channelId = null, pageable = pageable)

            assertEquals("", result.content.single().channelName)
        }
    }

    private fun history(
        id: Long,
        videoId: Long,
        channelId: Long
    ): VideoAutoDeleteHistory {
        val history = mockk<VideoAutoDeleteHistory>()
        every { history.id } returns id
        every { history.videoId } returns videoId
        every { history.channelId } returns channelId
        every { history.title } returns "동영상 $videoId"
        every { history.fileSize } returns videoId * 1000
        every { history.videoCreatedAt } returns BASE_TIME
        every { history.createdAt } returns BASE_TIME
        return history
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

    companion object {
        private val BASE_TIME = LocalDateTime.of(2026, 6, 21, 12, 0)
    }

}
