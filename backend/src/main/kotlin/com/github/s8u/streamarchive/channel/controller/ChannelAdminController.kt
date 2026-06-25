package com.github.s8u.streamarchive.channel.controller

import com.github.s8u.streamarchive.channel.controller.dto.request.ChannelAdminCreateRequest
import com.github.s8u.streamarchive.channel.controller.dto.request.ChannelAdminQuickCreateRequest
import com.github.s8u.streamarchive.channel.controller.dto.request.ChannelAdminSearchRequest
import com.github.s8u.streamarchive.channel.controller.dto.request.ChannelAdminUpdateRequest
import com.github.s8u.streamarchive.channel.controller.dto.response.ChannelAdminCreateResponse
import com.github.s8u.streamarchive.channel.controller.dto.response.ChannelAdminGetResponse
import com.github.s8u.streamarchive.channel.controller.dto.response.ChannelAdminQuickCreateResponse
import com.github.s8u.streamarchive.channel.controller.dto.response.ChannelAdminSearchResponse
import com.github.s8u.streamarchive.channel.controller.dto.response.ChannelAdminUpdateResponse
import com.github.s8u.streamarchive.channel.usecase.ChannelAdminCreateUseCase
import com.github.s8u.streamarchive.channel.usecase.ChannelAdminDeleteUseCase
import com.github.s8u.streamarchive.channel.usecase.ChannelAdminGetUseCase
import com.github.s8u.streamarchive.channel.usecase.ChannelAdminQuickCreateUseCase
import com.github.s8u.streamarchive.channel.usecase.ChannelAdminSearchUseCase
import com.github.s8u.streamarchive.channel.usecase.ChannelAdminUpdateUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "ChannelAdmin", description = "채널 관리")
@RestController
@RequestMapping("/admin/channels")
class ChannelAdminController(
    private val channelAdminSearchUseCase: ChannelAdminSearchUseCase,
    private val channelAdminGetUseCase: ChannelAdminGetUseCase,
    private val channelAdminCreateUseCase: ChannelAdminCreateUseCase,
    private val channelAdminQuickCreateUseCase: ChannelAdminQuickCreateUseCase,
    private val channelAdminUpdateUseCase: ChannelAdminUpdateUseCase,
    private val channelAdminDeleteUseCase: ChannelAdminDeleteUseCase
) {

    @Operation(summary = "채널 목록 조회")
    @GetMapping
    fun searchAdminChannels(
        request: ChannelAdminSearchRequest,
        pageable: Pageable
    ): Page<ChannelAdminSearchResponse> {
        return channelAdminSearchUseCase.search(request.toCommand(), pageable)
            .map { ChannelAdminSearchResponse.from(it) }
    }

    @Operation(summary = "채널 단건 조회")
    @GetMapping("/{id}")
    fun getAdminChannelById(@PathVariable id: Long): ChannelAdminGetResponse {
        val result = channelAdminGetUseCase.get(id)
        return ChannelAdminGetResponse.from(result)
    }

    @Operation(summary = "채널 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createAdminChannel(@RequestBody request: ChannelAdminCreateRequest): ChannelAdminCreateResponse {
        val result = channelAdminCreateUseCase.create(request.toCommand())
        return ChannelAdminCreateResponse.from(result)
    }

    @Operation(summary = "간편 채널 생성 (채널·플랫폼·스케줄 한 번에)")
    @PostMapping("/quick")
    @ResponseStatus(HttpStatus.CREATED)
    fun createAdminChannelQuick(@RequestBody request: ChannelAdminQuickCreateRequest): ChannelAdminQuickCreateResponse {
        val result = channelAdminQuickCreateUseCase.create(request.toCommand())
        return ChannelAdminQuickCreateResponse.from(result)
    }

    @Operation(summary = "채널 수정")
    @PutMapping("/{id}")
    fun updateAdminChannel(
        @PathVariable id: Long,
        @RequestBody request: ChannelAdminUpdateRequest
    ): ChannelAdminUpdateResponse {
        val result = channelAdminUpdateUseCase.update(id, request.toCommand())
        return ChannelAdminUpdateResponse.from(result)
    }

    @Operation(summary = "채널 삭제")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteAdminChannel(@PathVariable id: Long) {
        channelAdminDeleteUseCase.delete(id)
    }

}
