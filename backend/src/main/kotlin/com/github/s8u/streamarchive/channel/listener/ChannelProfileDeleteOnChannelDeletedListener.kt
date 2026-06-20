package com.github.s8u.streamarchive.channel.listener

import com.github.s8u.streamarchive.channel.event.ChannelDeletedEvent
import com.github.s8u.streamarchive.channel.service.ChannelProfileDeleteService
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * 채널이 삭제되면 프로필 이미지 파일을 삭제하는 리스너
 */
@Component
class ChannelProfileDeleteOnChannelDeletedListener(
    private val channelProfileDeleteService: ChannelProfileDeleteService
) {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: ChannelDeletedEvent) {
        channelProfileDeleteService.deleteProfile(event.channelId)
    }

}
