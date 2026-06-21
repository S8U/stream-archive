package com.github.s8u.streamarchive.platform.chat

import com.github.s8u.streamarchive.platform.chat.dto.PlatformChatMessageDto
import com.github.s8u.streamarchive.platform.enums.PlatformType
import java.time.LocalDateTime

/**
 * 플랫폼별 채팅 수집 전략
 *
 * 채팅을 지원하는 플랫폼만 이 전략을 구현한다.
 * 전략의 존재 여부가 곧 채팅 지원 여부다.
 * 수집 방식(WebSocket·폴링)은 구현체 안에 숨기고, 시작·중지만 노출한다.
 */
interface PlatformChatStrategy {

    val platformType: PlatformType

    val chatSyncOffsetMillis: Long

    /**
     * 채팅 수집을 시작하고 세션을 반환한다.
     *
     * 채팅 접속 정보를 찾을 수 없으면 null을 반환한다.
     * [onClosed]는 연결이 끊겼을 때 호출된다(재연결 여부는 호출자가 정한다).
     */
    fun startCollecting(
        recordId: Long,
        videoId: Long,
        platformChannelId: String,
        recordStartedAt: LocalDateTime,
        onChat: (PlatformChatMessageDto) -> Unit,
        onClosed: () -> Unit
    ): PlatformChatCollectionSession?

}
