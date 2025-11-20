package com.github.s8u.streamarchive.controller

import com.github.s8u.streamarchive.dto.AdminRecordScheduleCreateRequest
import com.github.s8u.streamarchive.dto.AdminRecordScheduleResponse
import com.github.s8u.streamarchive.dto.AdminRecordScheduleSearchRequest
import com.github.s8u.streamarchive.dto.AdminRecordScheduleUpdateRequest
import com.github.s8u.streamarchive.service.RecordScheduleService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "녹화 스케줄", description = "녹화 스케줄 관리 API (관리자)")
@RestController
@RequestMapping("/admin/record-schedules")
class AdminRecordScheduleController(
    private val recordScheduleService: RecordScheduleService
) {
    @Operation(summary = "녹화 스케줄 목록 조회")
    @GetMapping
    fun search(
        request: AdminRecordScheduleSearchRequest,
        @PageableDefault(size = 20) pageable: Pageable
    ): Page<AdminRecordScheduleResponse> {
        return recordScheduleService.search(request, pageable)
    }

    @Operation(summary = "녹화 스케줄 단건 조회")
    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): AdminRecordScheduleResponse {
        return recordScheduleService.getById(id)
    }

    @Operation(summary = "녹화 스케줄 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: AdminRecordScheduleCreateRequest): AdminRecordScheduleResponse {
        return recordScheduleService.create(request)
    }

    @Operation(summary = "녹화 스케줄 수정")
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody request: AdminRecordScheduleUpdateRequest
    ): AdminRecordScheduleResponse {
        return recordScheduleService.update(id, request)
    }

    @Operation(summary = "녹화 스케줄 삭제")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) {
        recordScheduleService.delete(id)
    }
}