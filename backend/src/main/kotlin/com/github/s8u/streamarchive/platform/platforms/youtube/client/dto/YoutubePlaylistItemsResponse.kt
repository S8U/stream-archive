package com.github.s8u.streamarchive.platform.platforms.youtube.client.dto

data class YoutubePlaylistItemsResponse(
    val items: List<YoutubePlaylistItem> = emptyList()
)

data class YoutubePlaylistItem(
    val contentDetails: YoutubePlaylistItemContentDetails
)

data class YoutubePlaylistItemContentDetails(
    val videoId: String
)
