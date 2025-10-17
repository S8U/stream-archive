package com.github.s8u.streamarchive.client.chzzk

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("local")
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

        assert(response?.code == 200)
        assert(response?.content != null)
    }

    @Test
    fun testGetLiveDetail() {
        val channelId = "6e06f5e1907f17eff543abd06cb62891"
        val response = chzzkApiClient.getLiveDetail(channelId)

        println("response: ${objectMapper.writeValueAsString(response)}")

        assert(response?.code == 200)
        assert(response?.content != null)
    }

}