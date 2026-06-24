package com.github.s8u.streamarchive.video.usecase

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.global.service.UrlService
import com.github.s8u.streamarchive.video.repository.VideoChatHistoryRepository
import com.github.s8u.streamarchive.video.repository.VideoRepository
import com.github.s8u.streamarchive.video.service.VideoAccessAssertService
import com.github.s8u.streamarchive.video.usecase.dto.result.VideoChatEmojiSearchResult
import com.github.s8u.streamarchive.video.usecase.dto.result.VideoChatHistorySearchResult
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 동영상 채팅 이력 조회 (공개)
 */
@Service
class VideoChatHistorySearchUseCase(
    private val videoChatHistoryRepository: VideoChatHistoryRepository,
    private val videoRepository: VideoRepository,
    private val videoAccessAssertService: VideoAccessAssertService,
    private val urlService: UrlService,
    private val objectMapper: ObjectMapper
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional(readOnly = true)
    fun search(uuid: String, offsetStart: Long, offsetEnd: Long): List<VideoChatHistorySearchResult> {
        // 입력 검증
        if (offsetStart < 0 || offsetEnd < 0) {
            throw BusinessException("offset 값은 0 이상이어야 합니다.", HttpStatus.BAD_REQUEST)
        }
        if (offsetStart > offsetEnd) {
            throw BusinessException(
                "offsetStart는 offsetEnd보다 작거나 같아야 합니다.",
                HttpStatus.BAD_REQUEST
            )
        }

        // Video 조회 및 Privacy 체크
        val video = videoRepository.findByUuid(uuid)
            ?: throw BusinessException("동영상을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)

        videoAccessAssertService.assertAccessible(video.contentPrivacy, video.channel?.contentPrivacy)

        // 채팅 이력 조회
        val chatHistories = videoChatHistoryRepository
            .findByVideoIdAndOffsetMillisGreaterThanEqualAndOffsetMillisLessThanOrderByOffsetMillisAsc(
                videoId = video.id!!,
                startOffset = offsetStart,
                endOffset = offsetEnd
            )

        return chatHistories.map {
            VideoChatHistorySearchResult.from(
                chatHistory = it,
                emojis = getEmojis(it.emojis, video.uuid)
            )
        }
    }

    private fun getEmojis(emojisJson: String?, videoUuid: String): List<VideoChatEmojiSearchResult> {
        if (emojisJson.isNullOrBlank()) return emptyList()

        return try {
            val filenames = objectMapper.readValue(
                emojisJson,
                object : TypeReference<Map<String, String>>() {}
            )

            filenames.map { (placeholder, filename) ->
                VideoChatEmojiSearchResult(
                    placeholder = placeholder,
                    filename = filename,
                    imageUrl = urlService.videoEmojiUrl(videoUuid, filename)
                )
            }
        } catch (e: Exception) {
            logger.warn("VideoChatHistorySearchUseCase: Failed to parse chat emojis: videoUuid={}", videoUuid, e)
            emptyList()
        }
    }

}
