package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.dto.AdminVideoResponse
import com.github.s8u.streamarchive.dto.AdminVideoSearchRequest
import com.github.s8u.streamarchive.dto.AdminVideoUpdateRequest
import com.github.s8u.streamarchive.dto.PublicVideoResponse
import com.github.s8u.streamarchive.dto.PublicVideoSearchRequest
import com.github.s8u.streamarchive.entity.Video
import com.github.s8u.streamarchive.enums.ContentPrivacy
import com.github.s8u.streamarchive.exception.BusinessException
import com.github.s8u.streamarchive.properties.StorageProperties
import com.github.s8u.streamarchive.repository.VideoRepository
import com.github.s8u.streamarchive.util.UrlBuilder
import org.slf4j.LoggerFactory
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.file.Files
import java.time.LocalDateTime
import java.util.Comparator

@Service
class VideoService(
    private val videoRepository: VideoRepository,
    private val authenticationService: AuthenticationService,
    private val storageProperties: StorageProperties,
    private val urlBuilder: UrlBuilder
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional(readOnly = true)
    fun searchForAdmin(request: AdminVideoSearchRequest, pageable: Pageable): Page<AdminVideoResponse> {
        return videoRepository.searchForAdmin(request, pageable)
            .map { video ->
                val channelProfileUrl = urlBuilder.channelProfileUrl(video.channel?.uuid!!)
                AdminVideoResponse.from(
                    video = video,
                    channelProfileUrl = channelProfileUrl,
                    thumbnailUrl = urlBuilder.videoThumbnailUrl(video.uuid),
                    playlistUrl = urlBuilder.videoPlaylistUrl(video.uuid)
                )
            }
    }

    @Transactional(readOnly = true)
    fun getForAdmin(id: Long): AdminVideoResponse {
        val video = videoRepository.findById(id).orElseThrow {
            BusinessException("동영상을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        val channelProfileUrl = urlBuilder.channelProfileUrl(video.channel?.uuid!!)
        return AdminVideoResponse.from(
            video = video,
            channelProfileUrl = channelProfileUrl,
            thumbnailUrl = urlBuilder.videoThumbnailUrl(video.uuid),
            playlistUrl = urlBuilder.videoPlaylistUrl(video.uuid)
        )
    }

    @Transactional
    fun updateForAdmin(id: Long, request: AdminVideoUpdateRequest): AdminVideoResponse {
        val video = videoRepository.findById(id).orElseThrow {
            BusinessException("동영상을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        request.title?.let { video.title = it }
        request.contentPrivacy?.let { video.contentPrivacy = it }
        request.chatSyncOffsetMillis?.let { video.chatSyncOffsetMillis = it }

        val channelProfileUrl = urlBuilder.channelProfileUrl(video.channel?.uuid!!)
        return AdminVideoResponse.from(
            video = video,
            channelProfileUrl = channelProfileUrl,
            thumbnailUrl = urlBuilder.videoThumbnailUrl(video.uuid),
            playlistUrl = urlBuilder.videoPlaylistUrl(video.uuid)
        )
    }

    @Transactional
    fun delete(id: Long) {
        val video = videoRepository.findById(id).orElseThrow {
            BusinessException("동영상을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        deleteVideoFiles(video)

        video.isActive = false
        video.deletedAt = LocalDateTime.now()
    }

    @Transactional
    fun deleteAllByChannelId(channelId: Long) {
        val videos = videoRepository.findByChannelId(channelId)

        videos.forEach { video ->
            deleteVideoFiles(video)

            video.isActive = false
            video.deletedAt = LocalDateTime.now()
        }
    }

    @Transactional(readOnly = true)
    fun searchForPublic(request: PublicVideoSearchRequest, pageable: Pageable): Page<PublicVideoResponse> {
        return videoRepository.searchForPublic(request, pageable)
            .map { video ->
                if (video.channel == null) {
                    throw BusinessException("채널을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
                }
                PublicVideoResponse.from(
                    video = video,
                    channelProfileUrl = urlBuilder.channelProfileUrl(video.channel!!.uuid),
                    thumbnailUrl = urlBuilder.videoThumbnailUrl(video.uuid),
                    playlistUrl = urlBuilder.videoPlaylistUrl(video.uuid)
                )
            }
    }

    @Transactional(readOnly = true)
    fun getByUuidForPublic(uuid: String): PublicVideoResponse {
        val video = videoRepository.findByUuid(uuid) ?: throw BusinessException(
            "동영상을 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND
        )

        // Privacy 체크
        if (video.contentPrivacy == ContentPrivacy.PRIVATE && !authenticationService.isAdmin()) {
            throw BusinessException("동영상을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        if (video.channel == null) {
            throw BusinessException("채널을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        return PublicVideoResponse.from(
            video = video,
            channelProfileUrl = urlBuilder.channelProfileUrl(video.channel!!.uuid),
            thumbnailUrl = urlBuilder.videoThumbnailUrl(video.uuid),
            playlistUrl = urlBuilder.videoPlaylistUrl(video.uuid)
        )
    }

    fun getThumbnailByUuid(uuid: String): Resource {
        val video = videoRepository.findByUuid(uuid) ?: throw BusinessException(
            "동영상을 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND
        )

        // Privacy 체크
        if (video.contentPrivacy == ContentPrivacy.PRIVATE && !authenticationService.isAdmin()) {
            throw BusinessException("동영상을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        val thumbnailPath = storageProperties.getVideoThumbnailPath(video.id!!)
        if (!Files.exists(thumbnailPath)) {
            throw BusinessException("썸네일을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        return FileSystemResource(thumbnailPath)
    }

    fun getPlaylistByUuid(uuid: String): Resource {
        val video = videoRepository.findByUuid(uuid) ?: throw BusinessException(
            "동영상을 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND)

        // Privacy 체크
        if (video.contentPrivacy == ContentPrivacy.PRIVATE && !authenticationService.isAdmin()) {
            throw BusinessException("동영상을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        val playlistPath = storageProperties.getVideoPlaylistPath(video.id!!)
        if (!Files.exists(playlistPath)) {
            throw BusinessException("플레이리스트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        return FileSystemResource(playlistPath)
    }

    fun getSegmentByUuid(uuid: String, filename: String): Resource {
        // 파일명 검증 (보안)
        if (!filename.matches(Regex("^segment_\\d+\\.ts$"))) {
            throw BusinessException("잘못된 파일명입니다.", HttpStatus.BAD_REQUEST)
        }

        val video = videoRepository.findByUuid(uuid) ?: throw BusinessException(
            "동영상을 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND
        )

        // Privacy 체크
        if (video.contentPrivacy == ContentPrivacy.PRIVATE && !authenticationService.isAdmin()) {
            throw BusinessException("동영상을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        val segmentPath = storageProperties.getVideoPath(video.id!!).resolve(filename)
        if (!Files.exists(segmentPath)) {
            throw BusinessException("세그먼트 파일을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        return FileSystemResource(segmentPath)
    }

    private fun deleteVideoFiles(video: Video) {
        val videoDir = storageProperties.getVideoPath(video.id!!)
        if (Files.exists(videoDir)) {
            try {
                Files.walk(videoDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach { Files.delete(it) }
                logger.info("Video files deleted: videoId={}", video.id)
            } catch (e: Exception) {
                logger.error("Failed to delete video files: videoId={}", video.id, e)
            }
        }
    }

}