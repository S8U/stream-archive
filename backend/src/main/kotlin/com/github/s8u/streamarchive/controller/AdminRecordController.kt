package com.github.s8u.streamarchive.controller

import com.github.s8u.streamarchive.dto.RecordResponse
import com.github.s8u.streamarchive.service.RecordService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
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
    fun getAll(): List<RecordResponse> {
        return recordService.getAll()
    }

    @Operation(summary = "녹화 단건 조회")
    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): RecordResponse {
        return recordService.getById(id)
    }

    @Operation(summary = "녹화 취소")
    @PostMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun cancel(@PathVariable id: Long) {
        recordService.endRecording(id, isCancel = true)
    }
}
