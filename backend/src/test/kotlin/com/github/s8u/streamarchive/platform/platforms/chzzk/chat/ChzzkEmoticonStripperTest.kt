package com.github.s8u.streamarchive.platform.platforms.chzzk.chat

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChzzkEmoticonStripperTest {

    private val chzzkEmoticonStripper = ChzzkEmoticonStripper()

    @Test
    fun `이모티콘 코드를 제거한다`() {
        val result = chzzkEmoticonStripper.strip("{:lck_2:}{:lck_2:} 한화 이겨라")

        assertTrue("{:" !in result && "lck" !in result, "결과: '$result'")
        assertTrue("한화 이겨라" in result, "결과: '$result'")
    }

    @Test
    fun `이모티콘 코드가 없으면 그대로 둔다`() {
        val message = "한화 이겨라"
        assertEquals(message, chzzkEmoticonStripper.strip(message).trim())
    }

}
