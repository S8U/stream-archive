package com.github.s8u.streamarchive.platform.platforms.youtube.strategy

import com.github.s8u.streamarchive.platform.chat.PlatformChatCollectionSession
import com.github.s8u.streamarchive.platform.chat.PlatformChatStrategy
import com.github.s8u.streamarchive.platform.chat.dto.PlatformChatMessageDto
import com.github.s8u.streamarchive.platform.enums.PlatformType
import com.github.s8u.streamarchive.platform.platforms.youtube.chat.YoutubeChatCollectionSession
import com.github.s8u.streamarchive.platform.platforms.youtube.client.YoutubeApiClient
import com.github.s8u.streamarchive.platform.platforms.youtube.service.YoutubeChannelFindService
import com.github.s8u.streamarchive.platform.platforms.youtube.service.YoutubeLiveFindService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * 유튜브 채팅 전략
 */
@Component
class YoutubeChatStrategy(
    private val apiClient: YoutubeApiClient,
    private val channelFindService: YoutubeChannelFindService,
    private val liveFindService: YoutubeLiveFindService
) : PlatformChatStrategy {

    private val logger = LoggerFactory.getLogger(javaClass)

    override val platformType: PlatformType
        get() = PlatformType.YOUTUBE

    override val chatSyncOffsetMillis: Long
        get() = 0L

    override fun startCollecting(
        recordId: Long,
        videoId: Long,
        platformChannelId: String,
        recordStartedAt: LocalDateTime,
        onChat: (PlatformChatMessageDto) -> Unit,
        onClosed: () -> Unit
    ): PlatformChatCollectionSession? {
        // 라이브 동영상은 감지 단계에서 캐싱돼 있어 추가 조회 비용이 거의 없다
        val channel = channelFindService.find(platformChannelId) ?: return null
        val live = liveFindService.find(channel.id) ?: return null
        val liveChatId = live.video.liveStreamingDetails?.activeLiveChatId ?: return null

        val session = YoutubeChatCollectionSession(
            apiClient = apiClient,
            recordId = recordId,
            videoId = videoId,
            liveChatId = liveChatId,
            recordStartedAt = recordStartedAt,
            onChat = onChat,
            onClosed = onClosed
        )
        session.start()

        logger.info(
            "YoutubeChatStrategy: Started YouTube chat collect: recordId={}, platformChannelId={}, liveVideoId={}",
            recordId,
            platformChannelId,
            live.video.id
        )

        return session
    }

}
