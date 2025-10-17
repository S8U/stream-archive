package com.github.s8u.streamarchive.client.twitch

data class TwitchOauthResponseDto(
    val accessToken: String,
    val expiresIn: Long,
    val tokenType: String
)