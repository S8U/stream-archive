package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.chat.ChatMessageDto
import com.github.s8u.streamarchive.repository.VideoDataChatHistoryBulkRepository
import com.github.s8u.streamarchive.repository.VideoMetadataChatHistoryRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class VideoDataChatHistoryService(
    private val chatHistoryRepository: VideoMetadataChatHistoryRepository,
    private val bulkRepository: VideoDataChatHistoryBulkRepository
) {
    private val logger = LoggerFactory.getLogger(VideoDataChatHistoryService::class.java)

    private val chatBuffer: MutableList<ChatMessageDto> = Collections.synchronizedList(mutableListOf())

    /**
     * 채팅 메시지 버퍼에 추가
     */
    fun addBuffer(chatMessageDto: ChatMessageDto) {
        chatBuffer.add(chatMessageDto)
    }

    /**
     * 버퍼 flush (배치 저장)
     */
    @Transactional
    fun flush() {
        synchronized(chatBuffer) {
            if (chatBuffer.isEmpty()) return

            val items = chatBuffer.toList()
            chatBuffer.clear()

            bulkRepository.bulkInsert(items)
            logger.debug("Flushed {} chat messages", items.size)
        }
    }

}