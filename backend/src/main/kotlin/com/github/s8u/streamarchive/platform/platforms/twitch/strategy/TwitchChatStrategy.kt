package com.github.s8u.streamarchive.platform.platforms.twitch.strategy

import com.github.s8u.streamarchive.platform.chat.dto.PlatformChatMessageDto
import com.github.s8u.streamarchive.platform.chat.websocket.PlatformChatWebSocketConnection
import com.github.s8u.streamarchive.platform.chat.websocket.PlatformChatWebSocketStrategy
import com.github.s8u.streamarchive.platform.enums.PlatformType
import com.github.s8u.streamarchive.platform.platforms.twitch.chat.TwitchChatWebSocketHandler
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class TwitchChatStrategy : PlatformChatWebSocketStrategy() {

    override val platformType: PlatformType
        get() = PlatformType.TWITCH

    override val chatSyncOffsetMillis: Long
        get() = 15000L

    override fun createConnection(
        recordId: Long,
        videoId: Long,
        platformChannelId: String,
        recordStartedAt: LocalDateTime,
        onChat: (PlatformChatMessageDto) -> Unit,
        onClosed: () -> Unit
    ): PlatformChatWebSocketConnection {
        return PlatformChatWebSocketConnection(
            url = "wss://irc-ws.chat.twitch.tv:443",
            handler = TwitchChatWebSocketHandler(
                recordId = recordId,
                videoId = videoId,
                platformChannelId = platformChannelId,
                recordStartedAt = recordStartedAt,
                onChat = onChat,
                onConnectionClosed = onClosed
            )
        )
    }

}
