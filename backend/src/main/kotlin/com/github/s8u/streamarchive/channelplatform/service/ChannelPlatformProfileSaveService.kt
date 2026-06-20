package com.github.s8u.streamarchive.channelplatform.service

import com.github.s8u.streamarchive.channelplatform.entity.ChannelPlatform
import com.github.s8u.streamarchive.global.properties.StorageProperties
import com.github.s8u.streamarchive.global.util.ImageDownloadUtils
import com.github.s8u.streamarchive.platform.strategy.PlatformStrategyFactory
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * 채널 프로필 이미지를 플랫폼 API에서 받아 저장한다.
 *
 * 단건·전체 동기화 UseCase가 공유한다.
 */
@Service
class ChannelPlatformProfileSaveService(
    private val platformStrategyFactory: PlatformStrategyFactory,
    private val storageProperties: StorageProperties
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun save(channelPlatform: ChannelPlatform) {
        val channelId = channelPlatform.channel?.id

        try {
            val strategy = platformStrategyFactory.getPlatformStrategy(channelPlatform.platformType)
            val channel = strategy.getChannel(channelPlatform.platformChannelId)

            if (channel == null) {
                logger.warn(
                    "ChannelPlatformProfileSaveService: Channel not found: platformType={}, platformChannelId={}",
                    channelPlatform.platformType,
                    channelPlatform.platformChannelId
                )
                return
            }

            val thumbnailUrl = channel.thumbnailUrl
            if (thumbnailUrl == null) {
                logger.warn(
                    "ChannelPlatformProfileSaveService: Thumbnail URL is null: platformType={}, platformChannelId={}",
                    channelPlatform.platformType,
                    channelPlatform.platformChannelId
                )
                return
            }

            val inputStream = ImageDownloadUtils.downloadImage(thumbnailUrl) ?: return

            val filePath = storageProperties.getChannelProfilePath(channelId!!)
            Files.createDirectories(filePath.parent)

            inputStream.use {
                Files.copy(it, filePath, StandardCopyOption.REPLACE_EXISTING)
            }

            logger.debug("ChannelPlatformProfileSaveService: Profile saved: channelId={}, platformType={}", channelId, channelPlatform.platformType)
        } catch (e: Exception) {
            logger.error("ChannelPlatformProfileSaveService: Failed to save profile: channelId={}, platformType={}", channelId, channelPlatform.platformType, e)
        }
    }

}
