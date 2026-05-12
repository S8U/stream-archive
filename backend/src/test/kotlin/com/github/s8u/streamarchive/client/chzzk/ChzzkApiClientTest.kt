package com.github.s8u.streamarchive.client.chzzk

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@Tag("external")
@SpringBootTest
@ActiveProfiles("test")
class ChzzkApiClientTest {

    @Autowired
    private lateinit var chzzkApiClient: ChzzkApiClient

    private val objectMapper = ObjectMapper().apply {
        enable(SerializationFeature.INDENT_OUTPUT)
    }

    @Test
    fun testGetChannel() {
        val channelId = "6e06f5e1907f17eff543abd06cb62891"
        val response = chzzkApiClient.getChannel(channelId)

        println("response: ${objectMapper.writeValueAsString(response)}")

        val actual = assertNotNull(response)
        assertEquals(200, actual.code)
        assertNotNull(actual.content)
    }

    @Test
    fun testGetLiveDetail() {
        val channelId = "6e06f5e1907f17eff543abd06cb62891"
        val response = chzzkApiClient.getLiveDetail(channelId)

        println("response: ${objectMapper.writeValueAsString(response)}")

        val actual = assertNotNull(response)
        assertEquals(200, actual.code)
        assertNotNull(actual.content)
    }

}
