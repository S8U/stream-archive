package com.github.s8u.streamarchive.video.controller

import com.github.s8u.streamarchive.video.controller.dto.request.VideoAutoDeletePolicyUpdateRequest
import com.github.s8u.streamarchive.video.controller.dto.response.VideoAutoDeleteChannelPolicySearchResponse
import com.github.s8u.streamarchive.video.controller.dto.response.VideoAutoDeleteHistorySearchResponse
import com.github.s8u.streamarchive.video.controller.dto.response.VideoAutoDeletePolicyGetResponse
import com.github.s8u.streamarchive.video.controller.dto.response.VideoAutoDeletePolicyUpdateResponse
import com.github.s8u.streamarchive.video.controller.dto.response.VideoAutoDeletePreviewResponse
import com.github.s8u.streamarchive.video.controller.dto.response.VideoAutoDeletePreviewSummaryGetResponse
import com.github.s8u.streamarchive.video.controller.dto.response.VideoAutoDeleteRunResponse
import com.github.s8u.streamarchive.video.usecase.VideoAutoDeleteChannelPolicyDeleteUseCase
import com.github.s8u.streamarchive.video.usecase.VideoAutoDeleteChannelPolicySearchUseCase
import com.github.s8u.streamarchive.video.usecase.VideoAutoDeleteHistorySearchUseCase
import com.github.s8u.streamarchive.video.usecase.VideoAutoDeletePolicyGetUseCase
import com.github.s8u.streamarchive.video.usecase.VideoAutoDeletePolicyUpdateUseCase
import com.github.s8u.streamarchive.video.usecase.VideoAutoDeletePreviewSummaryGetUseCase
import com.github.s8u.streamarchive.video.usecase.VideoAutoDeletePreviewUseCase
import com.github.s8u.streamarchive.video.usecase.VideoAutoDeleteRunUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "VideoAutoDeleteAdmin", description = "동영상 자동 삭제 관리")
@RequestMapping("/admin/videos/auto-delete")
@RestController
class VideoAutoDeleteAdminController(
    private val videoAutoDeletePolicyGetUseCase: VideoAutoDeletePolicyGetUseCase,
    private val videoAutoDeleteChannelPolicySearchUseCase: VideoAutoDeleteChannelPolicySearchUseCase,
    private val videoAutoDeletePolicyUpdateUseCase: VideoAutoDeletePolicyUpdateUseCase,
    private val videoAutoDeleteChannelPolicyDeleteUseCase: VideoAutoDeleteChannelPolicyDeleteUseCase,
    private val videoAutoDeletePreviewSummaryGetUseCase: VideoAutoDeletePreviewSummaryGetUseCase,
    private val videoAutoDeletePreviewUseCase: VideoAutoDeletePreviewUseCase,
    private val videoAutoDeleteHistorySearchUseCase: VideoAutoDeleteHistorySearchUseCase,
    private val videoAutoDeleteRunUseCase: VideoAutoDeleteRunUseCase
) {

    @Operation(summary = "전체 자동 삭제 정책 조회")
    @GetMapping("/policy")
    fun getAdminAutoDeleteGlobalPolicy(): VideoAutoDeletePolicyGetResponse {
        val result = videoAutoDeletePolicyGetUseCase.getGlobal()
        return VideoAutoDeletePolicyGetResponse.from(result)
    }

    @Operation(summary = "전체 자동 삭제 정책 설정")
    @PutMapping("/policy")
    fun updateAdminAutoDeleteGlobalPolicy(
        @RequestBody request: VideoAutoDeletePolicyUpdateRequest
    ): VideoAutoDeletePolicyUpdateResponse {
        val result = videoAutoDeletePolicyUpdateUseCase.update(request.toCommand(channelId = null))
        return VideoAutoDeletePolicyUpdateResponse.from(result)
    }

    @Operation(summary = "채널별 자동 삭제 정책 목록 조회")
    @GetMapping("/policy/channels")
    fun searchAdminAutoDeleteChannelPolicies(): List<VideoAutoDeleteChannelPolicySearchResponse> {
        return videoAutoDeleteChannelPolicySearchUseCase.search()
            .map { VideoAutoDeleteChannelPolicySearchResponse.from(it) }
    }

    @Operation(summary = "채널별 자동 삭제 정책 조회")
    @GetMapping("/policy/channels/{channelId}")
    fun getAdminAutoDeleteChannelPolicyByChannelId(@PathVariable channelId: Long): VideoAutoDeletePolicyGetResponse {
        val result = videoAutoDeletePolicyGetUseCase.getChannel(channelId)
        return VideoAutoDeletePolicyGetResponse.from(result)
    }

    @Operation(summary = "채널별 자동 삭제 정책 설정")
    @PutMapping("/policy/channels/{channelId}")
    fun updateAdminAutoDeleteChannelPolicy(
        @PathVariable channelId: Long,
        @RequestBody request: VideoAutoDeletePolicyUpdateRequest
    ): VideoAutoDeletePolicyUpdateResponse {
        val result = videoAutoDeletePolicyUpdateUseCase.update(request.toCommand(channelId = channelId))
        return VideoAutoDeletePolicyUpdateResponse.from(result)
    }

    @Operation(summary = "채널별 자동 삭제 정책 삭제 (전체 기본값 따름)")
    @DeleteMapping("/policy/channels/{channelId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteAdminAutoDeleteChannelPolicy(@PathVariable channelId: Long) {
        videoAutoDeleteChannelPolicyDeleteUseCase.delete(channelId)
    }

    @Operation(summary = "자동 삭제 미리보기 요약 (다음 삭제 대상 개수·용량)")
    @GetMapping("/preview/summary")
    fun getAdminAutoDeletePreviewSummary(
        @RequestParam(required = false) channelId: Long?
    ): VideoAutoDeletePreviewSummaryGetResponse {
        val result = videoAutoDeletePreviewSummaryGetUseCase.getSummary(channelId)
        return VideoAutoDeletePreviewSummaryGetResponse.from(result)
    }

    @Operation(summary = "자동 삭제 미리보기 목록 (다음 삭제 대상 동영상)")
    @GetMapping("/preview")
    fun searchAdminAutoDeletePreviews(
        @RequestParam(required = false) channelId: Long?,
        pageable: Pageable
    ): Page<VideoAutoDeletePreviewResponse> {
        return videoAutoDeletePreviewUseCase.preview(channelId, pageable)
            .map { VideoAutoDeletePreviewResponse.from(it) }
    }

    @Operation(summary = "자동 삭제 즉시 실행")
    @PostMapping("/run")
    fun runAdminAutoDelete(): VideoAutoDeleteRunResponse {
        val result = videoAutoDeleteRunUseCase.run()
        return VideoAutoDeleteRunResponse.from(result)
    }

    @Operation(summary = "자동 삭제 이력 조회")
    @GetMapping("/histories")
    fun searchAdminAutoDeleteHistories(
        @RequestParam(required = false) channelId: Long?,
        pageable: Pageable
    ): Page<VideoAutoDeleteHistorySearchResponse> {
        return videoAutoDeleteHistorySearchUseCase.search(channelId, pageable)
            .map { VideoAutoDeleteHistorySearchResponse.from(it) }
    }

}
