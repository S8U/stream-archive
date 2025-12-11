package com.github.s8u.streamarchive.controller

import com.github.s8u.streamarchive.dto.UserResponse
import com.github.s8u.streamarchive.dto.UserUpdatePasswordRequest
import com.github.s8u.streamarchive.dto.UserUpdateRequest
import com.github.s8u.streamarchive.exception.BusinessException
import com.github.s8u.streamarchive.service.AuthenticationService
import com.github.s8u.streamarchive.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "User", description = "사용자")
@RestController
@RequestMapping("/users")
class UserController(
    private val authenticationService: AuthenticationService,
    private val userService: UserService
) {

    @Operation(summary = "내 정보 조회")
    @GetMapping("/me")
    fun getUserMe(): UserResponse {
        val user = authenticationService.getCurrentUser()
            ?: throw BusinessException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED)

        return UserResponse.from(user)
    }

    @Operation(summary = "내 정보 수정")
    @PutMapping("/me")
    fun updateUserMe(@RequestBody request: UserUpdateRequest): UserResponse {
        return userService.updateCurrentUser(request)
    }

    @Operation(summary = "비밀번호 변경")
    @PutMapping("/me/password")
    fun updateUserPassword(@RequestBody request: UserUpdatePasswordRequest) {
        userService.updatePassword(request)
    }
}
