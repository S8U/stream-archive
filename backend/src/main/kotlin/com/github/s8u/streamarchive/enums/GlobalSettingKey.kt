package com.github.s8u.streamarchive.enums

enum class GlobalSettingKey(
    val defaultValue: String,
    val description: String
) {
    // 방송 감지 설정
    DETECTION_POLLING_INTERVAL_SECONDS("30","API 폴링 주기 (초)"),

    // 방송 녹화 설정
    RECORDER_MAX_CONCURRENT_RECORDINGS("10", "최대 동시 녹화 수"),

    // 보존 정책
    RETENTION_PERIOD_DAYS("30", "보존 기간 (일)"),
    RETENTION_AUTO_CLEANUP_ENABLED("true", "자동 정리 활성화 여부");
}