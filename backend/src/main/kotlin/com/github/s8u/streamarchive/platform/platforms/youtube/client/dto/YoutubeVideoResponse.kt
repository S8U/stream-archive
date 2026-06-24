package com.github.s8u.streamarchive.platform.platforms.youtube.client.dto

data class YoutubeVideosResponse(
    val items: List<YoutubeVideo> = emptyList()
)

data class YoutubeVideo(
    val id: String,
    val snippet: YoutubeVideoSnippet? = null,
    val liveStreamingDetails: YoutubeLiveStreamingDetails? = null
)

data class YoutubeVideoSnippet(
    val channelId: String? = null,
    val channelTitle: String? = null,
    val title: String? = null,
    val categoryId: String? = null,
    val publishedAt: String? = null,
    // 라이브 방송 상태 (live/upcoming/none)
    val liveBroadcastContent: String? = null,
    val thumbnails: Map<String, YoutubeThumbnail> = emptyMap()
)

data class YoutubeLiveStreamingDetails(
    val actualStartTime: String? = null,
    val scheduledStartTime: String? = null,
    val actualEndTime: String? = null,
    val concurrentViewers: String? = null,
    val activeLiveChatId: String? = null
)
