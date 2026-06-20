package com.github.s8u.streamarchive.channel.entity

import com.github.s8u.streamarchive.channel.enums.ChannelContentPrivacy
import kotlin.test.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ChannelTest {

    private fun channel(): Channel {
        return Channel(
            uuid = "channel-uuid",
            name = "원래 채널",
            contentPrivacy = ChannelContentPrivacy.PUBLIC
        )
    }

    @Nested
    inner class Update {

        @Test
        fun `name과 contentPrivacy를 넘기면 둘 다 갱신된다`() {
            val channel = channel()

            channel.update(name = "새 채널", contentPrivacy = ChannelContentPrivacy.PRIVATE)

            assertEquals("새 채널", channel.name)
            assertEquals(ChannelContentPrivacy.PRIVATE, channel.contentPrivacy)
        }

        @Test
        fun `name만 넘기면 name만 바뀌고 contentPrivacy는 유지된다`() {
            val channel = channel()

            channel.update(name = "새 채널", contentPrivacy = null)

            assertEquals("새 채널", channel.name)
            assertEquals(ChannelContentPrivacy.PUBLIC, channel.contentPrivacy)
        }

        @Test
        fun `모든 인자가 null이면 아무 값도 바뀌지 않는다`() {
            val channel = channel()

            channel.update(name = null, contentPrivacy = null)

            assertEquals("원래 채널", channel.name)
            assertEquals(ChannelContentPrivacy.PUBLIC, channel.contentPrivacy)
        }
    }
}
