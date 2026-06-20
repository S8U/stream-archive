package com.github.s8u.streamarchive.video.controller

import com.github.s8u.streamarchive.video.controller.dto.request.VideoAdminArchiveRequest
import com.github.s8u.streamarchive.video.controller.dto.request.VideoAdminSearchRequest
import com.github.s8u.streamarchive.video.controller.dto.request.VideoAdminUpdateRequest
import com.github.s8u.streamarchive.video.controller.dto.response.VideoAdminArchiveResponse
import com.github.s8u.streamarchive.video.controller.dto.response.VideoAdminGetResponse
import com.github.s8u.streamarchive.video.controller.dto.response.VideoAdminSearchResponse
import com.github.s8u.streamarchive.video.controller.dto.response.VideoAdminUpdateResponse
import com.github.s8u.streamarchive.video.usecase.VideoAdminArchiveUseCase
import com.github.s8u.streamarchive.video.usecase.VideoAdminDeleteUseCase
import com.github.s8u.streamarchive.video.usecase.VideoAdminGetUseCase
import com.github.s8u.streamarchive.video.usecase.VideoAdminSearchUseCase
import com.github.s8u.streamarchive.video.usecase.VideoAdminUpdateUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "VideoAdmin", description = "동영상 관리")
@RestController
@RequestMapping("/admin/videos")
class VideoAdminController(
    private val videoAdminSearchUseCase: VideoAdminSearchUseCase,
    private val videoAdminGetUseCase: VideoAdminGetUseCase,
    private val videoAdminUpdateUseCase: VideoAdminUpdateUseCase,
    private val videoAdminArchiveUseCase: VideoAdminArchiveUseCase,
    private val videoAdminDeleteUseCase: VideoAdminDeleteUseCase
) {

    @Operation(summary = "동영상 목록 조회")
    @GetMapping
    fun searchAdminVideos(
        request: VideoAdminSearchRequest,
        pageable: Pageable
    ): Page<VideoAdminSearchResponse> {
        return videoAdminSearchUseCase.search(request.toCommand(), pageable)
            .map { VideoAdminSearchResponse.from(it) }
    }

    @Operation(summary = "동영상 단건 조회")
    @GetMapping("/{id}")
    fun getAdminVideoById(@PathVariable id: Long): VideoAdminGetResponse {
        val result = videoAdminGetUseCase.get(id)
        return VideoAdminGetResponse.from(result)
    }

    @Operation(summary = "동영상 수정")
    @PutMapping("/{id}")
    fun updateAdminVideo(
        @PathVariable id: Long,
        @RequestBody request: VideoAdminUpdateRequest
    ): VideoAdminUpdateResponse {
        val result = videoAdminUpdateUseCase.update(id, request.toCommand())
        return VideoAdminUpdateResponse.from(result)
    }

    @Operation(summary = "동영상 소장 여부 설정")
    @PatchMapping("/{id}/archive")
    fun setArchivedAdminVideo(
        @PathVariable id: Long,
        @RequestBody request: VideoAdminArchiveRequest
    ): VideoAdminArchiveResponse {
        val result = videoAdminArchiveUseCase.setArchived(id, request.isArchived)
        return VideoAdminArchiveResponse.from(result)
    }

    @Operation(summary = "동영상 삭제")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteAdminVideo(@PathVariable id: Long) {
        videoAdminDeleteUseCase.delete(id)
    }

}
