package com.github.s8u.streamarchive.recording.manager

import com.github.s8u.streamarchive.platform.chat.PlatformChatCollectionSession
import com.github.s8u.streamarchive.platform.enums.PlatformType
import com.github.s8u.streamarchive.platform.chat.PlatformChatStrategyFactory
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

@Component
class RecordingChatCollectManager(
    private val platformChatStrategyFactory: PlatformChatStrategyFactory,
    private val recordingChatBufferManager: RecordingChatBufferManager
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val sessions = ConcurrentHashMap<Long, PlatformChatCollectionSession>()

    fun isCollecting(recordId: Long): Boolean {
        return sessions.containsKey(recordId)
    }

    fun startCollecting(
        recordId: Long,
        videoId: Long,
        platformType: PlatformType,
        platformChannelId: String,
        recordStartedAt: LocalDateTime
    ) {
        // 채팅 전략이 없으면 채팅 미지원 플랫폼이므로 건너뛴다
        val chatStrategy = platformChatStrategyFactory.findPlatformChatStrategy(platformType)
        if (chatStrategy == null) {
            logger.debug("RecordingChatCollectManager: Chat collect not supported for platform: {} (recordId: {})", platformType, recordId)
            return
        }

        try {
            val session = chatStrategy.startCollecting(
                recordId,
                videoId,
                platformChannelId,
                recordStartedAt,
                onChat = { chatMessageDto ->
                    recordingChatBufferManager.add(chatMessageDto)
                },
                onClosed = {
                    if (sessions.containsKey(recordId)) {
                        logger.info("RecordingChatCollectManager: Reconnecting chat: recordId={}", recordId)
                        sessions.remove(recordId)
                        startCollecting(recordId, videoId, platformType, platformChannelId, recordStartedAt)
                    }
                }
            ) ?: return
            sessions[recordId] = session

            logger.info("RecordingChatCollectManager: Started chat collect: recordId={}, platformType={}", recordId, platformType)
        } catch (e: Exception) {
            logger.error("RecordingChatCollectManager: Failed to start chat collect: recordId={}", recordId, e)
        }
    }

    fun stopCollecting(recordId: Long) {
        try {
            val session = sessions.remove(recordId)
            session?.stop()
            logger.info("RecordingChatCollectManager: Stopped chat collect: recordId={}", recordId)
        } catch (e: Exception) {
            logger.error("RecordingChatCollectManager: Failed to stop chat collect: recordId={}", recordId, e)
        }
    }

}
