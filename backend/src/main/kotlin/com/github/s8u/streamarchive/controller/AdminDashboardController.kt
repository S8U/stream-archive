package com.github.s8u.streamarchive.controller

import com.github.s8u.streamarchive.dto.AdminDashboardStatsResponse
import com.github.s8u.streamarchive.dto.AdminDashboardVideoHistoriesResponse
import com.github.s8u.streamarchive.service.DashboardService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "AdminDashboard", description = "대시보드 관리")
@RestController
@RequestMapping("/admin/dashboard")
class AdminDashboardController(private val dashboardService: DashboardService) {
    @Operation(summary = "대시보드 통계 조회")
    @GetMapping("/stats")
    fun getAdminDashboardStats(): AdminDashboardStatsResponse {
        return dashboardService.getStats()
    }

    @Operation(summary = "동영상 히스토리 조회")
    @GetMapping("/video-histories")
    fun getAdminDashboardVideoHistories(): AdminDashboardVideoHistoriesResponse {
        return dashboardService.getVideoHistories()
    }
}
