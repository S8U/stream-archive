package com.github.s8u.streamarchive.platform.platforms.youtube.client.dto

data class YoutubeLiveChatMessagesResponse(
    val nextPageToken: String? = null,
    val pollingIntervalMillis: Long? = null,
    val offlineAt: String? = null,
    val items: List<YoutubeLiveChatMessage> = emptyList()
)

data class YoutubeLiveChatMessage(
    val id: String,
    val snippet: YoutubeLiveChatMessageSnippet,
    val authorDetails: YoutubeLiveChatMessageAuthorDetails? = null
)

data class YoutubeLiveChatMessageSnippet(
    val type: String,
    val publishedAt: String? = null,
    val displayMessage: String? = null,
    val textMessageDetails: YoutubeLiveChatTextMessageDetails? = null
)

data class YoutubeLiveChatTextMessageDetails(
    val messageText: String? = null
)

data class YoutubeLiveChatMessageAuthorDetails(
    val displayName: String? = null,
    val channelId: String? = null
)
