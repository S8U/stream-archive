package com.github.s8u.streamarchive.channelplatform.listener

import com.github.s8u.streamarchive.channelplatform.event.ChannelPlatformUpdatedEvent
import com.github.s8u.streamarchive.channelplatform.usecase.ChannelPlatformProfileSyncUseCase
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * 채널 플랫폼이 수정되면 프로필을 동기화하는 리스너
 */
@Component
class ChannelPlatformProfileSyncOnUpdatedListener(
    private val channelPlatformProfileSyncUseCase: ChannelPlatformProfileSyncUseCase
) {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: ChannelPlatformUpdatedEvent) {
        channelPlatformProfileSyncUseCase.sync(event.channelId, event.platformType)
    }

}
