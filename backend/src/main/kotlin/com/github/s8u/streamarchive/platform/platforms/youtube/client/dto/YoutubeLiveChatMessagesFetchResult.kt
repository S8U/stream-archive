package com.github.s8u.streamarchive.platform.platforms.youtube.client.dto

/**
 * 유튜브 라이브 채팅 메시지 조회 결과
 *
 * 채팅 원본을 보관할 수 있게 응답 본문 원문을 함께 담는다.
 * 본문을 파싱하지 못하면 [response]는 null이다.
 */
data class YoutubeLiveChatMessagesFetchResult(
    val response: YoutubeLiveChatMessagesResponse?,
    val rawBody: String
)
