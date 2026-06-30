package com.github.s8u.streamarchive.platform.chat

import com.github.s8u.streamarchive.platform.enums.PlatformType
import org.springframework.stereotype.Component

@Component
class PlatformEmoticonStripperFactory(
    private val strippers: List<PlatformEmoticonStripper>
) {

    /**
     * 이모티콘 코드 제거기를 찾는다.
     *
     * 코드 형식이 정규식으로 식별되지 않는 플랫폼(트위치·유튜브)은 null을 반환한다.
     */
    fun findPlatformEmoticonStripper(platformType: PlatformType): PlatformEmoticonStripper? {
        return strippers.find { it.platformType == platformType }
    }

}
