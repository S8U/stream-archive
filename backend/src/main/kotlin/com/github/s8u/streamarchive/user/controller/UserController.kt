package com.github.s8u.streamarchive.user.controller

import com.github.s8u.streamarchive.user.controller.dto.request.UserPasswordUpdateRequest
import com.github.s8u.streamarchive.user.controller.dto.request.UserUpdateRequest
import com.github.s8u.streamarchive.user.controller.dto.response.UserGetResponse
import com.github.s8u.streamarchive.user.controller.dto.response.UserUpdateResponse
import com.github.s8u.streamarchive.user.usecase.UserGetUseCase
import com.github.s8u.streamarchive.user.usecase.UserPasswordUpdateUseCase
import com.github.s8u.streamarchive.user.usecase.UserUpdateUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@Tag(name = "User", description = "사용자")
@RestController
@RequestMapping("/users")
class UserController(
    private val userGetUseCase: UserGetUseCase,
    private val userUpdateUseCase: UserUpdateUseCase,
    private val userPasswordUpdateUseCase: UserPasswordUpdateUseCase
) {

    @Operation(summary = "내 정보 조회")
    @GetMapping("/me")
    fun getUserMe(): UserGetResponse {
        val result = userGetUseCase.get()
        return UserGetResponse.from(result)
    }

    @Operation(summary = "내 정보 수정")
    @PutMapping("/me")
    fun updateUserMe(@RequestBody request: UserUpdateRequest): UserUpdateResponse {
        val result = userUpdateUseCase.update(request.toCommand())
        return UserUpdateResponse.from(result)
    }

    @Operation(summary = "비밀번호 변경")
    @PutMapping("/me/password")
    fun updateUserPassword(@RequestBody request: UserPasswordUpdateRequest) {
        userPasswordUpdateUseCase.update(request.toCommand())
    }

}
