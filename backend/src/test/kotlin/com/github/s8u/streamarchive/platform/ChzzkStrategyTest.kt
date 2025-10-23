package com.github.s8u.streamarchive.platform

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.github.s8u.streamarchive.platform.impl.ChzzkStrategy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("local")
class ChzzkStrategyTest {

    @Autowired
    private lateinit var chzzkStrategy: ChzzkStrategy

    private val objectMapper = ObjectMapper().apply {
        registerModule(JavaTimeModule())
        enable(SerializationFeature.INDENT_OUTPUT)
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    @Test
    fun testGetStreamUrl() {
        val channelId = "6e06f5e1907f17eff543abd06cb62891"
        val streamUrl = chzzkStrategy.getStreamUrl(channelId)

        println("Stream URL: $streamUrl")
        assert(streamUrl.contains("chzzk.naver.com"))
    }

    @Test
    fun testGetChannel() {
        val channelId = "6e06f5e1907f17eff543abd06cb62891"
        val channel = chzzkStrategy.getChannel(channelId)

        println("Channel: ${objectMapper.writeValueAsString(channel)}")
        assert(channel != null)
        assert(channel?.id == channelId)
    }

    @Test
    fun testGetStream() {
        val channelId = "0d027498b18371674fac3ed17247e6b8"
        val stream = chzzkStrategy.getStream(channelId)

        println("Stream: ${objectMapper.writeValueAsString(stream)}")
        if (stream != null) {
            assert(stream.username == channelId)
        } else {
            println("No active stream")
        }
    }
}