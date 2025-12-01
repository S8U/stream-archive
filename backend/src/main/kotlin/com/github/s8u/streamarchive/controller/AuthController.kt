package com.github.s8u.streamarchive.controller

import com.github.s8u.streamarchive.config.properties.JwtProperties
import com.github.s8u.streamarchive.dto.*
import com.github.s8u.streamarchive.exception.BusinessException
import com.github.s8u.streamarchive.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.web.bind.annotation.*

@Tag(name = "Auth", description = "인증")
@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
    private val jwtProperties: JwtProperties
) {

    @Operation(summary = "로그인")
    @PostMapping("/login")
    fun login(
        @RequestBody request: LoginRequest,
        response: HttpServletResponse
    ): LoginResponse {
        val loginResponse = authService.login(request)

        // Access Token과 Refresh Token을 HttpOnly 쿠키로 설정
        setAccessTokenCookie(response, loginResponse.accessToken)
        setRefreshTokenCookie(response, loginResponse.refreshToken)

        return loginResponse
    }

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    fun signup(@RequestBody request: SignupRequest): SignupResponse {
        return authService.signup(request)
    }

    @Operation(summary = "토큰 갱신")
    @PostMapping("/refresh")
    fun refresh(
        @RequestBody(required = false) request: RefreshTokenRequest?,
        httpRequest: HttpServletRequest,
        response: HttpServletResponse
    ): RefreshTokenResponse {
        // 쿠키에서 Refresh Token 읽기
        val cookieRefreshToken = httpRequest.cookies
            ?.find { it.name == jwtProperties.cookie.refreshTokenName }
            ?.value

        // 쿠키 우선, 없으면 body에서 읽기
        val refreshToken = cookieRefreshToken ?: request?.refreshToken
            ?: throw BusinessException("리프레시 토큰이 필요합니다.", HttpStatus.BAD_REQUEST)

        val refreshResponse = authService.refresh(RefreshTokenRequest(refreshToken))

        // 새로운 Access Token과 Refresh Token을 쿠키에 설정
        setAccessTokenCookie(response, refreshResponse.accessToken)
        setRefreshTokenCookie(response, refreshResponse.refreshToken)

        return refreshResponse
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun logout(
        @RequestBody(required = false) request: LogoutRequest?,
        httpRequest: HttpServletRequest,
        response: HttpServletResponse
    ) {
        // 쿠키에서 Refresh Token 읽기
        val cookieRefreshToken = httpRequest.cookies
            ?.find { it.name == jwtProperties.cookie.refreshTokenName }
            ?.value

        // 쿠키 우선, 없으면 body에서 읽기
        val refreshToken = cookieRefreshToken ?: request?.refreshToken

        // DB의 Refresh Token 무효화
        if (refreshToken != null) {
            authService.logout(refreshToken)
        }

        // Access Token과 Refresh Token 쿠키 삭제
        clearAccessTokenCookie(response)
        clearRefreshTokenCookie(response)
    }

    private fun setAccessTokenCookie(response: HttpServletResponse, accessToken: String) {
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

    private fun setRefreshTokenCookie(response: HttpServletResponse, refreshToken: String) {
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

    private fun clearAccessTokenCookie(response: HttpServletResponse) {
        val cookie = ResponseCookie.from(jwtProperties.cookie.accessTokenName, "")
            .path(jwtProperties.cookie.path)
            .maxAge(0)
            .build()

        response.addHeader("Set-Cookie", cookie.toString())
    }

    private fun clearRefreshTokenCookie(response: HttpServletResponse) {
        val cookie = ResponseCookie.from(jwtProperties.cookie.refreshTokenName, "")
            .path(jwtProperties.cookie.path)
            .maxAge(0)
            .build()

        response.addHeader("Set-Cookie", cookie.toString())
    }
}
