package com.github.s8u.streamarchive.video.controller.dto.request

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "동영상 소장 여부 설정 요청 (관리자)")
data class VideoAdminArchiveRequest(
    @field:Schema(description = "소장 여부")
    val isArchived: Boolean
)
