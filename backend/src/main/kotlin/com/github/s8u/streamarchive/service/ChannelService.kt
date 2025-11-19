package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.dto.AdminChannelCreateRequest
import com.github.s8u.streamarchive.dto.AdminChannelResponse
import com.github.s8u.streamarchive.dto.AdminChannelUpdateRequest
import com.github.s8u.streamarchive.entity.Channel
import com.github.s8u.streamarchive.exception.BusinessException
import com.github.s8u.streamarchive.repository.ChannelPlatformRepository
import com.github.s8u.streamarchive.repository.ChannelRepository
import com.github.s8u.streamarchive.repository.RecordScheduleRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class ChannelService(
    private val channelRepository: ChannelRepository,
    private val channelPlatformRepository: ChannelPlatformRepository,
    private val channelProfileService: ChannelProfileService,
    private val recordScheduleRepository: RecordScheduleRepository
) {
    @Transactional(readOnly = true)
    fun getAll(): List<AdminChannelResponse> {
        return channelRepository.findAll()
            .map { AdminChannelResponse.from(it) }
    }

    @Transactional(readOnly = true)
    fun getById(id: Long): AdminChannelResponse {
        val channel = channelRepository.findById(id).orElseThrow {
            BusinessException("채널을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }
        return AdminChannelResponse.from(channel)
    }

    @Transactional
    fun create(request: AdminChannelCreateRequest): AdminChannelResponse {
        val channel = Channel(
            uuid = UUID.randomUUID().toString(),
            name = request.name,
            contentPrivacy = request.contentPrivacy
        )
        val saved = channelRepository.save(channel)
        return AdminChannelResponse.from(saved)
    }

    @Transactional
    fun update(id: Long, request: AdminChannelUpdateRequest): AdminChannelResponse {
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

        // 연결된 모든 녹화 스케줄 삭제
        recordScheduleRepository.findByChannelIdAndIsActive(id, true).forEach { schedule ->
            schedule.isActive = false
            schedule.deletedAt = java.time.LocalDateTime.now()
        }

        // 연결된 모든 채널 플랫폼 삭제
        channelPlatformRepository.findByChannelIdAndIsActive(id, true).forEach { platform ->
            platform.isActive = false
            platform.deletedAt = java.time.LocalDateTime.now()
        }

        // 프로필 파일 삭제
        channelProfileService.deleteProfile(id)

        // 채널 삭제
        channel.isActive = false
        channel.deletedAt = java.time.LocalDateTime.now()
    }
}
