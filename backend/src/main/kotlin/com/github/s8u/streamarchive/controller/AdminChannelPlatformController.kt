package com.github.s8u.streamarchive.controller

import com.github.s8u.streamarchive.dto.AdminChannelPlatformCreateRequest
import com.github.s8u.streamarchive.dto.AdminChannelPlatformResponse
import com.github.s8u.streamarchive.dto.AdminChannelPlatformSearchRequest
import com.github.s8u.streamarchive.dto.AdminChannelPlatformUpdateRequest
import com.github.s8u.streamarchive.service.ChannelPlatformService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "채널 플랫폼", description = "채널 플랫폼 연동 관리 API (관리자)")
@RestController
@RequestMapping("/admin/channel-platforms")
class AdminChannelPlatformController(
    private val channelPlatformService: ChannelPlatformService
) {
    @Operation(summary = "채널 플랫폼 목록 조회")
    @GetMapping
    fun search(
        request: AdminChannelPlatformSearchRequest,
        pageable: Pageable
    ): Page<AdminChannelPlatformResponse> {
        return channelPlatformService.searchForAdmin(request, pageable)
    }

    @Operation(summary = "채널 플랫폼 단건 조회")
    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): AdminChannelPlatformResponse {
        return channelPlatformService.getForAdmin(id)
    }

    @Operation(summary = "채널 플랫폼 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: AdminChannelPlatformCreateRequest): AdminChannelPlatformResponse {
        return channelPlatformService.createForAdmin(request)
    }

    @Operation(summary = "채널 플랫폼 수정")
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody request: AdminChannelPlatformUpdateRequest
    ): AdminChannelPlatformResponse {
        return channelPlatformService.updateForAdmin(id, request)
    }

    @Operation(summary = "채널 플랫폼 삭제")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) {
        channelPlatformService.delete(id)
    }
}
