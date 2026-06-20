package com.github.s8u.streamarchive.dashboard.controller

import com.github.s8u.streamarchive.dashboard.controller.dto.response.DashboardStatResponse
import com.github.s8u.streamarchive.dashboard.controller.dto.response.DashboardVideoHistoryResponse
import com.github.s8u.streamarchive.dashboard.usecase.DashboardStatGetUseCase
import com.github.s8u.streamarchive.dashboard.usecase.DashboardVideoHistoryGetUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "DashboardAdmin", description = "대시보드 관리")
@RestController
@RequestMapping("/admin/dashboard")
class DashboardAdminController(
    private val dashboardStatGetUseCase: DashboardStatGetUseCase,
    private val dashboardVideoHistoryGetUseCase: DashboardVideoHistoryGetUseCase
) {

    @Operation(summary = "대시보드 통계 조회")
    @GetMapping("/stats")
    fun getAdminDashboardStats(): DashboardStatResponse {
        val result = dashboardStatGetUseCase.get()
        return DashboardStatResponse.from(result)
    }

    @Operation(summary = "동영상 히스토리 조회")
    @GetMapping("/video-histories")
    fun getAdminDashboardVideoHistories(): DashboardVideoHistoryResponse {
        val result = dashboardVideoHistoryGetUseCase.get()
        return DashboardVideoHistoryResponse.from(result)
    }

}
