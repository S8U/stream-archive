package com.github.s8u.streamarchive.platform.platforms.soop.chat

import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.junit.jupiter.api.Test

class SoopChatPacketUtilsTest {

    @Test
    fun `패킷을 생성하고 다시 파싱한다`() {
        val packet = SoopChatPacketUtils.makePacket(
            commandType = SoopChatCommandType.CHAT_MESSAGE,
            body = listOf(
                SoopChatPacketUtils.FORM_FEED,
                "안녕하세요",
                SoopChatPacketUtils.FORM_FEED,
                "test-user",
                SoopChatPacketUtils.FORM_FEED
            )
        )

        val actual = assertNotNull(SoopChatPacketUtils.parsePacket(packet))

        assertEquals(SoopChatCommandType.CHAT_MESSAGE.value, actual.serviceCode)
        assertEquals(0, actual.resultCode)
        assertEquals(listOf("안녕하세요", "test-user"), actual.packet)
    }

}
