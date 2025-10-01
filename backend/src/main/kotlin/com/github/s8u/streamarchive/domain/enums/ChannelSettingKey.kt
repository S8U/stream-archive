package com.github.s8u.streamarchive.domain.enums

enum class ChannelSettingKey(
    val key: String,
    val defaultValue: String?,
    val description: String
) {
    // 보존 정책
    RETENTION_PERIOD_DAYS(
        "retention.period_days",
        null,
        "채널별 보존 기간 (일)"
    ),
    RETENTION_AUTO_CLEANUP_ENABLED(
        "retention.auto_cleanup_enabled",
        null,
        "채널별 자동 정리 활성화 여부"
    );

    companion object {
        fun fromKey(key: String): ChannelSettingKey? {
            return values().find { it.key == key }
        }
    }
}