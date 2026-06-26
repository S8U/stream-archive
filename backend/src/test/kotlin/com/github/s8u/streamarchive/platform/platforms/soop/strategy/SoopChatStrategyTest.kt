package com.github.s8u.streamarchive.platform.platforms.soop.strategy

import com.github.s8u.streamarchive.platform.platforms.soop.client.SoopApiClient
import com.github.s8u.streamarchive.platform.platforms.soop.service.SoopChatEmoticonResolveService
import io.mockk.mockk
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test

class SoopChatStrategyTest {

    private val apiClient = mockk<SoopApiClient>()
    private val soopChatEmoticonResolveService = mockk<SoopChatEmoticonResolveService>()
    private val soopChatStrategy = SoopChatStrategy(apiClient, soopChatEmoticonResolveService)

    @Test
    fun `채팅 IP와 포트로 WebSocket URL을 생성한다`() {
        val url = soopChatStrategy.getChatWebSocketUrl(
            chatIp = "1.2.3.4",
            chatPort = 8000,
            platformChannelId = "test-user"
        )

        assertEquals("wss://chat-01020304.sooplive.co.kr:8001/Websocket/test-user", url)
    }

}
