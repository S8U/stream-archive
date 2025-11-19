package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.entity.ChannelPlatform
import com.github.s8u.streamarchive.enums.PlatformType
import com.github.s8u.streamarchive.exception.BusinessException
import com.github.s8u.streamarchive.platform.PlatformStrategyFactory
import com.github.s8u.streamarchive.properties.StorageProperties
import com.github.s8u.streamarchive.repository.ChannelPlatformRepository
import com.github.s8u.streamarchive.repository.ChannelRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption

@Service
class ChannelProfileService(
    private val channelRepository: ChannelRepository,
    private val channelPlatformRepository: ChannelPlatformRepository,
    private val platformStrategyFactory: PlatformStrategyFactory,
    private val storageProperties: StorageProperties
) {
    private val logger = LoggerFactory.getLogger(ChannelProfileService::class.java)
    private val restClient = RestClient.create()

    fun syncProfile(channelId: Long) {
        val channel = channelRepository.findById(channelId).orElseThrow {
            BusinessException("채널을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        val platforms = channelPlatformRepository.findByIsSyncProfileAndIsActive(true, true)
            .filter { it.channelId == channelId }

        platforms.forEach { downloadAndSave(it) }
    }

    fun syncProfile(channelId: Long, platformType: PlatformType) {
        val channelPlatform = channelPlatformRepository.findByChannelIdAndPlatformTypeAndIsActive(channelId, platformType, true)
            ?: throw BusinessException("채널 플랫폼을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)

        if (channelPlatform.isSyncProfile) {
            downloadAndSave(channelPlatform)
        }
    }

    fun syncAllProfiles() {
        val platforms = channelPlatformRepository.findByIsSyncProfileAndIsActive(true, true)
        platforms.forEach { downloadAndSave(it) }
    }

    fun deleteProfile(channelId: Long) {
        val channelDir = storageProperties.getChannelPath(channelId)
        if (Files.exists(channelDir)) {
            Files.walk(channelDir)
                .sorted(Comparator.reverseOrder())
                .forEach { Files.delete(it) }
        }
    }

    fun deleteProfile(channelId: Long, platformType: PlatformType) {
        val fileName = "profile-${platformType.name.lowercase()}.jpg"
        val filePath = storageProperties.getChannelPath(channelId).resolve(fileName)

        if (Files.exists(filePath)) {
            Files.delete(filePath)
        }
    }

    private fun downloadAndSave(channelPlatform: ChannelPlatform) {
        try {
            val strategy = platformStrategyFactory.getPlatformStrategy(channelPlatform.platformType)
            val channel = strategy.getChannel(channelPlatform.platformChannelId)

            if (channel == null) {
                logger.warn("Channel not found: ${channelPlatform.platformType} - ${channelPlatform.platformChannelId}")
                return
            }

            val thumbnailUrl = channel.thumbnailUrl
            if (thumbnailUrl == null) {
                logger.warn("Thumbnail URL is null: ${channelPlatform.platformType} - ${channelPlatform.platformChannelId}")
                return
            }

            val inputStream = downloadImage(thumbnailUrl) ?: return

            val channelDir = storageProperties.getChannelPath(channelPlatform.channelId)
            Files.createDirectories(channelDir)

            val fileName = "profile-${channelPlatform.platformType.name.lowercase()}.jpg"
            val filePath = channelDir.resolve(fileName)

            inputStream.use {
                Files.copy(it, filePath, StandardCopyOption.REPLACE_EXISTING)
            }

            logger.info("Profile synced: ${channelPlatform.channelId} - ${channelPlatform.platformType}")
        } catch (e: Exception) {
            logger.error("Failed to sync profile: ${channelPlatform.channelId} - ${channelPlatform.platformType}", e)
        }
    }

    private fun downloadImage(url: String): InputStream? {
        return try {
            val bytes = restClient.get()
                .uri(url)
                .retrieve()
                .body(ByteArray::class.java)

            bytes?.inputStream()
        } catch (e: Exception) {
            logger.error("Failed to download image from $url", e)
            null
        }
    }
}