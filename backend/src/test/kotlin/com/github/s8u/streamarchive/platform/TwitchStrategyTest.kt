package com.github.s8u.streamarchive.platform

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.github.s8u.streamarchive.platform.impl.TwitchStrategy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("local")
class TwitchStrategyTest {

    @Autowired
    private lateinit var twitchStrategy: TwitchStrategy

    private val objectMapper = ObjectMapper().apply {
        registerModule(JavaTimeModule())
        enable(SerializationFeature.INDENT_OUTPUT)
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    @Test
    fun testGetStreamUrl() {
        val username = "nokduro"
        val streamUrl = twitchStrategy.getStreamUrl(username)

        println("Stream URL: $streamUrl")
        assert(streamUrl.contains("twitch.tv"))
    }

    @Test
    fun testGetChannel() {
        val username = "nokduro"
        val channel = twitchStrategy.getChannel(username)

        println("Channel: ${objectMapper.writeValueAsString(channel)}")
        assert(channel != null)
        assert(channel?.username == username)
    }

    @Test
    fun testGetStream() {
        val username = "nokduro"
        val stream = twitchStrategy.getStream(username)

        println("Stream: ${objectMapper.writeValueAsString(stream)}")
        if (stream != null) {
            assert(stream.username == username)
        } else {
            println("No active stream")
        }
    }
}