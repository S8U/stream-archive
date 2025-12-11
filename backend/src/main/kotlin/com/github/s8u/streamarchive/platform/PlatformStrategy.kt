package com.github.s8u.streamarchive.platform

import com.github.s8u.streamarchive.chat.ChatMessageDto
import com.github.s8u.streamarchive.chat.ChatWebSocketHandler
import com.github.s8u.streamarchive.enums.PlatformType
import java.time.LocalDateTime

interface PlatformStrategy {

    val platformType: PlatformType

    fun getStreamUrl(username: String): String

    fun getChannel(username: String): PlatformChannelDto?

    fun getStream(username: String): PlatformStreamDto?

    fun getStreamlinkArgs(): List<String> = emptyList()

    fun isSupportChatRecord(): Boolean

    fun getChatWebSocketUrl(): String?

    fun createChatWebSocketHandler(
        recordId: Long,
        videoId: Long,
        platformType: PlatformType,
        platformChannelId: String,
        recordStartedAt: LocalDateTime,
        onChat: (ChatMessageDto) -> Unit,
        onConnectionClosed: () -> Unit
    ): ChatWebSocketHandler?

}