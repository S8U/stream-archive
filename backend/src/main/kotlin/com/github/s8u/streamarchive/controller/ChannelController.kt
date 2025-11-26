package com.github.s8u.streamarchive.controller

import com.github.s8u.streamarchive.dto.ChannelPlatformResponse
import com.github.s8u.streamarchive.dto.ChannelResponse
import com.github.s8u.streamarchive.dto.ChannelSearchRequest
import com.github.s8u.streamarchive.service.ChannelPlatformService
import com.github.s8u.streamarchive.service.ChannelProfileService
import com.github.s8u.streamarchive.service.ChannelService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.core.io.Resource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "채널")
@RestController
@RequestMapping("/channels")
class ChannelController(
    private val channelService: ChannelService,
    private val channelProfileService: ChannelProfileService,
    private val channelPlatformService: ChannelPlatformService
) {
    @Operation(summary = "채널 목록 조회")
    @GetMapping
    fun search(
        request: ChannelSearchRequest,
        pageable: Pageable
    ): Page<ChannelResponse> {
        return channelService.searchForPublic(request, pageable)
    }

    @Operation(summary = "채널 단건 조회")
    @GetMapping("/{uuid}")
    fun getByUuid(@PathVariable uuid: String): ChannelResponse {
        return channelService.getByUuidForPublic(uuid)
    }

    @Operation(summary = "채널 프로필 이미지 조회")
    @GetMapping("/{uuid}/profile")
    fun getProfile(@PathVariable uuid: String): ResponseEntity<Resource> {
        val resource = channelProfileService.getProfileImage(uuid)

        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_PNG)
            .body(resource)
    }

    @Operation(summary = "채널의 플랫폼 목록 조회")
    @GetMapping("/{uuid}/platforms")
    fun getPlatforms(@PathVariable uuid: String): List<ChannelPlatformResponse> {
        return channelPlatformService.getByChannelUuidForPublic(uuid)
    }
}
