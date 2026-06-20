package com.github.s8u.streamarchive.platform.platforms.twitch.strategy

import com.github.s8u.streamarchive.platform.platforms.twitch.client.TwitchApiClient
import com.github.s8u.streamarchive.platform.platforms.twitch.client.TwitchStreamsRequestDto
import com.github.s8u.streamarchive.platform.platforms.twitch.client.TwitchUsersRequestDto
import com.github.s8u.streamarchive.platform.enums.PlatformType
import com.github.s8u.streamarchive.platform.strategy.dto.PlatformChannelDto
import com.github.s8u.streamarchive.platform.strategy.PlatformStrategy
import com.github.s8u.streamarchive.platform.strategy.dto.PlatformStreamDto
import com.github.s8u.streamarchive.platform.platforms.twitch.properties.TwitchProperties
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.ZoneId

@Component
class TwitchStrategy(
    private val apiClient: TwitchApiClient,
    private val twitchProperties: TwitchProperties
) : PlatformStrategy {

    override val platformType: PlatformType
        get() = PlatformType.TWITCH

    override fun getStreamUrl(username: String): String {
        return "https://www.twitch.tv/$username"
    }

    override fun getChannel(username: String): PlatformChannelDto? {
        val response = apiClient.getUsers(TwitchUsersRequestDto(login = listOf(username)))
            ?: return null

        if (response.data.isEmpty()) {
            return null
        }

        val responseFirst = response.data.first()

        return PlatformChannelDto(
            platformDto = responseFirst,
            platformType = platformType,
            id = responseFirst.id,
            username = responseFirst.login,
            name = responseFirst.displayName,
            thumbnailUrl = responseFirst.profileImageUrl
        )
    }

    override fun getStream(username: String): PlatformStreamDto? {
        val response = apiClient.getStreams(TwitchStreamsRequestDto(userLogin = username))
            ?: return null

        if (response.data.isEmpty()) {
            return null
        }

        val responseFirst = response.data.first()

        return PlatformStreamDto(
            platformDto = responseFirst,
            platformType = platformType,
            id = responseFirst.id,
            username = responseFirst.userLogin,
            title = responseFirst.title,
            category = responseFirst.tags?.joinToString(", "),
            viewerCount = responseFirst.viewerCount,
            thumbnailUrl = responseFirst.thumbnailUrl?.replace("{width}", "1280")?.replace("{height}", "720"),
            startedAt = Instant.parse(responseFirst.startedAt).atZone(ZoneId.systemDefault()).toLocalDateTime()
        )
    }

    override fun getStreamlinkArgs(): List<String> {
        val args = mutableListOf<String>()

        // 트위치 OAuth 토큰이 있으면 API 헤더 추가
        if (!twitchProperties.personalOauthToken.isNullOrBlank()) {
            args.add("--twitch-api-header=Authorization=OAuth ${twitchProperties.personalOauthToken}")
        }

        // 저지연 모드
        args.add("--twitch-low-latency")

        return args
    }

}
