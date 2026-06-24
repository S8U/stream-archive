package com.github.s8u.streamarchive.video.usecase

import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.global.properties.StorageProperties
import com.github.s8u.streamarchive.video.repository.VideoRepository
import com.github.s8u.streamarchive.video.service.VideoAccessAssertService
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.file.Files

/**
 * 동영상 채팅 이모지 파일 조회 (공개)
 */
@Service
class VideoChatEmojiGetUseCase(
    private val videoRepository: VideoRepository,
    private val videoAccessAssertService: VideoAccessAssertService,
    private val storageProperties: StorageProperties
) {

    @Transactional(readOnly = true)
    fun getByUuid(uuid: String, filename: String): Resource {
        // 파일명 검증 (보안)
        if (!filename.matches(FILENAME_REGEX)) {
            throw BusinessException("잘못된 파일명입니다.", HttpStatus.BAD_REQUEST)
        }

        val video = videoRepository.findByUuid(uuid) ?: throw BusinessException(
            "동영상을 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND
        )

        videoAccessAssertService.assertAccessible(video.contentPrivacy, video.channel?.contentPrivacy)

        val emojiPath = storageProperties.getVideoEmojiPath(video.id!!, filename)
        if (!Files.exists(emojiPath)) {
            throw BusinessException("이모지를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        return FileSystemResource(emojiPath)
    }

    companion object {
        private val FILENAME_REGEX = Regex("^[A-Za-z0-9_-]+\\.(png|gif|webp|jpg|jpeg)$", RegexOption.IGNORE_CASE)
    }

}
