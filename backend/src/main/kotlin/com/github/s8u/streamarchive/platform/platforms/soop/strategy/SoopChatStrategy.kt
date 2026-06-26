package com.github.s8u.streamarchive.platform.platforms.soop.strategy

import com.github.s8u.streamarchive.platform.chat.dto.PlatformChatMessageDto
import com.github.s8u.streamarchive.platform.chat.websocket.PlatformChatWebSocketConnection
import com.github.s8u.streamarchive.platform.chat.websocket.PlatformChatWebSocketStrategy
import com.github.s8u.streamarchive.platform.enums.PlatformType
import com.github.s8u.streamarchive.platform.platforms.soop.chat.SoopChatWebSocketHandler
import com.github.s8u.streamarchive.platform.platforms.soop.client.SoopApiClient
import com.github.s8u.streamarchive.platform.platforms.soop.service.SoopChatEmoticonResolveService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * SOOP 채팅 전략
 *
 * 라이브 상세 정보에서 채팅 접속 정보를 가져온다.
 * SOOP 채팅 WebSocket 연결 정보를 만든다(연결·세션은 공통 베이스가 맡는다).
 */
@Component
class SoopChatStrategy(
    private val soopApiClient: SoopApiClient,
    private val soopChatEmoticonResolveService: SoopChatEmoticonResolveService
) : PlatformChatWebSocketStrategy() {

    private val logger = LoggerFactory.getLogger(javaClass)

    override val platformType: PlatformType
        get() = PlatformType.SOOP

    override val chatSyncOffsetMillis: Long
        get() = 15000L

    override fun createConnection(
        recordId: Long,
        videoId: Long,
        platformChannelId: String,
        recordStartedAt: LocalDateTime,
        onChat: (PlatformChatMessageDto) -> Unit,
        onClosed: () -> Unit
    ): PlatformChatWebSocketConnection? {
        val channel = soopApiClient.getLiveDetail(platformChannelId)?.channel
        if (channel == null || channel.result != 1) {
            logger.warn(
                "SoopChatStrategy: SOOP live detail not found: recordId={}, platformChannelId={}",
                recordId,
                platformChannelId
            )
            return null
        }

        val chatRoomNo = channel.chatNo
        val chatIp = channel.chatIp
        val chatPort = channel.chatPort
        if (chatRoomNo.isNullOrBlank() || chatIp.isNullOrBlank() || chatPort == null) {
            logger.warn(
                "SoopChatStrategy: SOOP chat info not found: recordId={}, platformChannelId={}",
                recordId,
                platformChannelId
            )
            return null
        }

        return PlatformChatWebSocketConnection(
            url = getChatWebSocketUrl(
                chatIp = chatIp,
                chatPort = chatPort,
                platformChannelId = platformChannelId
            ),
            handler = SoopChatWebSocketHandler(
                chatRoomNo = chatRoomNo,
                fanTicket = channel.fanTicket ?: "",
                soopChatEmoticonResolveService = soopChatEmoticonResolveService,
                recordId = recordId,
                videoId = videoId,
                platformChannelId = platformChannelId,
                recordStartedAt = recordStartedAt,
                onChat = onChat,
                onConnectionClosed = onClosed
            ),
            binary = true
        )
    }

    /**
     * SOOP 채팅 WebSocket URL을 생성한다.
     *
     * IP 주소 형식의 호스트는 SOOP 채팅 도메인 형식으로 바꾼다.
     */
    fun getChatWebSocketUrl(
        chatIp: String,
        chatPort: Int,
        platformChannelId: String
    ): String {
        val host = if (IP_REGEX.matches(chatIp)) {
            val hex = chatIp
                .split(".")
                .joinToString("") { part ->
                    part.toInt().toString(16).padStart(2, '0')
                }
                .uppercase()

            "chat-$hex.sooplive.co.kr"
        } else {
            chatIp
        }

        return "wss://$host:${chatPort + 1}/Websocket/$platformChannelId"
    }

    companion object {
        private val IP_REGEX = Regex("""^\d{1,3}(\.\d{1,3}){3}$""")
    }

}
