package com.github.s8u.streamarchive.controller

import com.github.s8u.streamarchive.dto.AdminUserResponse
import com.github.s8u.streamarchive.dto.AdminUserSearchRequest
import com.github.s8u.streamarchive.dto.AdminUserUpdateRequest
import com.github.s8u.streamarchive.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "사용자", description = "사용자 관리 API (관리자)")
@RestController
@RequestMapping("/admin/users")
class AdminUserController(
    private val userService: UserService
) {
    @Operation(summary = "사용자 목록 조회")
    @GetMapping
    fun search(
        request: AdminUserSearchRequest,
        pageable: Pageable
    ): Page<AdminUserResponse> {
        return userService.searchForAdmin(request, pageable)
    }

    @Operation(summary = "사용자 상세 조회")
    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): AdminUserResponse {
        return userService.getForAdmin(id)
    }

    @Operation(summary = "사용자 수정")
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody request: AdminUserUpdateRequest
    ): AdminUserResponse {
        return userService.updateForAdmin(id, request)
    }

    @Operation(summary = "사용자 삭제")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) {
        userService.delete(id)
    }
}
