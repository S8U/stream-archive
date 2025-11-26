package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.dto.*
import com.github.s8u.streamarchive.entity.Channel
import com.github.s8u.streamarchive.enums.ContentPrivacy
import com.github.s8u.streamarchive.exception.BusinessException
import com.github.s8u.streamarchive.repository.ChannelPlatformRepository
import com.github.s8u.streamarchive.repository.ChannelRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class ChannelService(
    private val channelRepository: ChannelRepository,
    private val channelPlatformRepository: ChannelPlatformRepository,
    private val channelPlatformService: ChannelPlatformService,
    private val channelProfileService: ChannelProfileService,
    private val recordScheduleService: RecordScheduleService,
    private val authenticationService: AuthenticationService,
    private val videoService: VideoService
) {

    @Transactional(readOnly = true)
    fun searchForAdmin(request: AdminChannelSearchRequest, pageable: Pageable): Page<AdminChannelResponse> {
        return channelRepository.searchForAdmin(request, pageable)
            .map { AdminChannelResponse.from(it) }
    }

    @Transactional(readOnly = true)
    fun searchForPublic(request: ChannelSearchRequest, pageable: Pageable): Page<ChannelResponse> {
        return channelRepository.searchForPublic(request, pageable)
            .map { ChannelResponse.from(it) }
    }

    @Transactional(readOnly = true)
    fun getForAdmin(id: Long): AdminChannelResponse {
        val channel = channelRepository.findById(id).orElseThrow {
            BusinessException("채널을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }
        return AdminChannelResponse.from(channel)
    }

    @Transactional(readOnly = true)
    fun getByUuidForPublic(uuid: String): ChannelResponse {
        val channel = channelRepository.findByUuid(uuid) ?: throw BusinessException(
            "채널을 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND
        )

        if (channel.contentPrivacy == ContentPrivacy.PRIVATE && !authenticationService.isAdmin()) {
            throw BusinessException("채널을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        return ChannelResponse.from(channel)
    }

    @Transactional
    fun createForAdmin(request: AdminChannelCreateRequest): AdminChannelResponse {
        val channel = Channel(
            uuid = UUID.randomUUID().toString(),
            name = request.name,
            contentPrivacy = request.contentPrivacy
        )
        val saved = channelRepository.save(channel)
        return AdminChannelResponse.from(saved)
    }

    @Transactional
    fun updateForAdmin(id: Long, request: AdminChannelUpdateRequest): AdminChannelResponse {
        val channel = channelRepository.findById(id).orElseThrow {
            BusinessException("채널을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        request.name?.let { channel.name = it }
        request.contentPrivacy?.let { channel.contentPrivacy = it }

        return AdminChannelResponse.from(channel)
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
}
