package com.github.s8u.streamarchive.controller

import com.github.s8u.streamarchive.dto.AdminChannelCreateRequest
import com.github.s8u.streamarchive.dto.AdminChannelResponse
import com.github.s8u.streamarchive.dto.AdminChannelSearchRequest
import com.github.s8u.streamarchive.dto.AdminChannelUpdateRequest
import com.github.s8u.streamarchive.service.ChannelService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "채널", description = "채널 관리 API (관리자)")
@RestController
@RequestMapping("/admin/channels")
class AdminChannelController(
    private val channelService: ChannelService
) {
    @Operation(summary = "채널 목록 조회")
    @GetMapping
    fun search(
        request: AdminChannelSearchRequest,
        @PageableDefault(size = 20) pageable: Pageable
    ): Page<AdminChannelResponse> {
        return channelService.searchForAdmin(request, pageable)
    }

    @Operation(summary = "채널 단건 조회")
    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): AdminChannelResponse {
        return channelService.getForAdmin(id)
    }

    @Operation(summary = "채널 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: AdminChannelCreateRequest): AdminChannelResponse {
        return channelService.createForAdmin(request)
    }

    @Operation(summary = "채널 수정")
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody request: AdminChannelUpdateRequest
    ): AdminChannelResponse {
        return channelService.updateForAdmin(id, request)
    }

    @Operation(summary = "채널 삭제")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) {
        channelService.delete(id)
    }
}
