package com.github.s8u.streamarchive.auth.controller

import com.github.s8u.streamarchive.auth.controller.dto.request.AuthLoginRequest
import com.github.s8u.streamarchive.auth.controller.dto.request.AuthLogoutRequest
import com.github.s8u.streamarchive.auth.controller.dto.request.AuthTokenRefreshRequest
import com.github.s8u.streamarchive.auth.controller.dto.request.UserSignupRequest
import com.github.s8u.streamarchive.auth.controller.dto.response.AuthLoginResponse
import com.github.s8u.streamarchive.auth.controller.dto.response.AuthTokenRefreshResponse
import com.github.s8u.streamarchive.auth.jwt.properties.JwtProperties
import com.github.s8u.streamarchive.auth.jwt.service.JwtCookieService
import com.github.s8u.streamarchive.auth.usecase.AuthLoginUseCase
import com.github.s8u.streamarchive.auth.usecase.AuthLogoutUseCase
import com.github.s8u.streamarchive.auth.usecase.AuthTokenRefreshUseCase
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.user.usecase.UserSignupUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "Auth", description = "인증")
@RestController
@RequestMapping("/auth")
class AuthController(
    private val authLoginUseCase: AuthLoginUseCase,
    private val authTokenRefreshUseCase: AuthTokenRefreshUseCase,
    private val authLogoutUseCase: AuthLogoutUseCase,
    private val userSignupUseCase: UserSignupUseCase,
    private val jwtProperties: JwtProperties,
    private val jwtCookieService: JwtCookieService
) {

    @Operation(summary = "로그인")
    @PostMapping("/login")
    fun login(
        @RequestBody request: AuthLoginRequest,
        response: HttpServletResponse
    ): AuthLoginResponse {
        val result = authLoginUseCase.login(request.toCommand())

        // Access Token과 Refresh Token을 HttpOnly 쿠키로 설정
        jwtCookieService.setAccessTokenCookie(response, result.accessToken)
        jwtCookieService.setRefreshTokenCookie(response, result.refreshToken)

        return AuthLoginResponse.from(result)
    }

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    fun signup(@RequestBody request: UserSignupRequest) {
        userSignupUseCase.signup(request.toCommand())
    }

    @Operation(summary = "토큰 갱신")
    @PostMapping("/refresh")
    fun refresh(
        @RequestBody(required = false) request: AuthTokenRefreshRequest?,
        httpRequest: HttpServletRequest,
        response: HttpServletResponse
    ): AuthTokenRefreshResponse {
        val refreshToken = resolveRefreshToken(httpRequest, request?.refreshToken)
            ?: throw BusinessException("리프레시 토큰이 필요합니다.", HttpStatus.BAD_REQUEST)

        val result = authTokenRefreshUseCase.refresh(refreshToken)

        // 새로운 Access Token과 Refresh Token을 쿠키에 설정
        jwtCookieService.setAccessTokenCookie(response, result.accessToken)
        jwtCookieService.setRefreshTokenCookie(response, result.refreshToken)

        return AuthTokenRefreshResponse.from(result)
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun logout(
        @RequestBody(required = false) request: AuthLogoutRequest?,
        httpRequest: HttpServletRequest,
        response: HttpServletResponse
    ) {
        val refreshToken = resolveRefreshToken(httpRequest, request?.refreshToken)
        if (refreshToken != null) {
            authLogoutUseCase.logout(refreshToken)
        }

        jwtCookieService.clearAccessTokenCookie(response)
        jwtCookieService.clearRefreshTokenCookie(response)
    }

    // 쿠키 우선, 없으면 요청 본문에서 리프레시 토큰을 읽는다
    private fun resolveRefreshToken(httpRequest: HttpServletRequest, bodyToken: String?): String? {
        val cookieToken = httpRequest.cookies
            ?.find { it.name == jwtProperties.cookie.refreshTokenName }
            ?.value

        return cookieToken ?: bodyToken
    }

}
