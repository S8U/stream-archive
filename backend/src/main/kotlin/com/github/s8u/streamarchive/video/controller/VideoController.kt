package com.github.s8u.streamarchive.video.controller

import com.github.s8u.streamarchive.video.controller.dto.request.VideoSearchRequest
import com.github.s8u.streamarchive.video.controller.dto.response.VideoChatHistorySearchResponse
import com.github.s8u.streamarchive.video.controller.dto.response.VideoGetResponse
import com.github.s8u.streamarchive.video.controller.dto.response.VideoSearchResponse
import com.github.s8u.streamarchive.video.controller.dto.response.VideoViewerHistoryGetResponse
import com.github.s8u.streamarchive.video.usecase.VideoChatHistorySearchUseCase
import com.github.s8u.streamarchive.video.usecase.VideoGetUseCase
import com.github.s8u.streamarchive.video.usecase.VideoPlaylistGetUseCase
import com.github.s8u.streamarchive.video.usecase.VideoSearchUseCase
import com.github.s8u.streamarchive.video.usecase.VideoSegmentGetUseCase
import com.github.s8u.streamarchive.video.usecase.VideoThumbnailGetUseCase
import com.github.s8u.streamarchive.video.usecase.VideoViewerHistoryGetUseCase
import com.github.s8u.streamarchive.watchhistory.controller.dto.response.WatchHistoryGetResponse
import com.github.s8u.streamarchive.watchhistory.controller.dto.request.WatchHistorySaveRequest
import com.github.s8u.streamarchive.watchhistory.usecase.WatchHistoryGetUseCase
import com.github.s8u.streamarchive.watchhistory.usecase.WatchHistorySaveUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.core.io.Resource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Video", description = "동영상")
@RestController
@RequestMapping("/videos")
class VideoController(
    private val videoSearchUseCase: VideoSearchUseCase,
    private val videoGetUseCase: VideoGetUseCase,
    private val videoThumbnailGetUseCase: VideoThumbnailGetUseCase,
    private val videoPlaylistGetUseCase: VideoPlaylistGetUseCase,
    private val videoSegmentGetUseCase: VideoSegmentGetUseCase,
    private val videoChatHistorySearchUseCase: VideoChatHistorySearchUseCase,
    private val videoViewerHistoryGetUseCase: VideoViewerHistoryGetUseCase,
    private val watchHistoryGetUseCase: WatchHistoryGetUseCase,
    private val watchHistorySaveUseCase: WatchHistorySaveUseCase
) {

    @Operation(summary = "동영상 목록 조회")
    @GetMapping
    fun searchVideos(
        request: VideoSearchRequest,
        pageable: Pageable
    ): Page<VideoSearchResponse> {
        return videoSearchUseCase.search(request.toCommand(), pageable)
            .map { VideoSearchResponse.from(it) }
    }

    @Operation(summary = "동영상 단건 조회")
    @GetMapping("/{uuid}")
    fun getVideoByUuid(@PathVariable uuid: String): VideoGetResponse {
        val result = videoGetUseCase.getByUuid(uuid)
        return VideoGetResponse.from(result)
    }

    @Operation(summary = "동영상 썸네일 조회")
    @GetMapping("/{uuid}/thumbnail")
    fun getVideoThumbnail(@PathVariable uuid: String): ResponseEntity<Resource> {
        val resource = videoThumbnailGetUseCase.getByUuid(uuid)
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(resource)
    }

    @Operation(summary = "HLS 플레이리스트 조회")
    @GetMapping("/{uuid}/playlist.m3u8")
    fun getVideoPlaylist(@PathVariable uuid: String): ResponseEntity<Resource> {
        val resource = videoPlaylistGetUseCase.getByUuid(uuid)
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
        val resource = videoSegmentGetUseCase.getByUuid(uuid, filename)
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("video/mp2t"))
            .body(resource)
    }

    @Operation(summary = "동영상 채팅 이력 조회")
    @GetMapping("/{uuid}/chat")
    fun getVideoChatHistory(
        @PathVariable uuid: String,
        @RequestParam offsetStart: Long,
        @RequestParam offsetEnd: Long
    ): List<VideoChatHistorySearchResponse> {
        return videoChatHistorySearchUseCase.search(uuid, offsetStart, offsetEnd)
            .map { VideoChatHistorySearchResponse.from(it) }
    }

    @Operation(summary = "동영상 시청자 수 이력 조회")
    @GetMapping("/{uuid}/viewer-history")
    fun getVideoViewerHistory(@PathVariable uuid: String): List<VideoViewerHistoryGetResponse> {
        return videoViewerHistoryGetUseCase.getByVideoUuid(uuid)
            .map { VideoViewerHistoryGetResponse.from(it) }
    }

    @Operation(summary = "동영상 시청 기록 조회")
    @GetMapping("/{uuid}/watch-history")
    fun getVideoWatchHistory(@PathVariable uuid: String): WatchHistoryGetResponse? {
        val result = watchHistoryGetUseCase.getByVideoUuid(uuid)
        return result?.let { WatchHistoryGetResponse.from(it) }
    }

    @Operation(summary = "동영상 시청 위치 저장")
    @PostMapping("/{uuid}/watch-history")
    fun saveVideoWatchHistory(
        @PathVariable uuid: String,
        @RequestBody request: WatchHistorySaveRequest
    ) {
        watchHistorySaveUseCase.save(uuid, request.toCommand())
    }

}
