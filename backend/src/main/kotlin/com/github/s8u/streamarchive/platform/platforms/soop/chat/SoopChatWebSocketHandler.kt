package com.github.s8u.streamarchive.platform.platforms.soop.chat

import com.github.s8u.streamarchive.platform.chat.dto.PlatformChatEmojiDto
import com.github.s8u.streamarchive.platform.chat.dto.PlatformChatMessageDto
import com.github.s8u.streamarchive.platform.chat.websocket.PlatformChatWebSocketHandler
import com.github.s8u.streamarchive.platform.platforms.soop.service.SoopChatEmoticonResolveService
import org.slf4j.LoggerFactory
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.SubProtocolCapable
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * SOOP 채팅 WebSocket 핸들러
 *
 * SOOP 바이너리 채팅 패킷을 공통 채팅 메시지로 변환한다.
 * 로그인, 채팅방 입장, 연결 유지 패킷 전송을 처리한다.
 */
class SoopChatWebSocketHandler(
    private val chatRoomNo: String,
    private val fanTicket: String,
    private val soopChatEmoticonResolveService: SoopChatEmoticonResolveService,
    recordId: Long,
    videoId: Long,
    platformChannelId: String,
    recordStartedAt: LocalDateTime,
    onChat: (chatMessageDto: PlatformChatMessageDto) -> Unit,
    onConnectionClosed: () -> Unit
) : PlatformChatWebSocketHandler(
    recordId,
    videoId,
    platformChannelId,
    recordStartedAt,
    onChat,
    onConnectionClosed
), SubProtocolCapable {

    private val logger = LoggerFactory.getLogger(javaClass)
    private var keepAliveExecutor: ScheduledExecutorService? = null

    override fun afterConnectionEstablished(session: WebSocketSession) {
        logger.debug("SoopChatWebSocketHandler: SOOP chat established (recordId: {})", recordId)

        sendLoginPacket(session)
        startKeepAlive(session)
    }

    override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
        if (message !is BinaryMessage) return

        val bytes = ByteArray(message.payload.remaining())
        message.payload.get(bytes)

        val packet = SoopChatPacketUtils.parsePacket(bytes) ?: return
        if (packet.resultCode > 0) {
            logger.warn(
                "SoopChatWebSocketHandler: SOOP chat packet returned error: recordId={}, serviceCode={}, resultCode={}",
                recordId,
                packet.serviceCode,
                packet.resultCode
            )
            return
        }

        when (packet.serviceCode) {
            // 로그인 성공 → 채팅방 입장
            SoopChatCommandType.LOGIN.value -> sendJoinChannelPacket(session)

            // 일반 채팅
            SoopChatCommandType.CHAT_MESSAGE.value -> handleChatMessage(packet)

            // OGQ 이모티콘 (스트리머 이모티콘도 이 패킷으로 들어올 수 있다)
            SoopChatCommandType.OGQ_EMOTICON.value -> handleOgqEmoticonMessage(packet)
        }
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        logger.error(
            "SoopChatWebSocketHandler: SOOP chat transport error (recordId: {}, sessionId: {})",
            recordId,
            session.id,
            exception
        )
    }

    override fun afterConnectionClosed(session: WebSocketSession, closeStatus: CloseStatus) {
        stopKeepAlive()

        if (closeStatus == CloseStatus.NORMAL) {
            logger.info(
                "SoopChatWebSocketHandler: SOOP chat closed (recordId: {}, sessionId: {}, code: {}, reason: {})",
                recordId,
                session.id,
                closeStatus.code,
                closeStatus.reason
            )
        } else {
            logger.warn(
                "SoopChatWebSocketHandler: SOOP chat closed abnormally " +
                    "(recordId: {}, sessionId: {}, code: {}, reason: {})",
                recordId,
                session.id,
                closeStatus.code,
                closeStatus.reason
            )
        }

        onConnectionClosed()
    }

    override fun supportsPartialMessages(): Boolean {
        return false
    }

    override fun getSubProtocols(): List<String> {
        return listOf(CHAT_PROTOCOL)
    }

    override fun stop() {
        stopKeepAlive()
    }

    private fun handleChatMessage(packet: SoopChatPacketDto) {
        val items = packet.packet
        val message = items.getOrNull(0)?.replace("\r", "") ?: return
        val userId = items.getOrNull(1)
        val nickname = items.getOrNull(5)?.ifBlank { null } ?: userId ?: return
        val emojis = soopChatEmoticonResolveService.resolve(message)

        val time = LocalDateTime.now()
        val offsetMillis = Duration.between(recordStartedAt, time).toMillis()

        onChat(
            PlatformChatMessageDto(
                recordId = recordId,
                videoId = videoId,
                username = nickname,
                message = message,
                emojis = emojis,
                offsetMillis = offsetMillis,
                createdAt = time
            )
        )
    }

    private fun handleOgqEmoticonMessage(packet: SoopChatPacketDto) {
        val items = packet.packet
        val message = items.getOrNull(1)?.replace("\r", "") ?: ""
        val groupId = items.getOrNull(2)?.ifBlank { null } ?: return
        val subId = items.getOrNull(3)?.ifBlank { null } ?: return
        val userId = items.getOrNull(5)
        val nickname = items.getOrNull(6)?.ifBlank { null } ?: userId ?: return
        val type = items.getOrNull(10)?.toIntOrNull() ?: 0
        if (type == STAFF_CHAT_TYPE || type == POLICE_CHAT_TYPE) return

        // OGQ는 패킷 값으로 이미지 URL을 만들 수 있어 채널별 매니페스트를 조회하지 않는다
        val placeholder = "{:soop-ogq-$groupId-$subId:}"
        val chatMessage = if (message.isBlank()) {
            placeholder
        } else {
            "$placeholder $message"
        }
        val imageUrl = getOgqEmoticonImageUrl(
            groupId = groupId,
            subId = subId,
            extension = getOgqEmoticonExtension(items)
        )

        val time = LocalDateTime.now()
        val offsetMillis = Duration.between(recordStartedAt, time).toMillis()

        onChat(
            PlatformChatMessageDto(
                recordId = recordId,
                videoId = videoId,
                username = nickname,
                message = chatMessage,
                emojis = listOf(
                    PlatformChatEmojiDto(
                        placeholder = placeholder,
                        imageUrl = imageUrl
                    )
                ),
                offsetMillis = offsetMillis,
                createdAt = time
            )
        )
    }

    private fun sendLoginPacket(session: WebSocketSession) {
        val packet = SoopChatPacketUtils.makePacket(
            commandType = SoopChatCommandType.LOGIN,
            body = listOf(
                SoopChatPacketUtils.FORM_FEED,
                "",
                SoopChatPacketUtils.FORM_FEED,
                "",
                SoopChatPacketUtils.FORM_FEED,
                GUEST_FLAG,
                SoopChatPacketUtils.FORM_FEED
            )
        )

        sendMessage(session, packet)
    }

    private fun sendJoinChannelPacket(session: WebSocketSession) {
        val packet = SoopChatPacketUtils.makePacket(
            commandType = SoopChatCommandType.JOIN_CHANNEL,
            body = listOf(
                SoopChatPacketUtils.FORM_FEED,
                chatRoomNo,
                SoopChatPacketUtils.FORM_FEED,
                fanTicket,
                SoopChatPacketUtils.FORM_FEED,
                0,
                SoopChatPacketUtils.FORM_FEED,
                "",
                SoopChatPacketUtils.FORM_FEED,
                "",
                SoopChatPacketUtils.FORM_FEED
            )
        )

        sendMessage(session, packet)
    }

    private fun sendKeepAlivePacket(session: WebSocketSession) {
        val packet = SoopChatPacketUtils.makePacket(
            commandType = SoopChatCommandType.KEEP_ALIVE,
            body = listOf(SoopChatPacketUtils.FORM_FEED)
        )

        sendMessage(session, packet)
    }

    private fun sendMessage(session: WebSocketSession, bytes: ByteArray) {
        if (session.isOpen) {
            session.sendMessage(BinaryMessage(bytes))
        }
    }

    private fun getOgqEmoticonImageUrl(
        groupId: String,
        subId: String,
        extension: String
    ): String {
        return "$OGQ_IMAGE_HOST/sticker/$groupId/${subId}_$OGQ_CHAT_IMAGE_SIZE.$extension"
    }

    private fun getOgqEmoticonExtension(items: List<String>): String {
        val isAnimated = items.getOrNull(17) == "1"
        return if (isAnimated) "webp" else "png"
    }

    private fun startKeepAlive(session: WebSocketSession) {
        stopKeepAlive()

        keepAliveExecutor = Executors.newSingleThreadScheduledExecutor { runnable ->
            Thread(runnable, "soop-chat-keepalive-$recordId").apply {
                isDaemon = true
            }
        }.apply {
            scheduleAtFixedRate(
                { sendKeepAlivePacket(session) },
                KEEP_ALIVE_INTERVAL_SECONDS,
                KEEP_ALIVE_INTERVAL_SECONDS,
                TimeUnit.SECONDS
            )
        }
    }

    private fun stopKeepAlive() {
        keepAliveExecutor?.shutdownNow()
        keepAliveExecutor = null
    }

    companion object {
        private const val CHAT_PROTOCOL = "chat"
        private const val GUEST_FLAG = 16
        private const val KEEP_ALIVE_INTERVAL_SECONDS = 60L
        private const val STAFF_CHAT_TYPE = 1
        private const val POLICE_CHAT_TYPE = 2
        private const val OGQ_CHAT_IMAGE_SIZE = 80
        private const val OGQ_IMAGE_HOST = "https://ogq-sticker-global-cdn-z01.sooplive.co.kr"
    }

}
