package com.github.s8u.streamarchive.client.twitch

data class TwitchStreamsRequestDto(
    val userId: String? = null,
    val userLogin: String? = null,
    val gameId: String? = null,
    val type: String? = null,
    val language: String? = null,
    val first: Int? = null,
    val before: String? = null,
    val after: String? = null
)

data class TwitchStreamsResponseDto(
    val data: List<TwitchStreamResponseDto>
)

data class TwitchStreamResponseDto(
    val id: String,
    val userId: String,
    val userLogin: String,
    val userName: String,
    val gameId: String,
    val gameName: String,
    val type: String,
    val title: String,
    val tags: List<String>,
    val viewerCount: Int,
    val startedAt: String,
    val language: String,
    val thumbnailUrl: String,
    val tagIds: List<String>,
    val isMature: Boolean
)