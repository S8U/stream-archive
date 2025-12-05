package com.github.s8u.streamarchive.util

import com.github.s8u.streamarchive.properties.UrlProperties
import org.springframework.stereotype.Component

@Component
class UrlBuilder(
    private val urlProperties: UrlProperties
) {
    // Channel URLs
    fun channelProfileUrl(channelUuid: String): String {
        return "${urlProperties.apiBase}/channels/$channelUuid/profile"
    }

    // Video URLs
    fun videoThumbnailUrl(videoUuid: String): String {
        return "${urlProperties.apiBase}/videos/$videoUuid/thumbnail"
    }

    fun videoPlaylistUrl(videoUuid: String): String {
        return "${urlProperties.apiBase}/videos/$videoUuid/playlist.m3u8"
    }

    fun videoSegmentUrl(videoUuid: String, filename: String): String {
        return "${urlProperties.apiBase}/videos/$videoUuid/$filename"
    }
}