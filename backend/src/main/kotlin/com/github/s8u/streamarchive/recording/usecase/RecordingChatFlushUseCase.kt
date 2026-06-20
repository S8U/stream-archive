package com.github.s8u.streamarchive.recording.usecase

import com.github.s8u.streamarchive.recording.manager.RecordingChatBufferManager
import com.github.s8u.streamarchive.video.repository.VideoChatHistoryBulkRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 채팅 버퍼 플러시
 *
 * 버퍼에 쌓인 채팅 메시지를 꺼내 배치로 저장한다.
 */
@Service
class RecordingChatFlushUseCase(
    private val recordingChatBufferManager: RecordingChatBufferManager,
    private val videoChatHistoryBulkRepository: VideoChatHistoryBulkRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun flush() {
        val items = recordingChatBufferManager.drain()
        if (items.isEmpty()) return

        videoChatHistoryBulkRepository.bulkInsert(items)
        logger.debug("RecordingChatFlushUseCase: flushed {} chat messages", items.size)
    }

}
