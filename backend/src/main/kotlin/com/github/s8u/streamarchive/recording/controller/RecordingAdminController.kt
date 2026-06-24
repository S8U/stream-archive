package com.github.s8u.streamarchive.recording.controller

import com.github.s8u.streamarchive.recording.usecase.RecordingEndUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "RecordingAdmin", description = "녹화 과정 관리")
@RequestMapping("/admin/records")
@RestController
class RecordingAdminController(
    private val recordingEndUseCase: RecordingEndUseCase
) {

    @Operation(summary = "녹화 취소")
    @PostMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun cancelAdminRecord(@PathVariable id: Long) {
        recordingEndUseCase.end(id, isCancelled = true)
    }

}
