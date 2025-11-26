package com.github.s8u.streamarchive.controller

import com.github.s8u.streamarchive.config.properties.JwtProperties
import com.github.s8u.streamarchive.dto.*
import com.github.s8u.streamarchive.exception.BusinessException
import com.github.s8u.streamarchive.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.web.bind.annotation.*

@Tag(name = "인증", description = "인증 API")
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

        // Refresh Token을 HttpOnly 쿠키로 설정
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
        @CookieValue(name = "RT", required = false) cookieRefreshToken: String?,
        @RequestBody(required = false) request: RefreshTokenRequest?,
        response: HttpServletResponse
    ): RefreshTokenResponse {
        // 쿠키 우선, 없으면 body에서 읽기
        val refreshToken = cookieRefreshToken ?: request?.refreshToken
            ?: throw BusinessException("리프레시 토큰이 필요합니다.", HttpStatus.BAD_REQUEST)

        val refreshResponse = authService.refresh(RefreshTokenRequest(refreshToken))

        // 새로운 Refresh Token을 쿠키에 설정
        setRefreshTokenCookie(response, refreshResponse.refreshToken)

        return refreshResponse
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun logout(
        @CookieValue(name = "RT", required = false) cookieRefreshToken: String?,
        @RequestBody(required = false) request: LogoutRequest?,
        response: HttpServletResponse
    ) {
        // 쿠키 우선, 없으면 body에서 읽기
        val refreshToken = cookieRefreshToken ?: request?.refreshToken

        // DB의 Refresh Token 무효화
        if (refreshToken != null) {
            authService.logout(refreshToken)
        }

        // Refresh Token 쿠키 삭제
        clearRefreshTokenCookie(response)
    }

    private fun setRefreshTokenCookie(response: HttpServletResponse, refreshToken: String) {
        val cookieConfig = jwtProperties.cookie
        val maxAge = (jwtProperties.refreshTokenExpiration / 1000)

        val cookie = ResponseCookie.from("RT", refreshToken)
            .path(cookieConfig.path)
            .maxAge(maxAge)
            .httpOnly(cookieConfig.httpOnly)
            .secure(cookieConfig.secure)
            .sameSite(cookieConfig.sameSite)
            .build()

        response.addHeader("Set-Cookie", cookie.toString())
    }

    private fun clearRefreshTokenCookie(response: HttpServletResponse) {
        val cookie = ResponseCookie.from("RT", "")
            .path(jwtProperties.cookie.path)
            .maxAge(0)
            .build()

        response.addHeader("Set-Cookie", cookie.toString())
    }
}
