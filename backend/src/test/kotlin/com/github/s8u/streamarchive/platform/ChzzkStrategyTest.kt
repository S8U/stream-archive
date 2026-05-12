package com.github.s8u.streamarchive.platform

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.github.s8u.streamarchive.platform.impl.ChzzkStrategy
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@Tag("external")
@SpringBootTest
@ActiveProfiles("test")
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
        assertTrue(streamUrl.contains("chzzk.naver.com"))
    }

    @Test
    fun testGetChannel() {
        val channelId = "6e06f5e1907f17eff543abd06cb62891"
        val channel = chzzkStrategy.getChannel(channelId)

        println("Channel: ${objectMapper.writeValueAsString(channel)}")
        val actual = assertNotNull(channel)
        assertEquals(channelId, actual.id)
    }

    @Test
    fun testGetStream() {
        val channelId = "6e06f5e1907f17eff543abd06cb62891"
        val stream = chzzkStrategy.getStream(channelId)

        println("Stream: ${objectMapper.writeValueAsString(stream)}")
        if (stream != null) {
            assertEquals(channelId, stream.username)
        } else {
            println("No active stream")
        }
    }
}
