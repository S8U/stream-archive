package com.github.s8u.streamarchive.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "platform.twitch")
data class TwitchClientConfig(
    val appClientId: String,
    val appClientSecret: String,
    val personalOauthToken: String
)