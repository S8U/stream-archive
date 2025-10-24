package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.dto.ChannelCreateRequest
import com.github.s8u.streamarchive.dto.ChannelResponse
import com.github.s8u.streamarchive.dto.ChannelUpdateRequest
import com.github.s8u.streamarchive.entity.Channel
import com.github.s8u.streamarchive.repository.ChannelPlatformRepository
import com.github.s8u.streamarchive.repository.ChannelRepository
import com.github.s8u.streamarchive.repository.RecordScheduleRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class ChannelService(
    private val channelRepository: ChannelRepository,
    private val channelPlatformRepository: ChannelPlatformRepository,
    private val channelProfileService: ChannelProfileService,
    private val recordScheduleRepository: RecordScheduleRepository
) {
    @Transactional(readOnly = true)
    fun getAll(): List<ChannelResponse> {
        return channelRepository.findAll()
            .map { ChannelResponse.from(it) }
    }

    @Transactional(readOnly = true)
    fun getById(id: Long): ChannelResponse {
        val channel = channelRepository.findByIdOrNull(id)
            ?: throw NoSuchElementException("Channel not found: $id")
        return ChannelResponse.from(channel)
    }

    @Transactional
    fun create(request: ChannelCreateRequest): ChannelResponse {
        val channel = Channel(
            uuid = UUID.randomUUID().toString(),
            name = request.name,
            contentPrivacy = request.contentPrivacy
        )
        val saved = channelRepository.save(channel)
        return ChannelResponse.from(saved)
    }

    @Transactional
    fun update(id: Long, request: ChannelUpdateRequest): ChannelResponse {
        val channel = channelRepository.findByIdOrNull(id)
            ?: throw NoSuchElementException("Channel not found: $id")

        request.name?.let { channel.name = it }
        request.contentPrivacy?.let { channel.contentPrivacy = it }
        request.isActive?.let { channel.isActive = it }

        return ChannelResponse.from(channel)
    }

    @Transactional
    fun delete(id: Long) {
        val channel = channelRepository.findByIdOrNull(id)
            ?: throw NoSuchElementException("Channel not found: $id")

        // 연결된 모든 녹화 스케줄 삭제
        val recordSchedules = recordScheduleRepository.findAll()
            .filter { it.channelId == id }

        recordSchedules.forEach { schedule ->
            schedule.deletedAt = LocalDateTime.now()
            schedule.isActive = false
        }

        // 연결된 모든 채널 플랫폼 삭제
        val channelPlatforms = channelPlatformRepository.findAll()
            .filter { it.channelId == id }

        channelPlatforms.forEach { platform ->
            platform.deletedAt = LocalDateTime.now()
            platform.isActive = false
        }

        // 프로필 파일 삭제
        channelProfileService.deleteProfile(id)

        // 채널 삭제
        channel.deletedAt = LocalDateTime.now()
        channel.isActive = false
    }
}
