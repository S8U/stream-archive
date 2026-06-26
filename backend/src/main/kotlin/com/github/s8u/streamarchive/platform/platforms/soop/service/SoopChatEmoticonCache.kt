package com.github.s8u.streamarchive.platform.platforms.soop.service

import java.time.Instant

/**
 * SOOP 채팅 이모티콘 캐시
 *
 * 키워드(/이름/)별 이미지 URL 맵과 만료 시각을 함께 담는다.
 */
data class SoopChatEmoticonCache(
    val emoticons: Map<String, String>,
    val expiresAt: Instant
)
