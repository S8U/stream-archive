package com.github.s8u.streamarchive.enums

enum class ChannelSettingKey(
    val defaultValue: String?,
    val description: String
) {
    // 보존 정책
    RETENTION_PERIOD_DAYS(null, "채널별 보존 기간 (일)"),
    RETENTION_AUTO_CLEANUP_ENABLED(null, "채널별 자동 정리 활성화 여부");
}