package com.github.s8u.streamarchive.auth.jwt.service

import com.github.s8u.streamarchive.auth.jwt.properties.JwtProperties
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
        clearCookie(response, jwtProperties.cookie.accessTokenName)
    }

    fun clearRefreshTokenCookie(response: HttpServletResponse) {
        clearCookie(response, jwtProperties.cookie.refreshTokenName)
    }

    // 삭제 쿠키는 생성 때와 같은 속성(domain·path·secure·sameSite)을 줘야 브라우저가 지운다
    private fun clearCookie(response: HttpServletResponse, name: String) {
        val cookieConfig = jwtProperties.cookie

        val cookie = ResponseCookie.from(name, "")
            .domain(cookieConfig.domain)
            .path(cookieConfig.path)
            .maxAge(0)
            .httpOnly(cookieConfig.httpOnly)
            .secure(cookieConfig.secure)
            .sameSite(cookieConfig.sameSite)
            .build()

        response.addHeader("Set-Cookie", cookie.toString())
    }

}
