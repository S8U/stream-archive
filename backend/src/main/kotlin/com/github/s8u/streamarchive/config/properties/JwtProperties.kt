package com.github.s8u.streamarchive.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    val secret: String,
    val accessTokenExpiration: Long,
    val refreshTokenExpiration: Long,
    val cookie: CookieProperties
) {
    data class CookieProperties(
        val secure: Boolean,
        val sameSite: String,
        val httpOnly: Boolean,
        val path: String
    )
}
