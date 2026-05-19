package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.dto.AdminVideoResponse
import com.github.s8u.streamarchive.dto.AdminVideoSearchRequest
import com.github.s8u.streamarchive.dto.AdminVideoUpdateRequest
import com.github.s8u.streamarchive.dto.PublicVideoResponse
import com.github.s8u.streamarchive.dto.PublicVideoSearchRequest
import com.github.s8u.streamarchive.entity.Video
import com.github.s8u.streamarchive.entity.VideoArchiveLog
import com.github.s8u.streamarchive.exception.BusinessException
import com.github.s8u.streamarchive.properties.StorageProperties
import com.github.s8u.streamarchive.repository.ChannelRepository
import com.github.s8u.streamarchive.repository.VideoArchiveLogRepository
import com.github.s8u.streamarchive.repository.VideoMetadataViewerHistoryRepository
import com.github.s8u.streamarchive.repository.VideoRepository
import com.github.s8u.streamarchive.util.RequestUtils
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
    private val channelRepository: ChannelRepository,
    private val viewerHistoryRepository: VideoMetadataViewerHistoryRepository,
    private val videoArchiveLogRepository: VideoArchiveLogRepository,
    private val contentPrivacyService: ContentPrivacyService,
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
        request.description?.let { video.description = it }
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
    fun setArchivedForAdmin(id: Long, isArchived: Boolean): AdminVideoResponse {
        val video = videoRepository.findById(id).orElseThrow {
            BusinessException("동영상을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        val userId = authenticationService.getCurrentUserId()
        val clientIp = RequestUtils.getClientIp()

        if (isArchived) {
            video.isArchived = true
            video.archivedAt = LocalDateTime.now()
            video.archivedBy = userId
            video.archivedIp = clientIp
        } else {
            video.isArchived = false
            video.archivedAt = null
            video.archivedBy = null
            video.archivedIp = null
        }

        videoArchiveLogRepository.save(
            VideoArchiveLog(
                videoId = video.id!!,
                isArchived = isArchived,
                actionBy = userId,
                actionIp = clientIp
            )
        )

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

        if (video.isArchived) {
            throw BusinessException("소장된 동영상은 삭제할 수 없습니다. 소장 해제 후 삭제해주세요.", HttpStatus.CONFLICT)
        }

        deleteVideoFiles(video)

        video.isActive = false
        video.deletedAt = LocalDateTime.now()
    }

    @Transactional
    fun deleteAllByChannelId(channelId: Long) {
        if (videoRepository.existsByChannelIdAndIsArchivedTrue(channelId)) {
            throw BusinessException("소장된 동영상이 있는 채널은 삭제할 수 없습니다. 소장 해제 후 삭제해주세요.", HttpStatus.CONFLICT)
        }

        val videos = videoRepository.findByChannelId(channelId)

        videos.forEach { video ->
            deleteVideoFiles(video)

            video.isActive = false
            video.deletedAt = LocalDateTime.now()
        }
    }

    @Transactional(readOnly = true)
    fun searchForPublic(request: PublicVideoSearchRequest, pageable: Pageable): Page<PublicVideoResponse> {
        request.channelUuid?.let { channelUuid ->
            val channel = channelRepository.findByUuid(channelUuid) ?: throw BusinessException(
                "채널을 찾을 수 없습니다.",
                HttpStatus.NOT_FOUND
            )
            contentPrivacyService.assertCanAccessChannel(channel)
        }

        return videoRepository.searchForPublic(request, pageable)
            .map { video ->
                if (video.channel == null) {
                    throw BusinessException("채널을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
                }

                val peakViewerCount = viewerHistoryRepository
                    .findTopByVideoIdOrderByViewerCountDescOffsetMillisAsc(video.id!!)
                    ?.viewerCount

                PublicVideoResponse.from(
                    video = video,
                    channelProfileUrl = urlBuilder.channelProfileUrl(video.channel!!.uuid),
                    thumbnailUrl = urlBuilder.videoThumbnailUrl(video.uuid),
                    playlistUrl = urlBuilder.videoPlaylistUrl(video.uuid),
                    peakViewerCount = peakViewerCount
                )
            }
    }

    @Transactional(readOnly = true)
    fun getByUuidForPublic(uuid: String): PublicVideoResponse {
        val video = videoRepository.findByUuid(uuid) ?: throw BusinessException(
            "동영상을 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND
        )

        contentPrivacyService.assertCanAccessVideo(video)

        val peakViewerCount = viewerHistoryRepository
            .findTopByVideoIdOrderByViewerCountDescOffsetMillisAsc(video.id!!)
            ?.viewerCount

        return PublicVideoResponse.from(
            video = video,
            channelProfileUrl = urlBuilder.channelProfileUrl(video.channel!!.uuid),
            thumbnailUrl = urlBuilder.videoThumbnailUrl(video.uuid),
            playlistUrl = urlBuilder.videoPlaylistUrl(video.uuid),
            peakViewerCount = peakViewerCount
        )
    }

    @Transactional(readOnly = true)
    fun getThumbnailByUuid(uuid: String): Resource {
        val video = videoRepository.findByUuid(uuid) ?: throw BusinessException(
            "동영상을 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND
        )

        contentPrivacyService.assertCanAccessVideo(video)

        val thumbnailPath = storageProperties.getVideoThumbnailPath(video.id!!)
        if (!Files.exists(thumbnailPath)) {
            throw BusinessException("썸네일을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        return FileSystemResource(thumbnailPath)
    }

    @Transactional(readOnly = true)
    fun getPlaylistByUuid(uuid: String): Resource {
        val video = videoRepository.findByUuid(uuid) ?: throw BusinessException(
            "동영상을 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND)

        contentPrivacyService.assertCanAccessVideo(video)

        val playlistPath = storageProperties.getVideoPlaylistPath(video.id!!)
        if (!Files.exists(playlistPath)) {
            throw BusinessException("플레이리스트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        return FileSystemResource(playlistPath)
    }

    @Transactional(readOnly = true)
    fun getSegmentByUuid(uuid: String, filename: String): Resource {
        // 파일명 검증 (보안)
        if (!filename.matches(Regex("^segment_\\d+\\.ts$"))) {
            throw BusinessException("잘못된 파일명입니다.", HttpStatus.BAD_REQUEST)
        }

        val video = videoRepository.findByUuid(uuid) ?: throw BusinessException(
            "동영상을 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND
        )

        contentPrivacyService.assertCanAccessVideo(video)

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
