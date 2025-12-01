package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.dto.AdminChannelPlatformCreateRequest
import com.github.s8u.streamarchive.dto.AdminChannelPlatformResponse
import com.github.s8u.streamarchive.dto.AdminChannelPlatformSearchRequest
import com.github.s8u.streamarchive.dto.AdminChannelPlatformUpdateRequest
import com.github.s8u.streamarchive.dto.ChannelPlatformResponse
import com.github.s8u.streamarchive.entity.ChannelPlatform
import com.github.s8u.streamarchive.enums.ContentPrivacy
import com.github.s8u.streamarchive.exception.BusinessException
import com.github.s8u.streamarchive.platform.PlatformStrategyFactory
import com.github.s8u.streamarchive.repository.ChannelPlatformRepository
import com.github.s8u.streamarchive.repository.ChannelRepository
import com.github.s8u.streamarchive.util.UrlBuilder
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ChannelPlatformService(
    private val channelPlatformRepository: ChannelPlatformRepository,
    private val channelRepository: ChannelRepository,
    private val channelProfileService: ChannelProfileService,
    private val authenticationService: AuthenticationService,
    private val platformStrategyFactory: PlatformStrategyFactory,
    private val urlBuilder: UrlBuilder
) {
    @Transactional(readOnly = true)
    fun searchForAdmin(request: AdminChannelPlatformSearchRequest, pageable: Pageable): Page<AdminChannelPlatformResponse> {
        return channelPlatformRepository.searchForAdmin(request, pageable)
            .map { channelPlatform ->
                val strategy = platformStrategyFactory.getPlatformStrategy(channelPlatform.platformType)
                val platformUrl = strategy.getStreamUrl(channelPlatform.platformChannelId)
                val channelProfileUrl = urlBuilder.channelProfileUrl(channelPlatform.channel?.uuid!!)

                AdminChannelPlatformResponse.from(channelPlatform, platformUrl, channelProfileUrl)
            }
    }

    @Transactional(readOnly = true)
    fun getForAdmin(id: Long): AdminChannelPlatformResponse {
        val channelPlatform = channelPlatformRepository.findById(id).orElseThrow {
            BusinessException("채널 플랫폼을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        val strategy = platformStrategyFactory.getPlatformStrategy(channelPlatform.platformType)
        val platformUrl = strategy.getStreamUrl(channelPlatform.platformChannelId)
        val channelProfileUrl = urlBuilder.channelProfileUrl(channelPlatform.channel?.uuid!!)

        return AdminChannelPlatformResponse.from(channelPlatform, platformUrl, channelProfileUrl)
    }

    @Transactional(readOnly = true)
    fun getByChannelUuidForPublic(channelUuid: String): List<ChannelPlatformResponse> {
        val channel = channelRepository.findByUuid(channelUuid) ?: throw BusinessException(
            "채널을 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND
        )

        if (channel.contentPrivacy == ContentPrivacy.PRIVATE && !authenticationService.isAdmin()) {
            throw BusinessException("채널을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        return channelPlatformRepository.findByChannelId(channel.id!!)
            .map { channelPlatform ->
                val strategy = platformStrategyFactory.getPlatformStrategy(channelPlatform.platformType)
                val streamUrl = strategy.getStreamUrl(channelPlatform.platformChannelId)
                val channelProfileUrl = urlBuilder.channelProfileUrl(channelPlatform.channel?.uuid!!)

                ChannelPlatformResponse.from(channelPlatform, streamUrl)
            }
    }

    @Transactional
    fun createForAdmin(request: AdminChannelPlatformCreateRequest): AdminChannelPlatformResponse {
        val channel = channelRepository.findById(request.channelId).orElseThrow {
            BusinessException("채널을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        val channelPlatform = ChannelPlatform(
            channel = channel,
            platformType = request.platformType,
            platformChannelId = request.platformChannelId,
            isSyncProfile = request.isSyncProfile
        )
        val saved = channelPlatformRepository.save(channelPlatform)

        if (saved.isSyncProfile) {
            channelProfileService.syncProfile(saved.channel?.id!!, saved.platformType)
        }

        val strategy = platformStrategyFactory.getPlatformStrategy(saved.platformType)
        val platformUrl = strategy.getStreamUrl(saved.platformChannelId)
        val channelProfileUrl = urlBuilder.channelProfileUrl(channelPlatform.channel?.uuid!!)

        return AdminChannelPlatformResponse.from(saved, platformUrl, channelProfileUrl)
    }

    @Transactional
    fun updateForAdmin(id: Long, request: AdminChannelPlatformUpdateRequest): AdminChannelPlatformResponse {
        val channelPlatform = channelPlatformRepository.findById(id).orElseThrow {
            BusinessException("채널 플랫폼을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        request.isSyncProfile?.let { channelPlatform.isSyncProfile = it }
        request.platformChannelId?.let { channelPlatform.platformChannelId = it }

        if (channelPlatform.isSyncProfile) {
            channelProfileService.syncProfile(channelPlatform.channel?.id!!, channelPlatform.platformType)
        }

        val strategy = platformStrategyFactory.getPlatformStrategy(channelPlatform.platformType)
        val platformUrl = strategy.getStreamUrl(channelPlatform.platformChannelId)
        val channelProfileUrl = urlBuilder.channelProfileUrl(channelPlatform.channel?.uuid!!)

        return AdminChannelPlatformResponse.from(channelPlatform, platformUrl, channelProfileUrl)
    }

    @Transactional
    fun delete(id: Long) {
        val channelPlatform = channelPlatformRepository.findById(id).orElseThrow {
            BusinessException("채널 플랫폼을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        channelProfileService.deleteProfile(channelPlatform.channel?.id!!)

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