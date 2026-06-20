package com.github.s8u.streamarchive.platform.platforms.chzzk.strategy

import com.github.s8u.streamarchive.platform.chat.websocket.PlatformChatWebSocketHandler
import com.github.s8u.streamarchive.platform.chat.websocket.PlatformChatWebSocketStrategy
import com.github.s8u.streamarchive.platform.enums.PlatformType
import com.github.s8u.streamarchive.platform.platforms.chzzk.chat.ChzzkChatWebSocketHandler
import com.github.s8u.streamarchive.platform.platforms.chzzk.client.ChzzkApiClient
import com.github.s8u.streamarchive.platform.chat.dto.PlatformChatMessageDto
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class ChzzkChatStrategy(
    private val apiClient: ChzzkApiClient
) : PlatformChatWebSocketStrategy() {

    override val platformType: PlatformType
        get() = PlatformType.CHZZK

    override val chatSyncOffsetMillis: Long
        get() = 5000L

    override val chatWebSocketUrl: String
        get() = "wss://kr-ss1.chat.naver.com/chat"

    override fun createHandler(
        recordId: Long,
        videoId: Long,
        platformChannelId: String,
        recordStartedAt: LocalDateTime,
        onChat: (PlatformChatMessageDto) -> Unit,
        onClosed: () -> Unit
    ): PlatformChatWebSocketHandler {
        return ChzzkChatWebSocketHandler(
            chzzkApiClient = apiClient,
            recordId = recordId,
            videoId = videoId,
            platformChannelId = platformChannelId,
            recordStartedAt = recordStartedAt,
            onChat = onChat,
            onConnectionClosed = onClosed
        )
    }

}
