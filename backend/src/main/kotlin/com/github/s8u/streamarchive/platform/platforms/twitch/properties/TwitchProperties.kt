package com.github.s8u.streamarchive.platform.platforms.twitch.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "platform.twitch")
data class TwitchProperties(
    val appClientId: String,
    val appClientSecret: String,
    val personalOauthToken: String
)
