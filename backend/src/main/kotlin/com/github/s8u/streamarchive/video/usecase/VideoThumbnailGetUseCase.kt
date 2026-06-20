package com.github.s8u.streamarchive.video.usecase

import com.github.s8u.streamarchive.video.service.VideoAccessAssertService
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.global.properties.StorageProperties
import com.github.s8u.streamarchive.video.repository.VideoRepository
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.file.Files

/**
 * 동영상 썸네일 파일 조회 (공개)
 */
@Service
class VideoThumbnailGetUseCase(
    private val videoRepository: VideoRepository,
    private val videoAccessAssertService: VideoAccessAssertService,
    private val storageProperties: StorageProperties
) {

    @Transactional(readOnly = true)
    fun getByUuid(uuid: String): Resource {
        val video = videoRepository.findByUuid(uuid) ?: throw BusinessException(
            "동영상을 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND
        )

        videoAccessAssertService.assertAccessible(video.contentPrivacy, video.channel?.contentPrivacy)

        val thumbnailPath = storageProperties.getVideoThumbnailPath(video.id!!)
        if (!Files.exists(thumbnailPath)) {
            throw BusinessException("썸네일을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        return FileSystemResource(thumbnailPath)
    }

}
