package com.github.s8u.streamarchive.platform.platforms.twitch.strategy

import com.github.s8u.streamarchive.platform.chat.websocket.PlatformChatWebSocketHandler
import com.github.s8u.streamarchive.platform.chat.websocket.PlatformChatWebSocketStrategy
import com.github.s8u.streamarchive.platform.enums.PlatformType
import com.github.s8u.streamarchive.platform.platforms.twitch.chat.TwitchChatWebSocketHandler
import com.github.s8u.streamarchive.platform.chat.dto.PlatformChatMessageDto
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class TwitchChatStrategy : PlatformChatWebSocketStrategy() {

    override val platformType: PlatformType
        get() = PlatformType.TWITCH

    override val chatSyncOffsetMillis: Long
        get() = 15000L

    override val chatWebSocketUrl: String
        get() = "wss://irc-ws.chat.twitch.tv:443"

    override fun createHandler(
        recordId: Long,
        videoId: Long,
        platformChannelId: String,
        recordStartedAt: LocalDateTime,
        onChat: (PlatformChatMessageDto) -> Unit,
        onClosed: () -> Unit
    ): PlatformChatWebSocketHandler {
        return TwitchChatWebSocketHandler(
            recordId = recordId,
            videoId = videoId,
            platformChannelId = platformChannelId,
            recordStartedAt = recordStartedAt,
            onChat = onChat,
            onConnectionClosed = onClosed
        )
    }

}
