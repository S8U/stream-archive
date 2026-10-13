package com.github.s8u.streamarchive.recording.manager

import com.github.s8u.streamarchive.platform.chat.PlatformChatCollectionSession
import com.github.s8u.streamarchive.platform.chat.PlatformChatStrategy
import com.github.s8u.streamarchive.platform.enums.PlatformType
import com.github.s8u.streamarchive.platform.chat.PlatformChatStrategyFactory
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

/**
 * 녹화 채팅 수집 매니저
 *
 * 녹화별 채팅 수집 세션을 열고 닫는다.
 * 받은 채팅은 버퍼에 쌓고, 받은 원본은 원본 파일에 이어 쓴다.
 * 연결이 끊기면 수집 중인 녹화에 한해 다시 연결한다.
 */
@Component
class RecordingChatCollectManager(
    private val platformChatStrategyFactory: PlatformChatStrategyFactory,
    private val recordingChatBufferManager: RecordingChatBufferManager,
    private val recordingChatRawWriteManager: RecordingChatRawWriteManager
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

        // 연결 직후부터 원본을 받을 수 있게 수집 시작 전에 연다
        recordingChatRawWriteManager.open(recordId, videoId)

        connect(recordId, videoId, platformType, platformChannelId, recordStartedAt, chatStrategy)
    }

    fun stopCollecting(recordId: Long) {
        try {
            val session = sessions.remove(recordId)
            session?.stop()
            recordingChatRawWriteManager.close(recordId)
            logger.info("RecordingChatCollectManager: Stopped chat collect: recordId={}", recordId)
        } catch (e: Exception) {
            logger.error("RecordingChatCollectManager: Failed to stop chat collect: recordId={}", recordId, e)
        }
    }

    /**
     * 채팅 수집 세션을 연결한다.
     *
     * 연결이 끊기면 수집 중인 녹화에 한해 다시 연결한다.
     */
    private fun connect(
        recordId: Long,
        videoId: Long,
        platformType: PlatformType,
        platformChannelId: String,
        recordStartedAt: LocalDateTime,
        chatStrategy: PlatformChatStrategy
    ) {
        try {
            val session = chatStrategy.startCollecting(
                recordId,
                videoId,
                platformChannelId,
                recordStartedAt,
                onChat = { chatMessageDto ->
                    recordingChatBufferManager.add(chatMessageDto)
                },
                onRaw = { rawMessageDto ->
                    recordingChatRawWriteManager.write(recordId, rawMessageDto)
                },
                onClosed = {
                    // 원본 기록기는 다시 열지 않는다
                    // 종료와 재연결이 겹쳐도 압축을 마친 뒤에 평문 원본이 새로 생기지 않게 하기 위함이다
                    if (sessions.containsKey(recordId)) {
                        logger.info("RecordingChatCollectManager: Reconnecting chat: recordId={}", recordId)
                        sessions.remove(recordId)
                        connect(recordId, videoId, platformType, platformChannelId, recordStartedAt, chatStrategy)
                    }
                }
            ) ?: return
            sessions[recordId] = session

            logger.info("RecordingChatCollectManager: Started chat collect: recordId={}, platformType={}", recordId, platformType)
        } catch (e: Exception) {
            logger.error("RecordingChatCollectManager: Failed to start chat collect: recordId={}", recordId, e)
        }
    }

}
