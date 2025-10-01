package com.github.s8u.streamarchive.domain.enums

enum class GlobalSettingKey(
    val key: String,
    val defaultValue: String,
    val description: String
) {
    // Detection Server 설정
    DETECTION_POLLING_INTERVAL_SECONDS(
        "detection.polling_interval_seconds",
        "30",
        "API 폴링 주기 (초)"
    ),

    // Recorder Server 설정
    RECORDER_MAX_CONCURRENT_RECORDINGS(
        "recorder.max_concurrent_recordings",
        "10",
        "최대 동시 녹화 수"
    ),

    // 보존 정책
    RETENTION_PERIOD_DAYS(
        "retention.period_days",
        "30",
        "보존 기간 (일)"
    ),
    RETENTION_AUTO_CLEANUP_ENABLED(
        "retention.auto_cleanup_enabled",
        "true",
        "자동 정리 활성화 여부"
    );

    companion object {
        fun fromKey(key: String): GlobalSettingKey? {
            return values().find { it.key == key }
        }
    }
}