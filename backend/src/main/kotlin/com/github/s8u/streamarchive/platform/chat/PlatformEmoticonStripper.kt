package com.github.s8u.streamarchive.platform.chat

import com.github.s8u.streamarchive.platform.enums.PlatformType

/**
 * 플랫폼별 이모티콘 코드 제거기
 *
 * 채팅 메시지에서 그 플랫폼의 이모티콘 코드를 걷어낸다.
 * emojis 컬럼으로 분리되지 못한 이모티콘 코드(예: 일부 구독 이모티콘)를 정제할 때 쓴다.
 * 코드 형식이 정규식으로 식별되는 플랫폼만 이 전략을 구현한다(트위치·유튜브는 평문이라 대상이 아니다).
 */
interface PlatformEmoticonStripper {

    val platformType: PlatformType

    /** 메시지에서 이 플랫폼의 이모티콘 코드를 제거한다. */
    fun strip(message: String): String

}
