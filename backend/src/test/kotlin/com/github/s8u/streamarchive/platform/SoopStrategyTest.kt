package com.github.s8u.streamarchive.platform

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.github.s8u.streamarchive.platform.impl.SoopStrategy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("local")
class SoopStrategyTest {

    @Autowired
    private lateinit var soopStrategy: SoopStrategy

    private val objectMapper = ObjectMapper().apply {
        registerModule(JavaTimeModule())
        enable(SerializationFeature.INDENT_OUTPUT)
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    @Test
    fun testGetStreamUrl() {
        val userId = "jdm1197"
        val streamUrl = soopStrategy.getStreamUrl(userId)

        println("Stream URL: $streamUrl")
        assert(streamUrl.contains("play.sooplive.co.kr"))
        assert(streamUrl.contains(userId))
    }

    @Test
    fun testGetChannel() {
        val userId = "jdm1197"
        val channel = soopStrategy.getChannel(userId)

        println("Channel: ${objectMapper.writeValueAsString(channel)}")
        assert(channel != null)
        assert(channel?.id == userId)
        assert(channel?.username == userId)
        assert(channel?.name != null)
    }

    @Test
    fun testGetChannelNotFound() {
        val userId = "nonexistentuser123456"
        val channel = soopStrategy.getChannel(userId)

        println("Channel: $channel")
        assert(channel == null)
    }

    @Test
    fun testGetStream() {
        val userId = "jdm1197"
        val stream = soopStrategy.getStream(userId)

        println("Stream: ${objectMapper.writeValueAsString(stream)}")
        if (stream != null) {
            assert(stream.username == userId)
            assert(stream.id != null)
            println("Stream is live!")
        } else {
            println("No active stream")
        }
    }

}
