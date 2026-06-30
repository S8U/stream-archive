package com.github.s8u.streamarchive.platform.platforms.soop.chat

import com.github.s8u.streamarchive.platform.chat.PlatformEmoticonStripper
import com.github.s8u.streamarchive.platform.enums.PlatformType
import org.springframework.stereotype.Component

/**
 * SOOP 이모티콘 코드 제거기
 *
 * SOOP OGQ 이모티콘 코드는 `{:soop-ogq-그룹-번호:}` 형식이다.
 * 일반 이모티콘(`/키워드/`)은 URL·날짜 등과 혼동될 위험이 있어 정규식으로 걷어내지 않고 emojis 분리에 맡긴다.
 */
@Component
class SoopEmoticonStripper : PlatformEmoticonStripper {

    override val platformType = PlatformType.SOOP

    override fun strip(message: String): String {
        return OGQ_CODE_PATTERN.replace(message, " ")
    }

    companion object {
        /** SOOP OGQ 이모티콘 코드 형식 `{:soop-ogq-...:}`. */
        private val OGQ_CODE_PATTERN = Regex("""\{:soop-ogq-[^{}]*:\}""")
    }

}
