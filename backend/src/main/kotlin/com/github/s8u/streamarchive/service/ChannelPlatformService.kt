package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.dto.AdminChannelPlatformCreateRequest
import com.github.s8u.streamarchive.dto.AdminChannelPlatformResponse
import com.github.s8u.streamarchive.dto.AdminChannelPlatformSearchRequest
import com.github.s8u.streamarchive.dto.AdminChannelPlatformUpdateRequest
import com.github.s8u.streamarchive.entity.ChannelPlatform
import com.github.s8u.streamarchive.exception.BusinessException
import com.github.s8u.streamarchive.repository.ChannelPlatformRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ChannelPlatformService(
    private val channelPlatformRepository: ChannelPlatformRepository,
    private val channelProfileService: ChannelProfileService
) {
    @Transactional(readOnly = true)
    fun searchForAdmin(request: AdminChannelPlatformSearchRequest, pageable: Pageable): Page<AdminChannelPlatformResponse> {
        return channelPlatformRepository.searchForAdmin(request, pageable)
            .map { AdminChannelPlatformResponse.from(it) }
    }

    @Transactional(readOnly = true)
    fun getForAdmin(id: Long): AdminChannelPlatformResponse {
        val channelPlatform = channelPlatformRepository.findById(id).orElseThrow {
            BusinessException("채널 플랫폼을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }
        return AdminChannelPlatformResponse.from(channelPlatform)
    }

    @Transactional
    fun createForAdmin(request: AdminChannelPlatformCreateRequest): AdminChannelPlatformResponse {
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

        return AdminChannelPlatformResponse.from(saved)
    }

    @Transactional
    fun updateForAdmin(id: Long, request: AdminChannelPlatformUpdateRequest): AdminChannelPlatformResponse {
        val channelPlatform = channelPlatformRepository.findById(id).orElseThrow {
            BusinessException("채널 플랫폼을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        request.isSyncProfile?.let { channelPlatform.isSyncProfile = it }

        return AdminChannelPlatformResponse.from(channelPlatform)
    }

    @Transactional
    fun delete(id: Long) {
        val channelPlatform = channelPlatformRepository.findById(id).orElseThrow {
            BusinessException("채널 플랫폼을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        channelProfileService.deleteProfile(channelPlatform.channelId)

        channelPlatform.isActive = false
        channelPlatform.deletedAt = LocalDateTime.now()
    }

    @Transactional
    fun deleteAllByChannelId(channelId: Long) {
        val channelPlatforms = channelPlatformRepository.findByChannelId(channelId)

        channelPlatforms.forEach { channelPlatform ->
            channelPlatform.isActive = false
            channelPlatform.deletedAt = LocalDateTime.now()
        }

        if (channelPlatforms.isNotEmpty()) {
            channelProfileService.deleteProfile(channelId)
        }
    }

}