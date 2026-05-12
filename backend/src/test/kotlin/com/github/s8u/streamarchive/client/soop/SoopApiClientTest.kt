package com.github.s8u.streamarchive.client.soop

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("external")
class SoopApiClientTest {

    private val soopApiClient = SoopApiClient()

    private val objectMapper = ObjectMapper().apply {
        enable(SerializationFeature.INDENT_OUTPUT)
    }

    @Test
    fun `스테이션을 조회하면 채널 정보를 반환한다`() {
        val response = soopApiClient.getStation(USER_ID)

        println("response: ${objectMapper.writeValueAsString(response)}")

        val actual = assertNotNull(response)
        val station = assertNotNull(actual.station)
        assertEquals(USER_ID, station.userId)
        assertFalse(station.userNick.isBlank())
    }

    @Test
    fun `존재하지 않는 스테이션을 조회하면 null을 반환한다`() {
        val response = soopApiClient.getStation(NONEXISTENT_USER_ID)

        println("response: ${objectMapper.writeValueAsString(response)}")

        assertNull(response)
    }

    @Test
    fun `라이브 상세를 조회하면 방송 상태와 무관하게 응답을 파싱할 수 있다`() {
        val response = soopApiClient.getLiveDetail(USER_ID)

        println("response: ${objectMapper.writeValueAsString(response)}")

        val actual = assertNotNull(response)
        val channel = assertNotNull(actual.channel)
        assertTrue(channel.result in setOf(0, 1))

        if (channel.result == 1) {
            assertEquals(USER_ID, channel.bjid)
            assertFalse(channel.bno.isNullOrBlank())
            assertFalse(channel.title.isNullOrBlank())
        } else {
            assertEquals(0, channel.result)
        }
    }

    @Test
    fun `방송 중이 아닐 때 라이브 상세를 조회하면 빈 응답을 반환한다`() {
        val response = soopApiClient.getLiveDetail(NONEXISTENT_USER_ID)

        println("response: ${objectMapper.writeValueAsString(response)}")

        val actual = assertNotNull(response)
        val channel = assertNotNull(actual.channel)
        assertEquals(0, channel.result)
        assertNull(channel.bjid)
        assertNull(channel.bno)
        assertNull(channel.title)
    }

    companion object {
        private const val USER_ID = "jdm1197"
        private const val NONEXISTENT_USER_ID = "nonexistentuser123456"
    }
}
