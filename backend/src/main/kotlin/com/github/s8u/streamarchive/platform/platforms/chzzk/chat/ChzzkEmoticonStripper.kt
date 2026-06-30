package com.github.s8u.streamarchive.platform.platforms.chzzk.chat

import com.github.s8u.streamarchive.platform.chat.PlatformEmoticonStripper
import com.github.s8u.streamarchive.platform.enums.PlatformType
import org.springframework.stereotype.Component

/**
 * 치지직 이모티콘 코드 제거기
 *
 * 치지직 이모티콘 코드는 `{:키:}` 형식이다(예: `{:lck_2:}`, `{:d_1:}`).
 */
@Component
class ChzzkEmoticonStripper : PlatformEmoticonStripper {

    override val platformType = PlatformType.CHZZK

    override fun strip(message: String): String {
        return EMOTICON_CODE_PATTERN.replace(message, " ")
    }

    companion object {
        /** 치지직 이모티콘 코드 형식 `{:키:}`. */
        private val EMOTICON_CODE_PATTERN = Regex("""\{:[^{}]*:\}""")
    }

}
