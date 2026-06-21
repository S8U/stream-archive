package com.github.s8u.streamarchive.platform.platforms.chzzk.strategy

import com.github.s8u.streamarchive.platform.chat.dto.PlatformChatMessageDto
import com.github.s8u.streamarchive.platform.chat.websocket.PlatformChatWebSocketConnection
import com.github.s8u.streamarchive.platform.chat.websocket.PlatformChatWebSocketStrategy
import com.github.s8u.streamarchive.platform.enums.PlatformType
import com.github.s8u.streamarchive.platform.platforms.chzzk.chat.ChzzkChatWebSocketHandler
import com.github.s8u.streamarchive.platform.platforms.chzzk.client.ChzzkApiClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class ChzzkChatStrategy(
    private val chzzkApiClient: ChzzkApiClient
) : PlatformChatWebSocketStrategy() {

    private val logger = LoggerFactory.getLogger(javaClass)

    override val platformType: PlatformType
        get() = PlatformType.CHZZK

    override val chatSyncOffsetMillis: Long
        get() = 5000L

    override fun createConnection(
        recordId: Long,
        videoId: Long,
        platformChannelId: String,
        recordStartedAt: LocalDateTime,
        onChat: (PlatformChatMessageDto) -> Unit,
        onClosed: () -> Unit
    ): PlatformChatWebSocketConnection? {
        // 채팅 접속 정보 조회
        val chatChannelId = chzzkApiClient.getLiveDetail(platformChannelId)
            ?.content
            ?.chatChannelId
        if (chatChannelId == null) {
            logger.warn(
                "ChzzkChatStrategy: Chat channel ID not found: recordId={}, platformChannelId={}",
                recordId,
                platformChannelId
            )
            return null
        }

        val chatAccessToken = chzzkApiClient.getChatAccessToken(chatChannelId)
            ?.content
            ?.accessToken
        if (chatAccessToken == null) {
            logger.warn(
                "ChzzkChatStrategy: Chat access token not found: recordId={}, platformChannelId={}, chatChannelId={}",
                recordId,
                platformChannelId,
                chatChannelId
            )
            return null
        }

        // 채팅 채널에 맞는 서버 선택
        val serverId = chatChannelId.sumOf { it.code } % CHAT_SERVER_COUNT + 1

        return PlatformChatWebSocketConnection(
            url = "wss://kr-ss$serverId.chat.naver.com/chat",
            handler = ChzzkChatWebSocketHandler(
                chatChannelId = chatChannelId,
                chatAccessToken = chatAccessToken,
                recordId = recordId,
                videoId = videoId,
                platformChannelId = platformChannelId,
                recordStartedAt = recordStartedAt,
                onChat = onChat,
                onConnectionClosed = onClosed
            )
        )
    }

    companion object {
        private const val CHAT_SERVER_COUNT = 9
    }

}
