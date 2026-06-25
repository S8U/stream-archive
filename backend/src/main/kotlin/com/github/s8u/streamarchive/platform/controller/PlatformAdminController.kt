package com.github.s8u.streamarchive.platform.controller

import com.github.s8u.streamarchive.platform.controller.dto.request.PlatformAdminChannelResolveRequest
import com.github.s8u.streamarchive.platform.controller.dto.response.PlatformAdminChannelResolveResponse
import com.github.s8u.streamarchive.platform.usecase.PlatformAdminChannelResolveUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "PlatformAdmin", description = "플랫폼 관리")
@RestController
@RequestMapping("/admin/platforms")
class PlatformAdminController(
    private val platformAdminChannelResolveUseCase: PlatformAdminChannelResolveUseCase
) {

    @Operation(summary = "플랫폼 채널 조회 (URL로 플랫폼·채널 인식)")
    @GetMapping("/resolve")
    fun resolveAdminPlatformChannel(request: PlatformAdminChannelResolveRequest): PlatformAdminChannelResolveResponse {
        val result = platformAdminChannelResolveUseCase.resolve(request.toCommand())
        return PlatformAdminChannelResolveResponse.from(result)
    }

}
