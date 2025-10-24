package com.github.s8u.streamarchive.enums

enum class RecordQuality(val streamlinkValue: String) {
    BEST("best"),
    P2160_60("2160p60"),
    P2160("2160p"),
    P1440_60("1440p60"),
    P1440("1440p"),
    P1080_60("1080p60"),
    P1080("1080p"),
    P720_60("720p60"),
    P720("720p"),
    P480("480p"),
    P240("240p"),
    P144("144p"),
    WORST("worst");

    fun buildFallbackString(): String {
        return entries.drop(ordinal).joinToString(",") { it.streamlinkValue }
    }
}