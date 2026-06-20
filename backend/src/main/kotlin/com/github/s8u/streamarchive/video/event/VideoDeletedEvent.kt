package com.github.s8u.streamarchive.video.event

/**
 * 동영상 삭제 이벤트
 *
 * 구독 리스너가 @Async로 트랜잭션 밖에서 받으므로 JPA 엔티티를 담지 않는다.
 * 삭제된 동영상 식별자만 담아, 리스너가 관련 파일을 정리한다.
 */
data class VideoDeletedEvent(
    val videoId: Long
)
