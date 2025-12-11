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
    val userId: String? = null,
    val userLogin: String,
    val userName: String? = null,
    val gameId: String? = null,
    val gameName: String? = null,
    val type: String? = null,
    val title: String? = null,
    val tags: List<String>? = null,
    val viewerCount: Int? = null,
    val startedAt: String? = null,
    val language: String? = null,
    val thumbnailUrl: String? = null,
    val tagIds: List<String>? = null,
    val isMature: Boolean? = null
)