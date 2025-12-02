package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.config.properties.JwtProperties
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Service

@Service
class JwtCookieService(
    private val jwtProperties: JwtProperties
) {

    fun setAccessTokenCookie(response: HttpServletResponse, accessToken: String) {
        val cookieConfig = jwtProperties.cookie
        val maxAge = (jwtProperties.accessTokenExpiration / 1000)

        val cookie = ResponseCookie.from(cookieConfig.accessTokenName, accessToken)
            .domain(cookieConfig.domain)
            .path(cookieConfig.path)
            .maxAge(maxAge)
            .httpOnly(cookieConfig.httpOnly)
            .secure(cookieConfig.secure)
            .sameSite(cookieConfig.sameSite)
            .build()

        response.addHeader("Set-Cookie", cookie.toString())
    }

    fun setRefreshTokenCookie(response: HttpServletResponse, refreshToken: String) {
        val cookieConfig = jwtProperties.cookie
        val maxAge = (jwtProperties.refreshTokenExpiration / 1000)

        val cookie = ResponseCookie.from(cookieConfig.refreshTokenName, refreshToken)
            .domain(cookieConfig.domain)
            .path(cookieConfig.path)
            .maxAge(maxAge)
            .httpOnly(cookieConfig.httpOnly)
            .secure(cookieConfig.secure)
            .sameSite(cookieConfig.sameSite)
            .build()

        response.addHeader("Set-Cookie", cookie.toString())
    }

    fun clearAccessTokenCookie(response: HttpServletResponse) {
        val cookie = ResponseCookie.from(jwtProperties.cookie.accessTokenName, "")
            .path(jwtProperties.cookie.path)
            .maxAge(0)
            .build()

        response.addHeader("Set-Cookie", cookie.toString())
    }

    fun clearRefreshTokenCookie(response: HttpServletResponse) {
        val cookie = ResponseCookie.from(jwtProperties.cookie.refreshTokenName, "")
            .path(jwtProperties.cookie.path)
            .maxAge(0)
            .build()

        response.addHeader("Set-Cookie", cookie.toString())
    }
}