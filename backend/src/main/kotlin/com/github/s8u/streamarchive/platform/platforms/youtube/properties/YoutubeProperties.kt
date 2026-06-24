package com.github.s8u.streamarchive.platform.platforms.youtube.properties

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * 유튜브 설정값
 */
@ConfigurationProperties(prefix = "platform.youtube")
data class YoutubeProperties(
    val apiKey: String
)
