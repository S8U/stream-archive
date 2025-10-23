package com.github.s8u.streamarchive.controller

import com.github.s8u.streamarchive.dto.ChannelCreateRequest
import com.github.s8u.streamarchive.dto.ChannelResponse
import com.github.s8u.streamarchive.dto.ChannelUpdateRequest
import com.github.s8u.streamarchive.service.ChannelService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
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
    fun getAll(): List<ChannelResponse> {
        return channelService.getAll()
    }

    @Operation(summary = "채널 단건 조회")
    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ChannelResponse {
        return channelService.getById(id)
    }

    @Operation(summary = "채널 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: ChannelCreateRequest): ChannelResponse {
        return channelService.create(request)
    }

    @Operation(summary = "채널 수정")
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody request: ChannelUpdateRequest
    ): ChannelResponse {
        return channelService.update(id, request)
    }

    @Operation(summary = "채널 삭제")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) {
        channelService.delete(id)
    }
}
