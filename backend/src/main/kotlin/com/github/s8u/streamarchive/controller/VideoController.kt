package com.github.s8u.streamarchive.controller

import com.github.s8u.streamarchive.dto.PublicVideoResponse
import com.github.s8u.streamarchive.dto.PublicVideoSearchRequest
import com.github.s8u.streamarchive.service.VideoService
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

@Tag(name = "Video", description = "동영상")
@RestController
@RequestMapping("/videos")
class VideoController(
    private val videoService: VideoService
) {
    @Operation(summary = "동영상 목록 조회")
    @GetMapping
    fun searchVideos(
        request: PublicVideoSearchRequest,
        pageable: Pageable
    ): Page<PublicVideoResponse> {
        return videoService.searchForPublic(request, pageable)
    }

    @Operation(summary = "동영상 단건 조회")
    @GetMapping("/{uuid}")
    fun getVideoByUuid(@PathVariable uuid: String): PublicVideoResponse {
        return videoService.getByUuidForPublic(uuid)
    }

    @Operation(summary = "동영상 썸네일 조회")
    @GetMapping("/{uuid}/thumbnail")
    fun getVideoThumbnail(@PathVariable uuid: String): ResponseEntity<Resource> {
        val resource = videoService.getThumbnailByUuid(uuid)
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(resource)
    }

    @Operation(summary = "HLS 플레이리스트 조회")
    @GetMapping("/{uuid}/playlist.m3u8")
    fun getVideoPlaylist(@PathVariable uuid: String): ResponseEntity<Resource> {
        val resource = videoService.getPlaylistByUuid(uuid)
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("application/vnd.apple.mpegurl"))
            .body(resource)
    }

    @Operation(summary = "HLS 세그먼트 파일 조회")
    @GetMapping("/{uuid}/{filename:segment_\\d+\\.ts}")
    fun getVideoSegment(
        @PathVariable uuid: String,
        @PathVariable filename: String
    ): ResponseEntity<Resource> {
        val resource = videoService.getSegmentByUuid(uuid, filename)
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("video/mp2t"))
            .body(resource)
    }
}
