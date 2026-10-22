package com.github.s8u.streamarchive.video.repository

import com.github.s8u.streamarchive.channel.entity.Channel
import com.github.s8u.streamarchive.channel.enums.ChannelContentPrivacy
import com.github.s8u.streamarchive.channel.repository.ChannelRepository
import com.github.s8u.streamarchive.video.entity.Video
import com.github.s8u.streamarchive.video.enums.VideoContentPrivacy
import com.github.s8u.streamarchive.video.usecase.dto.command.VideoSearchCommand
import kotlin.test.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class VideoRepositorySearchTest @Autowired constructor(
    private val videoRepository: VideoRepository,
    private val channelRepository: ChannelRepository
) {

    @Nested
    inner class SearchForPublic {

        @Test
        fun `넘긴 동영상 공개 범위와 채널 공개 범위에 모두 드는 동영상만 조회한다`() {
            val publicChannel = channelRepository.save(channel("public-channel", ChannelContentPrivacy.PUBLIC))
            val unlistedChannel = channelRepository.save(channel("unlisted-channel", ChannelContentPrivacy.UNLISTED))
            saveVideo("public-in-public", publicChannel.id!!, VideoContentPrivacy.PUBLIC)
            saveVideo("unlisted-in-public", publicChannel.id!!, VideoContentPrivacy.UNLISTED)
            saveVideo("private-in-public", publicChannel.id!!, VideoContentPrivacy.PRIVATE)
            saveVideo("public-in-unlisted", unlistedChannel.id!!, VideoContentPrivacy.PUBLIC)
            saveVideo("unlisted-in-unlisted", unlistedChannel.id!!, VideoContentPrivacy.UNLISTED)

            val publicOnly = search(
                videoPrivacies = listOf(VideoContentPrivacy.PUBLIC),
                channelPrivacies = listOf(ChannelContentPrivacy.PUBLIC)
            )
            val withUnlistedVideos = search(
                videoPrivacies = listOf(VideoContentPrivacy.PUBLIC, VideoContentPrivacy.UNLISTED),
                channelPrivacies = listOf(ChannelContentPrivacy.PUBLIC)
            )
            val withUnlistedChannels = search(
                videoPrivacies = listOf(VideoContentPrivacy.PUBLIC),
                channelPrivacies = listOf(ChannelContentPrivacy.PUBLIC, ChannelContentPrivacy.UNLISTED)
            )
            val withBoth = search(
                videoPrivacies = listOf(VideoContentPrivacy.PUBLIC, VideoContentPrivacy.UNLISTED),
                channelPrivacies = listOf(ChannelContentPrivacy.PUBLIC, ChannelContentPrivacy.UNLISTED)
            )

            assertEquals(setOf("public-in-public"), publicOnly)
            assertEquals(setOf("public-in-public", "unlisted-in-public"), withUnlistedVideos)
            assertEquals(setOf("public-in-public", "public-in-unlisted"), withUnlistedChannels)
            assertEquals(
                setOf("public-in-public", "unlisted-in-public", "public-in-unlisted", "unlisted-in-unlisted"),
                withBoth
            )
        }

        @Test
        fun `채널을 지정하면 채널 공개 범위는 보지 않는다`() {
            val unlistedChannel = channelRepository.save(channel("unlisted-channel", ChannelContentPrivacy.UNLISTED))
            saveVideo("public-in-unlisted", unlistedChannel.id!!, VideoContentPrivacy.PUBLIC)
            saveVideo("unlisted-in-unlisted", unlistedChannel.id!!, VideoContentPrivacy.UNLISTED)

            val videos = search(
                videoPrivacies = listOf(VideoContentPrivacy.PUBLIC),
                channelPrivacies = listOf(ChannelContentPrivacy.PUBLIC),
                channelUuid = unlistedChannel.uuid
            )

            assertEquals(setOf("public-in-unlisted"), videos)
        }
    }

    @Nested
    inner class CountByChannelId {

        @Test
        fun `넘긴 공개 범위의 동영상만 센다`() {
            val channel = channelRepository.save(channel("channel", ChannelContentPrivacy.PUBLIC))
            saveVideo("public", channel.id!!, VideoContentPrivacy.PUBLIC, fileSize = 100L)
            saveVideo("unlisted", channel.id!!, VideoContentPrivacy.UNLISTED, fileSize = 200L)
            saveVideo("private", channel.id!!, VideoContentPrivacy.PRIVATE, fileSize = 400L)
            val withUnlisted = listOf(VideoContentPrivacy.PUBLIC, VideoContentPrivacy.UNLISTED)

            assertEquals(1L, videoRepository.countByChannelId(channel.id!!, listOf(VideoContentPrivacy.PUBLIC)))
            assertEquals(2L, videoRepository.countByChannelId(channel.id!!, withUnlisted))
            assertEquals(300L, videoRepository.sumFileSizeByChannelId(channel.id!!, withUnlisted))
        }
    }

    private fun search(
        videoPrivacies: List<VideoContentPrivacy>,
        channelPrivacies: List<ChannelContentPrivacy>,
        channelUuid: String? = null
    ): Set<String> {
        // 다른 테스트가 남긴 데이터는 빼고 이 테스트가 만든 채널만 본다.
        val videos = videoRepository.searchForPublic(
            command = VideoSearchCommand(channelName = CHANNEL_NAME_PREFIX, channelUuid = channelUuid),
            videoPrivacies = videoPrivacies,
            channelPrivacies = channelPrivacies,
            pageable = PageRequest.of(0, 10)
        )

        assertEquals(videos.content.size.toLong(), videos.totalElements)
        return videos.content.map { it.uuid }.toSet()
    }

    private fun channel(
        uuid: String,
        contentPrivacy: ChannelContentPrivacy
    ): Channel {
        return Channel(
            uuid = uuid,
            name = "$CHANNEL_NAME_PREFIX-$uuid",
            contentPrivacy = contentPrivacy
        )
    }

    private fun saveVideo(
        uuid: String,
        channelId: Long,
        contentPrivacy: VideoContentPrivacy,
        fileSize: Long = 0L
    ) {
        val video = Video(
            uuid = uuid,
            channelId = channelId,
            title = uuid,
            contentPrivacy = contentPrivacy
        )
        video.applyMetadata(fileSize = fileSize, duration = 60)
        videoRepository.save(video)
    }

    companion object {
        private const val CHANNEL_NAME_PREFIX = "video-search-test"
    }

}
