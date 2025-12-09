package com.github.s8u.streamarchive.controller

import com.github.s8u.streamarchive.dto.UserMeResponse
import com.github.s8u.streamarchive.exception.BusinessException
import com.github.s8u.streamarchive.service.AuthenticationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "User", description = "사용자")
@RestController
@RequestMapping("/users")
class UserController(private val authenticationService: AuthenticationService) {

    @Operation(summary = "내 정보 조회")
    @GetMapping("/me")
    fun getUserMe(): UserMeResponse {
        val user = authenticationService.getCurrentUser()
            ?: throw BusinessException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED)

        return UserMeResponse.from(user)
    }
}
