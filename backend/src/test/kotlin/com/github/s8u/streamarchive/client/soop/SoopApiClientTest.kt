package com.github.s8u.streamarchive.client.soop

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@Tag("external")
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

        val actual = assertNotNull(response)
        val station = assertNotNull(actual.station)
        assertEquals(userId, station.userId)
    }

    @Test
    fun testGetStationNotFound() {
        val userId = "nonexistentuser123456"
        val response = soopApiClient.getStation(userId)

        println("response: ${objectMapper.writeValueAsString(response)}")

        assertNull(response)
    }

    @Test
    fun testGetLiveDetail() {
        val userId = "jdm1197"
        val response = soopApiClient.getLiveDetail(userId)

        println("response: ${objectMapper.writeValueAsString(response)}")

        val actual = assertNotNull(response)
        assertNotNull(actual.channel)
    }

    @Test
    fun testGetLiveDetailNotLive() {
        val userId = "nonexistentuser123456"
        val response = soopApiClient.getLiveDetail(userId)

        println("response: ${objectMapper.writeValueAsString(response)}")

        val actual = assertNotNull(response)
        val channel = assertNotNull(actual.channel)
        // RESULT가 0이면 방송 중이 아님
        assertEquals(0, channel.result)
    }

}
