package com.github.s8u.streamarchive.recording.usecase

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.s8u.streamarchive.global.service.TransactionRunner
import com.github.s8u.streamarchive.platform.chat.dto.PlatformChatMessageDto
import com.github.s8u.streamarchive.recording.manager.RecordingChatBufferManager
import com.github.s8u.streamarchive.video.entity.VideoChatHistory
import com.github.s8u.streamarchive.video.repository.VideoChatHistoryBulkRepository
import com.github.s8u.streamarchive.video.service.VideoChatEmojiSaveService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * 채팅 버퍼 플러시
 *
 * 버퍼에 쌓인 채팅 메시지를 꺼내 배치로 저장한다.
 */
@Service
class RecordingChatFlushUseCase(
    private val recordingChatBufferManager: RecordingChatBufferManager,
    private val videoChatEmojiSaveService: VideoChatEmojiSaveService,
    private val videoChatHistoryBulkRepository: VideoChatHistoryBulkRepository,
    private val transactionRunner: TransactionRunner,
    private val objectMapper: ObjectMapper
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun flush() {
        val items = recordingChatBufferManager.drain()
        if (items.isEmpty()) return

        val chatHistories = items.map { saveEmojisAndCreateChatHistory(it) }
        transactionRunner.run {
            videoChatHistoryBulkRepository.bulkInsert(chatHistories)
        }

        logger.debug("RecordingChatFlushUseCase: flushed {} chat messages", items.size)
    }

    private fun saveEmojisAndCreateChatHistory(item: PlatformChatMessageDto): VideoChatHistory {
        val emojiUrls = item.emojis.associate { it.placeholder to it.imageUrl }
        val savedEmojis = videoChatEmojiSaveService.saveAll(item.videoId, emojiUrls)
        val emojis = if (savedEmojis.isEmpty()) {
            null
        } else {
            objectMapper.writeValueAsString(savedEmojis.associate { it.placeholder to it.filename })
        }

        return VideoChatHistory(
            videoId = item.videoId,
            username = item.username,
            message = item.message,
            emojis = emojis,
            offsetMillis = item.offsetMillis,
            createdAt = item.createdAt
        )
    }

}
