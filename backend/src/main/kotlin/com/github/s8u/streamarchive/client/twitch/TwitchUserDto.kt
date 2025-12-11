package com.github.s8u.streamarchive.client.twitch

data class TwitchUsersRequestDto(
    val id: List<String>? = null,
    val login: List<String>? = null
)

data class TwitchUsersResponseDto(
    val data: List<TwitchUserResponseDto>
)

data class TwitchUserResponseDto(
    val id: String,
    val login: String,
    val displayName: String,
    val type: String? = null,
    val broadcasterType: String? = null,
    val description: String? = null,
    val profileImageUrl: String? = null,
    val offlineImageUrl: String? = null,
    val email: String? = null,
    val createdAt: String? = null
)