package com.github.s8u.streamarchive.platform.impl

import com.github.s8u.streamarchive.client.twitch.TwitchApiClient
import com.github.s8u.streamarchive.client.twitch.TwitchStreamsRequestDto
import com.github.s8u.streamarchive.client.twitch.TwitchUsersRequestDto
import com.github.s8u.streamarchive.enums.PlatformType
import com.github.s8u.streamarchive.platform.PlatformChannelDto
import com.github.s8u.streamarchive.platform.PlatformStrategy
import com.github.s8u.streamarchive.platform.PlatformStreamDto
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.ZoneId

@Component
class TwitchStrategy(
    private val apiClient: TwitchApiClient
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
            category = responseFirst.tags.joinToString { ", " },
            viewerCount = responseFirst.viewerCount,
            thumbnailUrl = responseFirst.thumbnailUrl,
            startedAt = Instant.parse(responseFirst.startedAt).atZone(ZoneId.systemDefault()).toLocalDateTime()
        )
    }

}