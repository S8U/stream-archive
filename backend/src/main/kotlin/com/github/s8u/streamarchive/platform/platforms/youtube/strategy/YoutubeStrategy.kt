package com.github.s8u.streamarchive.platform.platforms.youtube.strategy

import com.github.s8u.streamarchive.platform.enums.PlatformType
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeThumbnail
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeVideo
import com.github.s8u.streamarchive.platform.platforms.youtube.service.YoutubeChannelFindService
import com.github.s8u.streamarchive.platform.platforms.youtube.service.YoutubeLiveFindService
import com.github.s8u.streamarchive.platform.strategy.PlatformStrategy
import com.github.s8u.streamarchive.platform.strategy.dto.PlatformChannelDto
import com.github.s8u.streamarchive.platform.strategy.dto.PlatformStreamDto
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * 유튜브 플랫폼 전략
 */
@Component
class YoutubeStrategy(
    private val channelFindService: YoutubeChannelFindService,
    private val liveFindService: YoutubeLiveFindService
) : PlatformStrategy {

    override val platformType: PlatformType
        get() = PlatformType.YOUTUBE

    override fun getStreamUrl(username: String): String {
        val normalized = channelFindService.normalize(username)

        return if (channelFindService.isChannelId(normalized)) {
            "https://www.youtube.com/channel/$normalized/live"
        } else {
            "https://www.youtube.com/@$normalized/live"
        }
    }

    override fun getChannel(username: String): PlatformChannelDto? {
        val channel = channelFindService.find(username) ?: return null

        return PlatformChannelDto(
            platformDto = channel,
            platformType = platformType,
            id = channel.id,
            username = channel.snippet.customUrl ?: channel.id,
            name = channel.snippet.title,
            thumbnailUrl = channel.snippet.thumbnails.getBestUrl()
        )
    }

    override fun getStream(username: String): PlatformStreamDto? {
        val channel = channelFindService.find(username) ?: return null
        val live = liveFindService.find(channel.id) ?: return null
        val video = live.video

        return PlatformStreamDto(
            platformDto = video,
            platformType = platformType,
            id = video.id,
            username = channel.id,
            title = video.snippet?.title,
            category = video.snippet?.categoryId,
            viewerCount = video.liveStreamingDetails?.concurrentViewers?.toIntOrNull(),
            thumbnailUrl = video.snippet?.thumbnails?.getBestUrl(),
            startedAt = getStartedAt(video)
        )
    }

    private fun getStartedAt(video: YoutubeVideo): LocalDateTime? {
        val startedAt = video.liveStreamingDetails?.actualStartTime
            ?: video.snippet?.publishedAt
            ?: return null

        return Instant.parse(startedAt)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
    }

    private fun Map<String, YoutubeThumbnail>.getBestUrl(): String? {
        return this["maxres"]?.url
            ?: this["standard"]?.url
            ?: this["high"]?.url
            ?: this["medium"]?.url
            ?: this["default"]?.url
    }

}
