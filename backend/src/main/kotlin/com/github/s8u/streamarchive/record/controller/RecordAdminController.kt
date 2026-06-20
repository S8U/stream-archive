package com.github.s8u.streamarchive.record.controller

import com.github.s8u.streamarchive.record.controller.dto.request.RecordAdminSearchRequest
import com.github.s8u.streamarchive.record.controller.dto.response.RecordAdminGetResponse
import com.github.s8u.streamarchive.record.controller.dto.response.RecordAdminSearchResponse
import com.github.s8u.streamarchive.record.usecase.RecordAdminGetUseCase
import com.github.s8u.streamarchive.record.usecase.RecordAdminSearchUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.*

@Tag(name = "RecordAdmin", description = "녹화 기록 관리")
@RestController
@RequestMapping("/admin/records")
class RecordAdminController(
    private val recordAdminSearchUseCase: RecordAdminSearchUseCase,
    private val recordAdminGetUseCase: RecordAdminGetUseCase
) {

    @Operation(summary = "녹화 기록 조회")
    @GetMapping
    fun searchAdminRecords(
        request: RecordAdminSearchRequest,
        pageable: Pageable
    ): Page<RecordAdminSearchResponse> {
        return recordAdminSearchUseCase.search(request.toCommand(), pageable)
            .map { RecordAdminSearchResponse.from(it) }
    }

    @Operation(summary = "녹화 단건 조회")
    @GetMapping("/{id}")
    fun getAdminRecordById(@PathVariable id: Long): RecordAdminGetResponse {
        val result = recordAdminGetUseCase.get(id)
        return RecordAdminGetResponse.from(result)
    }

}
