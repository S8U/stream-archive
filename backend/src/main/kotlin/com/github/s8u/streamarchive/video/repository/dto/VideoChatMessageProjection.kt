package com.github.s8u.streamarchive.video.repository.dto

/**
 * 동영상 채팅 메시지 투영
 *
 * 채팅 분석에 필요한 오프셋·메시지·이모티콘 정보만 담는다(엔티티 전체 로드를 피한다).
 */
data class VideoChatMessageProjection(
    val offsetMillis: Long,
    val message: String,
    /** 이모티콘 매핑 JSON(`{placeholder: filename}`). 없으면 null. */
    val emojis: String?
)
