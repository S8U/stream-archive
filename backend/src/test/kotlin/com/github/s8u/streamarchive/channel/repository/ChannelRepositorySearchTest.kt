package com.github.s8u.streamarchive.channel.repository

import com.github.s8u.streamarchive.channel.entity.Channel
import com.github.s8u.streamarchive.channel.enums.ChannelContentPrivacy
import com.github.s8u.streamarchive.channel.usecase.dto.command.ChannelAdminSearchCommand
import com.github.s8u.streamarchive.channel.usecase.dto.command.ChannelSearchCommand
import com.github.s8u.streamarchive.video.entity.Video
import com.github.s8u.streamarchive.video.enums.VideoContentPrivacy
import com.github.s8u.streamarchive.video.repository.VideoRepository
import kotlin.test.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ChannelRepositorySearchTest @Autowired constructor(
    private val channelRepository: ChannelRepository,
    private val videoRepository: VideoRepository
) {

    @Nested
    inner class SearchForAdmin {

        @Test
        fun `id 내림차순으로 조회한다`() {
            val first = channelRepository.save(channel(uuid = "admin-channel-1", name = "가가"))
            val second = channelRepository.save(channel(uuid = "admin-channel-2", name = "다다"))
            val third = channelRepository.save(channel(uuid = "admin-channel-3", name = "나나"))

            val channels = channelRepository.searchForAdmin(
                command = ChannelAdminSearchCommand(),
                pageable = PageRequest.of(0, 10)
            )

            assertEquals(listOf(third.id, second.id, first.id), channels.content.map { it.id })
        }

        @Test
        fun `동영상 용량 합계로 정렬한다`() {
            val small = channelRepository.save(channel(uuid = "admin-channel-small", name = "작은 채널"))
            val large = channelRepository.save(channel(uuid = "admin-channel-large", name = "큰 채널"))
            val empty = channelRepository.save(channel(uuid = "admin-channel-empty", name = "빈 채널"))
            saveVideo(channelId = small.id!!, fileSize = 100L)
            saveVideo(channelId = large.id!!, fileSize = 1000L)
            saveVideo(channelId = large.id!!, fileSize = 2000L)

            val channels = channelRepository.searchForAdmin(
                command = ChannelAdminSearchCommand(),
                pageable = PageRequest.of(
                    0,
                    10,
                    Sort.by(Sort.Direction.DESC, "totalVideoFileSize")
                )
            )

            assertEquals(listOf(large.id, small.id, empty.id), channels.content.map { it.id })
            assertEquals(listOf(3000L, 100L, 0L), channels.content.map { it.totalVideoFileSize })
        }
    }

    @Nested
    inner class SearchForPublic {

        @Test
        fun `이름 오름차순 id 오름차순으로 조회한다`() {
            val first = channelRepository.save(channel(uuid = "public-channel-1", name = "나나"))
            val second = channelRepository.save(channel(uuid = "public-channel-2", name = "가가"))
            val third = channelRepository.save(channel(uuid = "public-channel-3", name = "가가"))
            channelRepository.save(
                channel(
                    uuid = "private-channel",
                    name = "가가",
                    contentPrivacy = ChannelContentPrivacy.PRIVATE
                )
            )

            val channels = channelRepository.searchForPublic(
                command = ChannelSearchCommand(),
                pageable = PageRequest.of(0, 10)
            )

            assertEquals(listOf(second.id, third.id, first.id), channels.content.map { it.id })
        }
    }

    private fun channel(
        uuid: String,
        name: String,
        contentPrivacy: ChannelContentPrivacy = ChannelContentPrivacy.PUBLIC
    ): Channel {
        return Channel(
            uuid = uuid,
            name = name,
            contentPrivacy = contentPrivacy
        )
    }

    private fun saveVideo(channelId: Long, fileSize: Long) {
        val video = Video(
            uuid = "video-$channelId-$fileSize",
            channelId = channelId,
            title = "동영상",
            contentPrivacy = VideoContentPrivacy.PUBLIC
        )
        video.applyMetadata(fileSize = fileSize, duration = 60)
        videoRepository.save(video)
    }

}
