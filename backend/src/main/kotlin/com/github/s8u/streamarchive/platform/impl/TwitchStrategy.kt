package com.github.s8u.streamarchive.platform.impl

import com.github.s8u.streamarchive.chat.ChatMessageDto
import com.github.s8u.streamarchive.chat.ChatWebSocketHandler
import com.github.s8u.streamarchive.chat.twitch.TwitchChatWebSocketHandler
import com.github.s8u.streamarchive.client.twitch.TwitchApiClient
import com.github.s8u.streamarchive.client.twitch.TwitchStreamsRequestDto
import com.github.s8u.streamarchive.client.twitch.TwitchUsersRequestDto
import com.github.s8u.streamarchive.enums.PlatformType
import com.github.s8u.streamarchive.platform.PlatformChannelDto
import com.github.s8u.streamarchive.platform.PlatformStrategy
import com.github.s8u.streamarchive.platform.PlatformStreamDto
import com.github.s8u.streamarchive.properties.TwitchProperties
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDateTime
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
            category = responseFirst.tags?.joinToString { ", " },
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

    override fun getChatSyncOffsetMillis(): Long {
        return 15000L
    }

    override fun isSupportChatRecord(): Boolean {
        return true
    }

    override fun getChatWebSocketUrl(): String? {
        return "wss://irc-ws.chat.twitch.tv:443"
    }

    override fun createChatWebSocketHandler(
        recordId: Long,
        videoId: Long,
        platformType: PlatformType,
        platformChannelId: String,
        recordStartedAt: LocalDateTime,
        onChat: (ChatMessageDto) -> Unit,
        onConnectionClosed: () -> Unit
    ): ChatWebSocketHandler? {
        return TwitchChatWebSocketHandler(
            recordId = recordId,
            videoId = videoId,
            platformChannelId = platformChannelId,
            recordStartedAt = recordStartedAt,
            onChat = onChat,
            onConnectionClosed = onConnectionClosed
        )
    }

}