package com.github.s8u.streamarchive.platform.platforms.youtube.client.dto

data class YoutubeChannelsResponse(
    val items: List<YoutubeChannelItem> = emptyList()
)

data class YoutubeChannelItem(
    val id: String,
    val snippet: YoutubeChannelSnippet
)

data class YoutubeChannelSnippet(
    val title: String,
    val customUrl: String? = null,
    val thumbnails: Map<String, YoutubeThumbnail> = emptyMap()
)

data class YoutubeThumbnail(
    val url: String? = null,
    val width: Int? = null,
    val height: Int? = null
)
