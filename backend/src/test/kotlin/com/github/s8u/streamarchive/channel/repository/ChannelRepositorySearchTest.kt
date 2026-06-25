package com.github.s8u.streamarchive.channel.repository

import com.github.s8u.streamarchive.channel.entity.Channel
import com.github.s8u.streamarchive.channel.enums.ChannelContentPrivacy
import com.github.s8u.streamarchive.channel.usecase.dto.command.ChannelAdminSearchCommand
import com.github.s8u.streamarchive.channel.usecase.dto.command.ChannelSearchCommand
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
class ChannelRepositorySearchTest @Autowired constructor(
    private val channelRepository: ChannelRepository
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

}
