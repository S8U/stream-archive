package com.github.s8u.streamarchive.platform.platforms.chzzk.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("external")
class ChzzkApiClientTest {

    private val chzzkApiClient = ChzzkApiClient()

    private val objectMapper = ObjectMapper().apply {
        enable(SerializationFeature.INDENT_OUTPUT)
    }

    @Test
    fun `채널을 조회하면 채널 정보를 반환한다`() {
        val response = chzzkApiClient.getChannel(CHANNEL_ID)

        println("response: ${objectMapper.writeValueAsString(response)}")

        val actual = assertNotNull(response)
        assertEquals(200, actual.code)
        val content = assertNotNull(actual.content)
        assertEquals(CHANNEL_ID, content.channelId)
        assertFalse(content.channelName.isBlank())
        assertNotNull(content.openLive)
    }

    @Test
    fun `라이브 상세를 조회하면 방송 상태와 무관하게 응답을 파싱할 수 있다`() {
        val response = chzzkApiClient.getLiveDetail(CHANNEL_ID)

        println("response: ${objectMapper.writeValueAsString(response)}")

        val actual = assertNotNull(response)
        assertEquals(200, actual.code)
        val content = assertNotNull(actual.content)
        assertTrue(content.status in setOf("OPEN", "CLOSE"))
        assertEquals(CHANNEL_ID, assertNotNull(content.channel).channelId)

        if (content.status == "OPEN") {
            assertNotNull(content.liveId)
            assertFalse(content.liveTitle.isNullOrBlank())
            assertFalse(content.chatChannelId.isNullOrBlank())
            assertFalse(content.livePlaybackJson.isNullOrBlank())
        }
    }

    companion object {
        private const val CHANNEL_ID = "6e06f5e1907f17eff543abd06cb62891"
    }
}
