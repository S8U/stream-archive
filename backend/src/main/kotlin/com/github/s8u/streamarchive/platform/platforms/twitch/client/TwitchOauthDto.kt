package com.github.s8u.streamarchive.platform.platforms.twitch.client

data class TwitchOauthResponseDto(
    val accessToken: String,
    val expiresIn: Long,
    val tokenType: String
)
