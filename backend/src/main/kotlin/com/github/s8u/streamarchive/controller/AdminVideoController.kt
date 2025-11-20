package com.github.s8u.streamarchive.controller

import com.github.s8u.streamarchive.dto.AdminVideoResponse
import com.github.s8u.streamarchive.dto.AdminVideoSearchRequest
import com.github.s8u.streamarchive.dto.AdminVideoUpdateRequest
import com.github.s8u.streamarchive.service.VideoService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "동영상", description = "동영상 관리 API (관리자)")
@RestController
@RequestMapping("/admin/videos")
class AdminVideoController(
    private val videoService: VideoService
) {
    @Operation(summary = "동영상 목록 조회")
    @GetMapping
    fun search(
        request: AdminVideoSearchRequest,
        @PageableDefault(size = 20) pageable: Pageable
    ): Page<AdminVideoResponse> {
        return videoService.search(request, pageable)
    }

    @Operation(summary = "동영상 단건 조회")
    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): AdminVideoResponse {
        return videoService.getById(id)
    }

    @Operation(summary = "동영상 수정")
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody request: AdminVideoUpdateRequest
    ): AdminVideoResponse {
        return videoService.update(id, request)
    }

    @Operation(summary = "동영상 삭제")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) {
        videoService.delete(id)
    }
}
