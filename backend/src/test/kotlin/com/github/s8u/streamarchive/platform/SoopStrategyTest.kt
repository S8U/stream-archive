package com.github.s8u.streamarchive.platform

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.github.s8u.streamarchive.platform.impl.SoopStrategy
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@Tag("external")
@SpringBootTest
@ActiveProfiles("test")
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
        assertTrue(streamUrl.contains("play.sooplive.co.kr"))
        assertTrue(streamUrl.contains(userId))
    }

    @Test
    fun testGetChannel() {
        val userId = "jdm1197"
        val channel = soopStrategy.getChannel(userId)

        println("Channel: ${objectMapper.writeValueAsString(channel)}")
        val actual = assertNotNull(channel)
        assertEquals(userId, actual.id)
        assertEquals(userId, actual.username)
        assertNotNull(actual.name)
    }

    @Test
    fun testGetChannelNotFound() {
        val userId = "nonexistentuser123456"
        val channel = soopStrategy.getChannel(userId)

        println("Channel: $channel")
        assertNull(channel)
    }

    @Test
    fun testGetStream() {
        val userId = "jdm1197"
        val stream = soopStrategy.getStream(userId)

        println("Stream: ${objectMapper.writeValueAsString(stream)}")
        if (stream != null) {
            assertEquals(userId, stream.username)
            assertNotNull(stream.id)
            println("Stream is live!")
        } else {
            println("No active stream")
        }
    }

}
