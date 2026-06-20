package com.github.s8u.streamarchive.user.controller

import com.github.s8u.streamarchive.user.controller.dto.request.UserAdminSearchRequest
import com.github.s8u.streamarchive.user.controller.dto.request.UserAdminUpdateRequest
import com.github.s8u.streamarchive.user.controller.dto.response.UserAdminGetResponse
import com.github.s8u.streamarchive.user.controller.dto.response.UserAdminSearchResponse
import com.github.s8u.streamarchive.user.controller.dto.response.UserAdminUpdateResponse
import com.github.s8u.streamarchive.user.usecase.UserAdminDeleteUseCase
import com.github.s8u.streamarchive.user.usecase.UserAdminGetUseCase
import com.github.s8u.streamarchive.user.usecase.UserAdminSearchUseCase
import com.github.s8u.streamarchive.user.usecase.UserAdminUpdateUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "UserAdmin", description = "사용자 관리")
@RestController
@RequestMapping("/admin/users")
class UserAdminController(
    private val userAdminSearchUseCase: UserAdminSearchUseCase,
    private val userAdminGetUseCase: UserAdminGetUseCase,
    private val userAdminUpdateUseCase: UserAdminUpdateUseCase,
    private val userAdminDeleteUseCase: UserAdminDeleteUseCase
) {

    @Operation(summary = "사용자 목록 조회")
    @GetMapping
    fun searchUsers(
        request: UserAdminSearchRequest,
        pageable: Pageable
    ): Page<UserAdminSearchResponse> {
        return userAdminSearchUseCase.search(request.toCommand(), pageable)
            .map { UserAdminSearchResponse.from(it) }
    }

    @Operation(summary = "사용자 상세 조회")
    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: Long): UserAdminGetResponse {
        val result = userAdminGetUseCase.get(id)
        return UserAdminGetResponse.from(result)
    }

    @Operation(summary = "사용자 수정")
    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: Long,
        @RequestBody request: UserAdminUpdateRequest
    ): UserAdminUpdateResponse {
        val result = userAdminUpdateUseCase.update(id, request.toCommand())
        return UserAdminUpdateResponse.from(result)
    }

    @Operation(summary = "사용자 삭제")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteUser(@PathVariable id: Long) {
        userAdminDeleteUseCase.delete(id)
    }

}
