package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.chat.ChatMessageDto
import com.github.s8u.streamarchive.dto.ChatHistoryResponse
import com.github.s8u.streamarchive.enums.ContentPrivacy
import com.github.s8u.streamarchive.exception.BusinessException
import com.github.s8u.streamarchive.repository.VideoDataChatHistoryBulkRepository
import com.github.s8u.streamarchive.repository.VideoDataChatHistoryRepository
import com.github.s8u.streamarchive.repository.VideoRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class VideoDataChatHistoryService(
    private val videoDataChatHistoryRepository: VideoDataChatHistoryRepository,
    private val videoDataChatHistoryBulkRepository: VideoDataChatHistoryBulkRepository,
    private val videoRepository: VideoRepository,
    private val authenticationService: AuthenticationService
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

            videoDataChatHistoryBulkRepository.bulkInsert(items)
            logger.debug("Flushed {} chat messages", items.size)
        }
    }

    /**
     * 채팅 이력 조회 (공개 API)
     */
    @Transactional(readOnly = true)
    fun getChatHistoriesByVideoIdForPublic(
        uuid: String,
        offsetStart: Long,
        offsetEnd: Long
    ): List<ChatHistoryResponse> {
        // 입력 검증
        if (offsetStart < 0 || offsetEnd < 0) {
            throw BusinessException("offset 값은 0 이상이어야 합니다.", HttpStatus.BAD_REQUEST)
        }
        if (offsetStart > offsetEnd) {
            throw BusinessException("offsetStart는 offsetEnd보다 작거나 같아야 합니다.", HttpStatus.BAD_REQUEST)
        }

        // Video 조회 및 Privacy 체크
        val video = videoRepository.findByUuid(uuid)
            ?: throw BusinessException("동영상을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)

        if (video.contentPrivacy == ContentPrivacy.PRIVATE && !authenticationService.isAdmin()) {
            throw BusinessException("동영상을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        // 채팅 이력 조회
        val chatHistories = videoDataChatHistoryRepository.findByVideoIdAndOffsetMillisGreaterThanEqualAndOffsetMillisLessThanOrderByOffsetMillisAsc(
            videoId = video.id!!,
            startOffset = offsetStart,
            endOffset = offsetEnd
        )

        return chatHistories.map { ChatHistoryResponse.from(it) }
    }

}