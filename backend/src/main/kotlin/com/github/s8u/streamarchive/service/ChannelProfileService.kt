package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.entity.ChannelPlatform
import com.github.s8u.streamarchive.enums.PlatformType
import com.github.s8u.streamarchive.exception.BusinessException
import com.github.s8u.streamarchive.platform.PlatformStrategyFactory
import com.github.s8u.streamarchive.properties.StorageProperties
import com.github.s8u.streamarchive.repository.ChannelPlatformRepository
import com.github.s8u.streamarchive.util.ImageDownloadUtil
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.StandardCopyOption

@Service
class ChannelProfileService(
    private val channelPlatformRepository: ChannelPlatformRepository,
    private val platformStrategyFactory: PlatformStrategyFactory,
    private val storageProperties: StorageProperties
) {
    private val logger = LoggerFactory.getLogger(ChannelProfileService::class.java)

    fun syncProfile(channelId: Long, platformType: PlatformType) {
        val channelPlatform = channelPlatformRepository.findByChannelIdAndPlatformType(channelId, platformType)
            ?: throw BusinessException("채널 플랫폼을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)

        if (channelPlatform.isSyncProfile) {
            saveProfile(channelPlatform)
        }
    }

    fun syncAllProfiles() {
        val platforms = channelPlatformRepository.findByIsSyncProfile(true)
        platforms.forEach { saveProfile(it) }
    }

    fun deleteProfile(channelId: Long) {
        val filePath = storageProperties.getChannelProfilePath(channelId)
        if (Files.exists(filePath)) {
            Files.delete(filePath)
        }
    }

    private fun saveProfile(channelPlatform: ChannelPlatform) {
        try {
            val strategy = platformStrategyFactory.getPlatformStrategy(channelPlatform.platformType)
            val channel = strategy.getChannel(channelPlatform.platformChannelId)

            if (channel == null) {
                logger.warn("Channel not found: platformType={}, platformChannelId={}", channelPlatform.platformType, channelPlatform.platformChannelId)
                return
            }

            val thumbnailUrl = channel.thumbnailUrl
            if (thumbnailUrl == null) {
                logger.warn("Thumbnail URL is null: platformType={}, platformChannelId={}", channelPlatform.platformType, channelPlatform.platformChannelId)
                return
            }

            val inputStream = ImageDownloadUtil.downloadImage(thumbnailUrl) ?: return

            val filePath = storageProperties.getChannelProfilePath(channelPlatform?.channel?.id!!)
            Files.createDirectories(filePath.parent)

            inputStream.use {
                Files.copy(it, filePath, StandardCopyOption.REPLACE_EXISTING)
            }

            logger.debug("Profile synced: channelId={}, platformType={}", channelPlatform?.channel?.id, channelPlatform.platformType)
        } catch (e: Exception) {
            logger.error("Failed to sync profile: channelId={}, platformType={}", channelPlatform?.channel?.id, channelPlatform.platformType, e)
        }
    }
}