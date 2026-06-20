package com.github.s8u.streamarchive.recordschedule.controller

import com.github.s8u.streamarchive.recordschedule.controller.dto.request.RecordScheduleAdminCreateRequest
import com.github.s8u.streamarchive.recordschedule.controller.dto.request.RecordScheduleAdminSearchRequest
import com.github.s8u.streamarchive.recordschedule.controller.dto.request.RecordScheduleAdminUpdateRequest
import com.github.s8u.streamarchive.recordschedule.controller.dto.response.RecordScheduleAdminCreateResponse
import com.github.s8u.streamarchive.recordschedule.controller.dto.response.RecordScheduleAdminGetResponse
import com.github.s8u.streamarchive.recordschedule.controller.dto.response.RecordScheduleAdminSearchResponse
import com.github.s8u.streamarchive.recordschedule.controller.dto.response.RecordScheduleAdminUpdateResponse
import com.github.s8u.streamarchive.recordschedule.usecase.RecordScheduleAdminCreateUseCase
import com.github.s8u.streamarchive.recordschedule.usecase.RecordScheduleAdminDeleteUseCase
import com.github.s8u.streamarchive.recordschedule.usecase.RecordScheduleAdminGetUseCase
import com.github.s8u.streamarchive.recordschedule.usecase.RecordScheduleAdminSearchUseCase
import com.github.s8u.streamarchive.recordschedule.usecase.RecordScheduleAdminUpdateUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "RecordScheduleAdmin", description = "녹화 스케줄 관리")
@RestController
@RequestMapping("/admin/record-schedules")
class RecordScheduleAdminController(
    private val recordScheduleAdminSearchUseCase: RecordScheduleAdminSearchUseCase,
    private val recordScheduleAdminGetUseCase: RecordScheduleAdminGetUseCase,
    private val recordScheduleAdminCreateUseCase: RecordScheduleAdminCreateUseCase,
    private val recordScheduleAdminUpdateUseCase: RecordScheduleAdminUpdateUseCase,
    private val recordScheduleAdminDeleteUseCase: RecordScheduleAdminDeleteUseCase
) {

    @Operation(summary = "녹화 스케줄 목록 조회")
    @GetMapping
    fun searchAdminRecordSchedules(
        request: RecordScheduleAdminSearchRequest,
        pageable: Pageable
    ): Page<RecordScheduleAdminSearchResponse> {
        return recordScheduleAdminSearchUseCase.search(request.toCommand(), pageable)
            .map { RecordScheduleAdminSearchResponse.from(it) }
    }

    @Operation(summary = "녹화 스케줄 단건 조회")
    @GetMapping("/{id}")
    fun getAdminRecordScheduleById(@PathVariable id: Long): RecordScheduleAdminGetResponse {
        val result = recordScheduleAdminGetUseCase.get(id)
        return RecordScheduleAdminGetResponse.from(result)
    }

    @Operation(summary = "녹화 스케줄 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createAdminRecordSchedule(
        @RequestBody request: RecordScheduleAdminCreateRequest
    ): RecordScheduleAdminCreateResponse {
        val result = recordScheduleAdminCreateUseCase.create(request.toCommand())
        return RecordScheduleAdminCreateResponse.from(result)
    }

    @Operation(summary = "녹화 스케줄 수정")
    @PutMapping("/{id}")
    fun updateAdminRecordSchedule(
        @PathVariable id: Long,
        @RequestBody request: RecordScheduleAdminUpdateRequest
    ): RecordScheduleAdminUpdateResponse {
        val result = recordScheduleAdminUpdateUseCase.update(id, request.toCommand())
        return RecordScheduleAdminUpdateResponse.from(result)
    }

    @Operation(summary = "녹화 스케줄 삭제")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteAdminRecordSchedule(@PathVariable id: Long) {
        recordScheduleAdminDeleteUseCase.delete(id)
    }

}
