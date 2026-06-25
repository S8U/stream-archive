package com.github.s8u.streamarchive.platform.usecase.dto.result

import com.github.s8u.streamarchive.platform.enums.PlatformType

/**
 * 플랫폼 채널 조회 결과 (관리자)
 */
data class PlatformAdminChannelResolveResult(
    val platformType: PlatformType,
    val platformChannelId: String,
    val name: String,
    val thumbnailUrl: String?
)
