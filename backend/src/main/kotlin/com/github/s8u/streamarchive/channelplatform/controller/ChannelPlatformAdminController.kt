package com.github.s8u.streamarchive.channelplatform.controller

import com.github.s8u.streamarchive.channelplatform.controller.dto.request.ChannelPlatformAdminCreateRequest
import com.github.s8u.streamarchive.channelplatform.controller.dto.request.ChannelPlatformAdminSearchRequest
import com.github.s8u.streamarchive.channelplatform.controller.dto.request.ChannelPlatformAdminUpdateRequest
import com.github.s8u.streamarchive.channelplatform.controller.dto.response.ChannelPlatformAdminCreateResponse
import com.github.s8u.streamarchive.channelplatform.controller.dto.response.ChannelPlatformAdminGetResponse
import com.github.s8u.streamarchive.channelplatform.controller.dto.response.ChannelPlatformAdminSearchResponse
import com.github.s8u.streamarchive.channelplatform.controller.dto.response.ChannelPlatformAdminUpdateResponse
import com.github.s8u.streamarchive.channelplatform.usecase.ChannelPlatformAdminCreateUseCase
import com.github.s8u.streamarchive.channelplatform.usecase.ChannelPlatformAdminDeleteUseCase
import com.github.s8u.streamarchive.channelplatform.usecase.ChannelPlatformAdminGetUseCase
import com.github.s8u.streamarchive.channelplatform.usecase.ChannelPlatformAdminSearchUseCase
import com.github.s8u.streamarchive.channelplatform.usecase.ChannelPlatformAdminUpdateUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "ChannelPlatformAdmin", description = "채널 플랫폼 관리")
@RestController
@RequestMapping("/admin/channel-platforms")
class ChannelPlatformAdminController(
    private val channelPlatformAdminSearchUseCase: ChannelPlatformAdminSearchUseCase,
    private val channelPlatformAdminGetUseCase: ChannelPlatformAdminGetUseCase,
    private val channelPlatformAdminCreateUseCase: ChannelPlatformAdminCreateUseCase,
    private val channelPlatformAdminUpdateUseCase: ChannelPlatformAdminUpdateUseCase,
    private val channelPlatformAdminDeleteUseCase: ChannelPlatformAdminDeleteUseCase
) {

    @Operation(summary = "채널 플랫폼 목록 조회")
    @GetMapping
    fun searchAdminChannelPlatforms(
        request: ChannelPlatformAdminSearchRequest,
        pageable: Pageable
    ): Page<ChannelPlatformAdminSearchResponse> {
        return channelPlatformAdminSearchUseCase.search(request.toCommand(), pageable)
            .map { ChannelPlatformAdminSearchResponse.from(it) }
    }

    @Operation(summary = "채널 플랫폼 단건 조회")
    @GetMapping("/{id}")
    fun getAdminChannelPlatformById(@PathVariable id: Long): ChannelPlatformAdminGetResponse {
        val result = channelPlatformAdminGetUseCase.get(id)
        return ChannelPlatformAdminGetResponse.from(result)
    }

    @Operation(summary = "채널 플랫폼 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createAdminChannelPlatform(
        @RequestBody request: ChannelPlatformAdminCreateRequest
    ): ChannelPlatformAdminCreateResponse {
        val result = channelPlatformAdminCreateUseCase.create(request.toCommand())
        return ChannelPlatformAdminCreateResponse.from(result)
    }

    @Operation(summary = "채널 플랫폼 수정")
    @PutMapping("/{id}")
    fun updateAdminChannelPlatform(
        @PathVariable id: Long,
        @RequestBody request: ChannelPlatformAdminUpdateRequest
    ): ChannelPlatformAdminUpdateResponse {
        val result = channelPlatformAdminUpdateUseCase.update(id, request.toCommand())
        return ChannelPlatformAdminUpdateResponse.from(result)
    }

    @Operation(summary = "채널 플랫폼 삭제")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteAdminChannelPlatform(@PathVariable id: Long) {
        channelPlatformAdminDeleteUseCase.delete(id)
    }

}
