package com.github.s8u.streamarchive.channel.controller

import com.github.s8u.streamarchive.channel.controller.dto.request.ChannelSearchRequest
import com.github.s8u.streamarchive.channel.controller.dto.response.ChannelGetResponse
import com.github.s8u.streamarchive.channel.controller.dto.response.ChannelSearchResponse
import com.github.s8u.streamarchive.channel.controller.dto.response.ChannelStatsResponse
import com.github.s8u.streamarchive.channel.usecase.ChannelGetUseCase
import com.github.s8u.streamarchive.channel.usecase.ChannelProfileImageGetUseCase
import com.github.s8u.streamarchive.channel.usecase.ChannelSearchUseCase
import com.github.s8u.streamarchive.channel.usecase.ChannelStatsGetUseCase
import com.github.s8u.streamarchive.channelplatform.controller.dto.response.ChannelPlatformSearchResponse
import com.github.s8u.streamarchive.channelplatform.usecase.ChannelPlatformSearchUseCase
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

@Tag(name = "Channel", description = "채널")
@RestController
@RequestMapping("/channels")
class ChannelController(
    private val channelSearchUseCase: ChannelSearchUseCase,
    private val channelGetUseCase: ChannelGetUseCase,
    private val channelProfileImageGetUseCase: ChannelProfileImageGetUseCase,
    private val channelStatsGetUseCase: ChannelStatsGetUseCase,
    private val channelPlatformSearchUseCase: ChannelPlatformSearchUseCase
) {

    @Operation(summary = "채널 목록 조회")
    @GetMapping
    fun searchChannels(
        request: ChannelSearchRequest,
        pageable: Pageable
    ): Page<ChannelSearchResponse> {
        return channelSearchUseCase.search(request.toCommand(), pageable)
            .map { ChannelSearchResponse.from(it) }
    }

    @Operation(summary = "채널 단건 조회")
    @GetMapping("/{uuid}")
    fun getChannelByUuid(@PathVariable uuid: String): ChannelGetResponse {
        val result = channelGetUseCase.getByUuid(uuid)
        return ChannelGetResponse.from(result)
    }

    @Operation(summary = "채널 프로필 이미지 조회")
    @GetMapping("/{uuid}/profile")
    fun getChannelProfile(@PathVariable uuid: String): ResponseEntity<Resource> {
        val resource = channelProfileImageGetUseCase.getByUuid(uuid)

        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(resource)
    }

    @Operation(summary = "채널의 플랫폼 목록 조회")
    @GetMapping("/{uuid}/platforms")
    fun getChannelPlatforms(@PathVariable uuid: String): List<ChannelPlatformSearchResponse> {
        return channelPlatformSearchUseCase.searchByChannelUuid(uuid)
            .map { ChannelPlatformSearchResponse.from(it) }
    }

    @Operation(summary = "채널 통계 조회")
    @GetMapping("/{uuid}/stats")
    fun getChannelStats(@PathVariable uuid: String): ChannelStatsResponse {
        val result = channelStatsGetUseCase.getByUuid(uuid)
        return ChannelStatsResponse.from(result)
    }

}
