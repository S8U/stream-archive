package com.github.s8u.streamarchive.platform.platforms.chzzk.strategy

import com.github.s8u.streamarchive.platform.platforms.chzzk.client.ChzzkApiClient
import com.github.s8u.streamarchive.platform.enums.PlatformType
import com.github.s8u.streamarchive.platform.strategy.dto.PlatformChannelDto
import com.github.s8u.streamarchive.platform.strategy.PlatformStrategy
import com.github.s8u.streamarchive.platform.strategy.dto.PlatformStreamDto
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class ChzzkStrategy(
    private val apiClient: ChzzkApiClient
) : PlatformStrategy {

    override val platformType: PlatformType
        get() = PlatformType.CHZZK

    override fun getStreamUrl(username: String): String {
        return "https://chzzk.naver.com/live/$username"
    }

    override fun parseChannelId(url: String): String? {
        val match = CHANNEL_ID_REGEX.find(url) ?: return null
        return match.groupValues[1]
    }

    override fun getChannel(username: String): PlatformChannelDto? {
        val response = apiClient.getChannel(username) ?: return null

        if (response.code != 200 || response.content == null) {
            return null
        }

        val content = response.content

        return PlatformChannelDto(
            platformDto = content,
            platformType = platformType,
            id = content.channelId,
            username = content.channelId,
            name = content.channelName,
            thumbnailUrl = content.channelImageUrl
        )
    }

    override fun getStream(username: String): PlatformStreamDto? {
        val response = apiClient.getLiveDetail(username) ?: return null

        if (response.code != 200 || response.content == null) {
            return null
        }

        val content = response.content

        // status가 "CLOSE"이거나 liveId가 없으면 방송 중이 아님
        if (content.status != "OPEN" || content.liveId == null) {
            return null
        }

        return PlatformStreamDto(
            platformDto = content,
            platformType = platformType,
            id = content.liveId.toString(),
            username = username,
            title = content.liveTitle,
            category = content.liveCategoryValue,
            viewerCount = content.concurrentUserCount,
            thumbnailUrl = content.liveImageUrl?.replace("{type}", "720"),
            startedAt = content.openDate?.let {
                LocalDateTime.parse(it, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            } ?: LocalDateTime.now()
        )
    }

    companion object {
        // chzzk.naver.com/live/{id} 또는 chzzk.naver.com/{id} 에서 채널 ID(32자리 hex)를 뽑는다
        private val CHANNEL_ID_REGEX = Regex("chzzk\\.naver\\.com/(?:live/)?([0-9a-f]{32})")
    }

}
