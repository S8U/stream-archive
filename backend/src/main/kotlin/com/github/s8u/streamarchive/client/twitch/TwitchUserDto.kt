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
    val type: String,
    val broadcasterType: String,
    val description: String,
    val profileImageUrl: String,
    val offlineImageUrl: String,
    val email: String,
    val createdAt: String
)