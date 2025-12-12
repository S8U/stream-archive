package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.dto.*
import com.github.s8u.streamarchive.entity.Channel
import com.github.s8u.streamarchive.enums.ContentPrivacy
import com.github.s8u.streamarchive.exception.BusinessException
import com.github.s8u.streamarchive.properties.StorageProperties
import com.github.s8u.streamarchive.repository.ChannelPlatformRepository
import com.github.s8u.streamarchive.repository.ChannelRepository
import com.github.s8u.streamarchive.repository.VideoRepository
import com.github.s8u.streamarchive.util.UrlBuilder
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.file.Files
import java.time.LocalDateTime
import java.util.*

@Service
class ChannelService(
    private val channelRepository: ChannelRepository,
    private val channelPlatformRepository: ChannelPlatformRepository,
    private val videoRepository: VideoRepository,
    private val channelPlatformService: ChannelPlatformService,
    private val channelProfileService: ChannelProfileService,
    private val recordScheduleService: RecordScheduleService,
    private val authenticationService: AuthenticationService,
    private val videoService: VideoService,
    private val storageProperties: StorageProperties,
    private val urlBuilder: UrlBuilder
) {

    @Transactional(readOnly = true)
    fun searchForAdmin(request: AdminChannelSearchRequest, pageable: Pageable): Page<AdminChannelResponse> {
        return channelRepository.searchForAdmin(request, pageable)
            .map { channel ->
                AdminChannelResponse.from(
                    channel = channel,
                    profileUrl = urlBuilder.channelProfileUrl(channel.uuid)
                )
            }
    }

    @Transactional(readOnly = true)
    fun searchForPublic(request: PublicChannelSearchRequest, pageable: Pageable): Page<PublicChannelResponse> {
        return channelRepository.searchForPublic(request, pageable)
            .map { channel ->
                PublicChannelResponse.from(
                    channel = channel,
                    profileUrl = urlBuilder.channelProfileUrl(channel.uuid)
                )
            }
    }

    @Transactional(readOnly = true)
    fun getForAdmin(id: Long): AdminChannelResponse {
        val channel = channelRepository.findById(id).orElseThrow {
            BusinessException("채널을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }
        return AdminChannelResponse.from(
            channel = channel,
            profileUrl = urlBuilder.channelProfileUrl(channel.uuid)
        )
    }

    @Transactional(readOnly = true)
    fun getByUuidForPublic(uuid: String): PublicChannelResponse {
        val channel = channelRepository.findByUuid(uuid) ?: throw BusinessException(
            "채널을 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND
        )

        if (channel.contentPrivacy == ContentPrivacy.PRIVATE && !authenticationService.isAdmin()) {
            throw BusinessException("채널을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        return PublicChannelResponse.from(
            channel = channel,
            profileUrl = urlBuilder.channelProfileUrl(channel.uuid)
        )
    }

    @Transactional
    fun createForAdmin(request: AdminChannelCreateRequest): AdminChannelResponse {
        val channel = Channel(
            uuid = UUID.randomUUID().toString(),
            name = request.name,
            contentPrivacy = request.contentPrivacy
        )
        val saved = channelRepository.save(channel)
        return AdminChannelResponse.from(
            channel = saved,
            profileUrl = urlBuilder.channelProfileUrl(saved.uuid)
        )
    }

    @Transactional
    fun updateForAdmin(id: Long, request: AdminChannelUpdateRequest): AdminChannelResponse {
        val channel = channelRepository.findById(id).orElseThrow {
            BusinessException("채널을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        request.name?.let { channel.name = it }
        request.contentPrivacy?.let { channel.contentPrivacy = it }

        return AdminChannelResponse.from(
            channel = channel,
            profileUrl = urlBuilder.channelProfileUrl(channel.uuid)
        )
    }

    @Transactional
    fun delete(id: Long) {
        val channel = channelRepository.findById(id).orElseThrow {
            BusinessException("채널을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        // 모든 녹화 스케줄 삭제
        recordScheduleService.deleteAllByChannelId(id)

        // 모든 채널 플랫폼 삭제
        channelPlatformService.deleteAllByChannelId(id)

        // 프로필 파일 삭제
        channelProfileService.deleteProfile(id)

        // 모든 동영상 삭제
        videoService.deleteAllByChannelId(id)

        // 채널 삭제
        channel.isActive = false
        channel.deletedAt = LocalDateTime.now()
    }

    fun getProfileImageByUuid(uuid: String): Resource {
        val channel = channelRepository.findByUuid(uuid) ?: throw BusinessException(
            "채널을 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND
        )

        val profilePath = storageProperties.getChannelProfilePath(channel.id!!)

        if (!Files.exists(profilePath)) {
            throw BusinessException("프로필 이미지를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        return FileSystemResource(profilePath)
    }

    @Transactional(readOnly = true)
    fun getStatsByUuid(uuid: String): ChannelStatsResponse {
        val channel = channelRepository.findByUuid(uuid) ?: throw BusinessException(
            "채널을 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND
        )

        if (channel.contentPrivacy == ContentPrivacy.PRIVATE && !authenticationService.isAdmin()) {
            throw BusinessException("채널을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        val videoCount = videoRepository.countByChannelId(channel.id!!)
        val totalFileSize = videoRepository.sumFileSizeByChannelId(channel.id!!)

        return ChannelStatsResponse(
            videoCount = videoCount,
            totalFileSize = totalFileSize
        )
    }
}
