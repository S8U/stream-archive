package com.github.s8u.streamarchive.channelplatform.event

import com.github.s8u.streamarchive.platform.enums.PlatformType

/**
 * 채널 플랫폼 수정 이벤트
 *
 * 구독 리스너가 @Async로 트랜잭션 밖에서 받으므로 JPA 엔티티를 담지 않는다.
 * 식별자와 원시값만 담고, 엔티티가 필요하면 리스너가 자기 트랜잭션에서 다시 조회한다.
 */
data class ChannelPlatformUpdatedEvent(
    val channelId: Long,
    val platformType: PlatformType
)
