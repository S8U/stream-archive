package com.github.s8u.streamarchive.platform.chat

/**
 * 채팅 수집 세션
 *
 * 수집 방식(WebSocket·폴링)에 상관없이 진행 중인 수집을 멈추는 핸들이다.
 */
interface PlatformChatCollectionSession {

    fun stop()

}
