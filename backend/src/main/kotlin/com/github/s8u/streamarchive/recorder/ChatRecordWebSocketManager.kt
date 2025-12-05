package com.github.s8u.streamarchive.recorder

import com.github.s8u.streamarchive.enums.PlatformType
import com.github.s8u.streamarchive.platform.PlatformStrategyFactory
import com.github.s8u.streamarchive.service.VideoDataChatHistoryService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import java.net.URI
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

@Component
class ChatRecordWebSocketManager(
    private val platformStrategyFactory: PlatformStrategyFactory,
    private val videoDataChatHistoryService: VideoDataChatHistoryService
) {

    private val logger = LoggerFactory.getLogger(ChatRecordWebSocketManager::class.java)

    private val webSocketSessions = ConcurrentHashMap<Long, WebSocketSession>()

    fun isRecording(recordId: Long): Boolean {
        return webSocketSessions.containsKey(recordId)
    }

    fun startRecording(
        recordId: Long,
        videoId: Long,
        platformType: PlatformType,
        platformChannelId: String,
        recordStartedAt: LocalDateTime
    ) {
        val platformStrategy = platformStrategyFactory.getPlatformStrategy(platformType)
        if (!platformStrategy.isSupportChatRecord()) {
            logger.debug("Chat record not supported for platform: {} (recordId: {})", platformType, recordId)
            return
        }

        try {
            val client = StandardWebSocketClient()

            val platformWebSocketUrl = platformStrategy.getChatWebSocketUrl()
                ?: return

            val platformWebSocketHandler = platformStrategy.createChatWebSocketHandler(
                recordId,
                videoId,
                platformType,
                platformChannelId,
                recordStartedAt,
                onChat = { chatMessageDto ->
                    videoDataChatHistoryService.addBuffer(chatMessageDto)
                },
                onConnectionClosed = {
                    if (webSocketSessions.containsKey(recordId)) {
                        logger.info("Reconnecting chat: recordId={}", recordId)
                        webSocketSessions.remove(recordId)
                        startRecording(recordId, videoId, platformType, platformChannelId, recordStartedAt)
                    }
                }
            ) ?: return

            val session = client.execute(platformWebSocketHandler, null, URI(platformWebSocketUrl)).get()
            webSocketSessions[recordId] = session

            logger.info("Started chat recording: recordId={}, platformType={}", recordId, platformType)
        } catch (e: Exception) {
            logger.error("Failed to start chat recording: recordId={}", recordId, e)
        }
    }

    fun stopRecording(recordId: Long) {
        try {
            val session = webSocketSessions.remove(recordId)
            if (session != null && session.isOpen) {
                session.close()
            }
            logger.info("Stopped chat recording: recordId={}", recordId)
        } catch (e: Exception) {
            logger.error("Failed to stop chat recording: recordId={}", recordId, e)
        }
    }

}