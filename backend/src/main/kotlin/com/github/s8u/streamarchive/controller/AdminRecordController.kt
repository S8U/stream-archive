package com.github.s8u.streamarchive.controller

import com.github.s8u.streamarchive.dto.AdminRecordResponse
import com.github.s8u.streamarchive.dto.AdminRecordSearchRequest
import com.github.s8u.streamarchive.service.RecordService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "녹화", description = "녹화 관리 API (관리자)")
@RestController
@RequestMapping("/admin/records")
class AdminRecordController(
    private val recordService: RecordService
) {
    @Operation(summary = "녹화 기록 조회")
    @GetMapping
    fun search(
        request: AdminRecordSearchRequest,
        pageable: Pageable
    ): Page<AdminRecordResponse> {
        return recordService.searchForAdmin(request, pageable)
    }

    @Operation(summary = "녹화 단건 조회")
    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): AdminRecordResponse {
        return recordService.getForAdmin(id)
    }

    @Operation(summary = "녹화 취소")
    @PostMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun cancel(@PathVariable id: Long) {
        recordService.endRecording(id, isCancel = true)
    }
}
