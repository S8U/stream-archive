package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.dto.ChannelPlatformCreateRequest
import com.github.s8u.streamarchive.dto.ChannelPlatformResponse
import com.github.s8u.streamarchive.dto.ChannelPlatformUpdateRequest
import com.github.s8u.streamarchive.entity.ChannelPlatform
import com.github.s8u.streamarchive.repository.ChannelPlatformRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class ChannelPlatformService(
    private val channelPlatformRepository: ChannelPlatformRepository,
    private val channelProfileService: ChannelProfileService
) {
    fun getAll(): List<ChannelPlatformResponse> {
        return channelPlatformRepository.findAll()
            .map { ChannelPlatformResponse.from(it) }
    }

    fun getById(id: Long): ChannelPlatformResponse {
        val channelPlatform = channelPlatformRepository.findByIdOrNull(id)
            ?: throw NoSuchElementException("ChannelPlatform not found: $id")
        return ChannelPlatformResponse.from(channelPlatform)
    }

    @Transactional
    fun create(request: ChannelPlatformCreateRequest): ChannelPlatformResponse {
        val channelPlatform = ChannelPlatform(
            channelId = request.channelId,
            platformType = request.platformType,
            platformChannelId = request.platformChannelId,
            isSyncProfile = request.isSyncProfile
        )
        val saved = channelPlatformRepository.save(channelPlatform)

        if (saved.isSyncProfile) {
            channelProfileService.syncProfile(saved.channelId, saved.platformType)
        }

        return ChannelPlatformResponse.from(saved)
    }

    @Transactional
    fun update(id: Long, request: ChannelPlatformUpdateRequest): ChannelPlatformResponse {
        val channelPlatform = channelPlatformRepository.findByIdOrNull(id)
            ?: throw NoSuchElementException("ChannelPlatform not found: $id")

        request.isSyncProfile?.let { channelPlatform.isSyncProfile = it }
        request.isActive?.let { channelPlatform.isActive = it }

        return ChannelPlatformResponse.from(channelPlatform)
    }

    @Transactional
    fun delete(id: Long) {
        val channelPlatform = channelPlatformRepository.findByIdOrNull(id)
            ?: throw NoSuchElementException("ChannelPlatform not found: $id")

        channelProfileService.deleteProfile(channelPlatform.channelId, channelPlatform.platformType)

        channelPlatform.deletedAt = LocalDateTime.now()
        channelPlatform.isActive = false
    }
}