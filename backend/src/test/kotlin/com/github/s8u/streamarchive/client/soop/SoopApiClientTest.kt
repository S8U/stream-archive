package com.github.s8u.streamarchive.client.soop

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class SoopApiClientTest {

    @Autowired
    private lateinit var soopApiClient: SoopApiClient

    private val objectMapper = ObjectMapper().apply {
        enable(SerializationFeature.INDENT_OUTPUT)
    }

    @Test
    fun testGetStation() {
        val userId = "jdm1197"
        val response = soopApiClient.getStation(userId)

        println("response: ${objectMapper.writeValueAsString(response)}")

        assert(response != null)
        assert(response?.station != null)
        assert(response?.station?.userId == userId)
    }

    @Test
    fun testGetStationNotFound() {
        val userId = "nonexistentuser123456"
        val response = soopApiClient.getStation(userId)

        println("response: ${objectMapper.writeValueAsString(response)}")

        assert(response == null)
    }

    @Test
    fun testGetLiveDetail() {
        val userId = "jdm1197"
        val response = soopApiClient.getLiveDetail(userId)

        println("response: ${objectMapper.writeValueAsString(response)}")

        assert(response != null)
        assert(response?.channel != null)
    }

    @Test
    fun testGetLiveDetailNotLive() {
        val userId = "nonexistentuser123456"
        val response = soopApiClient.getLiveDetail(userId)

        println("response: ${objectMapper.writeValueAsString(response)}")

        assert(response != null)
        assert(response?.channel != null)
        // RESULT가 0이면 방송 중이 아님
        assert(response?.channel?.result == 0)
    }

}
